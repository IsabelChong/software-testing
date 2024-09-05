package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

/**
 * Tests for the GrepApplication class.
 */
public class GrepApplicationTest {

    private final static String TEXT_ONE = "apple";
    private final static String TEXT_TWO = "banana";
    private final static String TEXT_MULTI_LINE = TEXT_ONE + STRING_NEWLINE + TEXT_TWO + STRING_NEWLINE + "orange";
    private static final String TEST_FILE = "file.txt";
    private final static String STRING_STDIN_DASH = "-";

    private GrepApplication grepApplication;

    /**
     * Sets up the test environment before each test case.
     *
     * @throws IOException if an I/O error occurs
     */
    @BeforeEach
    void setUp() throws IOException {
        grepApplication = new GrepApplication();
        createFile();
    }

    /**
     * Deletes the test file after each test case.
     *
     * @throws IOException if an I/O error occurs
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    /**
     * Helper method to create a test file.
     *
     * @throws IOException if an I/O error occurs
     */
    private void createFile() throws IOException {
        Files.write(Paths.get(TEST_FILE), TEXT_MULTI_LINE.getBytes());
    }

    /**
     * Tests the behavior of `grepFromStdin` method when an empty pattern is provided.
     * It now asserts that all input lines are returned.
     */
    @Test
    void grepFromStdin_EmptyPattern_ReturnsAllLines() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream("apple\nbanana\norange\n".getBytes());
        String expected = "apple\nbanana\norange\n";
        String actual = grepApplication.grepFromStdin("", false, false, false, stdin);
        assertEquals(expected, actual);
    }

    /**
     * Tests the behavior of `grepFromStdin` method when the pattern is found in stdin.
     * It asserts that the matching line is returned.
     *
     * @throws AbstractApplicationException if an error occurs during execution
     */
    @Test
    void grepFromStdin_PatternFoundInStdin_ReturnsMatchingLine() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream("apple\nbanana\norange\n".getBytes());
        String expected = TEXT_TWO;
        String actual = grepApplication.grepFromStdin(TEXT_TWO, false, false, false, stdin);
        assertEquals(expected, actual.trim());
    }

    /**
     * Tests the behavior of `grepFromFiles` method when the pattern is not found in the file.
     * It asserts that an empty string is returned.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void grepFromFiles_PatternNotFound_ReturnsEmptyString() throws Exception {
        String[] fileNames = new String[]{TEST_FILE};
        String expected = "";
        String actual = grepApplication.grepFromFiles("watermelon", false, false, false, fileNames);
        assertEquals(expected, actual.trim());
    }

    /**
     * Tests the behavior of `grepFromFiles` method when the pattern is found in the file.
     * It asserts that the matching line is returned.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void grepFromFiles_PatternFoundInFile_ReturnsMatchingLine() throws Exception {
        String[] fileNames = new String[]{TEST_FILE};
        String expected = TEXT_ONE;
        String actual = grepApplication.grepFromFiles(TEXT_ONE, false, false, false, fileNames);
        assertEquals(expected, actual.trim());
    }

    /**
     * Tests the behavior of `grepFromFiles` method when null pattern and file names are provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void grepFromFiles_NullPatternAndFileNames_ThrowsException() {
        assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(null, false, false, false, (String[]) null));
    }

    /**
     * Tests the behavior of `grepFromFileAndStdin` method when null pattern, file names, and stdin are provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void grepFromFileAndStdin_NullPatternFileNamesAndStdin_ThrowsException() {
        InputStream stdin = new ByteArrayInputStream(TEXT_TWO.getBytes());
        assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(null, false, false, false, stdin, (String[]) null));
    }

    /**
     * Tests the behavior of `grepFromFileAndStdin` method when an invalid pattern is provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void grepFromFileAndStdin_InvalidPattern_ThrowsException() {
        InputStream stdin = new ByteArrayInputStream(TEXT_TWO.getBytes());
        String[] fileNames = new String[]{TEST_FILE};
        assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin("[", false, false, false, stdin, fileNames));
    }

    /**
     * Tests the behavior of `grepFromFileAndStdin` method when the pattern is not found.
     * It asserts that an empty string is returned.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void grepFromFileAndStdin_PatternNotFound_ReturnsEmptyString() throws Exception {
        InputStream stdin = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String[] fileNames = new String[]{TEST_FILE};
        String expected = "";
        String actual = grepApplication.grepFromFileAndStdin("watermelon", false, false, false, stdin, fileNames);
        assertEquals(expected, actual.trim());
    }

    /**
     * Tests the behavior of the `run` method when null arguments are provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void run_NullArguments_ThrowsException() {
        assertThrows(GrepException.class,
                () -> grepApplication.run(null, null, null));
    }

    /**
     * Tests the behavior of the `run` method when an empty pattern is provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void run_EmptyPattern_ThrowsException() {
        assertThrows(GrepException.class,
                () -> grepApplication.run(new String[]{""}, null, null));
    }

    /**
     * Tests the behavior of the `run` method when no input is provided.
     * It asserts that a {@link GrepException} is thrown.
     */
    @Test
    void run_NoInput_ThrowsException() {
        assertThrows(GrepException.class,
                () -> grepApplication.run(new String[]{TEXT_TWO}, null, null));
    }

    /**
     * Tests the behavior of the `run` method when the pattern is found in both file and stdin.
     * It asserts that both matching lines are returned.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void run_PatternFoundBoth_ReturnsMatchingLines() throws Exception {
        String[] args = new String[]{TEXT_TWO, TEST_FILE, STRING_STDIN_DASH};
        String expected = "file.txt: banana" + STRING_NEWLINE + "(standard input): banana";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(TEXT_TWO.getBytes());

        grepApplication.run(args, stdin, outputStream);
        assertEquals(expected, outputStream.toString().trim());
    }
}
