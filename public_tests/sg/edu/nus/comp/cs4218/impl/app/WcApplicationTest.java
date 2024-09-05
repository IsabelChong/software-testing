package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.deleteFilesAndDirectoriesFrom;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.writeToFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

/**
 * Bottom up order of units under test:
 * 1. getCountReport(InputStream input) throws WcException
 * 2. countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, String... fileNames) throws WcException
 * 3. countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin) throws WcException
 * 4. countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin, String... fileNames) throws WcException
 */

public class WcApplicationTest {
    private static WcApplication wcApp;
    private OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = System.getProperty("user.dir");;

    private static final String NUMBER_FORMAT = " %7d";
    private static final String SPACE_TOTAL = " total";
    private static final String WC_PREFIX = "wc: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String TEXT1_FILE = "text1";
    private static final String TEXT2_FILE = "text2";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for WcCommandTest
    public static final String WC_TEXT1 = "What is Lorem Ipsum?\n";
    public static final String WC_TEXT2 = "What is Lorem Ipsum?";
    public static final String WC_TEXT3 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s.\n";
    public static final String WC_TEXT4 = "@#$$%^&*()!";

    private static Path text1File;
    private static Path text2File;
    private static Path noReadPermFile;

    @BeforeEach
    public void setUp() {
        wcApp = new WcApplication();
        stdout = new ByteArrayOutputStream();

        // Make a test directory containing test files
        testDir = new File(CURR_DIR + fileSeparator() +
                "public_tests" + fileSeparator() +
                "tempWcTestDir");
        testDir.mkdir();

        try {
            // Create temporary files used across various tests
            text1File = Files.createTempFile(testDir.toPath(), TEXT1_FILE, TXT_POSTFIX);
            text2File = Files.createTempFile(testDir.toPath(), TEXT2_FILE, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{text1File, text2File},
                    new String[]{WC_TEXT1, WC_TEXT2});

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
     * This tests for a WcException thrown when the input stream is null.
     */
    @Test
    public void getCountReport_emptyInputStream_shouldReturnZeros() {
        try {
            InputStream stdin = new ByteArrayInputStream("".getBytes());
            long[] output = wcApp.getCountReport(stdin);
            assertEquals(0, output[0]); // lines
            assertEquals(0, output[1]); // words
            assertEquals(0, output[2]); // bytes
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the getCountReport method correctly counts the number of lines, words, and bytes
     * when the input stream contains multiple lines and the last line does not end with a newline character.
     */
    @Test
    public void getCountReport_multipleLinesLastLineNoNewLine_shouldReturnCorrectCounts() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            long[] output = wcApp.getCountReport(stdin);
            assertEquals(1, output[0]); // lines
            assertEquals(4, output[1]); // words
            assertEquals(21, output[2]); // bytes
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the getCountReport method correctly counts the number of lines, words, and bytes
     * when the input stream contains multiple lines and the last line ends with a newline character.
     */
    @Test
    public void getCountReport_multipleLinesLastLineWithNewLine_shouldReturnCorrectCounts() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT2.getBytes());
            long[] output = wcApp.getCountReport(stdin);
            assertEquals(0, output[0]); // lines
            assertEquals(4, output[1]); // words
            assertEquals(20, output[2]); // bytes
        }   catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the getCountReport method correctly counts the number of lines, words, and bytes
     * when the input stream contains multiple words and multiple lines.
     */
    @Test
    public void getCountReport_multipleWordsMultipleLines_shouldReturnCorrectCounts() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT3.getBytes());
            long[] output = wcApp.getCountReport(stdin);
            assertEquals(2, output[0]); // lines
            assertEquals(25, output[1]); // words
            assertEquals(153, output[2]); // bytes
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the getCountReport method correctly counts the number of lines, words, and bytes
     * when the input stream contains special characters.
     */
    @Test
    public void getCountReport_specialCharacters_shouldReturnCorrectCounts() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT4.getBytes());
            long[] output = wcApp.getCountReport(stdin);
            assertEquals(0, output[0]); // lines
            assertEquals(1, output[1]); // words
            assertEquals(11, output[2]); // bytes
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks the behavior of the countFromFiles method when the fileNames parameter is null.
     * The method is expected to throw a WcException with a specific error message.
     */
    @Test
    public void countFromFiles_nullFiles_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.countFromFiles(false, false, false, null));
        assertEquals(WC_PREFIX + ERR_NULL_ARGS, result.getMessage());
    }

    /**
     * This test checks the behavior of the countFromFiles method when a file with no read permissions is passed.
     * The method is expected to throw a WcException with a specific error message.
     */
    @Test
    public void countFromFiles_fileWithNoReadPerms_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.countFromFiles(false, false, false, noReadPermFile.toFile().getPath()));
        assertEquals(WC_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This test checks the behavior of the countFromFiles method when a directory is passed instead of a file.
     * The method is expected to throw a WcException with a specific error message.
     */
    @Test
    public void countFromFiles_fileIsDirectoryInput_ShouldThrowWcException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.countFromFiles(false, false, false, pathName));
        assertEquals(WC_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void countFromFiles_invalidFile_ShouldThrowWcException() {
        String invalidFile = "invalidFile.txt";
        String pathName = testDir + File.separator + invalidFile;

        Throwable result = assertThrows(WcException.class, () ->
                wcApp.countFromFiles(false, false, false, pathName));
        assertEquals(WC_PREFIX + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when the file given does not have read permissions.
     */
    @Test
    public void run_fileWithNoReadPerms_ShouldThrowWcException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(WC_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests if multiple files given to wc would be outputted correctly.
     */
    @Test
    public void countFromFiles_MultipleFilesWithAllFlag_ShouldReturnIndividualLWBWithTotal() {
        try {
            String output = wcApp.countFromFiles(true, true, true,
                    text1File.toFile().getPath(), text2File.toFile().getPath());
            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 21) +
                            " " + text1File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 0) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 20) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 8) +
                            String.format(NUMBER_FORMAT, 41) +
                            SPACE_TOTAL
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks the countFromFiles method of the WcApplication class.
     * The method is expected to count the number of lines in multiple files when the "-l" flag is supplied.
     */
     @Test
    public void countFromFiles_MultipleFilesWithLFlag_ShouldReturnIndividualLWithTotalLOnly() {
        try {
            String output = wcApp.countFromFiles(false, true, false,
                    text1File.toFile().getPath(), text2File.toFile().getPath());
            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1) +
                            " " + text1File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 0) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 1) +
                            SPACE_TOTAL
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the countFromStdin method correctly counts the number of lines, words, and bytes
     * when the input stream contains a string and no flag is supplied.
     * The method is expected to return a formatted string of the counts.
     */
     @Test
    public void countFromStdin_NoFlag_ShouldReturnLWB() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromStdin(true, true, true, stdin);

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1) + String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 21)
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the countFromStdin method correctly counts the number of lines
     * when the input stream contains a string and the "-l" flag is supplied.
     */
     @Test
    public void countFromStdin_LFlag_ShouldReturnLOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromStdin(false, true, false, stdin);

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1)
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly with the "-w" flag supplied.
     */
    @Test
    public void countFromStdin_WFlag_ShouldReturnWOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromStdin(false, false, true, stdin);

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 4)
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the countFromStdin method correctly counts the number of bytes
     * when the input stream contains a string and the "-c" flag is supplied.
     */
    @Test
    public void countFromStdin_CFlag_ShouldReturnBOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromStdin(true, false, false, stdin);

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 21)
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the countFromStdin method correctly counts the number of lines, words, and bytes
     * when the input stream and no flag is supplied. This tests the input stream followed by file input in order.
     */
     @Test
     public void countFromFileAndStdin_stdInAndFileInputWithNoFlag_ShouldReturnLWB() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromFileAndStdin(true, true, true, stdin, STRING_STDIN_DASH, text2File.toFile().getPath());

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 21) +
                            " " + STRING_STDIN_DASH + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 0) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 20) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 8) +
                            String.format(NUMBER_FORMAT, 41) +
                            SPACE_TOTAL + STRING_NEWLINE

            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the countFromStdin method correctly counts the number of lines, words, and bytes
     * when the input stream and no flag is supplied. This tests the file followed by input stream in order.
     */
     @Test
     public void countFromFileAndStdin_fileInputAndStdinWithNoFlag_ShouldReturnLWB() {
        try {
            InputStream stdin = new ByteArrayInputStream(WC_TEXT1.getBytes());
            String output = wcApp.countFromFileAndStdin(true, true, true, stdin, text2File.toFile().getPath(), STRING_STDIN_DASH);

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 0) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 20) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                    String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 21) +
                            " " + STRING_STDIN_DASH + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 8) +
                            String.format(NUMBER_FORMAT, 41) +
                            SPACE_TOTAL + STRING_NEWLINE

            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This test checks if the countFromStdin method correctly counts the number of lines
     * when the input stream contains an empty string and the "-l" flag is supplied.
     */
     @Test
    public void countFromFileAndStdin_LFlag_ShouldReturnLOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream("Hi".getBytes());
            String output = wcApp.countFromFileAndStdin(false, true, false, stdin, STRING_STDIN_DASH, text1File.toFile().getPath());

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 0) + " " + STRING_STDIN_DASH + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 1) + " " + text1File.toFile().getPath() + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 1) + SPACE_TOTAL  + STRING_NEWLINE
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly with the "-w" flag supplied.
     */
    @Test
    public void countFromFileAndStdin_WFlag_ShouldReturnWOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream("Hi".getBytes());
            String output = wcApp.countFromFileAndStdin(false, false, true, stdin, STRING_STDIN_DASH, text1File.toFile().getPath());

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 1) + " " + STRING_STDIN_DASH + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 4) + " " + text1File.toFile().getPath() + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 5) + SPACE_TOTAL  + STRING_NEWLINE
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the countFromStdin method correctly counts the number of bytes
     * when the input stream contains a string and the "-c" flag is supplied.
     */
    @Test
    public void countFromFileAndStdin_CFlag_ShouldReturnBOnly() {
        try {
            InputStream stdin = new ByteArrayInputStream("Hi".getBytes());
            String output = wcApp.countFromFileAndStdin(true, false, false, stdin, STRING_STDIN_DASH, text1File.toFile().getPath());

            String expectedOutput = (
                    String.format(NUMBER_FORMAT, 2) + " " + STRING_STDIN_DASH + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 21) + " " + text1File.toFile().getPath() + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 23) + SPACE_TOTAL  + STRING_NEWLINE
            );
            assertEquals(expectedOutput, output);
        } catch (WcException e) {
            fail();
        }
    }
}
