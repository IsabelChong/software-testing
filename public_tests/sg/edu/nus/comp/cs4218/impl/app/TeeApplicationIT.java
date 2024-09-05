package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import sg.edu.nus.comp.cs4218.exception.TeeException;

import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Tests for Tee
 * Positive test cases:
 * - Writing to single file from stdin
 * - Writing to non-existing single file from stdin
 * - Appending to single file from stdin
 * - Writing to multiple files from stdin
 * - Appending to multiple files from stdin
 * - Writing to single file from stdin with no args
 * - (Integration Testing) Writing to file using pipe operator from another command's output
 * - (Integration Testing) Appending to file using pipe operator from another command's output
 * - Appending to no files
 * <p>
 * Negative test cases:
 * - Writing to file without permission
 * - Appending to file without permission
 * - Writing to file with invalid file name (directory)
 * - Appending to file with invalid file name (directory)
 * - Passing invalid option to tee
 */

/**
 * Tests for Tee
 */
public class TeeApplicationIT { //NOPMD

    private TeeApplication teeApp;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static final String FILE1 = "file1.txt";//NOPMD
    private static final String FILE1_CONTENT = "Hello CS4218!";
    private static final String NON_EXISITNG_FILE = "non-existing-file.txt";
    private static final String NO_PERM_TXT = "NoPermissionInput.txt";
    private static final String NO_PERM_O_TXT = "NoPermissionOutput.txt";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");
    private static final Set<PosixFilePermission> NW_PERM = PosixFilePermissions.fromString("r--r--r--");
    private static final String TEST_FOLDER = "TestFolder";


    /**
     * Initialize TeeApplication and OutputStream.
     */
    @BeforeEach
    public void setUp() throws IOException {
        teeApp = new TeeApplication();
        inputStream = new ByteArrayInputStream("Original Input Stream".getBytes());//NOPMD
        outputStream = new ByteArrayOutputStream();
        Files.createFile(Paths.get(FILE1));
        Files.write(Paths.get(FILE1), FILE1_CONTENT.getBytes());
        Files.createFile(Paths.get(NO_PERM_TXT));
        Files.setPosixFilePermissions(Paths.get(NO_PERM_TXT), NR_PERM);
        Files.createFile(Paths.get(NO_PERM_O_TXT));
        Files.setPosixFilePermissions(Paths.get(NO_PERM_O_TXT), NW_PERM);
        Files.createDirectory(Paths.get(TEST_FOLDER));
    }

