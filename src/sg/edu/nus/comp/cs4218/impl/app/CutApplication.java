package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_DASH;

/**
 * The CutApplication class implements the CutInterface and provides functionality
 * for cutting portions of input strings or files based on character or byte positions.
 * It allows users to specify ranges and options to perform cutting operations.
 */
public class CutApplication implements CutInterface { //NOPMD

    /**
     * Executes the cut command based on the provided arguments.
     *
     * @param args   An array of strings representing the arguments passed to the cut command.
     *               Valid arguments include flags for specifying cutting mode (-c for character, -b for byte),
     *               and range specifications indicating which portions of each line to cut.
     * @param stdin  An InputStream representing standard input. The content to cut if no file path is provided.
     * @param stdout An OutputStream representing standard output. The result of the cut operation is written here.
     * @throws CutException If an error occurs during the cut operation, such as invalid arguments or file reading errors.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {//NOPMD
        if (args == null || args.length < 1) {
            throw new CutException(ERR_MISSING_ARG);
        }

        if (stdout == null || stdin == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }

        // Initialize variables to store cutting options and ranges
        boolean isCharPo;
        boolean isBytePo;
        List<int[]> ranges;
        List<String> fileNames;

        // Parse command-line arguments using the parser
        CutArgsParser argsParser = new CutArgsParser();
        try {
            argsParser.parse(args);
        } catch (InvalidArgsException e) {
            CutException cutException = new CutException(e.getMessage());
            cutException.initCause(e);
            throw cutException;
        }

        // Retrieve parsing results
        isCharPo = argsParser.isCharPo();
        isBytePo = argsParser.isBytePo();
        ranges = argsParser.getRanges();
        fileNames = argsParser.getFileNames();

        if (ranges.isEmpty()) {
            throw new CutException(ERR_MISSING_ARG);
        }

        // Perform the cut operation based on the parsed arguments
        try {
            String result;
            if (fileNames == null || fileNames.isEmpty()) {
                // If no file names are provided, cut from stdin
                result = cutFromStdin(isCharPo, isBytePo, ranges, stdin);
            } else if (fileNames.contains(STRING_STDIN_DASH)) {
                // If standard input is included in file names
                result = cutFromFilesAndStdin(isCharPo, isBytePo, ranges, stdin, fileNames.toArray(new String[0]));
            } else {
                // If no standard input and file names are provided
                result = cutFromFiles(isCharPo, isBytePo, ranges, fileNames.toArray(new String[0]));
            }

            // Write the result to the output stream
            stdout.write(result.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Cuts portions of content from files based on the provided options and ranges.
     *
     * @param isCharPo Flag indicating if cutting is based on character position.
     * @param isBytePo Flag indicating if cutting is based on byte position.
     * @param ranges   A list of int arrays representing the start and end indices of ranges to cut.
     * @param fileName An array of strings representing the file paths from which to cut content.
     * @return A string representing the cut content from files.
     * @throws CutException If an error occurs during the cut operation, such as file not found or invalid arguments.
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws CutException {
        if (fileName == null || fileName.length == 0) {
            throw new CutException("No files provided.");
        }

        StringBuilder result = new StringBuilder();

        for (String fileString : fileName) {
            File file = IOUtils.resolveFilePath(fileString).toFile();
            if (!file.exists() || !file.isFile()) {
                throw new CutException("File does not exist or is not a regular file: " + fileName);
            }

            // Check file read permission
            if (!file.canRead()) {
                throw new CutException(ERR_READING_FILE);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                cutInputString(isCharPo, isBytePo, ranges, lines);
                result.append(String.join(System.lineSeparator(), lines)).append(System.lineSeparator());
            } catch (IOException e) {
                CutException cutException = new CutException("Error reading file: " + fileString + ". " + e.getMessage());
                cutException.initCause(e);
                throw cutException;
            } catch (InvalidArgsException e) {
                CutException cutException = new CutException(e.getMessage());
                cutException.initCause(e);
                throw cutException;
            } catch (Exception e) {
                CutException cutException = new CutException(e.getMessage());
                cutException.initCause(e);
                throw cutException;
            }
        }

        return result.toString();
    }

    /**
     * Cuts portions of content from standard input based on the provided options and ranges.
     *
     * @param isCharPo Flag indicating if cutting is based on character position.
     * @param isBytePo Flag indicating if cutting is based on byte position.
     * @param ranges   A list of int arrays representing the start and end indices of ranges to cut.
     * @param stdin    An InputStream representing standard input.
     * @return A string representing the cut content from standard input.
     * @throws CutException If an error occurs during the cut operation, such as invalid arguments or input reading errors.
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws CutException {
        if (stdin == null) {
            throw new CutException("Null input stream provided.");
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdin))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            cutInputString(isCharPo, isBytePo, ranges, lines);
            result.append(String.join(System.lineSeparator(), lines)).append(System.lineSeparator());
        } catch (IOException e) {
            CutException cutException = new CutException("Error reading input stream. " + e.getMessage());
            cutException.initCause(e);
            throw cutException;
        } catch (InvalidArgsException e) {
            CutException cutException = new CutException(e.getMessage());
            cutException.initCause(e);
            throw cutException;
        }

        return result.toString();
    }

    protected void cutInputString(boolean isCharPo, boolean isBytePo, List<int[]> ranges, List<String> lines) throws InvalidArgsException {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            StringBuilder cutLine = new StringBuilder();

            // Check if the line is not empty
            if (!line.isEmpty()) {
                if (isCharPo) {
                    cutByCharacterPosition(line, ranges, cutLine);
                }
                if (isBytePo) {
                    cutByBytePosition(line, ranges, cutLine);
                }
            }

            lines.set(i, cutLine.toString());
        }
    }

    private void cutByCharacterPosition(String line, List<int[]> ranges, StringBuilder cutLine) throws InvalidArgsException {
        for (int[] range : ranges) {
            int startIdx = range[0] - 1; // Adjust index to 0-based
            int endIdx = range[1]; // End index is inclusive

            // Ignore if end index is lower than start index
            if (startIdx <= endIdx) {
                // Adjust start index if it is less than 0
                if (startIdx < 0) {
                    throw new InvalidArgsException("Start index cannot start with 0 or lesser");
                }
                // Adjust end index if it is greater than the last index of the line
                if (endIdx > line.length()) {
                    endIdx = line.length();
                }

                // Append the substring based on the adjusted start and end indices
                cutLine.append(line, startIdx, endIdx);
            }
        }
    }

    private void cutByBytePosition(String line, List<int[]> ranges, StringBuilder cutLine) {
        for (int[] range : ranges) {
            int startByte = range[0] - 1; // Adjust index to 0-based
            int endByte = range[1]; // End index is inclusive

            try {
                byte[] lineBytes = line.getBytes("UTF-8");
                int actualEndByte = Math.min(endByte, lineBytes.length); // Ensure endByte does not exceed file length
                if (startByte >= 0 && startByte < lineBytes.length) {
                    cutLine.append(new String(lineBytes, startByte, actualEndByte - startByte, "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                // Handle encoding exception
                e.printStackTrace();
            }
        }
    }



    /**
     * Cuts portions of content from files and standard input based on the provided options and ranges.
     * It handles the dash symbol convention for standard input.
     *
     * @param isCharPo  Flag indicating if cutting is based on character position.
     * @param isBytePo  Flag indicating if cutting is based on byte position.
     * @param ranges    A list of int arrays representing the start and end indices of ranges to cut.
     * @param stdin     An InputStream representing standard input.
     * @param fileNames An array of strings representing the file paths from which to cut content, including the dash symbol "-" for standard input.
     * @return A string representing the cut content from files and standard input.
     * @throws CutException If an error occurs during the cut operation, such as invalid arguments or input reading errors.
     */
    public String cutFromFilesAndStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin, String... fileNames) throws CutException { //NOPMD
        if (stdin == null) {
            throw new CutException("Null input stream provided.");
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new CutException("No files provided.");
        }

        StringBuilder result = new StringBuilder();

        // Flag to check if standard input is included
        boolean includeStdin = false;

        // Handle standard input if "-" is present in fileNames
        for (String fileName : fileNames) {
            if (fileName.equals(STRING_STDIN_DASH)) {
                includeStdin = true;
                break;
            }
        }

        // Handle standard input
        if (includeStdin) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdin))) {
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                cutInputString(isCharPo, isBytePo, ranges, lines);
                result.append(String.join(System.lineSeparator(), lines)).append(System.lineSeparator());
            } catch (IOException e) {
                CutException cutException = new CutException("Error reading input stream. " + e.getMessage());
                cutException.initCause(e);
                throw cutException;
            } catch (InvalidArgsException e) {
                CutException cutException = new CutException(e.getMessage());
                cutException.initCause(e);
                throw cutException;
            }
        }

        // Handle files
        for (String fileString : fileNames) {
            // Skip standard input as it has already been handled
            if (fileString.equals(STRING_STDIN_DASH)) {
                continue;
            }

            File file = IOUtils.resolveFilePath(fileString).toFile();
            if (!file.exists() || !file.isFile()) {
                throw new CutException("File does not exist or is not a regular file: " + fileString);
            }

            // Check file read permission
            if (!file.canRead()) {
                throw new CutException(ERR_READING_FILE);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                cutInputString(isCharPo, isBytePo, ranges, lines);
                result.append(String.join(System.lineSeparator(), lines)).append(System.lineSeparator());
            } catch (IOException e) {
                CutException cutException = new CutException("Error reading file: " + fileString + ". " + e.getMessage());
                cutException.initCause(e);
                throw cutException;
            } catch (InvalidArgsException e) {
                CutException cutException = new CutException(e.getMessage());
                cutException.initCause(e);
                throw cutException;
            }
        }

        return result.toString();
    }

}
