package sg.edu.nus.comp.cs4218.impl.app;

import net.bytebuddy.TypeCache;
import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_DASH;

public class SortApplication implements SortInterface { //NOPMD
    public static final int RANK_SPECIAL_CHAR = 1;
    public static final int RANK_ZERO = 2;
    public static final int RANK_UPPERCASE = 3;
    public static final int RANK_LOWERCASE = 4;
    public static final int RANK_POS_NUM = 5;
    public static final String NEGATIVE_POSTFIX = "-";

    /**
     * Runs the sort application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws SortException If any of the given arguments are null or unable to be read
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws SortException {
        // Format: sort [-nrf] [FILES]
        if (args == null) {
            throw new SortException(ERR_NULL_ARGS);
        }
        if (stdin == null || stdout == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        SortArgsParser sortArgsParser = new SortArgsParser();
        try {
            sortArgsParser.parse(args);
            StringBuilder output = new StringBuilder();

            if (sortArgsParser.getFileNames().contains(STRING_STDIN_DASH)) {
                output.append(sortFilesAndStdin(sortArgsParser.isFirstWordNumber(), sortArgsParser.isReverseOrder(), sortArgsParser.isCaseIndependent(), stdin, sortArgsParser.getFileNames().toArray(new String[0])));
            } else if (sortArgsParser.getFileNames().isEmpty()) {
                output.append(sortFromStdin(sortArgsParser.isFirstWordNumber(), sortArgsParser.isReverseOrder(), sortArgsParser.isCaseIndependent(), stdin));
            } else {
                output.append(sortFromFiles(sortArgsParser.isFirstWordNumber(), sortArgsParser.isReverseOrder(), sortArgsParser.isCaseIndependent(), sortArgsParser.getFileNames().toArray(new String[0])));
            }

            if (!output.toString().isEmpty()) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (SortException e) {
            throw e;
        } catch (Exception e) {
            SortException sortException = new SortException(e.getMessage());
            sortException.initCause(e);
            throw sortException;
        }
    }

    /**
     * Returns string containing the orders of the lines of the specified file
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param fileNames         Array of String of file names
     * @throws SortException    If the given files are unable to be read
     */
    @Override
    public String sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                String... fileNames) throws SortException {
        if (fileNames == null) {
            throw new SortException(ERR_NULL_ARGS);
        }
        List<String> lines = new ArrayList<>();
        for (String file : fileNames) {
            appendFileContents(lines, file);
        }
        sortList(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    protected void appendFileContents(List<String> lines, String filePath) throws SortException {
        File node = IOUtils.resolveFilePath(filePath).toFile();
        if (!node.exists()) {
            throw new SortException(ERR_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new SortException(ERR_IS_DIR);
        }
        if (!node.canRead()) {
            throw new SortException(ERR_NO_PERM);
        }
        try (InputStream input = IOUtils.openInputStream(filePath)) {
            try {
                lines.addAll(IOUtils.getLinesFromInputStream(input));
            } catch (IOException e) {
                SortException sortException = new SortException(ERR_IO_EXCEPTION);
                sortException.initCause(e);
                throw sortException;
            }
            try {
                IOUtils.closeInputStream(input);
            } catch (ShellException e) {
                SortException sortException = new SortException(e.getMessage());
                sortException.initCause(e);
                throw sortException;
            }
        } catch (ShellException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns string containing the orders of the lines from the standard input
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @throws SortException    If the given stdin is null or unable to be read
     */
    @Override
    public String sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                InputStream stdin) throws SortException {
        if (stdin == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        List<String> lines = null;
        try {
            lines = IOUtils.getLinesFromInputStream(stdin);
        } catch (Exception e) {
            SortException sortException = new SortException(ERR_IO_EXCEPTION);
            sortException.initCause(e);
            throw sortException;
        }
        sortList(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns string containing the orders of the lines of the specified file together with the stdin inputs.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @param fileNames         Array of String of file names
     * @throws SortException    If the given stdin or files is null or unable to be read
     */
    protected String sortFilesAndStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent, InputStream stdin, String... fileNames) throws SortException {//NOPMD
        if (stdin == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }
        if (fileNames == null) {
            throw new SortException(ERR_NULL_ARGS);
        }

        // Handle file inputs
        List<String> lines = new ArrayList<>();
        for (String file : fileNames) {
            if (file.equals(STRING_STDIN_DASH)) {
                // To handle precedence of dashes over file names
                try {
                    List<String> stdinLines = IOUtils.getLinesFromInputStream(stdin);
                    lines.addAll(stdinLines);
                } catch (Exception e) {
                    SortException sortException = new SortException(ERR_IO_EXCEPTION);
                    sortException.initCause(e);
                    throw sortException;
                }
            } else {
                appendFileContents(lines, file);
            }
        }
        sortList(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Sorts the input ArrayList based on the given conditions. Invoking this function will mutate the ArrayList.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param input             ArrayList of Strings of lines
     */
    protected void sortList(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                          List<String> input) {
        ArrayList<String> negativeNumbers = new ArrayList<>();
        ArrayList<Integer> negativeIndex = new ArrayList<>();

        if (isFirstWordNumber) {
            // Separate out negative numbers (from first two characters)
            for (int i = 0; i < input.size(); i++) {
                String temp = input.get(i);
                if (temp.length() >= 2 && temp.startsWith(NEGATIVE_POSTFIX) && Character.isDigit(temp.charAt(1))) {
                    negativeNumbers.add(temp);
                    negativeIndex.add(i);
                }
            }

            for (int i = negativeIndex.size() - 1; i >= 0; i--) {
                int index = negativeIndex.get(i);
                input.remove(index);
            }

            // Remove negative sign from negativeNumbers
            negativeNumbers.replaceAll(str -> str.substring(1));

            // Sort negative numbers
            negativeNumbers.sort(negNumberComparator(true, isCaseIndependent));

            // Add back negative sign
            negativeNumbers.replaceAll(str -> "-" + str);
        }

        // Sort non-negative numbers
        input.sort(nonNegNumberComparator(isFirstWordNumber, isCaseIndependent));

        if (isFirstWordNumber) {
            // Add negative and non-negative lists together
            input.addAll(0, negativeNumbers);
        }

        if (isCaseIndependent) {
            swapCaseIndependent(input);
        }

        if (isReverseOrder) {
            Collections.reverse(input);
        }
    }

    /**
     * Returns a rank score for the given character. A higher score indicates a larger character.
     *
     * @param inputChar Input character to get the score
     */
    protected int getRankScore(char inputChar) {
        if (Character.isDigit(inputChar)) {
            return Character.getNumericValue(inputChar) == 0 ? RANK_ZERO : RANK_POS_NUM;
        } else if (Character.isUpperCase(inputChar)) {
            return RANK_UPPERCASE;
        } else if (Character.isLowerCase(inputChar)) {
            return RANK_LOWERCASE;
        } else {
            return RANK_SPECIAL_CHAR;
        }
    }

    /**
     * Checks if the given score is for a number.
     *
     * @param rank Rank to check if it is a number.
     */
    protected boolean isRankAPositiveNumber(int rank) {
        return rank == RANK_POS_NUM;
    }

    /**
     * Sorts same letter alphabets in the order of upper case before lower case.
     *
     * @param input Input string to be sorted
     */
    protected void swapCaseIndependent(List<String> input) {
        for (int i = 0; i < input.size() - 1; i++) {
            String current = input.get(i);
            String next = input.get(i + 1);
            if (next.length() == 1 && Character.isLowerCase(current.charAt(0)) &&
                    Character.toUpperCase(current.charAt(0)) == next.charAt(0)) {
                input.set(i, next);
                input.set(i + 1, current);
                i++;  // Move to skip the next element as it has been swapped
            }
        }
    }

    /**
     * Extracts a chunk of numbers or non-numbers from str starting from index 0.
     *
     * @param str Input string to read from
     */
    protected String getChunk(String str) {
        int startIndexLocal = 0;
        StringBuilder chunk = new StringBuilder();
        final int strLen = str.length();
        if (strLen == 0) {
            return "";
        }
        char chr = str.charAt(startIndexLocal++);
        chunk.append(chr);
        final boolean extractDigit = Character.isDigit(chr);
        while (startIndexLocal < strLen) {
            chr = str.charAt(startIndexLocal++);
            if ((extractDigit && !Character.isDigit(chr)) || (!extractDigit && Character.isDigit(chr))) {
                break;
            }
            chunk.append(chr);
        }
        return chunk.toString();
    }

    /**
     * Returns a comparator which compares negative elements. Negative elements represents strings with a postfix of
     * one dash ("-") followed by a number. It compares
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @return Comparator comparing negative elements
     */
    protected Comparator<String> negNumberComparator(boolean isFirstWordNumber, boolean isCaseIndependent) {
        return new Comparator<>() {
            @Override
            public int compare(String str1, String str2) {
                String temp1 = isCaseIndependent && !isFirstWordNumber ? str1.toLowerCase(Locale.ROOT) : str1;
                String temp2 = isCaseIndependent && !isFirstWordNumber ? str2.toLowerCase(Locale.ROOT) : str2;

                if (isFirstWordNumber && !str1.isEmpty() && !str2.isEmpty()) {
                    String chunk1 = getChunk(temp1);
                    String chunk2 = getChunk(temp2);

                    // Compare ranks of the first character
                    int rank1 = getRankScore(chunk1.charAt(0));
                    int rank2 = getRankScore(chunk2.charAt(0));

                    if (rank1 != rank2) {
                        return rank1 - rank2; // Compare based on rank score
                    }

                    if (chunk1.length() > chunk2.length()) {
                        return -1;
                    } else if (chunk1.length() < chunk2.length()) {
                        return 1;
                    }

                    // If ranks are equal, compare the chunks lexicographically
                    int result = chunk1.compareTo(chunk2);
                    if (result != 0) {
                        return result * -1;
                    }

                    // Check if it's the last part of comparison
                    if ((chunk1.length() == temp1.length() && chunk2.length() == temp2.length())
                            || (temp1.length() == 1 && temp2.length() == 1)) {
                        return 0;
                    } else {
                        // Recursively compare the rest of the string not in chunk
                        return compare(temp1.substring(chunk1.length()), temp2.substring(chunk2.length()));
                    }
                }
                // Compare strings lexicographically
                return temp1.compareTo(temp2);
            }
        };
    }

    /**
     * Returns a comparator which compares elements. The elements here should not have negative elements, which are
     * elements with a postfix of a dash ("-") followed by any one positive number.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @return Comparator comparing non-negative elements
     */
    protected Comparator<String> nonNegNumberComparator(boolean isFirstWordNumber, boolean isCaseIndependent) {
        return new Comparator<>() {
            @Override
            public int compare(String str1, String str2) {
                String temp1 = isCaseIndependent && !isFirstWordNumber ? str1.toUpperCase(Locale.ROOT) : str1;
                String temp2 = isCaseIndependent && !isFirstWordNumber ? str2.toUpperCase(Locale.ROOT) : str2;

                if (isFirstWordNumber && !temp1.isEmpty() && !temp2.isEmpty()) {
                    String chunk1 = getChunk(temp1);
                    String chunk2 = getChunk(temp2);

                    // Compare ranks of the first character
                    int rank1 = getRankScore(chunk1.charAt(0));
                    int rank2 = getRankScore(chunk2.charAt(0));
                    int result = rank1 - rank2;
                    if (result != 0) {
                        return result;
                    }

                    // Check if both are numbers
                    if (isRankAPositiveNumber(rank1) && isRankAPositiveNumber(rank2)) {
                        result = new BigInteger(chunk1).compareTo(new BigInteger(chunk2));
                    } else {
                        result = chunk1.compareTo(chunk2);
                    }

                    if (result != 0) {
                        return result;
                    }

                    boolean isLastInT1 = chunk1.length() == temp1.length();
                    boolean isLastInT2 = chunk2.length() == temp2.length();

                    // Check if it's the last part of comparison
                    if (isLastInT1 && isLastInT2) {
                        return 0;
                    } else if (isLastInT1) {
                        return -1;
                    } else if (isLastInT2) {
                        return 1;
                    }

                    // Recursively compare the rest of the string not in chunk
                    return compare(temp1.substring(chunk1.length()), temp2.substring(chunk2.length()));
                }
                // Compare strings lexicographically
                return temp1.compareTo(temp2);
            }
        };
    }
}
