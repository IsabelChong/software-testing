package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_DASH;

public class WcApplication implements WcInterface { //NOPMD
    private static final String NUMBER_FORMAT = " %7d";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException If the given arguments are null.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws WcException {
        if (args == null || stdout == null) {
            throw new WcException(ERR_NULL_ARGS);
        }

        try {
            WcArgsParser wcArgsParser = new WcArgsParser();
            wcArgsParser.parse(args);
            StringBuilder output = new StringBuilder();

            if (wcArgsParser.getFileNames().contains(STRING_STDIN_DASH)) {
                output.append(countFromFileAndStdin(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), stdin, wcArgsParser.getFileNames().toArray(new String[0])));
            } else if (wcArgsParser.getFileNames().isEmpty()) {
                output.append(countFromStdin(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), stdin));
            } else {
                output.append(countFromFiles(wcArgsParser.isByteCount(), wcArgsParser.isLineCount(), wcArgsParser.isWordCount(), wcArgsParser.getFileNames().toArray(new String[0])));
            }

            if (output.length() > 0) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (WcException e) {
            throw e;
        } catch (Exception e) {
            WcException wcException = new WcException(e.getMessage());
            wcException.initCause(e);
            throw wcException;
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes   Boolean option to count the number of Bytes
     * @param isLines   Boolean option to count the number of lines
     * @param isWords   Boolean option to count the number of words
     * @param fileNames Array of String of file names
     * @throws WcException If the given file name is null or files cannot be read.
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, String... fileNames) //NOPMD
            throws WcException {
        if (fileNames == null) {
            throw new WcException(ERR_NULL_ARGS);
        }
        StringBuilder stringBuilder = new StringBuilder();
        long totalLines = 0;
        long totalWords = 0;
        long totalBytes = 0;

        for (String file : fileNames) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                throw new WcException(ERR_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                throw new WcException(ERR_IS_DIR);
            }
            if (!node.canRead()) {
                throw new WcException(ERR_NO_PERM);
            }

            try (InputStream input = IOUtils.openInputStream(file)) {
                long[] count = getCountReport(input);
                totalLines += count[LINES_INDEX];
                totalWords += count[WORDS_INDEX];
                totalBytes += count[BYTES_INDEX];

                if (isLines) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
                }
                if (isWords) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
                }
                if (isBytes) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
                }
                stringBuilder.append(" ").append(file).append(STRING_NEWLINE); //NOPMD
            } catch (IOException | ShellException e) {
                e.printStackTrace();
            }
        }

        if (fileNames.length > 1) {
            if (isLines) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            stringBuilder.append(" total");
        }

        // Remove the final newline character from each file count individually
        int length = stringBuilder.length();
        if (length > 0 && stringBuilder.charAt(length - 1) == '\n') {
            stringBuilder.deleteCharAt(length - 1);
        }

        return stringBuilder.toString();
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws WcException If the stdin is null.
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }

        long[] count = getCountReport(stdin);

        StringBuilder stringBuilder = new StringBuilder();
        if (isLines) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
        }
        if (isWords) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
        }
        if (isBytes) {
            stringBuilder.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
        }

        return stringBuilder.toString();
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws WcException If the stdin is null or given file name is null or cannot be read.
     */
    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin, //NOPMD
                                        String... fileNames) throws WcException {
        if (fileNames == null) {
            throw new WcException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }

        StringBuilder stringBuilder = new StringBuilder();
        long totalLines = 0;
        long totalWords = 0;
        long totalBytes = 0;

        for (String file : fileNames) {
            InputStream input = null; //NOPMD

            long[] count;
            if (file.equals(STRING_STDIN_DASH)) {
                input = stdin;
            } else {
                try {
                    input = IOUtils.openInputStream(file);
                } catch (ShellException e) {
                    e.printStackTrace();
                }
            }

            count = getCountReport(input);

            totalLines += count[LINES_INDEX];
            totalWords += count[WORDS_INDEX];
            totalBytes += count[BYTES_INDEX];

            if (file.equals(STRING_STDIN_DASH)) {
                if (isLines) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
                }
                if (isWords) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
                }
                if (isBytes) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
                }
                stringBuilder.append(" " + STRING_STDIN_DASH).append(STRING_NEWLINE); //NOPMD
            } else {
                if (isLines) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[LINES_INDEX]));
                }
                if (isWords) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[WORDS_INDEX]));
                }
                if (isBytes) {
                    stringBuilder.append(String.format(NUMBER_FORMAT, count[BYTES_INDEX]));
                }
                stringBuilder.append(" " + file).append(STRING_NEWLINE); //NOPMD
            }

            try {
                if (!file.equals(STRING_STDIN_DASH)) {
                    IOUtils.closeInputStream(input);
                }
            } catch (ShellException e) {
                WcException wcException = new WcException(e.getMessage());
                wcException.initCause(e);
                throw wcException;
            }
        }

        if (fileNames.length > 1) {
            if (isLines) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                stringBuilder.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            stringBuilder.append(" total");
        }

        return stringBuilder.append(STRING_NEWLINE).toString();
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws WcException If reading and writing to input or output is not successful
     */
    public long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }

        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead = 0;
        boolean inWord = false;

        try {
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }
                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
                buffer.write(data, 0, inRead);
            }
            buffer.flush();
            if (inWord) {
                ++result[WORDS_INDEX];
            }
        } catch (IOException e) {
            WcException wcException = new WcException(e.getMessage());
            wcException.initCause(e);
            throw wcException;
        }

        return result;
    }
}
