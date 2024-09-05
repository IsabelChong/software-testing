package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.generateExpectedOutput;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

/**
 * Bottom up order of units under test:
 * 1. negNumberComparator(boolean isFirstWordNumber, boolean isCaseIndependent)
 * 2. nonNegNumberComparator(boolean isFirstWordNumber, boolean isCaseIndependent)
 * 3. isRankAPositiveNumber(int rank): Positive number has rank 5
 * 4. getRankScore(char inputChar)
 * 5. getChunk(String str)
 * 6. appendFileContents(List<String> lines, String file) throws SortException
 * 7. sortList(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
 *                            List<String> input)
 * 8. sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
 *                                 String... fileNames) throws AbstractApplicationException
 * 9. sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
 *                                 InputStream stdin) throws SortException
 * 10. sortFilesAndStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent, InputStream stdin, String... fileNames) throws SortException
 */

public class SortApplicationTest {
    private static SortApplication sortApp;
    private static OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = Environment.currentDirectory;

    public static final int RANK_SPECIAL_CHAR = 1;
    public static final int RANK_ZERO = 2;
    public static final int RANK_UPPERCASE = 3;
    public static final int RANK_LOWERCASE = 4;
    public static final int RANK_POS_NUM = 5;

    private static final String SORT_PREFIX = "sort: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String MIXED_FILE = "mixed";
    private static final String NUMBERS_FILE = "numbers";
    private static final String SP_CHAR_FILE = "specialChars";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for SortCommandTest
    public static final String MIXED_CONTENT = generateExpectedOutput(new String[]
            {"1", "&", "1*", "A", "1A", "0", "-11", "-1A", ")", "b", "-1*", "8", "(", "11", "a", "!", // NOPMD
                    "-cb", "-ca", "ca", "cb", "-%", "-A", "-a"}); // NOPMD
    public static final String NUMBERS_CONTENT = generateExpectedOutput(new String[]
            {"2", "1", "6", "5", "-1", "10", "0", "-5"});
    public static final String SP_CHAR_CONTENT = generateExpectedOutput(new String[]
            {"@", "^", "&", "~", "$", "(", "%", "!", ")", "#"});

    private static Path mixedFile;
    private static Path numbersFile;
    private static Path specialCharsFile;
    private static Path noReadPermFile;

    @BeforeEach
    public void setUp() {
        sortApp = new SortApplication();
        stdout = new ByteArrayOutputStream();

        // Make a test directory containing test files
        testDir = new File(CURR_DIR + fileSeparator() +
                "public_tests" + fileSeparator() +
                "tempSortTestDir");
        testDir.mkdir();

        try {
            // Create temporary files used across various tests
            mixedFile = Files.createTempFile(testDir.toPath(), MIXED_FILE, TXT_POSTFIX);
            numbersFile = Files.createTempFile(testDir.toPath(), NUMBERS_FILE, TXT_POSTFIX);
            specialCharsFile = Files.createTempFile(testDir.toPath(), SP_CHAR_FILE, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{mixedFile, numbersFile, specialCharsFile},
                    new String[]{MIXED_CONTENT, NUMBERS_CONTENT, SP_CHAR_CONTENT});

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteFilesAndDirectoriesFrom(testDir);
        testDir.delete();
        stdout.flush();
    }

    /**
     * This tests if negNumberComparator compares characters correctly (isFirstWordNumber is false).
     */
    @Test
    public void negNumberComparator_compareSmallWithBiggerCharacter_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.negNumberComparator(false, false);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if negNumberComparator compares numbers correctly even with case being true
     * (isFirstWordNumber is false and isCaseIndependent is true).
     */
    @Test
    public void negNumberComparator_compareSmallWithBiggerCharacterWithCaseTrue_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.negNumberComparator(false, true);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if negNumberComparator compares numbers correctly (isFirstWordNumber is true).
     */
    @Test
    public void negNumberComparator_compareSmallWithBiggerNumber_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.negNumberComparator(true, false);
        int output = comparator.compare("2", "3");
        assertEquals(1, output);
    }

