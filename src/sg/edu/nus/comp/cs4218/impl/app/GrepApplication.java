package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

/**
 * The GrepApplication class implements the GrepInterface and provides functionality
 * for searching patterns in files or standard input and printing matching lines.
 */
public class GrepApplication implements GrepInterface { //NOPMD
    // Constants for error messages
    public static final String INVALID_PATTERN = "Invalid pattern syntax";
    public static final String EMPTY_PATTERN = "Pattern should not be empty.";
    public static final String IS_DIRECTORY = "Is a directory";
    public static final String NULL_POINTER = "Null Pointer Exception";

    public static final String STD_INPUT = "(standard input): ";

    private static final int NUM_ARGUMENTS = 3;
    private static final char CASE_INSEN_IDENT = 'i';
    private static final char COUNT_IDENT = 'c';
    private static final char PREFIX_FN = 'H';
    private static final int CASE_INSEN_IDX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int PREFIX_FN_IDX = 2;


    /**
     * Searches for the pattern in the provided files and standard input and returns the matching lines.
     *
     * @param pattern           The pattern to search for.
     * @param isCaseInsensitive Flag indicating whether the search should be case-insensitive.
     * @param isCountLines      Flag indicating whether to count the number of matching lines.
     * @param isPrefixFileName  Flag indicating whether to prefix each matching line with the file name.
     * @param fileNames         An array of strings representing the file names to search in.
     * @return A string containing the matching lines or counts, based on the provided flags.
     * @throws GrepException If an
     *                       error occurs during the search operation, such as file not found or invalid pattern.
     */
    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws GrepException {
        if (fileNames == null || pattern == null) {
            throw new GrepException(NULL_POINTER);
        }


        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        int flags = 0;
        if (isCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        Pattern compiledPattern;
        try {
            compiledPattern = Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            GrepException grepException = new GrepException(INVALID_PATTERN + e.getMessage());
            grepException.initCause(e);
            throw grepException;
        }

        grepResultsFromFiles(compiledPattern, isPrefixFileName, lineResults, countResults, fileNames);

        String results = "";
        if (isCountLines) {
            results = countResults.toString() + STRING_NEWLINE;
        } else {
            if (!lineResults.toString().isEmpty()) {
                results = lineResults.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Searches for the pattern in the provided files and accumulates the matching lines and counts.
     * Results are stored in separate StringJoiners for lines and counts.
     *
     * @param pattern          The compiled pattern to search for in each line.
     * @param isPrefixFileName Flag indicating whether to prefix each matching line with the file name.
     * @param lineResults      A StringJoiner to accumulate matching lines from all files.
     * @param countResults     A StringJoiner to accumulate counts of matching lines from all files.
     * @param fileNames        An array of strings representing the file names to search in.
     * @throws GrepException If an error occurs during the search operation, such as file not found or IO exception.
     */
    private void grepResultsFromFiles(Pattern pattern, Boolean isPrefixFileName, StringJoiner lineResults, StringJoiner countResults, String... fileNames) throws GrepException {//NOPMD

        int count;
        boolean isSingleFile = (fileNames.length == 1);

        for (String f : fileNames) {
            BufferedReader reader = null;
            try {
                String path = convertToAbsolutePath(f);
                File file = new File(path);
                if (!file.exists()) {
                    lineResults.add(f + ": " + ERR_FILE_NOT_FOUND);
                    countResults.add(f + ": " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (file.isDirectory()) { // ignore if it's a directory
                    lineResults.add(f + ": " + IS_DIRECTORY);
                    countResults.add(f + ": " + IS_DIRECTORY);
                    continue;
                }

                // Check file read permission
                if (!file.canRead()) {
                    lineResults.add(f + ": " + ERR_READING_FILE);
                    countResults.add(f + ": " + ERR_READING_FILE);
                    continue;
                }

                reader = new BufferedReader(new FileReader(path));
                String line;
                count = 0;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        if (isSingleFile) {
                            if (isPrefixFileName) {
                                lineResults.add(f + ": " + line);
                            } else {
                                lineResults.add(line);
                            }
                        } else {
                            lineResults.add(f + ": " + line);
                        }
                        count++;
                    }
                }

                if (!isSingleFile || isPrefixFileName) {
                    countResults.add(f + ": " + count);
                } else {
                    countResults.add("" + count);
                }

                reader.close();
            } catch (FileNotFoundException e) {
                GrepException grepException = new GrepException(ERR_FILE_NOT_FOUND);
                grepException.initCause(e);
                throw grepException;
            } catch (IOException e) {
                GrepException grepException = new GrepException(ERR_IO_EXCEPTION);
                grepException.initCause(e);
                throw grepException;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        GrepException grepException = new GrepException(ERR_IO_EXCEPTION);
                        grepException.initCause(e);
                        throw grepException;
                    }
                }
            }
        }
    }


    /**
     * Converts filename to absolute path, if initially was relative path
     *
     * @param fileName supplied by user
     * @return a String of the absolute path of the filename
     */
    protected String convertToAbsolutePath(String fileName) {
        String home = System.getProperty("user.home").trim();
        String currentDir = Environment.currentDirectory.trim();
        String convertedPath = convertPathToSystemPath(fileName);

        String newPath;
        if (convertedPath.length() >= home.length() && convertedPath.substring(0, home.length()).trim().equals(home)) {
            newPath = convertedPath;
        } else {
            newPath = currentDir + CHAR_FILE_SEP + convertedPath;
        }
        return newPath;
    }

    /**
     * Converts path provided by user into path recognised by the system
     *
     * @param path supplied by user
     * @return a String of the converted path
     */
    protected String convertPathToSystemPath(String path) {
        String convertedPath = path;
        String pathIdentifier = "\\" + Character.toString(CHAR_FILE_SEP);
        convertedPath = convertedPath.replaceAll("(\\\\)+", pathIdentifier);
        convertedPath = convertedPath.replaceAll("/+", pathIdentifier);

        if (convertedPath.length() != 0 && convertedPath.charAt(convertedPath.length() - 1) == CHAR_FILE_SEP) {
            convertedPath = convertedPath.substring(0, convertedPath.length() - 1);
        }

        return convertedPath;
    }

    /**
     * Searches for the pattern in the standard input and returns the matching lines.
     *
     * @param pattern           The pattern to search for.
     * @param isCaseInsensitive Flag indicating whether the search should be case-insensitive.
     * @param isCountLines      Flag indicating whether to count the number of matching lines.
     * @param isPrefixFileName  Flag indicating whether to prefix each matching line with "(standard input)".
     * @param stdin             An InputStream representing the standard input.
     * @return A string containing the matching lines or counts, based on the provided flags.
     * @throws GrepException If an error occurs during the search operation, such as invalid pattern or IO exception.
     */
    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws GrepException { //NOPMD
        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        try {
            List<String> stdinLines = IOUtils.getLinesFromInputStream(stdin);
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }
            for (String line : stdinLines) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) {
                    if (isPrefixFileName) {
                        stringJoiner.add(STD_INPUT + line);
                    } else {
                        stringJoiner.add(line);
                    }
                    count++;
                }
            }
        } catch (PatternSyntaxException pse) {
            GrepException grepException = new GrepException(ERR_INVALID_REGEX);
            grepException.initCause(pse);
            throw grepException;
        } catch (IOException e) {
            GrepException grepException = new GrepException(ERR_IO_EXCEPTION);
            grepException.initCause(e);
            throw grepException;
        }

