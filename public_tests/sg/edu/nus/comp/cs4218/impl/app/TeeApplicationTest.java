package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

// Methods to test
// public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws TeeException

// Test Cases:
// Non append mode
// - teeFromStdin with null stdin
// - teeFromStdin with null fileName
// - teeFromStdin with null isAppend
// - teeFromStdin with empty fileName
// - teeFromStdin with empty stdin
// - teeFromStdin with empty fileName and stdin
// - teeFromStdin with stdin with one line
// - teeFromStdin with stdin with multiple lines
// - teeFromStdin with stdin with multiple lines and special characters and spaces and escape characters
// - teeFromStdin with non-existing file
// - teeFromStdin with existing file
// - teeFromStdin with multiple existing files
// - teeFromStdin with multiple existing files and one non-existing file (last)
// - teeFromStdin with multiple existing files and one non-existing file (first)
// - teeFromStdin with multiple existing files and one non-existing file (middle)
// two non-existing files
// Append mode
// - teeFromStdin with append mode and stdin with one line
// - teeFromStdin with append mode and stdin with multiple lines
// - teeFromStdin with append mode and stdin with multiple lines and special characters
// - teeFromStdin with append mode non-existing file
// - teeFromStdin with append mode existing file
// - teeFromStdin with append mode multiple existing files
// - teeFromStdin with append mode multiple existing files and one non-existing file (last)
// - teeFromStdin with append mode multiple existing files and one non-existing file (first)
// - teeFromStdin with append mode multiple existing files and one non-existing file (middle)
// two non-existing files

/**
 * Unit tests for TeeApplication
 */
public class TeeApplicationTest {
    // Tee error messages
    private static final String TEE_ERR_PREFIX = "tee: ";
    private static final String ERR_NULL_STREAMS = TEE_ERR_PREFIX + ErrorConstants.ERR_NULL_STREAMS;
    private static final String ERR_NULL_ISAPPEND = TEE_ERR_PREFIX + ErrorConstants.ERR_NULL_ARGS;



    // Files
    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";
    private static final String FILE3 = "file3.txt";
    // File contents
    private static final String FILE1_CONTENT = "file1";
    private static final String FILE2_CONTENT = "file2";
    private static final String FILE3_CONTENT = "file3";
    // Non-existing files
    private static final String NE_FILE1 = "NEfile1.txt";
    private static final String NE_FILE2 = "NEfile2.txt";
    // One line input
    private static final String ONE_LINE_INPUT = "test 1";
    // Multi line input
    private static final String MULTI_LINE_INPUT = ONE_LINE_INPUT + TestStringUtils.STRING_NEWLINE + ONE_LINE_INPUT;
    // Multi line input with special characters
    private static final String ML_SC_INPUT = ONE_LINE_INPUT + " !@#$%^&*()_+" + TestStringUtils.STRING_NEWLINE + ONE_LINE_INPUT;

    private TeeApplication teeApp;
    private InputStream olInputStream, mlInputStream, mlscInputStream;

