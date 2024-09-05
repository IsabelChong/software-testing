package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;

public class PasteApplication implements PasteInterface { //NOPMD

    /**
     * Runs application with specified input data and specified output stream.
     */
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException { //NOPMD
        // Format: paste [-s] [FILE]...

        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        if (stdin == null || stdout == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        PasteArgsParser pasteArgsParser = new PasteArgsParser();

        try {
            String results = "";
            pasteArgsParser.parse(args);
            boolean isSerial = pasteArgsParser.isSerial();

            List<String> fileNames = pasteArgsParser.getFileNames();
            List<String> workingFiles = new ArrayList<>();
            List<String> nonWorkingFiles = new ArrayList<>();

            if (isSerial) {
                for (String file : fileNames) {
                    Path resolvedPath = IOUtils.resolveFilePath(file);
                    File fileToRead = resolvedPath.toFile();

                    if ("-".equals(file)) {
                        workingFiles.add(file);
                        continue;
                    }

                    // TODO: Fix based on whether we wna care about directory
                    if (!fileToRead.exists() || fileToRead.isDirectory() || !fileToRead.canRead()) {
                        nonWorkingFiles.add(file);
                    } else {
                        workingFiles.add(file);
                    }
                }
            }

            if (pasteArgsParser.getFileNames().isEmpty()) {
                // Means using stdin to parse
                results = mergeStdin(isSerial, stdin);
            } else {
                // Check if have dash
                if (pasteArgsParser.getFileNames().contains("-")) {
                    if (isSerial) {
                        results = mergeFileAndStdin(true, stdin, workingFiles.toArray(new String[0]));
                    } else {
                        results = mergeFileAndStdin(false, stdin,
                                pasteArgsParser.getFileNames().toArray(new String[0]));
                    }
                } else {
                    if (isSerial) {
                        results = mergeFile(isSerial, workingFiles.toArray(new String[0]));
                    } else {
                        results = mergeFile(isSerial, pasteArgsParser.getFileNames().toArray(new String[0]));
                    }
                }
            }

            // Add newline to end of output if not empty
            if (!results.isEmpty()) {
                results += STRING_NEWLINE;
            }

            stdout.write(results.getBytes());

            if (isSerial) {
                StringBuilder stringB = new StringBuilder();
                for (String file : nonWorkingFiles) {
                    File fileToCheck = new File(file);
                    if (!fileToCheck.exists()) {
                        stringB.append(file);
                        stringB.append(": ");
                        stringB.append(ERR_FILE_NOT_FOUND);
                        stringB.append("\npaste: ");
                    }
                    if (fileToCheck.exists() && !fileToCheck.canRead()) {
                        stringB.append(file);
                        stringB.append(": ");
                        stringB.append(ERR_NO_PERM);
                        stringB.append("\npaste: ");
                        // TODO: Directory decision here
                    } else if (fileToCheck.exists() && fileToCheck.isDirectory()) {
                        stringB.append(file);
                        stringB.append(": ");
                        stringB.append(ERR_IS_DIR);
                        stringB.append("\npaste: ");
                    }
                }

                if (!nonWorkingFiles.isEmpty()) {
                    stringB.delete(stringB.length() - 8, stringB.length());
                    throw new PasteException(stringB.toString());
                }
            }

        } catch (PasteException e) {
            throw e;
        } catch (Exception e) {
            PasteException pasteException = new PasteException(e.getMessage());
            pasteException.initCause(e);
            throw pasteException;
        }
    };

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments. If only one Stdin
     * arg is specified, echo back the Stdin.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws PasteException {
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        List<String> input;

        try {
            input = IOUtils.getLinesFromInputStream(stdin);
        } catch (Exception e) {
            PasteException pasteException = new PasteException(e.getMessage());
            pasteException.initCause(e);
            throw pasteException;
        }

        if (isSerial) {
            // Return tabbed string
            return String.join("\t", input);
        } else {
            return String.join(STRING_NEWLINE, input);
        }
    };

    /**
     * Returns string of line-wise concatenated (tab-separated) files. If only one file is
     * specified, echo back the file content.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged (not including "-" for reading from stdin)
     * @throws Exception
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws PasteException { //NOPMD

        if (fileName == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        if (fileName.length == 0) {
            return "";
        }

        for (String file : fileName) {
            Path resolvedPath = IOUtils.resolveFilePath(file);
            File fileToRead = resolvedPath.toFile();

            if (!fileToRead.exists()) {
                throw new PasteException(file + ": " + ERR_FILE_NOT_FOUND);
            }

            if (fileToRead.isDirectory()) {
                throw new PasteException(file + ": " + ERR_IS_DIR);
            }

            if (!fileToRead.canRead()) {
                throw new PasteException(file + ": " + ERR_NO_PERM);
            }

        }

        StringBuilder strBuilder = new StringBuilder();
        if (isSerial) {
            for (String file : fileName) {
                try {
                    InputStream inputStream = IOUtils.openInputStream(file); //NOPMD
                    List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                    if (strBuilder.length() > 0) {
                        strBuilder.append(STRING_NEWLINE);
                    }
                    strBuilder.append(String.join(CHAR_TAB + "", lines));
                    IOUtils.closeInputStream(inputStream);
                } catch (Exception e) {
                    PasteException pasteException = new PasteException(e.getMessage());
                    pasteException.initCause(e);
                    throw pasteException;
                }
            }
        } else {
            // NON SERIAL
            int maxLines = 0;
            List<List<String>> fileLines = new ArrayList<>();
            for (String file : fileName) {
                try {
                    InputStream inputStream = IOUtils.openInputStream(file); //NOPMD
                    List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
                    fileLines.add(lines);
                    if (lines.size() > maxLines) {
                        maxLines = lines.size();
                    }
                    IOUtils.closeInputStream(inputStream);
                } catch (Exception e) {
                    PasteException pasteException = new PasteException(e.getMessage());
                    pasteException.initCause(e);
                    throw pasteException;
                }
            }

            for (int lineIndex = 0; lineIndex < maxLines; lineIndex++) {
                for (int fileIndex = 0; fileIndex < fileLines.size(); fileIndex++) {
                    if (fileIndex > 0) {
                        strBuilder.append(CHAR_TAB);
                    }
                    List<String> currentFileLines = fileLines.get(fileIndex);
                    String currentLine = currentFileLines.size() > lineIndex ? currentFileLines.get(lineIndex) : "";
                    strBuilder.append(currentLine);
                }
                if (lineIndex < maxLines - 1) {
                    strBuilder.append(STRING_NEWLINE);
                }
            }
        }
        return strBuilder.toString();

    };

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged (including "-" for reading from stdin)
     * @throws Exception
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws PasteException { //NOPMD

        if (stdin == null || fileName == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        List<BufferedReader> fileReaders = new ArrayList<>();
        StringBuilder resultBuilder = new StringBuilder();

        BufferedReader stdinBRuffRdr = new BufferedReader(new InputStreamReader(stdin));

        for (String file : fileName) {
            if ("-".equals(file)) {
                fileReaders.add(stdinBRuffRdr);
            } else {
                Path resolvedPath = IOUtils.resolveFilePath(file);
                File fileToRead = resolvedPath.toFile();

                if (!fileToRead.exists()) {
                    throw new PasteException(file + ": " + ERR_FILE_NOT_FOUND);
                }

                if (fileToRead.isDirectory()) {
                    throw new PasteException(file + ": " + ERR_IS_DIR);
                }

                if (!fileToRead.canRead()) {
                    throw new PasteException(file + ": " + ERR_NO_PERM);
                }

                try {
                    fileReaders.add(new BufferedReader(new InputStreamReader(IOUtils.openInputStream(file))));
                } catch (Exception e) {
                    PasteException pasteException = new PasteException(e.getMessage());
                    pasteException.initCause(e);
                    throw pasteException;
                }
            }
        }

        if (isSerial) {
            for (BufferedReader reader : fileReaders) {//NOPMD
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultBuilder.append(line);
                        resultBuilder.append(CHAR_TAB);
                    }
                    // Remove last tab
                    resultBuilder.deleteCharAt(resultBuilder.length() - 1);
                    // Append new line except for the last file
                    if (fileReaders.indexOf(reader) < fileReaders.size() - 1) {
                        resultBuilder.append(STRING_NEWLINE);
                    }
                } catch (IOException e) {
                    PasteException pasteException = new PasteException(e.getMessage());
                    pasteException.initCause(e);
                    throw pasteException;
                }
            }
        } else {
            // NON SERIAL
            // While there is line
            boolean isAllFileRead = false;
            while (!isAllFileRead) {
                isAllFileRead = true;
                StringBuilder lineBuilder = new StringBuilder();
                for (BufferedReader reader : fileReaders) {//NOPMD
                    try {
                        String line = reader.readLine();
                        if (line != null) {
                            isAllFileRead = false;
                            lineBuilder.append(line);
                            lineBuilder.append(CHAR_TAB);
                        } else {
                            lineBuilder.append(CHAR_TAB);
                        }
                    } catch (IOException e) {
                        PasteException pasteException = new PasteException(e.getMessage());
                        pasteException.initCause(e);
                        throw pasteException;
                    }
                }
                if (!isAllFileRead) {
                    // Remove last tab
                    lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    resultBuilder.append(lineBuilder);
                    resultBuilder.append(STRING_NEWLINE);
                }
            }

            // remove last newline
            if (resultBuilder.length() > 0) {
                resultBuilder.deleteCharAt(resultBuilder.length() - 1);
            }

            // Close all readers
            for (BufferedReader reader : fileReaders) {//NOPMD
                try {
                    reader.close();
                } catch (IOException e) {
                    PasteException pasteException = new PasteException(e.getMessage());
                    pasteException.initCause(e);
                    throw pasteException;
                }
            }


        }

        if (isSerial) {
            return Arrays.stream(resultBuilder.toString().split("\n"))
                    .filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                    .collect(Collectors.joining(STRING_NEWLINE));
        }
        return resultBuilder.toString();
    };


}
