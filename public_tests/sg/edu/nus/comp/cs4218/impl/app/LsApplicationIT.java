package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Test class for the LsApplication.
 * Tests various functionalities of the ls command.
 */
public class LsApplicationIT {//NOPMD
    // CONSTANTS
    private final static String FILE1 = "file1";
    private final static String FILE2 = "file2";
    private final static String TXT = ".txt";
    private LsApplication lsApp;
    private ByteArrayOutputStream outputStream;

    /**
     * Setup method to initialize objects before each test case.
     */
    @BeforeEach
    public void setUp() {
        lsApp = new LsApplication();
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * Test method to check behavior when null arguments are provided.
     * Expects an LsException to be thrown.
     */
    @Test
    public void run_NullArgs_ThrowException() {
        Throwable exception = assertThrows(LsException.class, () -> lsApp.run(null, null, outputStream));
        assertEquals("ls: " + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Test method to check behavior when null stdout is provided.
     * Expects an LsException to be thrown.
     */
    @Test
    public void run_NullStdOut_ThrowException() {
        Throwable exception = assertThrows(LsException.class, () -> lsApp.run(new String[]{}, null, null));
        assertEquals("ls: " + ERR_NO_OSTREAM, exception.getMessage());
    }

    /**
     * Test method to check behavior when listing a non-existent directory.
     * Expects an error message to be printed indicating the non-existent directory.
     */
    @Test
    public void run_NonExistentDirectory_PrintErrorMessage() {
        String expected = "ls: cannot access 'nonexistent': No such file or directory\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> lsApp.run(new String[]{"nonexistent"}, null, outputStream));

        String output = outputStream.toString();
        assertTrue(output.contains(expected)); // Check if the error message is printed
    }
    /**
     * Test method to check behavior when listing the contents of the current directory.
     * Expects the output stream to contain the list of files and directories.
     */
    @Test
    public void run_CurrentDirectory_ListContents() {
        try {
            lsApp.run(new String[]{}, null, outputStream);
        } catch (LsException e) {
            fail(e);
        }
        String output = outputStream.toString();
        File currentDir = new File(".");
        File[] filesAndDirs = currentDir.listFiles(file -> !file.isHidden());
        List<String> fileAndDirNames = Arrays.stream(filesAndDirs)
                .map(File::getName)
                .collect(Collectors.toList());
        for (String fileOrDir : fileAndDirNames) {
            assertTrue(output.contains(fileOrDir));
        }
    }

    /**
     * Test method to check behavior when listing the contents of a specific directory.
     * Expects the output stream to contain the list of files and directories in the specified directory.
     */
    @Test
    void run_SpecificDirectory_ListContents() {
        Path directory = null;
        try {
            directory = Files.createTempDirectory("test");
            Path file1 = Files.createTempFile(directory, FILE1, TXT);

            Path file2 = Files.createTempFile(directory, FILE2, TXT);
            Path subdir = Files.createDirectory(directory.resolve("subdir"));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            lsApp.run(new String[]{directory.toString()}, null, outputStream);
            String output = outputStream.toString();
            assertTrue(output.contains(file1.getFileName().toString()));
            assertTrue(output.contains(file2.getFileName().toString()));
            assertTrue(output.contains(subdir.getFileName().toString()));
        } catch (IOException e) {
            fail(e);
        } catch (LsException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Files.walk(directory)
                        .sorted((p1, p2) -> -p1.toString().length() + p2.toString().length())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                fail(e);
            }
        }
    }

    /**
     * Test method to check behavior when an illegal option is provided.
     * Expects an LsException to be thrown with a specific error message.
     */
    @Test
    public void run_IllegalOption_ThrowException() {
        Throwable exception = assertThrows(LsException.class, () -> lsApp.run(new String[]{"-p"}, null, outputStream));
        assertEquals("ls: illegal option -- p", exception.getMessage());
    }

    /**
     * Test method to check behavior when listing contents with sort by extension option.
     * Expects the output stream to contain the list of files sorted by extension.
     */
    @Test
    public void run_SortByExtensionOption_ListContentsSortedByExtension() {
        Path directory = null;
        try {
            directory = Files.createTempDirectory("test");
            Path file1 = Files.createTempFile(directory, "file1", ".txt");
            Path file2 = Files.createTempFile(directory, "file2", ".docx");
            Path file3 = Files.createTempFile(directory, "file3", ".pdf");
            lsApp.run(new String[]{"-X", directory.toString()}, null, outputStream);
            assertTrue(outputStream.toString().contains(file2.getFileName().toString()));
            assertTrue(outputStream.toString().contains(file3.getFileName().toString()));
            assertTrue(outputStream.toString().contains(file1.getFileName().toString()));
        } catch (IOException e) {
            fail(e);
        } catch (LsException e) {
            fail(e);
        } finally {
            if (directory != null) {
                try {
                    Files.walk(directory)
                            .sorted((p1, p2) -> -p1.toString().length() + p2.toString().toString().length())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    fail(e);
                }
            }
        }
    }

    /**
     * Test method to check behavior when listing all files excluding hidden files.
     * Expects the output stream to contain the list of all files excluding hidden files.
     */
    @Test
    public void run_AllFilesExcludingHiddenFiles_ShouldDisplayEveryFileExceptHidden() {
        Path directory = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            directory = Files.createTempDirectory("test");
            Path file1 = Files.createTempFile(directory, "file1", ".txt");
            Path file2 = Files.createTempFile(directory, "file2", ".docx");
            Path hiddenFile = Files.createFile(directory.resolve(".hiddenFile"));
            lsApp.run(new String[]{directory.toString()}, null, outputStream);
            assertTrue(outputStream.toString().contains(file1.getFileName().toString()));
            assertTrue(outputStream.toString().contains(file2.getFileName().toString()));
            assertFalse(outputStream.toString().contains(hiddenFile.getFileName().toString()));
        } catch (IOException e) {
            fail(e);
        } catch (LsException e) {
            fail(e);
        } finally {
            if (directory != null) {
                try {
                    Files.walk(directory)
                            .sorted((p1, p2) -> -p1.toString().length() + p2.toString().length())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                fail(e);
            }
        }
    }
}