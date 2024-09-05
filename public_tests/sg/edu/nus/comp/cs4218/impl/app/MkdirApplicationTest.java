package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MkdirException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempDir;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.setPermissionsAndDelete;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class MkdirApplicationTest {
    private MkdirApplication mkdirApp;
    private File testDir;
    private final Path initDir = Paths.get(System.getProperty("user.dir"));
    private static final String MKDIR_PREFIX = "mkdir: ";
    private static final String TEST_TARGET_DIR = "testTargetDir";

    @BeforeEach
    public void setUp() {
        mkdirApp = new MkdirApplication();
        testDir = new File(initDir + File.separator + "testDir");
        testDir.mkdir();
        Environment.currentDirectory = testDir.toString();
    }

    @AfterEach
    public void tearDown() throws IOException {
        setPermissionsAndDelete(testDir);
        testDir.delete();
        Environment.currentDirectory = System.getProperty("user.dir");
    }

    /**
     * Test createFolder with one non-nested relative path.
     * Expected: Should create target directory.
     * Command: `mkdir testTargetDir`
     */
    @Test
    public void createFolder_oneNonNestedRelativePath_shouldCreateDirectory() {
        try {
            String path = TEST_TARGET_DIR;
            mkdirApp.createFolder(path);

            String[] testDirContents = testDir.list();
            assertTrue(testDirContents.length == 1);
            assertEquals(TEST_TARGET_DIR, testDirContents[0]);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test createFolder with one nested relative path, where parent directory exists.
     * Expected: Should create target directory.
     * Command: `mkdir testParentDir/testTargetDir`
     */
    @Test
    public void createFolder_oneNestedRelativePath_shouldCreateDirectory() {
        try {
            File parentDir = createTempDir(testDir + File.separator + "testParentDir");
            String path = "testParentDir/" + TEST_TARGET_DIR;
            mkdirApp.createFolder(path);

            String[] testDirContents = parentDir.list();
            assertTrue(testDirContents.length == 1);
            assertEquals(TEST_TARGET_DIR, testDirContents[0]);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test createFolder with two non-nested relative paths.
     * Expected: Should create target directories.
     * Command: `mkdir testTargetDir1 testTargetDir2`
     */
    @Test
    public void createFolder_twoNonNestedRelativePath_shouldCreateDirectory() {
        try {
            String path1 = "testTargetDir1";
            String path2 = "testTargetDir2";
            mkdirApp.createFolder(path1, path2);

            Set<String> testDirContents = Set.of(testDir.list());
            assertEquals(Set.of(path1, path2), testDirContents);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test createFolder with null arguments.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void createFolder_nullArgument_shouldThrowMkdirException() {
        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.createFolder(null));

        assertEquals(MKDIR_PREFIX + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Test createFolder with one nested relative path where parent directory doesn't exist.
     * Expected: Should not create folder.
     */
    @Test
    public void createFolder_oneNestedRelativePathParentDontExist_shouldNotCreateFolder() {
        try {
            String path = "testParentDir/" + TEST_TARGET_DIR;
            mkdirApp.createFolder(path);
            assertEquals(0, testDir.list().length);
        } catch (MkdirException e) {
            fail(e);
        }

    }

    /**
     * Test createFolder with one absolute path.
     * Expected: Should create target directory.
     */
    @Test
    public void createFolder_oneAbsolutePath_shouldCreateDirectory() {
        try {
            String path = testDir + File.separator + TEST_TARGET_DIR;
            mkdirApp.createFolder(path);

            String[] testDirContents = testDir.list();
            assertTrue(testDirContents.length == 1);
            assertEquals(TEST_TARGET_DIR, testDirContents[0]);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test createFolder with absolute path where one top level folder is missing.
     * Expected: Should not create folder.
     */
    @Test
    public void createFolder_oneAbsolutePathTopLevelMissing_shouldNotCreateFolder() {
        try {
            String path = testDir + File.separator + "hello" + File.separator + TEST_TARGET_DIR;
            mkdirApp.createFolder(path);

            assertEquals(0, testDir.list().length);
        } catch (MkdirException e) {
            fail(e);
        }

    }

//    @Test
//    public void createFolder_multiplePathFirstDirMissing_shouldThrowMkdirExceptionAndCreateDir() {
//        String path = TEST_TARGET_DIR;
//        Throwable exception = assertThrows(MkdirException.class,
//                () -> mkdirApp.createFolder(path));
//
//        assertEquals(MKDIR_PREFIX + ERR_TOP_LEVEL_MISSING, exception.getMessage());
//    }

    /**
     * Test getNormalisedAbsolutePath with relative path.
     * Expected: Should return normalised absolute path.
     */
    @Test
    public void getNormalizedAbsolutePath_relativePath_shouldReturnNormalizedAbsolutePath() {
        try {
            String processedPath = mkdirApp.getNormalizedAbsolutePath(TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test getNormalisedAbsolutePath with absolute path.
     * Expected: Should return normalised absolute path.
     */
    @Test
    public void getNormalizedAbsolutePath_absolutePath_shouldReturnNormalizedAbsolutePath() {
        try {
            String processedPath = mkdirApp.getNormalizedAbsolutePath(testDir.getPath() + File.separator + TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test getNormalisedAbsolutePath with non-normalised path.
     * Expected: Should return normalised absolute path.
     */
    @Test
    public void getNormalizedAbsolutePath_nonNormalizedPath_shouldReturnNormalizedAbsolutePath() {
        try {
            String processedPath = mkdirApp.getNormalizedAbsolutePath(
                    testDir.getPath() + File.separator + "././" + TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test getNormalisedAbsolutePath with empty string.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void getNormalizedAbsolutePath_emptyString_shouldThrowMkdirException() {
        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.createFolder(""));

        assertEquals(MKDIR_PREFIX + ERR_NO_ARGS, exception.getMessage());
    }

    /**
     * Test getNormalisedAbsolutePath with file separator.
     * Expected: Should return file separator.
     */
    @Test
    public void getNormalizedAbsolutePath_fileSeparator_shouldReturnFileSeparator() {
        try {
            String processedPath = mkdirApp.getNormalizedAbsolutePath("/");
            String expectedPath = "/";
            assertEquals(expectedPath, processedPath);
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void checkPermissions_relativePath_shouldThrowMkdirException() {
        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.checkPermissions("test", false, "test"));

        assertEquals(MKDIR_PREFIX + ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test checkPermissions with relative path where parent directory has no write permissions.
     * Expected: Should return false.
     */
    @Test
    public void checkPermissions_parentNoWritePerms_shouldThrowMkdirException() {
        try {
            String pathName = testDir + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString("r-x------"));

            Boolean permissionsPass = mkdirApp.checkPermissions(pathName, false, pathName);

            assertEquals(false, permissionsPass);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path where parent directory has no execute permissions.
     * Expected: Should return false.
     */
    @Test
    public void checkPermissions_parentNoExecPerms_shouldThrowMkdirException() {
        try {
            String pathName = testDir + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString("rw-------"));

            Boolean permissionsPass = mkdirApp.checkPermissions(pathName, false, pathName);

            assertEquals(false, permissionsPass);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path where one top level directory doesn't exist.
     * Expected: Should return false.
     */
    @Test
    public void checkPermissions_oneTopLevelDontExist_shouldThrowMkdirException() {
        try {
            String pathName = testDir + File.separator + "test" + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Boolean permissionsPass = mkdirApp.checkPermissions(pathName, false, pathName);

            assertEquals(false, permissionsPass);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path where parent directory has no write permissions, with p flag.
     * Expected: Should return false.
     */
    @Test
    public void checkPermissions_parentNoWritePermsWithPFlag_shouldThrowMkdirException() {
        try {
            String pathName = testDir + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString("r-x------"));

            Boolean permissionsPass = mkdirApp.checkPermissions(pathName, true, pathName);

            assertEquals(false, permissionsPass);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path where parent directory has no execute permissions, with p flag.
     * Expected: Should return false.
     */
    @Test
    public void checkPermissions_parentNoExecPermsWithPFlag_shouldThrowMkdirException() {
        try {
            String pathName = testDir + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString("rw-------"));
            Boolean permissionsPass = mkdirApp.checkPermissions(pathName, true, pathName);

            assertEquals(false, permissionsPass);
        } catch (MkdirException | IOException e) {
            fail(e);
        }
    }
}