    /**
     * Clean up the file created for testing.
     */
    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(NON_EXISITNG_FILE));
        Files.deleteIfExists(Paths.get(NO_PERM_TXT));
        Files.deleteIfExists(Paths.get(NO_PERM_O_TXT));
        Files.deleteIfExists(Paths.get(TEST_FOLDER));
    }

    /**
     * Test writing to single file from stdin.
     * Expected: Write to file
     */
    @Test
    public void run_singleFileArg_ShouldWriteToFile() {
        String[] args = {"file1.txt"};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", Files.readString(Paths.get(FILE1)));
        } catch (TeeException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test writing to single file from stdin.
     * Expected: Write to stdout
     */
    @Test
    public void run_singleFileArg_ShouldWriteToStdout() {
        String[] args = {"file1.txt"};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Test writing to non-existing file from stdin.
     * Expected: Create File
     */
    @Test
    public void run_singleNonExistingFileArg_ShouldCreateFile() {
        String[] args = {NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertTrue(Files.exists(Paths.get(NON_EXISITNG_FILE)));
        } catch (TeeException t) {
            fail(t);
        }
    }

    /**
     * Test writing to non-existing file from stdin.
     * Expected: Write to file
     */
    @Test
    public void run_singleNonExistingFileArg_ShouldWriteToFile() {
        String[] args = {NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", Files.readString(Paths.get(NON_EXISITNG_FILE)));
        } catch (TeeException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test writing to non-existing file from stdin.
     * Expected: Write to stdout
     */
    @Test
    public void run_singleNonExistingFileArg_ShouldWriteToStdout() {
        String[] args = {NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Test appending to single file from stdin.
     * Expected: Append to file
     */
    @Test
    public void run_singleFileArgWithAppendOption_ShouldAppendToFile() {
        String[] args = {"-a", "file1.txt"};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals(FILE1_CONTENT + "\nOriginal Input Stream", Files.readString(Paths.get(FILE1)));
        } catch (TeeException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test appending to single file from stdin.
     * Expected: Write to stdout
     */
    @Test
    public void run_singleFileArgWithAppendOption_ShouldWriteToStdout() {
        String[] args = {"-a", "file1.txt"};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Test writing to multiple files from stdin.
     * Expected: Write to files
     */
    @Test
    public void run_multipleFileArgs_ShouldWriteToFiles() {
        String[] args = {"file1.txt", NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", Files.readString(Paths.get(FILE1)));
            assertEquals("Original Input Stream", Files.readString(Paths.get(NON_EXISITNG_FILE)));
        } catch (TeeException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test writing to multiple files from stdin.
     * Expected: Write to stdout
     */
    @Test
    public void run_multipleFileArgs_ShouldWriteToStdout() {
        String[] args = {"file1.txt", NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Test appending to multiple files from stdin.
     * Expected: Append to files
     */
    @Test
    public void run_multipleFileArgsWithAppendOption_ShouldAppendToFiles() {
        String[] args = {"-a", "file1.txt", NON_EXISITNG_FILE};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals(FILE1_CONTENT + "\nOriginal Input Stream", Files.readString(Paths.get(FILE1)));
            assertEquals("Original Input Stream", Files.readString(Paths.get(NON_EXISITNG_FILE)));
        } catch (TeeException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test tee without args or files.
     * Expected: Write to stdout
     */
    @Test
    public void run_noFileArgs_ShouldWriteToStdout() {
        String[] args = {};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Tee with append but no files
     * Expected: Write to stdout
     */
    @Test
    public void run_noFileArgsWithAppendOption_ShouldWriteToStdout() {
        String[] args = {"-a"};
        try {
            teeApp.run(args, inputStream, outputStream);
            assertEquals("Original Input Stream", outputStream.toString());
        } catch (TeeException e) {
            fail(e);
        }
    }

    /**
     * Tee to file with no write permission
     * Expected: Throw TeeException
     */
    @Test
    public void run_noWritePermission_ShouldThrowTeeException() {
        String[] args = {NO_PERM_O_TXT};
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.run(args, inputStream, outputStream));
        assertEquals("tee: " + NO_PERM_O_TXT + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Tee to file with no write permission
     * Expected: Should write to stdout
     */
    @Test
    public void run_noWritePermission_ShouldWriteToStdout() {
        String[] args = {NO_PERM_O_TXT};
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.run(args, inputStream, outputStream));
        assertEquals("Original Input Stream", outputStream.toString());
    }

    /**
     * Tee to file with invalid file name (directory)
     * Expected: Throw TeeException
     */
    @Test
    public void run_invalidFileName_ShouldThrowTeeException() {
        String[] args = {TEST_FOLDER};
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.run(args, inputStream, outputStream));
        assertEquals("tee: " + TEST_FOLDER + ": " + ERR_IS_DIR, exception.getMessage());
    }

    /**
     * Tee to file with invalid file name (directory)
     * Expected: Should write to stdout
     */
    @Test
    public void run_invalidFileName_ShouldWriteToStdout() {
        String[] args = {TEST_FOLDER};
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.run(args, inputStream, outputStream));
        assertEquals("Original Input Stream", outputStream.toString());
    }

    /**
     * Passing invalid option to tee
     * Expected: Throw TeeException
     */
    @Test
    public void run_invalidOption_ShouldThrowTeeException() {
        String[] args = {"-z", FILE1};
        Throwable exception = assertThrows(TeeException.class, () -> teeApp.run(args, inputStream, outputStream));
        assertEquals("tee: " + ILLEGAL_FLAG_MSG + "z", exception.getMessage());
    }

}
