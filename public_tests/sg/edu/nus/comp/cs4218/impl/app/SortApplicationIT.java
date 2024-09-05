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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.generateExpectedOutput;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

/**
 * Negative Cases:
 * 1. Providing a null output stream.
 * 2. Specifying a file that does not exist.
 * 3. Specifying a directory instead of a file.
 * 4. Providing empty input via stdin.
 * 5. Providing wrong flag.
 * <p>
 * Positive Cases:
 * 1. Using no flags to sort.
 * 2. Using the "-n" flag to sort lines numerically.
 * 3. Using the "-r" flag to sort lines in reverse order.
 * 4. Using the "-f" flag to perform case-insensitive sorting.
 * 5. Using the "-nr" flags together to sort lines numerically and in reverse order.
 * 6. Using the "-nf" flags together to sort lines numerically and perform case-insensitive sorting.
 * 7. Using the "-rf" flags together to sort lines in reverse order and perform case-insensitive sorting.
 * 8. Using the "-nrf" flags together to sort lines numerically, in reverse order, and perform case-insensitive sorting.
 * 9. Supplying more than one file to be sorted.
 * 10. Not supplying any files and using stdin to provide input for sorting, to be terminated by end of stream.
 * 11. Different ways of specifying the flags, for example "-nf" and "-n -f" should provide the same outcome.
 */

public class SortApplicationIT { //NOPMD
    private static SortApplication sortApp;
    private static OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = Environment.currentDirectory;

    private static final String SORT_PREFIX = "sort: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String MIXED_FILE = "mixed";
    private static final String NUMBERS_FILE = "numbers";
    private static final String SP_CHAR_FILE = "specialChars";
    private static final String ALPHAS_FILE = "alphabets";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for SortCommandTest
    public static final String MIXED_CONTENT = generateExpectedOutput(new String[]
            {"1", "&", "1*", "A", "1A", "0", "-11", "-1A", ")", "b", "-1*", "8", "(", "11", "a", "!", // NOPMD
                    "-cb", "-ca", "ca", "cb", "-%", "-A", "-a"}); // NOPMD
    public static final String NUMBERS_CONTENT = generateExpectedOutput(new String[]
            {"2", "1", "6", "5", "-1", "10", "0", "-5"});
    public static final String ALPHAS_CONTENT = generateExpectedOutput(new String[]
            {"a", "b", "A", "B", "ca", "cb", "z", "y"});
    public static final String SP_CHAR_CONTENT = generateExpectedOutput(new String[]
            {"@", "^", "&", "~", "$", "(", "%", "!", ")", "#"});

