package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the LsApplication class.
 */
public class LsApplicationTest {

    public static final String SRC = "src";
    private static final String TEMP = "temp-ls";
    private static final String FIRST_FOLDER = "firstLevelFolder";
    private static final String FIRST_FILE = "firstLevelFile";
    private static final String FIRST_HID_FILE = ".firstLevelHidden";
    private static final String SECOND_FOLDER = "secondLevelFolder";
    private static Path path;
    private static File[] firstTestFiles;
    private static File[] allTestFiles;

    private LsApplication application;

    /**
     * Set up the test environment before running the test cases.
     *
     * @throws IOException              if an I/O error occurs
     * @throws NoSuchFieldException     if a specified field does not exist
     * @throws IllegalAccessException   if access to a class is denied
     */
    @BeforeAll
    static void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        path = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectories(Paths.get(path.toString(), FIRST_FOLDER, SECOND_FOLDER));
        Files.createFile(Paths.get(path.toString(), FIRST_FILE));
        Files.createFile(Paths.get(path.toString(), FIRST_HID_FILE));
        firstTestFiles = path.toFile().listFiles();
        File[] secondTestFiles = path.resolve(Paths.get(FIRST_FOLDER)).toFile().listFiles();
        allTestFiles = Stream.concat(Arrays.stream(firstTestFiles), Arrays.stream(secondTestFiles))
                .toArray(size -> (File[]) Array.newInstance(firstTestFiles.getClass().getComponentType(), size));
    }

    /**
     * Clean up the test environment after running all test cases.
     *
     * @throws IOException if an I/O error occurs
     */
    @AfterAll
    static void tearDown() throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Set up the temporary test environment before each test case.
     */
    @BeforeEach
    void createTemp() {
        application = new LsApplication();
    }

    /**
     * Asserts that the result contains the specified files.
     *
     * @param result the result to check against
     * @param files  the files to check for in the result
     */
    private void assertResultContainsFiles(String result, File... files) {
        for (File file : files) {
            String fileName = file.getName();
            if (file.isHidden()) {
                assertFalse(result.contains(fileName));
            } else {
                assertTrue(result.contains(fileName));
            }
        }
    }

    /**
     * Tests the behavior of listing folder content when no folders are specified and hidden files are excluded.
     * It asserts that the result does not contain hidden files.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_NoFoldersSpecifiedExcludesHiddenFiles_ReturnsCurrentFolderContentWithoutHiddenFiles() throws Exception {
        String result = application.listFolderContent(false, false);
        assertFalse(result.contains(FIRST_HID_FILE));
    }

    /**
     * Tests the behavior of listing folder content when a nonexistent folder is specified.
     * It asserts that an error message is returned.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_NonexistentFolder_ReturnsErrorMessage() throws Exception {
        String nonExistentFolder = "nonexistent";
        String result = application.listFolderContent(false, false, nonExistentFolder);
        assertTrue(result.contains("No such file or directory"));
    }

    /**
     * Tests the behavior of listing folder content when symlinks are present in the folder.
     * It asserts that the result contains the resolved contents.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_SymlinksInFolder_ReturnsResolvedContents() throws Exception {
        Path symlinkTarget = path.resolve(FIRST_FOLDER);
        Path symlink = path.resolve("symlink");
        Files.createSymbolicLink(symlink, symlinkTarget);

        String result = application.listFolderContent(false, false, TEMP);
        assertTrue(result.contains(FIRST_FOLDER));
    }

    /**
     * Tests the behavior of listing folder content when an empty folder is specified.
     * It asserts that the result contains the empty folder.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_EmptyFolder_ReturnsEmptyContent() throws Exception {
        Path emptyFolder = path.resolve("empty");
        Files.createDirectory(emptyFolder);

        String result = application.listFolderContent(false, false, TEMP);
        assertTrue(result.contains("empty"));
    }

    /**
     * Tests the behavior of listing folder content when the folder contains a large number of files.
     * It asserts that all files are included in the result.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_LargeDirectory_ReturnsAllContents() throws Exception {
        for (int i = 0; i < 1000; i++) {
            Files.createFile(path.resolve("file" + i + ".txt"));
        }

        String result = application.listFolderContent(false, false, TEMP);
        assertTrue(result.contains("file999.txt")); // Check for last file
    }

    /**
     * Tests the behavior of listing folder content when the folder contains special characters in its name.
     * It asserts that the result contains the contents correctly.
     *
     * @throws Exception if an error occurs during execution
     */
    @Test
    public void listFolderContent_FolderWithSpecialCharacters_ReturnsContentsCorrectly() throws Exception {
        Path specialFolder = path.resolve("special folder");
        Files.createDirectory(specialFolder);
        Files.createFile(specialFolder.resolve("file with spaces.txt"));

        String result = application.listFolderContent(false, false, specialFolder.toString());
        assertTrue(result.contains("file with spaces.txt"));
    }
}
