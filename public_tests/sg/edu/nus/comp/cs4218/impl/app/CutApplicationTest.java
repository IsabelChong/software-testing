package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CutException;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

/**
 * Tests for the CutApplication class.
 */
public class CutApplicationTest {
    private CutApplication cutApp;
    private OutputStream stdOut;
    private InputStream stdIn;
    private String projectRoot;

    /**
     * Setup method to initialize test environment before each test case.
     */
    @BeforeEach
    public void setUp() {
        cutApp = new CutApplication();
        stdOut = new ByteArrayOutputStream();
        stdIn = new ByteArrayInputStream("Test input".getBytes());
        projectRoot = System.getProperty("user.dir"); // Get project root directory
    }

    /**
     * Tests the behavior of the `run` method when given an empty file.
     * It asserts that the output is empty.
     */
    @Test
    public void run_EmptyFile_PrintEmptyOutput() {
        File emptyFile = null;
        try {
            emptyFile = File.createTempFile("empty", ".txt", new File(projectRoot));
            cutApp.run(new String[]{"-c", "1-4", emptyFile.getAbsolutePath()}, stdIn, stdOut);
            assertEquals("" + STRING_NEWLINE, stdOut.toString()); // Expect empty output
        } catch (IOException | CutException e) {
            fail(e);
        } finally {
            if (emptyFile != null) {
                emptyFile.delete();
            }
        }
    }


    /**
     * Tests the behavior of the `run` method when given multiple ranges.
     * It asserts that the output matches the expected cut content.
     */
    @Test
    public void run_MultipleRanges_PrintCorrectOutput() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("1234567890\n");
            }

            // Run the cut application with multiple ranges
            cutApp.run(new String[]{"-c", "1-4,6-8", tempFile.getAbsolutePath()}, stdIn, stdOut);
            String expectedOutput = "1234678" + System.lineSeparator(); // Expected output with two ranges

            // Assert the output
            assertEquals(expectedOutput, stdOut.toString());

        } catch (IOException | CutException e) {
            fail(e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    /**
     * Tests the behavior of the `run` method when given an out-of-bounds byte range.
     * It asserts that the full input is returned, mimicking the behavior of Bash.
     */
    @Test
    public void run_OutOfBoundsByteRange_PrintsAllInput() {
        String inputString = "S";
        stdIn = new ByteArrayInputStream(inputString.getBytes()); // Set the input stream with sample data

        try {
            // Attempting to cut a byte range that exceeds the input length
            cutApp.run(new String[]{"-b", "1-2"}, stdIn, stdOut);
            assertEquals(inputString + System.lineSeparator(), stdOut.toString()); // Expect full input returned
        } catch (CutException e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * Tests the behavior of the `run` method when a start index is missing in the range.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void run_MissingStartIndexInRange_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "-4"}, stdIn, stdOut));
        assertEquals("cut: Invalid range format: -4", exception.getMessage());
    }


    /**
     * Tests the behavior of the `run` method when an end index is missing in the range.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void run_MissingEndIndexInRange_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-"}, stdIn, stdOut));
        assertEquals("cut: Invalid range format: 1-", exception.getMessage());
    }


    /**
     * Tests the behavior of the `run` method when given a negative byte range.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void run_NegativeByteRange_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-b", "-1-4"}, stdIn, stdOut));
        assertEquals("cut: Invalid range format: -1-4", exception.getMessage());
    }


    /**
     * Tests the behavior of the `run` method when given an invalid file path.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void run_InvalidFilePath_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-4", "nonexistent.txt"}, stdIn, stdOut));
        assertEquals("cut: " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }


    /**
     * Tests the behavior of the `run` method when given a null input stream.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void run_NullInputStream_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-4"}, null, stdOut));
        assertEquals("cut: " + ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * Tests the behavior of the `cutFromFilesAndStdin` method when given both files and standard input.
     * It asserts that the output matches the expected cut content from both sources.
     */
    @Test
    public void cutFromFilesAndStdin_WithFilesAndStdin_PrintCorrectOutput() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("1234567890\n");
            }

            // Run the cut application with files and standard input
            String[] fileNames = {tempFile.getAbsolutePath(), "-"};
            String cutContent = cutApp.cutFromFilesAndStdin(true, false, Arrays.asList(new int[]{1, 4}), stdIn, fileNames);

            String expectedOutput = "Test\n1234" + System.lineSeparator(); // Expected output from file and stdin

            // Assert the output
            assertEquals(expectedOutput, cutContent);

        } catch (IOException | CutException e) {
            fail(e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    /**
     * Tests the behavior of the `cutFromFilesAndStdin` method when given invalid file paths.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void cutFromFilesAndStdin_WithInvalidFilePath_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.cutFromFilesAndStdin(true, false, Arrays.asList(new int[]{1, 4}), stdIn, "nonexistent.txt"));
        assertEquals("cut: " + "File does not exist or is not a regular file: nonexistent.txt", exception.getMessage());
    }

    /**
     * Tests the behavior of the `cutFromFilesAndStdin` method when given a null input stream.
     * It asserts that a {@link CutException} is thrown with the appropriate error message.
     */
    @Test
    public void cutFromFilesAndStdin_WithNullInputStream_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.cutFromFilesAndStdin(true, false, Arrays.asList(new int[]{1, 4}), null, "-"));
        assertEquals("cut: Null input stream provided.", exception.getMessage());
    }

}