    /**
     * Delete files before testing, in case previous test run failed to delete them.
     */
    @BeforeAll
    static void setUpAll() throws IOException{
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(NE_FILE1));
        Files.deleteIfExists(Paths.get(NE_FILE2));
    }

    /**
     * Initialize TeeApplication and inputStream.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Create files
        Files.createFile(Paths.get(FILE1));
        Files.createFile(Paths.get(FILE2));
        Files.createFile(Paths.get(FILE3));
        // Write to files
        Files.write(Paths.get(FILE1), FILE1_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE2), FILE2_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE3), FILE3_CONTENT.getBytes(StandardCharsets.UTF_8));

        // inputStreams creation
        olInputStream = new ByteArrayInputStream(ONE_LINE_INPUT.getBytes());
        mlInputStream = new ByteArrayInputStream(MULTI_LINE_INPUT.getBytes());
        mlscInputStream = new ByteArrayInputStream(ML_SC_INPUT.getBytes());

        teeApp = new TeeApplication();
    }

    /**
     * Clean up after testing
     */
    @AfterEach
    void tearDown() throws IOException {
        // Delete files
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(NE_FILE1));
        Files.deleteIfExists(Paths.get(NE_FILE2));
    }

    /**
     * Test teeFromStdin with null stdin.
     * Expected: Throw TeeException with error message.
     */
    @Test
    public void teeFromStdin_NullStdin_ThrowException() {
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.teeFromStdin(false, null, FILE1));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * Test teeFromStdin with null fileName.
     * Expected: Throw TeeException with error message.
     */
    @Test
    public void teeFromStdin_NullFileName_ThrowException() {
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.teeFromStdin(false, olInputStream, (String[]) null));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * Test teeFromStdin with null isAppend.
     * Expected: Throw TeeException with error message.
     */
    @Test
    public void teeFromStdin_NullIsAppend_ThrowException() {
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.teeFromStdin(null, olInputStream, FILE1));
        assertEquals( ERR_NULL_ISAPPEND, exception.getMessage());
    }

    /**
     * Test teeFromStdin with empty fileName.
     * Expected: Runs successfully.
     */
    @Test
    public void teeFromStdin_EmptyFileName_RunSuccessfully() {
        assertDoesNotThrow(() -> teeApp.teeFromStdin(false, olInputStream));
    }

    /**
     * Test teeFromStdin with empty stdin.
     * Expected: Write empty string to file.
     */
    @Test
    public void teeFromStdin_EmptyStdin_WriteEmptyStringToFile() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            teeApp.teeFromStdin(false, new ByteArrayInputStream("".getBytes()), FILE1);
            assertEquals("", Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with empty fileName and stdin.
     * Expected: Runs.
     */
    @Test
    public void teeFromStdin_EmptyFileNameAndStdin_RunSuccessfully() {
        assertDoesNotThrow(() -> teeApp.teeFromStdin(false, new ByteArrayInputStream("".getBytes())));
    }

    /**
     * Test teeFromStdin with stdin with one line.
     * Expected: Write one line to file.
     */
    @Test
    public void teeFromStdin_OneLineStdin_WriteOneLineToFile() {
        try {
            teeApp.teeFromStdin(false, olInputStream, FILE1);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with stdin with one line.
     * Expected: Returns one line.
     */
    @Test
    public void teeFromStdin_OneLineStdin_ReturnsOneLine() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            String results = teeApp.teeFromStdin(false, olInputStream, FILE1);
            assertEquals(ONE_LINE_INPUT, results);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with stdin with multiple lines.
     * Expected: Write multiple lines to file.
     */
    @Test
    public void teeFromStdin_MultiLineStdin_WriteMultiLineToFile() {
        try {
            teeApp.teeFromStdin(false, mlInputStream, FILE1);
            assertEquals(MULTI_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with stdin with multiple lines and special characters and spaces and escape characters.
     * Expected: Write multiple lines with special characters, spaces and escape characters to file.
     */
    @Test
    public void teeFromStdin_MultiLineScStdin_WriteMultiLineScToFile() {
        try {
            teeApp.teeFromStdin(false, mlscInputStream, FILE1);
            assertEquals(ML_SC_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with non-existing file.
     * Expected: Create file and write to it.
     */
    @Test
    public void teeFromStdin_NonExistingFile_CreateFileAndWriteToFile() {
        try {
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(false, olInputStream, NE_FILE1);
            assertTrue(Files.exists(Paths.get(NE_FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with existing file.
     * Expected: Write to file.
     */
    @Test
    public void teeFromStdin_ExistingFile_WriteToFile() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            teeApp.teeFromStdin(false, olInputStream, FILE1);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with multiple existing files.
     * Expected: Write to all files.
     */
    @Test
    public void teeFromStdin_MultipleExistingFiles_WriteToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            teeApp.teeFromStdin(false, olInputStream, FILE1, FILE2, FILE3);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with multiple existing files and one non-existing file (last).
     * Expected: Write to all existing files and create non-existing file and write to it.
     */
    @Test
    public void teeFromStdin_MultipleExistingFilesAndOneNonExistingFileLast_WriteToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(false, olInputStream, FILE1, FILE2, FILE3, NE_FILE1);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with multiple existing files and one non-existing file (first).
     * Expected: Create non-existing file and write to it and write to all existing files.
     */
    @Test
    public void teeFromStdin_MultipleExistingFilesAndOneNonExistingFileFirst_WriteToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(false, olInputStream, NE_FILE1, FILE1, FILE2, FILE3);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with multiple existing files and one non-existing file (middle).
     * Expected: Write to all existing files and create non-existing file and write to it.
     */
    @Test
    public void teeFromStdin_MultipleExistingFilesAndOneNonExistingFileMiddle_WriteToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(false, olInputStream, FILE1, NE_FILE1, FILE2, FILE3);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with two non-existing files.
     * Expected: Create both files and write to them.
     */
    @Test
    public void teeFromStdin_TwoNonExistingFiles_CreateFilesAndWriteToThem() {
        try {
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            assertFalse(Files.exists(Paths.get(NE_FILE2)));
            teeApp.teeFromStdin(false, olInputStream, NE_FILE1, NE_FILE2);
            assertTrue(Files.exists(Paths.get(NE_FILE1)));
            assertTrue(Files.exists(Paths.get(NE_FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE2)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode and stdin with one line.
     * Expected: Append one line to file.
     */
    @Test
    public void teeFromStdin_AppendOneLineStdin_AppendOneLineToFile() {
        try {
            teeApp.teeFromStdin(true, olInputStream, FILE1);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode and stdin with multiple lines.
     * Expected: Append multiple lines to file.
     */
    @Test
    public void teeFromStdin_AppendMultiLineStdin_AppendMultiLineToFile() {
        try {
            teeApp.teeFromStdin(true, mlInputStream, FILE1);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + MULTI_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode and stdin with multiple lines and special characters.
     * Expected: Append multiple lines with special characters to file.
     */
    @Test
    public void teeFromStdin_AppendMultiLineScStdin_AppendMultiLineScToFile() {
        try {
            teeApp.teeFromStdin(true, mlscInputStream, FILE1);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ML_SC_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode and stdin with multiple lines and special characters.
     * Expected: Returns lines with special characters.
     */
    @Test
    public void teeFromStdin_AppendMultiLineScStdin_ReturnsLinesWithSpecialChars() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            String results = teeApp.teeFromStdin(true, mlscInputStream, FILE1);
            assertEquals(ML_SC_INPUT, results);
        } catch (Exception e) {
            fail(e);
        }
    }



    /**
     * Test teeFromStdin with append mode non-existing file.
     * Expected: Create file and append to it.
     */
    @Test
    public void teeFromStdin_AppendNonExistingFile_CreateFileAndAppendToFile() {
        try {
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(true, olInputStream, NE_FILE1);
            assertTrue(Files.exists(Paths.get(NE_FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode existing file.
     * Expected: Append to file.
     */
    @Test
    public void teeFromStdin_AppendExistingFile_AppendToFile() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            teeApp.teeFromStdin(true, olInputStream, FILE1);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode multiple existing files.
     * Expected: Append to all files.
     */
    @Test
    public void teeFromStdin_AppendMultipleExistingFiles_AppendToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            teeApp.teeFromStdin(true, olInputStream, FILE1, FILE2, FILE3);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode multiple existing files and one non-existing file (last).
     * Expected: Append to all existing files and create non-existing file and append to it.
     */
    @Test
    public void teeFromStdin_AppendMultipleExistingFilesAndOneNonExistingFileLast_AppendToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(true, olInputStream, FILE1, FILE2, FILE3, NE_FILE1);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode multiple existing files and one non-existing file (first).
     * Expected: Create non-existing file and append to it and append to all existing files.
     */
    @Test
    public void teeFromStdin_AppendMultipleExistingFilesAndOneNonExistingFileFirst_AppendToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(true, olInputStream, NE_FILE1, FILE1, FILE2, FILE3);
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with append mode multiple existing files and one non-existing file (middle).
     * Expected: Append to all existing files and create non-existing file and append to it.
     */
    @Test
    public void teeFromStdin_AppendMultipleExistingFilesAndOneNonExistingFileMiddle_AppendToAllFiles() {
        try {
            assertEquals(FILE1_CONTENT, Files.readString(Paths.get(FILE1)));
            assertEquals(FILE2_CONTENT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT, Files.readString(Paths.get(FILE3)));
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            teeApp.teeFromStdin(true, olInputStream, FILE1, NE_FILE1, FILE2, FILE3);
            assertEquals(FILE1_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
            assertEquals(FILE2_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE2)));
            assertEquals(FILE3_CONTENT + STRING_NEWLINE + ONE_LINE_INPUT, Files.readString(Paths.get(FILE3)));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test teeFromStdin with two non-existing files.
     * Expected: Create both files and append to them.
     */
    @Test
    public void teeFromStdin_AppendTwoNonExistingFiles_CreateFilesAndAppendToThem() {
        try {
            assertFalse(Files.exists(Paths.get(NE_FILE1)));
            assertFalse(Files.exists(Paths.get(NE_FILE2)));
            teeApp.teeFromStdin(true, olInputStream, NE_FILE1, NE_FILE2);
            assertTrue(Files.exists(Paths.get(NE_FILE1)));
            assertTrue(Files.exists(Paths.get(NE_FILE2)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE1)));
            assertEquals(ONE_LINE_INPUT, Files.readString(Paths.get(NE_FILE2)));
        } catch (Exception e) {
            fail(e);
        }
    }
}