    /**
     * This tests if negNumberComparator compares numbers correctly even with case being true
     * (isFirstWordNumber and isCaseIndependent is true).
     */
    @Test
    public void negNumberComparator_compareSmallWithBiggerNumberWithCaseTrue_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.negNumberComparator(true, true);
        int output = comparator.compare("2", "3");
        assertEquals(1, output);
    }

    /**
     * This tests if negNumberComparator compares same characters or numbers correctly.
     */
    @Test
    public void negNumberComparator_compareSameCharacter_ShouldCompareCorrectly() {
        Comparator<String> comparator = null;
        int output = 0;

        comparator = sortApp.negNumberComparator(false, false);
        output = comparator.compare("1", "1");
        assertEquals(0, output);

        comparator = sortApp.negNumberComparator(true, false);
        output = comparator.compare("1", "1");
        assertEquals(0, output);

        comparator = sortApp.negNumberComparator(true, true);
        output = comparator.compare("1", "1");
        assertEquals(0, output);
    }

    /**
     * This tests if nonNegNumberComparator compares characters correctly (isFirstWordNumber is false).
     */
    @Test
    public void nonNegNumberComparator_compareSmallWithBiggerCharacter_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.nonNegNumberComparator(false, false);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if nonNegNumberComparator compares numbers correctly even with case being true
     * (isFirstWordNumber is false and isCaseIndependent is true).
     */
    @Test
    public void nonNegNumberComparator_compareSmallWithBiggerCharacterWithCaseTrue_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.nonNegNumberComparator(false, true);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if nonNegNumberComparator compares numbers correctly (isFirstWordNumber is true).
     */
    @Test
    public void nonNegNumberComparator_compareSmallWithBiggerNumber_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.nonNegNumberComparator(true, false);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if nonNegNumberComparator compares numbers correctly even with case being true
     * (isFirstWordNumber and isCaseIndependent is true).
     */
    @Test
    public void nonNegNumberComparator_compareSmallWithBiggerNumberWithCaseTrue_ShouldCompareCorrectly() {
        Comparator<String> comparator = sortApp.nonNegNumberComparator(true, true);
        int output = comparator.compare("2", "3");
        assertEquals(-1, output);
    }

    /**
     * This tests if nonNegNumberComparator compares same characters or numbers correctly.
     */
    @Test
    public void nonNegNumberComparator_compareSameCharacter_ShouldCompareCorrectly() {
        Comparator<String> comparator = null;
        int output = 0;

        comparator = sortApp.nonNegNumberComparator(false, false);
        output = comparator.compare("1", "1");
        assertEquals(0, output);

        comparator = sortApp.nonNegNumberComparator(true, false);
        output = comparator.compare("1", "1");
        assertEquals(0, output);

        comparator = sortApp.nonNegNumberComparator(true, true);
        output = comparator.compare("1", "1");
        assertEquals(0, output);
    }

    /**
     * This tests if isRankAPositiveNumber checks for a positive number correctly, which has a rank of 5.
     */
    @Test
    public void isRankAPositiveNumber_rankOfFive_ShouldOutputTrue() {
        boolean output = sortApp.isRankAPositiveNumber(RANK_POS_NUM);
        assertTrue(output);
    }

    /**
     * This tests if isRankAPositiveNumber checks for a non-positive character correctly, which are ranks
     * between 1 and 4.
     */
    @Test
    public void isRankAPositiveNumber_rankOfFour_ShouldOutputFalse() {
        boolean output = sortApp.isRankAPositiveNumber(RANK_LOWERCASE);
        assertFalse(output);
    }

    /**
     * This tests if getRankScore checks for a special character correctly.
     */
    @Test
    public void getRankScore_specialCharacter_ShouldOutputOne() {
        int output = sortApp.getRankScore('%');
        assertEquals(output, RANK_SPECIAL_CHAR);
    }

    /**
     * This tests if getRankScore checks for zero correctly.
     */
    @Test
    public void getRankScore_zero_ShouldOutputTwo() {
        int output = sortApp.getRankScore('0');
        assertEquals(output, RANK_ZERO);
    }

    /**
     * This tests if getRankScore checks for uppercase letter correctly.
     */
    @Test
    public void getRankScore_upperCaseAlphabet_ShouldOutputThree() {
        int output = sortApp.getRankScore('A');
        assertEquals(output, RANK_UPPERCASE);
    }


    /**
     * This tests if getRankScore checks for uppercase letter correctly.
     */
    @Test
    public void getRankScore_lowerCaseAlphabet_ShouldOutputFour() {
        int output = sortApp.getRankScore('a');
        assertEquals(output, RANK_LOWERCASE);
    }

    /**
     * This tests if getRankScore checks for a positive number correctly.
     */
    @Test
    public void getRankScore_positiveNumber_ShouldOutputFive() {
        int output = sortApp.getRankScore('5');
        assertEquals(output, RANK_POS_NUM);
    }

    /**
     * This tests if getChunk chunks an empty string correctly.
     */
    @Test
    public void getChunk_emptyString_ShouldOutputEmptyString() {
        String output = sortApp.getChunk("");
        assertEquals(output, "");
    }

    /**
     * This tests if getChunk chunks a string with numbers followed by alphabets correctly.
     */
    @Test
    public void getChunk_stringWithNumberAndAlphabet_ShouldOutputFirstNumber() {
        String output = sortApp.getChunk("10Hello");
        assertEquals(output, "10");
    }

    /**
     * This tests if getChunk chunks a string with alphabets followed by numbers correctly.
     */
    @Test
    public void getChunk_stringWithAlphabetAndNumber_ShouldOutputFirstAlphabets() {
        String output = sortApp.getChunk("Hello10");
        assertEquals(output, "Hello");
    }

    /**
     * This tests if appendFileContents appends file contents correctly to a currently empty "lines".
     */
    @Test
    public void appendFileContents_emptyLinesWithNumbersFileInput_ShouldOutputFirstAlphabets() {
        try {
            List<String> lines = new ArrayList<>(Arrays.asList(""));
            sortApp.appendFileContents(lines, numbersFile.toFile().getPath());
            ArrayList<String> output = new ArrayList<>(
                    Arrays.asList("", "2", "1", "6", "5", "-1", "10", "0", "-5"));
            assertEquals(lines, output);
        } catch (SortException e) {
            fail(e);
        }
    }

    /**
     * This tests if appendFileContents appends file contents correctly to a non-empty "lines".
     */
    @Test
    public void appendFileContents_nonEmptyLinesWithNumbersFileInput_ShouldOutputFirstAlphabets() {
        try {
            List<String> lines = new ArrayList<>(Arrays.asList("line1", "line2"));
            sortApp.appendFileContents(lines, numbersFile.toFile().getPath());
            ArrayList<String> output = new ArrayList<>(
                    Arrays.asList("line1", "line2", "2", "1", "6", "5", "-1", "10", "0", "-5"));
            assertEquals(lines, output);
        } catch (SortException e) {
            fail(e);
        }
    }

    /**
     * This tests if getChunk chunks a string with mixture of special characters, followed by alphabets and numbers
     *  correctly. Note that special characters and alphabets (upper and lower case) are chunked together.
     */
    @Test
    public void getChunk_mixedString_ShouldOutputAllChunks() {
        String input = "%*15$Hello";
        List<String> expectedChunks = Arrays.asList("%*", "15", "$Hello");
        List<String> actualChunks = new ArrayList<>();

        while (!input.isEmpty()) {
            String chunk = sortApp.getChunk(input);
            actualChunks.add(chunk);
            input = input.substring(chunk.length());
        }
        assertEquals(expectedChunks, actualChunks);
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortList_sortMixedContentWithNoFlag_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(false, false, false, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("!", "&", "(", ")", "-%", "-1*", "-11", "-1A",
                "-A", "-a", "-ca", "-cb", "0", "1", "1*", "11", "1A", "8", "A", "a", "b", "ca", "cb"));
        assertEquals(expectedOutput, numbersContent);
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortList_sortMixedContentWithNumberFlag_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(true, false, false, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("-11", "-1*", "-1A", "!", "&", "(", ")", "-%", "-A",
                "-a", "-ca", "-cb", "0", "A", "a", "b", "ca", "cb", "1", "1*", "1A", "8", "11"));
        assertEquals(expectedOutput, numbersContent);
    }

    /**
     * This tests if sortList sorts correctly when isReverseOrder is true.
     */
    @Test
    public void sortList_sortMixedContentWithReverseFlag_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(false, true, false, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("cb", "ca", "b", "a", "A", "8", "1A", "11",
                "1*", "1", "0", "-cb", "-ca", "-a", "-A", "-1A", "-11", "-1*", "-%", ")", "(", "&", "!"
        ));
        assertEquals(expectedOutput, numbersContent);
    }


    /**
     * This tests if sortList sorts correctly when isCaseIndependent is true.
     */
    @Test
    public void sortList_sortMixedContentWithCaseFlag_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(false, false, true, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("!", "&", "(", ")", "-%", "-1*", "-11", "-1A",
                "-A", "-a", "-ca", "-cb", "0", "1", "1*", "11", "1A", "8", "A", "a", "b", "ca", "cb"));
        assertEquals(expectedOutput, numbersContent);
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortList_sortMixedContentWithNumberAndCaseFlag_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(true, false, true, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("-11", "-1*", "-1A", "!", "&", "(", ")", "-%", "-A",
                "-a", "-ca", "-cb", "0", "A", "a", "b", "ca", "cb", "1", "1*", "1A", "8", "11"));
        assertEquals(expectedOutput, numbersContent);
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortList_sortMixedContentWithAllFlags_ShouldSortCorrectly() {
        List<String> numbersContent = new ArrayList<>(List.of(MIXED_CONTENT.split("\n")));
        sortApp.sortList(true, true, true, numbersContent);
        ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList("11", "8", "1A", "1*", "1", "cb", "ca", "b",
                "a", "A", "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11"
        ));
        assertEquals(expectedOutput, numbersContent);
    }

    // sortFromFiles, sortFromStdin, sortFilesAndStdin's checking and sorting mechanisms are done by the above
    // functions under test. Hence, the tests for these three functions does not focus on those functionalities,
    // but serves to test if the output from sortList is refactored to be separated by newlines, and ends with
    // a new line.

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortFromFiles_sortMixedContentWithAllFlags_ShouldSortCorrectly() {
        try {
            String output = sortApp.sortFromFiles(true, true, true, mixedFile.toFile().getPath());
            String expectedOutput = generateExpectedOutput("11", "8", "1A", "1*", "1", "cb", "ca", "b",
                    "a", "A", "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11");
            assertEquals(expectedOutput, output + STRING_NEWLINE);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortFromStdin_sortMixedContentWithAllFlags_ShouldSortCorrectly() {
        try {
            InputStream stdin = new ByteArrayInputStream(MIXED_CONTENT.getBytes());
            String output = sortApp.sortFromStdin(true, true, true, stdin);
            String expectedOutput = generateExpectedOutput("11", "8", "1A", "1*", "1", "cb", "ca", "b",
                    "a", "A", "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11");
            assertEquals(expectedOutput, output + STRING_NEWLINE);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sortList sorts correctly when isFirstWordNumber is true.
     */
    @Test
    public void sortFilesAndStdin_sortMixedContentWithAllFlags_ShouldSortCorrectly() {
        try {
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            String output = sortApp.sortFilesAndStdin(true, true, true, stdin, mixedFile.toFile().getPath());
            String expectedOutput = generateExpectedOutput("11", "8", "1A", "1*", "1", "cb", "ca", "b",
                    "a", "A", "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11");
            assertEquals(expectedOutput, output + STRING_NEWLINE);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * Deletes all files and directories in the given path.
     *
     * @param path Path to delete files and directories from
     */
    private void deleteFilesAndDirectoriesFrom(File path) {
        File[] filesList = path.listFiles();

        if (filesList != null) {
            for (File file : filesList) {
                if (file.isDirectory()) {
                    deleteFilesAndDirectoriesFrom(file.getAbsoluteFile());
                }
                file.delete();
            }
        }
    }

    /**
     * Writes multiple contents into files of matching array indexes.
     *
     * @param paths    An array of paths to write content of its matching index into
     * @param contents An array of contents to write into the path of its matching index
     */
    private void writeToFiles(Path[] paths, String... contents) {
        for (int i = 0; i < paths.length; i++) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(paths[i].toFile().getPath()))) {
                    writer.write(contents[i]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
