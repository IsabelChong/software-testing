package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_DASH;

public class CatApplication implements CatInterface { //NOPMD
    private static final String NUMBER_FORMAT = "%6d";
    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CatException {
        if (stdin == null || stdout == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }

        CatArgsParser catArgsParser = new CatArgsParser();
        try {
            catArgsParser.parse(args);
            StringBuilder output = new StringBuilder();

            if (catArgsParser.getFileNames().contains(STRING_STDIN_DASH)) {
                output.append(catFileAndStdin(catArgsParser.isLineNumberSpecified(), stdin, catArgsParser.getFileNames().toArray(new String[0])));
            } else if (catArgsParser.getFileNames().isEmpty()) {
                // Read from stdin if no files are specified
                output.append(catStdin(catArgsParser.isLineNumberSpecified(), stdin));
            } else {
                // Read from files
                output.append(catFiles(catArgsParser.isLineNumberSpecified(), catArgsParser.getFileNames().toArray(new String[0])));
            }
            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
            }

        } catch (CatException e) {
            throw e;
        } catch (Exception e) {
            CatException catException = new CatException(e.getMessage());
            catException.initCause(e);
            throw catException;
        }
    }

    /**
     * Adds line number prefix to the given lines.
     *
     * @param lines List of Strings of lines
     */
    protected List<String> addLineNumbers(List<String> lines) {
        List<String> numberedLines = new ArrayList<>();
        int lineNumber = 1;
        for (String line : lines) {
            numberedLines.add(String.format(NUMBER_FORMAT, lineNumber) + "\t" + line);
            lineNumber++;
        }
        return numberedLines;
    }

    /**
     * Adds line number prefix to the given lines.
     *
     * @param lines List of Strings of lines
     */
    protected String addLineNumbersToString(String lines) {
        if (lines.isEmpty()) {
            return "";
        }

        StringBuilder numberedLines = new StringBuilder();
        int lineNumber = 1;

        // Count the number of newline characters at the end of lines
        int numNewLinesAtEnd = 0;
        for (int i = lines.length() - 1; i >= 0; i--) {
            if (lines.charAt(i) == '\n') {
                numNewLinesAtEnd++;
            } else {
                break;
            }
        }

        Boolean endsWithNewLine = lines.endsWith(STRING_NEWLINE);
        String[] splitByNewLine = lines.split(STRING_NEWLINE);

        for (String line : splitByNewLine) {
            numberedLines.append(String.format(NUMBER_FORMAT, lineNumber)).append('\t').append(line).append(STRING_NEWLINE);
            lineNumber++;
        }

        while (numNewLinesAtEnd > 1) {
            numberedLines.append(String.format(NUMBER_FORMAT, lineNumber)).append('\t').append(STRING_NEWLINE);
            lineNumber++;
            numNewLinesAtEnd--;
        }

        if (!endsWithNewLine) {
            numberedLines.deleteCharAt(numberedLines.length() - 1);
        }

        return numberedLines.toString();
    }

    /**
     * Returns string containing the concatenated lines of the fileNames provided
     *
     * @param isLnumSpecified Boolean option to include line numbers in the output
     * @param fileNames       Array of String of file names
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public String catFiles(Boolean isLnumSpecified, String... fileNames) throws CatException {
        if (fileNames == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        StringBuilder outputBuilder = new StringBuilder();

        for (String file : fileNames) {
            validateFilePath(file);
            try (InputStream input = IOUtils.openInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String fileContent = "";
                int nextChar;
                while ((nextChar = reader.read()) != -1) {
                    fileContent += (char) nextChar;
                }
                if (isLnumSpecified) {
                    fileContent = addLineNumbersToString(fileContent);
                }
                outputBuilder.append(fileContent);
            } catch (IOException | ShellException e) {
                e.printStackTrace();
            }
        }
        return outputBuilder.toString();
    }

    protected void validateFilePath(String file) throws CatException {
        File node = IOUtils.resolveFilePath(file).toFile();
        if (!node.exists()) {
            throw new CatException(ERR_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new CatException(ERR_IS_DIR);
        }
        if (!node.canRead()) {
            throw new CatException(ERR_NO_PERM);
        }
    }

    /**
     * Returns string containing the concatenated lines from the standard input
     *
     * @param isLnumSpecified Boolean option to include line numbers in the output
     * @param stdin           InputStream containing arguments from Stdin
     * @throws CatException
     */
    @Override
    public String catStdin(Boolean isLnumSpecified, InputStream stdin) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }
        List<String> lines = null;
        try {
            lines = IOUtils.getLinesFromInputStream(stdin);
            if (isLnumSpecified) {
                lines = addLineNumbers(lines);
            }
        } catch (Exception e) {
            CatException catException = new CatException(ERR_IO_EXCEPTION);
            catException.initCause(e);
            throw catException;
        }
        return catOutput(lines);
    }

    /**
     * Returns string containing the concatenated lines from the files and standard input
     *
     * @param isLnumSpecified Boolean option to include line numbers in the output
     * @param stdin           InputStream containing arguments from Stdin
     * @param fileNames       Array of String of file names
     * @throws CatException If the arguments are not valid.
     */
    @Override
    public String catFileAndStdin(Boolean isLnumSpecified,//NOPMD
                                  InputStream stdin, String... fileNames) throws CatException {//NOPMD
        if (fileNames == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new CatException(ERR_NULL_STREAMS);
        }

        List<String> lines = null;
        StringBuilder outputBuilder = new StringBuilder();

        for (String file : fileNames) {
            if (file.equals(STRING_STDIN_DASH)) {
                try {
                    lines = IOUtils.getLinesFromInputStream(stdin);
                    if (isLnumSpecified) {
                        String lineToString = catOutput(lines);
                        outputBuilder.append(addLineNumbersToString(lineToString));
                    }
                    else {
                        outputBuilder.append(catOutput(lines));
                    }
                } catch (Exception e) {
                    CatException catException = new CatException(ERR_IO_EXCEPTION);
                    catException.initCause(e);
                    throw catException;
                }
            } else {
                validateFilePath(file);

                try (InputStream input = IOUtils.openInputStream(file);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    String fileContent = "";
                    int nextChar;
                    while ((nextChar = reader.read()) != -1) {
                        fileContent += (char) nextChar;
                    }
                    if (isLnumSpecified) {
                        fileContent = addLineNumbersToString(fileContent);
                    }
                    outputBuilder.append(fileContent);
                } catch (IOException | ShellException e) {
                    e.printStackTrace();
                }
            }
        }
        return outputBuilder.toString();
    }

    /**
     * Concatenates the given inputs based on the given condition. Invoking this function will mutate the List.
     *
     * @param input List of Strings of lines
     */
    protected String catOutput(List<String> input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            String line = input.get(i);
            output.append(line).append(STRING_NEWLINE);
        }
        return output.toString();
    }

}