package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.GrepException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Test class for {@code GrepApplication} class.
 */
class GrepApplicationIT {//NOPMD
    private GrepApplication grepApp;
    private ByteArrayOutputStream outputStream;
    private String projectRoot;

    /**
     * Sets up the test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        grepApp = new GrepApplication();
        outputStream = new ByteArrayOutputStream();
        projectRoot = System.getProperty("user.dir"); // Get project root directory
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin and contains the specified pattern.
     * Expect the output to contain the matching lines.
     */
    @Test
    void run_StateWithStdinContainingPattern_ExpectMatchingLines() {
        String input = "hello world\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";//NOPMD
        String[] args = {pattern};
        System.setIn(inputStream);
        try {
            grepApp.run(args, System.in, outputStream);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "hello world";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via a file and contains the specified pattern.
     * Expect the output to contain the matching lines.
     */
    @Test
    void run_StateWithFileContainingPattern_ExpectMatchingLines() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));//NOPMD
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");//NOPMD
                writer.write("foo bar\n");
            }
            assertTrue(tempFile.exists(), "Temporary file should exist");
            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath()};
            System.setOut(new PrintStream(outputStream));
            grepApp.run(args, null, System.out);
            String expectedOutput = "hello world";
            assertEquals(expectedOutput, outputStream.toString().trim());
            tempFile.delete();
        } catch (IOException e) {
            fail(e);
        } catch (GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin but does not contain the specified pattern.
     * Expect the output to have no matching lines.
     */
    @Test
    void run_StateWithStdinNotContainingPattern_ExpectNoMatchingLines() {
        String input = "foo bar\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";
        String[] args = {pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = ""; // No matching lines
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin with the case-insensitive option and contains the specified pattern.
     * Expect the output to contain the matching lines regardless of case.
     */
    @Test
    void run_StateWithCaseInsensitiveOptionAndStdinContainingPattern_ExpectMatchingLines() {
        String input = "HELLO world\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";
        String[] args = {"-i", pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "HELLO world";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin with the file prefix option and contains the specified pattern.
     * Expect the output to contain the matching lines with file name prefixes.
     */
    @Test
    void run_StateWithFilePrefixOptionAndStdinContainingPattern_ExpectMatchingLinesWithPrefix() {
        String input = "HELLO world\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "HELLO";
        String[] args = {"-H", pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "(standard input): HELLO world";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin with the count lines option and contains the specified pattern.
     * Expect the output to contain the count of matching lines.
     */
    @Test
    void run_StateWithCountLinesOptionAndStdinContainingPattern_ExpectMatchingLineCount() {
        String input = "hello world\nhello how are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";
        String[] args = {"-c", pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "2";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via a file with the print file name option and contains the specified pattern.
     * Expect the output to contain the matching lines with file names prefixed.
     */
    @Test
    void run_StateWithPrintFileNameOptionAndFileContainingPattern_ExpectMatchingLinesWithFileName() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");
                writer.write("foo bar\n");
            }
            String pattern = "hello";
            String[] args = {"-H", pattern, tempFile.getAbsolutePath()};
            System.setOut(new PrintStream(outputStream));
            grepApp.run(args, null, System.out);
            String expectedOutput = tempFile.getAbsolutePath() + ": hello world";
            tempFile.delete();
            assertEquals(expectedOutput, outputStream.toString().trim());
        } catch (IOException e) {
            fail(e);
        } catch (GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when input is provided via stdin with multiple options and contains the specified pattern.
     * Expect the output to contain the count of matching lines.
     */
    @Test
    void run_StateWithMultipleOptionsAndStdinContainingPattern_ExpectMatchingLineCount() {
        String input = "HELLO world\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";
        String[] args = {"-i", "-c", pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "1";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when no input is provided via stdin.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithStandardInputWhenNoInputProvided_ExpectNoInputError() {
        String pattern = "hello";
        String[] args = {pattern};
        Throwable exception = assertThrows(GrepException.class, () -> grepApp.run(args, null, System.out));
        assertEquals("grep: " + ERR_NO_INPUT, exception.getMessage());
    }

    /**
     * Tests the behavior of the {@code run} method when a file specified does not exist.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithFileWhenFileDoesNotExist_ExpectFileNotFoundError() {
        String pattern = "hello";
        String[] args = {pattern, "nonexistent.txt"};

        // Redirect System.out to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Run the grepApp
        try {
            grepApp.run(args, null, System.out);
        } catch (GrepException e) {
            fail(e);
        }

        // Reset System.out to the default
        System.setOut(System.out);

        // Verify the content written to System.out
        assertEquals("nonexistent.txt: " + ERR_FILE_NOT_FOUND, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when the specified pattern does not exist in stdin.
     * Expect the output to have no matching lines.
     */
    @Test
    void run_StateWithPatternNotExistingInStdin_ExpectNoMatchingLines() {
        String input = "foo bar\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "hello";
        String[] args = {pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = ""; // No matching lines
        assertEquals(expectedOutput, outputStream.toString().trim());
    }

    /**
     * Tests the behavior of the {@code run} method when the specified pattern does not exist in a file.
     * Expect the output to have no matching lines.
     */
    @Test
    void run_StateWithPatternNotExistingInFile_ExpectNoMatchingLines() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("foo bar\nhow are you?\n");
            }

            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath()};
            System.setOut(new PrintStream(outputStream));
            grepApp.run(args, null, System.out);
            String expectedOutput = ""; // No matching lines
            assertEquals(expectedOutput, outputStream.toString().trim());

            tempFile.delete();
        } catch (IOException e) {
            fail(e);
        } catch (GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when an invalid option is provided.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithInvalidOption_ExpectInvalidOptionError() {
        String pattern = "pattern";
        String[] args = {"-x", pattern, "file.txt"};
        assertThrows(GrepException.class, () -> grepApp.run(args, null, System.out));
    }

    /**
     * Tests the behavior of the {@code run} method when an invalid pattern is provided.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithInvalidPattern_ExpectInvalidPatternError() {
        String[] args = {"[]]"};
        assertThrows(GrepException.class, () -> grepApp.run(args, null, System.out));
    }

    /**
     * Tests the behavior of the {@code run} method when multiple files are provided but one does not exist.
     * Expect mixed output with existing file content and a file not found error message.
     */
    @Test
    void run_StateWithMultipleFilesButOneDoesNotExist_ExpectMixedOutput() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");
                writer.write("foo bar\n");
            }
            assertTrue(tempFile.exists(), "Temporary file should exist");
            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath(), "nonexistent.txt"};
            System.setOut(new PrintStream(outputStream));
            grepApp.run(args, null, System.out);
            String expectedOutput = tempFile.getAbsolutePath() + ": hello world\n" + "nonexistent.txt: " + ERR_FILE_NOT_FOUND;
            assertEquals(expectedOutput, outputStream.toString().trim());

            tempFile.delete();
        } catch (IOException e) {
            fail(e);
        } catch (GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when an empty file is provided.
     * Expect the output to have no matching lines.
     */
    @Test
    void run_StateWithEmptyFile_ExpectNoMatchingLines() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath()};
            System.setOut(new PrintStream(outputStream));
            grepApp.run(args, null, System.out);
            String expectedOutput = ""; // No matching lines
            assertEquals(expectedOutput, outputStream.toString().trim());

            tempFile.delete();
        } catch (IOException e) {
            fail(e);
        } catch (GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when an empty pattern is provided.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithEmptyPattern_ExpectInvalidPatternError() {
        String[] args = {""};
        assertThrows(GrepException.class, () -> grepApp.run(args, null, System.out));
    }

    /**
     * Tests the behavior of the {@code run} method when stdin contains special characters.
     * Expect the output to contain the lines matching the special pattern.
     */

    @Test
    void run_StateWithSpecialCharactersInStdin_ExpectMatchingLines() {
        String input = "foo bar\nhow are you?\n$pecial characters\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "[$]pecial";
        String[] args = {pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "$pecial characters";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }


    /**
     * Tests the behavior of the {@code run} method when a regex pattern is provided.
     * Expect the output to contain the lines matching the regex pattern.
     */
    @Test
    void run_StateWithRegexPattern_ExpectMatchingLines() {
        String input = "hello world\nhow are you?\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String pattern = "\\b\\w+\\b";
        String[] args = {pattern};
        System.setIn(inputStream);
        System.setOut(new PrintStream(outputStream));
        try {
            grepApp.run(args, System.in, System.out);
        } catch (GrepException e) {
            fail(e);
        }
        String expectedOutput = "hello world\nhow are you?";
        assertEquals(expectedOutput, outputStream.toString().trim());
    }


    /**
     * Tests the behavior of the {@code run} method when a file specified does not have read permission.
     * Expect a {@code GrepException} with a specific error message.
     */
    @Test
    void run_StateWithFileWithoutReadPermission_ExpectFileReadPermissionError() {
        File tempFile = null;
        try {
            // Create a temporary file without read permission
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            tempFile.setReadable(false);

            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath()};

            // Redirect System.out to capture the output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            // Run the grepApp
            grepApp.run(args, null, System.out);

            // Reset System.out to the default
            System.setOut(System.out);

            // Verify the content written to System.out
            String expectedOutput = tempFile.getAbsolutePath() + ": " + ERR_READING_FILE;
            assertEquals(expectedOutput, outputStream.toString().trim());

            tempFile.delete();
        } catch (IOException | GrepException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the {@code run} method when a file specified does not have write permission.
     * Expect the output to contain the matching lines.
     */
    @Test
    void run_StateWithFileWithoutWritePermission_ExpectMatchingLines() {
        File tempFile = null;
        try {
            // Create a temporary file without read permission
            tempFile = File.createTempFile("test", ".txt", new File(projectRoot));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("hello world\n");
            }
            tempFile.setWritable(false);

            String pattern = "hello";
            String[] args = {pattern, tempFile.getAbsolutePath()};

            // Redirect System.out to capture the output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            // Run the grepApp
            grepApp.run(args, null, System.out);

            // Reset System.out to the default
            System.setOut(System.out);

            // Verify the content written to System.out
            assertEquals("hello world", outputStream.toString().trim());

            tempFile.delete();
        } catch (IOException | GrepException e) {
            fail(e);
        }
    }


}
