package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

public class UniqApplication implements UniqInterface { // NOPMD
    private static final String PREFIX_FORMAT = "   %s";

    /**
     * Runs the uniq application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws UniqException If an error occurs during the execution of the uniq command.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        if (stdout == null) {
            throw new UniqException(ERR_NULL_STREAMS);
        }
        if (stdin == null) {
            throw new UniqException(ERR_NULL_STREAMS);
        }

        UniqArgsParser uniqArgsParser = new UniqArgsParser();
        try {
            uniqArgsParser.parse(args);
            StringBuilder output = new StringBuilder();

            List<String> files = uniqArgsParser.getFileNames();
            String inputFileName = getInputOutputFiles(files)[0];
            String outputFileName = getInputOutputFiles(files)[1];

            if (uniqArgsParser.getFileNames().isEmpty()) {
                // Read from stdin if no files are specified
                output.append(uniqFromStdin(uniqArgsParser.isCount(),
                        uniqArgsParser.isRepeated(),
                        uniqArgsParser.isAllRepeated(),
                        stdin, outputFileName));
            } else {
                // Read from files
                output.append(uniqFromFile(uniqArgsParser.isCount(),
                        uniqArgsParser.isRepeated(),
                        uniqArgsParser.isAllRepeated(),
                        inputFileName, outputFileName));
            }

            if (output.length() != 0) {
                if (outputFileName == null) {
                    stdout.write(output.toString().getBytes());
                } else {
                    writeToOutputFile(output.toString(), outputFileName);
                }
            }
        } catch (UniqException e) {
            throw e;
        } catch (Exception e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    /**
     * Extracts input and output file paths from a list of file paths. If only one file path is provided,
     * the output file path will be null.
     *
     * @param files             A list of file paths. Should contain either one or two file paths.
     * @return                  An array containing the input file path at index 0 and the output file path at index 1.
     * @throws UniqException    If an error occurs while processing the file paths.
     */
    public String[] getInputOutputFiles(List<String> files) throws UniqException {
        try {
            String inputFile = null;
            String outputFile = null;

            if (files.size() == 1) {
                inputFile = files.get(0);
            } else if (files.size() == 2) {
                inputFile = files.get(0);
                outputFile = files.get(1);
            } else if (files.size() > 2) {
                throw new UniqException("usage: uniq [-c | -d | -D | -u] [-i] [-f fields] [-s chars] [input [output]]");
            }

            return new String[]{inputFile, outputFile};

        } catch (Exception e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws UniqException {
        try {
            Path path = IOUtils.resolveFilePath(inputFileName);
            if (Files.exists(path) && !Files.isReadable(path)) {
                throw new UniqException(ERR_NO_PERM);
            }

            if (!Files.exists(path)) {
                throw new UniqException(ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(path)) {
                throw new UniqException(ERR_IS_DIR);
            }
            InputStream input = null; // NOPMD

            try {
                input = IOUtils.openInputStream(inputFileName);
            } catch (ShellException e) {
                e.printStackTrace();
            }

            if (input.available() <= 0) {
                IOUtils.closeInputStream(input);
                return StringUtils.STRING_NEWLINE;
            }

            String output = uniqFromStdin(isCount, isRepeated, isAllRepeated,
                    input, outputFileName);
            IOUtils.closeInputStream(input);

            return output;

        } catch (UniqException e) {
            throw e;
        } catch (ShellException | IOException e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws UniqException {
        try {
            List<String> result;
            StringBuilder stringBuilder = new StringBuilder();
            result = removeDuplicates(stdin, true);

            if (isRepeated || isAllRepeated) {
                result = getDuplicatesOnOption(result, isRepeated, isAllRepeated);
            }

            for (String line : result) {
                if (isCount) {
                    stringBuilder.append(String.format(PREFIX_FORMAT, line));
                } else {
                    stringBuilder.append(line.split("\\s+", 2)[1]);
                }
                stringBuilder.append(StringUtils.STRING_NEWLINE);
            }

            return stringBuilder.toString();

        } catch (UniqException e) {
            throw e;
        } catch (Exception e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    /**
     * Removes consecutive duplicate lines from the input stream.
     *
     * @param input                 InputStream containing lines of text.
     * @param isCount      Boolean indicating whether to include duplicate count.
     * @return                      A list of strings representing unique lines.
     * @throws UniqException        If an error occurs during duplicate removal.
     */
    public static List<String> removeDuplicates(InputStream input, Boolean isCount) throws UniqException {
        try {
            List<String> tokens = IOUtils.getLinesFromInputStream(input);
            List<String> result = new ArrayList<>();

            if (tokens.isEmpty()) {
                result.add(StringUtils.STRING_NEWLINE);
                return result;
            }

            String prev = tokens.get(0);
            int count = 1;

            for (int i = 1; i < tokens.size(); i++) {
                String curr = tokens.get(i);
                if (curr.equals(prev)) {
                    count++;
                } else {
                    String line = isCount ? count + " " + prev : prev;
                    result.add(line);
                    prev = curr;
                    count = 1;
                }
            }
            String line = isCount ? count + " " + prev : prev;
            result.add(line);
            return result;
        } catch (Exception e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    /**
     * Retrieves duplicate lines based on options.
     *
     * @param tokens                        List of lines to process.
     * @param isRepeated      Boolean indicating whether to include repeated lines.
     * @param isAllRepeated          Boolean indicating whether to include all repeated lines.
     * @return                              A list of strings representing duplicate lines.
     * @throws UniqException                If an error occurs during duplicate retrieval.
     */
    public static List<String> getDuplicatesOnOption(List<String> tokens, Boolean isRepeated, Boolean isAllRepeated) throws UniqException {
        try {
            List<String> result = new ArrayList<>();
            for (String str : tokens) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                int count = Integer.parseInt(str.split("\\s+", 2)[0]);

                if (isAllRepeated && count > 1) {
                    for (int i = 0; i < count; i++) {
                        result.add(str);
                    }
                } else if (isRepeated && count > 1) {
                    result.add(str);
                }
            }
            return result;

        } catch (Exception e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }

    /**
     * Writes text to a file.
     *
     * @param lines      Text to write.
     * @param outputFile Path of the output file.
     * @throws UniqException If an error occurs during file write.
     */
    public static void writeToOutputFile(String lines, String outputFile) throws UniqException {
        try {
            File node = IOUtils.resolveFilePath(outputFile).toFile();
            if (node.exists()) {
                if (node.isDirectory()) {
                    throw new UniqException(ERR_IS_DIR);
                }
                if (!node.canWrite()) {
                    throw new UniqException(ERR_NO_PERM);
                }
            }
            byte[] strToBytes = lines.getBytes();
            Files.write(node.toPath(), strToBytes);

        } catch (UniqException e) {
            throw e;
        } catch (IOException e) {
            UniqException uniqException = new UniqException(e.getMessage());
            uniqException.initCause(e);
            throw uniqException;
        }
    }
}
