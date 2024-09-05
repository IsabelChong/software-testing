package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the RmApplication class.
 */
public class RmApplicationTest {
    private static final String TEMP = "temp-rm";
    private static Deque<Path> files = new ArrayDeque<>();
    private static Path path;

    private RmApplication rmApplication;

    /**
     * Create temporary directory before running any tests.
     *
     * @throws IOException if an I/O error occurs
     * @throws NoSuchFieldException if a specified field does not exist
     * @throws IllegalAccessException if access to a class is denied
     */
    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        path = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectory(path);
    }

    /**
     * Set up the test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        rmApplication = new RmApplication();
    }

    /**
     * Delete temporary directory after all tests have been run.
     *
     * @throws IOException if an I/O error occurs
     */
    @AfterAll
    static void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(path);
    }

    /**
     * Create a file in the temporary directory.
     *
     * @param name the name of the file to create
     * @return the path to the created file
     * @throws IOException if an I/O error occurs
     */
    private Path createFile(String name) throws IOException {
        return createFile(name, path);
    }

    /**
     * Create a directory in the temporary directory.
     *
     * @param folder the name of the directory to create
     * @return the path to the created directory
     * @throws IOException if an I/O error occurs
     */
    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, path);
    }

    /**
     * Create a file in the specified directory.
     *
     * @param name the name of the file to create
     * @param inPath the path to the directory in which to create the file
     * @return the path to the created file
     * @throws IOException if an I/O error occurs
     */
    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    /**
     * Create a directory in the specified directory.
     *
     * @param folder the name of the directory to create
     * @param inPath the path to the directory in which to create the directory
     * @return the path to the created directory
     * @throws IOException if an I/O error occurs
     */
    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
        return path;
    }

    /**
     * Convert file names to arguments with the specified flag.
     *
     * @param flag the flag to use in the arguments
     * @param files the file names to include in the arguments
     * @return an array of arguments
     */
    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    /**
     * Tests the behavior of deleting a symbolic link to a file.
     * It asserts that the symbolic link is deleted but not the target file.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void run_SymbolicLinkToFile_DeletesSymbolicLink() throws Exception {
        // Create a file and a symbolic link pointing to the file
        Path file = createFile("file1");
        Path symlink = path.resolve("symlinkToFile");
        Files.createSymbolicLink(symlink, file);

        // Delete the symbolic link using rm
        rmApplication.run(toArgs("", "symlinkToFile"), System.in, System.out);

        // Verify that the symbolic link is deleted, but not the target file
        assertTrue(Files.notExists(symlink));
        assertTrue(Files.exists(file));
    }

    /**
     * Tests the behavior of deleting files with special characters in their names.
     * It asserts that the files are deleted successfully.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void run_FilesWithSpecialCharacters_DeletesFiles() throws Exception {
        // Create files with names containing special characters
        Path file1
                = createFile("file with spaces");
        Path file2 = createFile("file\nwith\nnewlines");
        Path file3 = createFile("file\twith\ttabs");

        // Delete the files using rm
        rmApplication.run(toArgs("", "file with spaces", "file\nwith\nnewlines", "file\twith\ttabs"), System.in, System.out);

        // Verify that the files are deleted
        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file2));
        assertTrue(Files.notExists(file3));
    }

    /**
     * Tests the behavior of deleting a directory with special characters in its name.
     * It asserts that the directory is deleted successfully.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    void run_DirectoryWithSpecialCharacters_DeletesDirectory() throws Exception {
        // Create a directory with a name containing special characters
        Path directory = createDirectory("dir with special chars");

        // Delete the directory using rm
        rmApplication.run(toArgs("d", "dir with special chars"), System.in, System.out);

        // Verify that the directory is deleted
        assertTrue(Files.notExists(directory));
    }

    /**
     * Tests the behavior when no arguments are provided.
     * It asserts that an exception is thrown.
     */
    @Test
    void run_EmptyArguments_ThrowsException() {
        // Try to delete with no arguments
        assertThrows(RmException.class, () -> rmApplication.run(new String[]{}, System.in, System.out));
    }
}
