package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.RmException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

/**
 * Test cases for the RmApplication class.
 */
public class RmApplicationIT {//NOPMD
    private RmApplication rmApp;
    private ByteArrayOutputStream outputStream;

    /**
     * Setup method to initialize objects before each test case.
     */
    @BeforeEach
    public void setUp() {
        rmApp = new RmApplication();
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * Tests the behavior of the run method when null arguments are provided.
     * Expects a RmException to be thrown.
     */
    @Test
    public void run_NullArgs_ThrowRmException() {
        Throwable exception = assertThrows(RmException.class, () -> rmApp.run(new String[]{}, null, outputStream));
        assertEquals("rm: " + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Tests the behavior of the run method when a non-existent file is provided.
     * Expects a RmException to be thrown.
     */
    @Test
    public void run_NonExistentFile_ThrowRmException() {
        Throwable exception = assertThrows(RmException.class, () -> rmApp.run(new String[]{"nonexistent.txt"}, null, outputStream));
        assertTrue(exception.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

    /**
     * Tests the behavior of the run method when removing a single file.
     * Expects the file to be removed successfully.
     */
    @Test
    public void run_SingleFile_FileRemoved() {
        // Create a temporary file
        Path file = null;
        try {
            file = Files.createTempFile("test", ".txt");//NOPMD
        } catch (IOException e) {
            fail(e);
        }
        assertTrue(Files.exists(file));

        // Run the application
        try {
            rmApp.run(new String[]{file.toString()}, null, outputStream);
        } catch (RmException e) {
            fail(e);
        }

        // Check if the file is deleted
        assertFalse(Files.exists(file));
    }

    /**
     * Tests the behavior of the run method when removing an empty folder.
     * Expects the folder to be removed successfully.
     */
    @Test
    public void run_EmptyFolder_FolderRemoved() {
        // Create a temporary directory
        Path directory = null;
        try {
            directory = Files.createTempDirectory("test");
        } catch (IOException e) {
            fail(e);
        }

        // Run the application
        try {
            rmApp.run(new String[]{"-d", directory.toString()}, null, outputStream);
        } catch (RmException e) {
            fail(e);
        }

        // Check if the directory is deleted
        assertFalse(Files.exists(directory));
    }

    /**
     * Tests the behavior of the run method when removing a non-empty folder.
     * Expects an AbstractApplicationException to be thrown.
     */
    @Test
    public void run_NonEmptyFolder_ThrowRmException() {
        try {
            // Create a temporary directory and a file inside it
            Path directory = Files.createTempDirectory("test");
            Path tempFile = Files.createTempFile(directory, "file", ".txt");

            // Run the application
            Path finalDirectory = directory;
            Throwable exception = assertThrows(RmException.class, () ->
                    rmApp.run(new String[]{"-d", finalDirectory.toString()}, null, outputStream));

            // Check if the expected exception is thrown
            assertTrue(exception.getMessage().contains("Directory is not empty"));

            // Check if the directory and file still exist
            assertTrue(Files.exists(directory));
            assertTrue(Files.exists(tempFile));
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the run method when removing a directory recursively.
     * Expects the directory and its contents to be removed successfully.
     */
    @Test
    public void run_Recursive_DeletesDirectoryRecursively() {
        // Create a temporary directory with nested subdirectories and files
        try {
            Path directory = Files.createTempDirectory("test");
            Path subdirectory = Files.createDirectory(directory.resolve("subdir"));
            Files.createTempFile(directory, "file1", ".txt");
            Files.createTempFile(subdirectory, "file2", ".txt");

            // Run the application
            rmApp.run(new String[]{"-r", directory.toString()}, null, outputStream);

            // Check if the directory and all its contents are deleted
            assertFalse(Files.exists(directory));
            assertFalse(Files.exists(subdirectory));
            assertFalse(Files.exists(directory.resolve("file1.txt")));
            assertFalse(Files.exists(subdirectory.resolve("file2.txt")));
        } catch (IOException e) {
            fail(e);
        } catch (RmException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the run method when removing multiple files.
     * Expects all files to be removed successfully.
     */
    @Test
    public void run_MultipleFiles_FilesRemoved() {
        // Create temporary files
        Path file1 = null;
        try {
            file1 = Files.createTempFile("test1", ".txt");
            Path file2 = Files.createTempFile("test2", ".txt");

            assertTrue(Files.exists(file1));
            assertTrue(Files.exists(file2));

            // Run the application
            rmApp.run(new String[]{file1.toString(), file2.toString()}, null, outputStream);

            // Check if the files are deleted
            assertFalse(Files.exists(file1));
            assertFalse(Files.exists(file2));
        } catch (IOException e) {
            fail(e);
        } catch (RmException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the run method when removing a directory with hidden files.
     * Expects the directory and its hidden files to be removed successfully.
     */
    @Test
    public void run_DirectoryWithHiddenFiles_AllFilesRemoved() {
        // Create a temporary directory
        Path directory = null;
        try {
            directory = Files.createTempDirectory("test");

            // Create hidden files inside the directory
            Path hiddenFile1 = Files.createFile(directory.resolve(".hidden1"));
            Path hiddenFile2 = Files.createFile(directory.resolve(".hidden2"));

            assertTrue(Files.exists(hiddenFile1));
            assertTrue(Files.exists(hiddenFile2));

            // Run the application
            rmApp.run(new String[]{"-r", directory.toString()}, null, outputStream);

            // Check if the directory and its hidden files are deleted
            assertFalse(Files.exists(directory));
            assertFalse(Files.exists(hiddenFile1));
            assertFalse(Files.exists(hiddenFile2));
        } catch (IOException e) {
            fail(e);
        } catch (RmException e) {
            fail(e);
        }
    }

    /**
     * Tests the behavior of the run method when removing non-existent files.
     * Expects a RmException to be thrown.
     */
    @Test
    public void run_NonExistentFiles_NoExceptionThrown() {
        // Attempt to remove non-existent files
        Throwable exception = assertThrows(RmException.class, () ->
                rmApp.run(new String[]{"nonexistent1.txt", "nonexistent2.txt"}, null, outputStream));

        // Assert that the expected exception is thrown
        assertEquals("rm: nonexistent1.txt: " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }
}