    private static Path mixedFile;
    private static Path numbersFile;
    private static Path specialCharsFile;
    private static Path alphabetsFile;
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
            alphabetsFile = Files.createTempFile(testDir.toPath(), ALPHAS_FILE, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{mixedFile, numbersFile, alphabetsFile, specialCharsFile},
                    new String[]{MIXED_CONTENT, NUMBERS_CONTENT, ALPHAS_CONTENT, SP_CHAR_CONTENT});

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
     * This tests for a SortException thrown when the input stream is null.
     */
    @Test
    public void run_nullInputStream_ShouldThrowSortException() {
        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{"a"}, null, stdout));
        assertEquals(SORT_PREFIX + ERR_NULL_STREAMS, result.getMessage());
    }

    /**
     * This tests for a SortException thrown when the output stream is null.
     */
    @Test
    public void run_nullOutputStream_ShouldThrowSortException() {
        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{"a"}, System.in, null));
        assertEquals(SORT_PREFIX + ERR_NULL_STREAMS, result.getMessage());
    }

    /**
     * This tests for a SortException thrown when a directory input is given to sort.
     */
    @Test
    public void run_DirectoryInput_ShouldThrowSortException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(SORT_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests for a SortException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_invalidFile_ShouldThrowSortException() {
        String invalidFile = "invalidFile.txt";
        String pathName = testDir + File.separator + invalidFile;

        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(SORT_PREFIX + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    public void run_fileWithNoReadPerms_ShouldThrowCatException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(SORT_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests for a SortException thrown when an illegal flag is given to the sort command.
     */
    @Test
    public void run_IllegalFlag_ShouldThrowSortException() {
        String illegalFlag = "b";
        Throwable result = assertThrows(SortException.class, () ->
                sortApp.run(new String[]{CHAR_FLAG_PREFIX + illegalFlag}, System.in, stdout));
        assertEquals(SORT_PREFIX + ILLEGAL_FLAG_MSG + illegalFlag, result.getMessage());
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements given (special characters, numbers,
     * alphabets, etc) when no flag is supplied.
     */
    @Test
    public void run_sortMixedWithNoFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1*", "-11", "-1A", //NOPMD
                    "-A", "-a", "-ca", "-cb", "0", "1", "1*", "11", "1A", "8", "A", "a", "b", "ca", "cb"}); //NOPMD
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on numbers when no flag is supplied.
     */
    @Test
    public void run_sortNumbersWithNoFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{numbersFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"-1", "-5", "0", "1", "10", "2", "5", "6"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on special characters when no flag is supplied.
     */
    @Test
    public void run_sortSpecialCharsWithNoFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{specialCharsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "#", "$", "%", "&", "(", ")", "@", "^", "~"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on alphabets when no flag is supplied.
     */
    @Test
    public void run_sortAlphabetsWithNoFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{alphabetsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"A", "B", "a", "b", "ca", "cb", "y", "z"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-n" flag is supplied.
     */
    @Test
    public void run_sortMixedWithNFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-n", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"-11", "-1*", "-1A", "!", "&", "(", ")", "-%", "-A",
                    "-a", "-ca", "-cb", "0", "A", "a", "b", "ca", "cb", "1", "1*", "1A", "8", "11"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on numbers when the "-n" flag is supplied.
     */
    @Test
    public void run_sortNumbersOnlyWithNFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-n", numbersFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"-5", "-1", "0", "1", "2", "5", "6", "10"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on alphabets when the "-n" flag is supplied.
     */
    @Test
    public void run_sortAlphabetsOnlyWithNFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-n", alphabetsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"A", "B", "a", "b", "ca", "cb", "y", "z"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on special characters when the "-n" flag is supplied.
     */
    @Test
    public void run_sortSpecialCharactersOnlyWithNFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-n", specialCharsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "#", "$", "%", "&", "(", ")", "@", "^", "~"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-r" flag is supplied.
     */
    @Test
    public void run_sortMixedWithRFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-r", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"cb", "ca", "b", "a", "A", "8", "1A", "11", "1*", "1",
                    "0", "-cb", "-ca", "-a", "-A", "-1A", "-11", "-1*", "-%", ")", "(", "&", "!"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on numbers when the "-r" flag is supplied.
     */
    @Test
    public void run_sortNumbersOnlyWithRFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-r", numbersFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"6", "5", "2", "10", "1", "0", "-5", "-1"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on alphabets when the "-r" flag is supplied.
     */
    @Test
    public void run_sortAlphabetsOnlyWithRFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-r", alphabetsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"z", "y", "cb", "ca", "b", "a", "B", "A"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on special characters when the "-r" flag is supplied.
     */
    @Test
    public void run_sortSpecialCharactersOnlyWithRFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-r", specialCharsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"~", "^", "@", ")", "(", "&", "%", "$", "#", "!"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-f" flag is supplied.
     */
    @Test
    public void run_sortMixedWithFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-f", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1*", "-11", "-1A", "-A",
                    "-a", "-ca", "-cb", "0", "1", "1*", "11", "1A", "8", "A", "a", "b", "ca", "cb"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on numbers when the "-f" flag is supplied.
     */
    @Test
    public void run_sortNumbersOnlyWithFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-f", numbersFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"-1", "-5", "0", "1", "10", "2", "5", "6"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on alphabets when the "-f" flag is supplied.
     */
    @Test
    public void run_sortAlphabetsOnlyWithFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-f", alphabetsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"A", "a", "B", "b", "ca", "cb", "y", "z"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on special characters when the "-f" flag is supplied.
     */
    @Test
    public void run_sortSpecialCharsOnlyWithFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-f", specialCharsFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "#", "$", "%", "&", "(", ")", "@", "^", "~"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-nr" flag is supplied.
     */
    @Test
    public void run_sortMixedWithNRFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-nr", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"11", "8", "1A", "1*", "1", "cb", "ca", "b", "a", "A",
                    "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-nf" flag is supplied.
     */
    @Test
    public void run_sortMixedWithNFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-nf", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"-11", "-1*", "-1A", "!", "&", "(", ")", "-%", "-A",
                    "-a", "-ca", "-cb", "0", "A", "a", "b", "ca", "cb", "1", "1*", "1A", "8", "11"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-rf" flag is supplied.
     */
    @Test
    public void run_sortMixedWithRFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-rf", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"cb", "ca", "b", "a", "A", "8", "1A", "11", "1*", "1",
                    "0", "-cb", "-ca", "-a", "-A", "-1A", "-11", "-1*", "-%", ")", "(", "&", "!"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly on a mixture of elements when the "-nrf" flag is supplied.
     */
    @Test
    public void run_sortMixedWithNRFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-nrf", mixedFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"11", "8", "1A", "1*", "1", "cb", "ca", "b", "a", "A",
                    "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly two file are specified as inputs to the sort command.
     */
    @Test
    public void run_sortTwoFiles_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{mixedFile.toFile().getPath(), numbersFile.toFile().getPath()};
            sortApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1", "-1*", "-11", "-1A",
                    "-5", "-A", "-a", "-ca", "-cb", "0", "0", "1", "1", "1*", "10", "11", "1A", "2", "5", "6", "8",
                    "A", "a", "b", "ca", "cb"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly when stdin inputs only are supplied to the sort command when
     * no flag is supplied. The sorting functionality is already tested above, so this function tests the stdin inputs
     * passover to the sorting function rather than the explicit testing of the correctness of sorting.
     */
    @Test
    public void run_sortStdinWithNoFlag_ShouldSortCorrectly() {
        try {
            String input = MIXED_CONTENT;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String[] args = new String[]{};
            sortApp.run(args, stdin, stdout);

            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1*", "-11", "-1A",
                    "-A", "-a", "-ca", "-cb", "0", "1", "1*", "11", "1A", "8", "A", "a", "b", "ca", "cb"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly when stdin inputs only are supplied to the sort command when
     * the "-nrf" flag is supplied. The sorting functionality is already tested above, so this function tests the
     * stdin inputs passover to the sorting function rather than the explicit testing of the correctness of sorting.
     */
    @Test
    public void run_sortStdinWithNRFFlag_ShouldSortCorrectly() {
        try {
            String[] args = new String[]{"-nrf"};
            String input = MIXED_CONTENT;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            sortApp.run(args, stdin, stdout);

            String output = generateExpectedOutput(new String[]{"11", "8", "1A", "1*", "1", "cb", "ca", "b", "a", "A",
                    "0", "-cb", "-ca", "-a", "-A", "-%", ")", "(", "&", "!", "-1A", "-1*", "-11"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly when stdin and file inputs are supplied to the sort command in order
     * when no flag is supplied. The sorting functionality is already tested above, so this function tests the stdin and
     * file inputs passover to the sorting function rather than the explicit testing of the correctness of sorting.
     */
    @Test
    public void run_sortStdinAndFileWithNoFlag_ShouldSortCorrectly() {
        try {
            String input = MIXED_CONTENT;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String[] args = new String[]{STRING_STDIN_DASH, numbersFile.toFile().getPath()};
            sortApp.run(args, stdin, stdout);

            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1", "-1*", "-11", "-1A",
                    "-5", "-A", "-a", "-ca", "-cb", "0", "0", "1", "1", "1*", "10", "11", "1A", "2", "5", "6", "8",
                    "A", "a", "b", "ca", "cb"});
            assertEquals(stdout.toString(), output);
        } catch (SortException e) {
            fail();
        }
    }

    /**
     * This tests if sorting is done correctly when file and stdin inputs are supplied to the sort command in order
     * when no flag is supplied. The sorting functionality is already tested above, so this function tests the stdin and
     * file inputs passover to the sorting function rather than the explicit testing of the correctness of sorting.
     */
    @Test
    public void run_sortFileAndStdinWithNoFlag_ShouldSortCorrectly() {
        try {
            String input = MIXED_CONTENT;
            InputStream stdin = new ByteArrayInputStream(input.getBytes());
            String[] args = new String[]{numbersFile.toFile().getPath(), STRING_STDIN_DASH};
            sortApp.run(args, stdin, stdout);

            String output = generateExpectedOutput(new String[]{"!", "&", "(", ")", "-%", "-1", "-1*", "-11", "-1A",
                    "-5", "-A", "-a", "-ca", "-cb", "0", "0", "1", "1", "1*", "10", "11", "1A", "2", "5", "6", "8",
                    "A", "a", "b", "ca", "cb"});
            assertEquals(stdout.toString(), output);
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
