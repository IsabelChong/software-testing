package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CutException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Tests for the CutApplication class.
 */
public class CutApplicationIT { //NOPMD
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
     * Test case for running cut command with null arguments.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_NullArgs_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(null, stdIn, stdOut));
        assertEquals("cut: " + ERR_MISSING_ARG, exception.getMessage());//NOPMD
    }

    /**
     * Test case for running cut command with empty arguments.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_EmptyArgs_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{}, stdIn, stdOut));
        assertEquals("cut: " + ERR_MISSING_ARG, exception.getMessage());
    }

    /**
     * Test case for running cut command with invalid option.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_InvalidOption_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-z"}, stdIn, stdOut));
        assertEquals("cut: " + ERR_INVALID_FLAG + ": -z", exception.getMessage());
    }

    /**
     * Test case for running cut command with missing flag.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_MissingFlag_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"1-2-3"}, stdIn, stdOut));
        assertEquals("cut: " + ERR_INVALID_FLAG + ": 1-2-3", exception.getMessage());
    }

    /**
     * Test case for running cut command with missing range.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_MissingRange_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c"}, stdIn, stdOut));
        assertEquals("cut: " + ERR_MISSING_ARG, exception.getMessage());
    }

    /**
     * Test case for running cut command with invalid range format.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_InvalidRangeFormat_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-2-3"}, stdIn, stdOut));
        assertEquals("cut: Invalid range format: 1-2-3", exception.getMessage());
    }

    /**
     * Test case for running cut command from standard input with character option.
     * Expects the correct portion of input to be printed to standard output.
     */
    @Test
    public void run_FromStdInCharOption_PrintCorrectOutput() {
        try {
            cutApp.run(new String[]{"-c", "1-4"}, stdIn, stdOut);//NOPMD
        } catch (CutException e) {
            fail(e);
        }
        assertEquals("Test\n", stdOut.toString());
    }

    /**
     * Test case for running cut command from standard input with byte option.
     * Expects the correct portion of input to be printed to standard output.
     */
    @Test
    public void run_FromStdInByteOption_PrintCorrectOutput() {
        try {
            cutApp.run(new String[]{"-b", "1-4"}, stdIn, stdOut);
        } catch (CutException e) {
            fail(e);
        }
        assertEquals("Test\n", stdOut.toString());
    }

    /**
     * Test case for running cut command from a file with character option.
     * Expects the correct portion of file content to be printed to standard output.
     */
    @Test
    public void run_FromFileCharOption_PrintCorrectOutput() {
        // Create a temporary file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));//NOPMD
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");
                writer.write("foo bar\n");
            } catch (IOException e) {
                fail(e);
            }


            // Run the cut application
            cutApp.run(new String[]{"-c", "1-4", tempFile.getAbsolutePath()}, stdIn, stdOut);
            String expectedOutput = "hell" + System.lineSeparator() + "foo " + System.lineSeparator(); // Expected output based on the given range

            // Assert the output
            assertEquals(expectedOutput, stdOut.toString());

            // Delete the temporary file
            tempFile.delete();

        } catch (IOException e) {
            fail(e);
        } catch (CutException e) {
            fail(e);
        }
    }

    /**
     * Test case for running cut command from a file with byte option.
     * Expects the correct portion of file content to be printed to standard output.
     */
    @Test
    public void run_FromFileByteOption_PrintCorrectOutput() {
        // Write content to the test file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("Testing123\n");
            }
            cutApp.run(new String[]{"-b", "1-4", tempFile.getAbsolutePath()}, stdIn, stdOut);
        } catch (IOException e) {
            fail(e);
        } catch (CutException e) {
            fail(e);
        }

        // Verify the output
        assertEquals("Test", stdOut.toString().trim());

        tempFile.delete();
    }

    /**
     * Test case for running cut command with an invalid file.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_InvalidFile_ThrowException() {
        Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-4", "invalid.txt"}, stdIn, stdOut));
        assertEquals("cut: " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test case for running cut command with invalid range values.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_InvalidRangeValues_ThrowException() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));

            // Run the cut application
            cutApp.run(new String[]{"-c", "1-4", tempFile.getAbsolutePath()}, stdIn, stdOut);

            // Test case for invalid range values
            File finalTempFile = tempFile;
            Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "a-b", finalTempFile.getAbsolutePath()}, stdIn, stdOut));
            assertEquals("cut: Invalid range format: a-b", exception.getMessage());

            File finalTempFile1 = tempFile;
            exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "-1-4", finalTempFile1.getAbsolutePath()}, stdIn, stdOut));
            assertEquals("cut: Invalid range format: -1-4", exception.getMessage());

            File finalTempFile2 = tempFile;
            exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1--4", finalTempFile2.getAbsolutePath()}, stdIn, stdOut));
            assertEquals("cut: Invalid range format: 1--4", exception.getMessage());

            tempFile.delete();

        } catch (IOException e) {
            fail(e);
        } catch (CutException e) {
            fail(e);
        }
    }

    /**
     * Test case for running cut command with out of bounds range values.
     * Expects the correct portion of file content to be printed to standard output.
     */
    @Test
    public void run_OutOfBoundsRangeValues_PrintCorrectOutput() {
        // Write content to the test file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("Test input\n");
            }

            // Test case for out of bounds range values
            cutApp.run(new String[]{"-c", "1-20", tempFile.getAbsolutePath()}, stdIn, stdOut);
            assertEquals("Test input", stdOut.toString().trim());

            File finalTempFile = tempFile;
            Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "0-4", finalTempFile.getAbsolutePath()}, stdIn, stdOut));
            assertEquals("cut: Start index cannot start with 0 or lesser", exception.getMessage());

            tempFile.delete();
        } catch (IOException e) {
            fail(e);
        } catch (CutException e) {
            fail(e);
        }
    }

    /**
     * Test case for running cut command with a file without read permission.
     * Expects a CutException with an appropriate error message.
     */
    @Test
    public void run_FileWithoutReadPermission_ThrowException() {
        File tempFile = null;
        try {
            // Create a temporary file without read permission
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            tempFile.setReadable(false);

            // Run the cut application
            File finalTempFile = tempFile;
            Throwable exception = assertThrows(CutException.class, () -> cutApp.run(new String[]{"-c", "1-4", finalTempFile.getAbsolutePath()}, stdIn, stdOut));

            // Assert the exception message
            assertEquals("cut: " + ERR_READING_FILE, exception.getMessage());

            // Delete the temporary file
            tempFile.delete();

        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test case for running cut command with a file without write permission.
     * Expects the correct portion of file content to be printed to standard output.
     */
    @Test
    public void run_FileWithoutWritePermission_PrintCorrectOutput() {
        // Create a temporary file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");
            }

            tempFile.setWritable(false);


            // Run the cut application
            cutApp.run(new String[]{"-c", "1-4", tempFile.getAbsolutePath()}, stdIn, stdOut);
            String expectedOutput = "hell" + System.lineSeparator(); // Expected output based on the given range

            // Assert the output
            assertEquals(expectedOutput, stdOut.toString());

            // Delete the temporary file
            tempFile.delete();

        } catch (IOException e) {
            fail(e);
        } catch (CutException e) {
            fail(e);
        }
    }




}
