package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_DASH;

public class CutArgsParser extends ArgsParser {
    private boolean isCharPosition;
    private boolean isBytePosition;

    private static final String BYTE_OP = "-b";
    private static final String CHAR_OP = "-c";

    private final List<int[]> ranges;
    private final List<String> fileNames;

    public CutArgsParser() {
        isCharPosition = false;
        isBytePosition = false;
        ranges = new ArrayList<>();
        fileNames = new ArrayList<>();
    }

    public boolean isCharPo() {
        return isCharPosition;
    }

    public boolean isBytePo() {
        return isBytePosition;
    }

    public List<int[]> getRanges() {
        return ranges;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void parse(String... args) throws InvalidArgsException {
        if (args == null || args.length < 1) {
            throw new InvalidArgsException("No arguments provided.");
        }

        boolean optionParsed = false;
        boolean hasRange = false;

        for (int argIdx = 0; argIdx < args.length; argIdx++) {
            if (optionParsed) {
                parseArgument(args[argIdx], hasRange);
                if (!hasRange) {
                    hasRange = true;
                }
            } else {
                parseOption(args[argIdx]);
                optionParsed = true;
            }
        }
    }

    private void parseOption(String arg) throws InvalidArgsException {
        if (arg.equals(CHAR_OP)) {
            isCharPosition = true;
        } else if (arg.equals(BYTE_OP)) {
            isBytePosition = true;
        } else {
            throw new InvalidArgsException(ERR_INVALID_FLAG + ": " + arg);
        }
    }

    private void parseArgument(String arg, boolean hasRange) throws InvalidArgsException {
        if (arg.contains(",")) {
            String[] charTokens = arg.split(",");
            for (String charToken : charTokens) {
                parseRange(charToken);
            }
        } else {
            if (hasRange) {
                parseFileName(arg);
            } else {
                parseRange(arg);
            }
        }
    }

    private void parseRange(String arg) throws InvalidArgsException {
        if (arg.contains("-")) {
            // Range specification
            String[] rangeTokens = arg.split("-");
            if (rangeTokens.length != 2) {
                throw new InvalidArgsException("Invalid range format: " + arg);
            }
            try {
                int start = Integer.parseInt(rangeTokens[0]);
                int end = Integer.parseInt(rangeTokens[1]);

                // Check if the current range overlaps with any existing range
                boolean overlap = false;
                for (int[] range : ranges) {
                    if ((start >= range[0] && start <= range[1]) ||
                            (end >= range[0] && end <= range[1]) ||
                            (start <= range[0] && end >= range[1])) {
                        // If overlap exists, merge the ranges
                        range[0] = Math.min(range[0], start);
                        range[1] = Math.max(range[1], end);
                        overlap = true;
                        break;
                    }
                }

                // If no overlap, add the range as it is
                if (!overlap) {
                    ranges.add(new int[]{start, end});
                }
            } catch (NumberFormatException e) {
                InvalidArgsException exception = new InvalidArgsException("Invalid range format: " + arg);
                exception.initCause(e);
                throw exception;
            }
        } else if (arg.matches("\\d+")) {
            // Single number
            try {
                int pos = Integer.parseInt(arg);
                ranges.add(new int[]{pos, pos});
            } catch (NumberFormatException e) {
                InvalidArgsException exception = new InvalidArgsException("Invalid position format: " + arg);
                exception.initCause(e);
                throw exception;
            }
        } else {
            throw new InvalidArgsException(ERR_MISSING_FIELD + ": cut Option LIST FILES...");
        }
    }

    private void parseFileName(String arg) throws InvalidArgsException {
        if (arg.equals(STRING_STDIN_DASH)) {
            fileNames.add(arg);
        } else {
            File file = IOUtils.resolveFilePath(arg).toFile();
            if (file.isFile() || arg.equals(STRING_STDIN_DASH)) {
                fileNames.add(arg);
            } else {
                throw new InvalidArgsException(ERR_FILE_NOT_FOUND);
            }
        }
    }
}