        String results = "";
        if (isCountLines) {
            if (isPrefixFileName) {
                results = STD_INPUT + count + STRING_NEWLINE;
            } else {
                results = count + STRING_NEWLINE;
            }
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Executes the grep command based on the provided arguments.
     *
     * @param args   An array of strings representing the arguments passed to the grep command.
     *               The pattern to search for, file names to search in, and optional flags.
     * @param stdin  An InputStream representing standard input.
     * @param stdout An OutputStream representing standard output.
     * @throws GrepException If an error occurs during the execution of the grep command.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
        try {
            GrepArgsParser parser = new GrepArgsParser();
            parser.parse(args);

            String pattern = parser.getPattern();
            String[] inputFiles = parser.getFileNames();
            Boolean isCaseInsensitive = parser.isCaseInsensitive();
            Boolean isCountLines = parser.isCountLines();
            Boolean isPrefixFileName = parser.isPrefixFileName();

            if (stdin == null && (inputFiles == null || inputFiles.length == 0)) {
                throw new GrepException(ERR_NO_INPUT);
            }

            if (pattern == null) {
                throw new GrepException(ERR_SYNTAX);
            }

            String result;
            if (inputFiles == null || inputFiles.length == 0) {
                result = grepFromStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin);
            } else if (Arrays.asList(inputFiles).contains(STRING_STDIN_DASH)) {
                result = grepFromFileAndStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin, inputFiles);
            } else {
                result = grepFromFiles(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, inputFiles);
            }

            stdout.write(result.getBytes());
        } catch (GrepException grepException) {
            throw grepException;
        } catch (Exception e) {
            GrepException grepException = new GrepException(e.getMessage());
            grepException.initCause(e);
            throw grepException;
        }
    }
    /**
     * Searches for the pattern in the provided files and standard input and returns the matching lines.
     * This method combines the functionality of searching in files and standard input.
     *
     * @param pattern           The pattern to search for.
     * @param isCaseInsensitive Flag indicating whether the search should be case-insensitive.
     * @param isCountLines      Flag indicating whether to count the number of matching lines.
     * @param isPrefixFileName  Flag indicating whether to prefix each matching line with the file name.
     * @param stdin             An InputStream representing the standard input.
     * @param fileNames         An array of strings representing the file names to search in.
     * @return A string containing the matching lines or counts, based on the provided flags.
     * @throws GrepException If an error occurs during the search operation, such as file not found or invalid pattern.
     */
    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws GrepException { //NOPMD
        if (fileNames == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }
        if (pattern == null) {
            throw new GrepException(EMPTY_PATTERN);
        }

        if (stdin == null) {
            throw new GrepException(ERR_NULL_STREAMS);
        }

        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        int flags = 0;

        if (isCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        Pattern compiledPattern;
        try {
            compiledPattern = Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            GrepException grepException = new GrepException(INVALID_PATTERN + e.getMessage());
            grepException.initCause(e);
            throw grepException;
        }

        // Handle file inputs
        for (String file : fileNames) {
            int count = 0; // Reset count for each file

            String fileIdentifier;
            if (file.equals(STRING_STDIN_DASH)) {
                if (fileNames.length == 1 && fileNames[0].equals(STRING_STDIN_DASH) && !isPrefixFileName) {
                    fileIdentifier = "";
                } else {
                    fileIdentifier = STD_INPUT;
                }
            } else {
                fileIdentifier = file + ": ";
            }

            boolean isFileName = Arrays.asList(fileNames).contains(STRING_STDIN_DASH) && fileNames.length > 1;

            if (file.equals(STRING_STDIN_DASH)) {
                // To handle precedence of dashes over file names
                try {
                    List<String> stdinLines = IOUtils.getLinesFromInputStream(stdin);
                    for (String line : stdinLines) {
                        Matcher matcher = compiledPattern.matcher(line);
                        if (matcher.find()) {
                            if (isPrefixFileName || isFileName) {
                                lineResults.add(STD_INPUT + line);
                            } else {
                                lineResults.add(line);
                            }
                            count++;
                        }
                    }
                } catch (IOException e) {
                    GrepException grepException = new GrepException(ERR_IO_EXCEPTION);
                    grepException.initCause(e);
                    throw grepException;
                }
            } else {
                // Handle file inputs
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    throw new GrepException(ERR_FILE_NOT_FOUND);
                }
                if (node.isDirectory()) {
                    throw new GrepException(ERR_IS_DIR);
                }
                if (!node.canRead()) {
                    throw new GrepException(ERR_NO_PERM);
                }

                try (InputStream input = new FileInputStream(file)) {
                    List<String> fileLines = IOUtils.getLinesFromInputStream(input);
                    for (String line : fileLines) {
                        Matcher matcher = compiledPattern.matcher(line);
                        if (matcher.find()) {
                            if (isPrefixFileName || isFileName) {
                                lineResults.add(file + ": " + line);
                            } else {
                                lineResults.add(line);
                            }
                            count++;
                        }
                    }
                } catch (IOException e) {
                    GrepException grepException = new GrepException(ERR_IO_EXCEPTION);
                    grepException.initCause(e);
                    throw grepException;
                }
            }

            // Add count for the current file to countResults
            if (isPrefixFileName || isFileName) {
                countResults.add(fileIdentifier + count);
            } else {
                countResults.add("" + count);
            }
        }

        String results = "";
        if (isCountLines) {
            results = countResults.toString() + STRING_NEWLINE;
        } else {
            results = lineResults.toString() + STRING_NEWLINE;
        }
        return results;
    }

}