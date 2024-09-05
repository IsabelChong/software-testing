package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempDir;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempFile;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CdApplicationTest {
    private CdApplication cdApp;
    private File testDir;
    private final Path initDir = Paths.get(System.getProperty("user.dir"));
    private static final String CD_PREFIX = "cd: ";
    private static final String TEST_TARGET_DIR = "testTargetDir";
    private static final String ERROR_SEMICOLON = ": ";

    @BeforeEach
    public void setUp() {
        cdApp = new CdApplication();
        testDir = new File(initDir + File.separator + "testDir");
        testDir.mkdir();
        Environment.currentDirectory = testDir.toString();
    }

    @AfterEach
    public void tearDown() throws IOException {
        setExecutePermissionsAndDelete(testDir);
        Files.delete(testDir.toPath());
        Environment.currentDirectory = initDir.toString();
    }

    /**
     * Sets owner read, write and execute permissions to all directories and files within a specified directory,
     * and deleting it afterward.
     *
     * @param path Path to directory to start recursively setting permissions and deleting all files and directories
     *             within it
     * @throws IOException If errors occur while setting permissions for the directory.
     */
    private void setExecutePermissionsAndDelete(File path) throws IOException {
        File filesList[] = path.listFiles();

        for (File file : filesList) {
            if (file.isDirectory()) {
                Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwx------"));
                setExecutePermissionsAndDelete(file.getAbsoluteFile());
            }
            file.delete();
        }
    }

    /**
     * Test change directory with relative path.
     * Expected: Should change working directory to target directory.
     * Command: `cd testTargetDir`
     */
    @Test
    public void changeToDirectory_relativePath_shouldChangeToTargetDir() {
        try {
            String targetDirString = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            File targetDir = createTempDir(targetDirString);
            cdApp.changeToDirectory(TEST_TARGET_DIR);

            String afterCd = Environment.currentDirectory;

            assertEquals(targetDirString, afterCd);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test change directory with absolute path.
     * Expected: Should change working directory to target directory.
     */
    @Test
    public void changeToDirectory_absolutePath_shouldChangeToTargetDir() {
        try {
            String targetDirString = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            File targetDir = createTempDir(targetDirString);
            cdApp.changeToDirectory(targetDirString);

            String afterCd = Environment.currentDirectory;

            assertEquals(targetDirString, afterCd);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test change directory with relative path that doesn't exist.
     * Expected: Should throw CdException.
     */
    @Test
    public void changeToDirectory_relativePathDirDontExist_shouldThrowCdException() {
        String targetDirString = testDir.getPath() + File.separator + TEST_TARGET_DIR;

        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.changeToDirectory(TEST_TARGET_DIR));

        assertEquals(CD_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test change directory with absolute path that doesn't exist.
     * Expected: Should throw CdException.
     */
    @Test
    public void changeToDirectory_absolutePathDirDontExist_shouldThrowCdException() {
        String targetDirString = testDir.getPath() + File.separator + TEST_TARGET_DIR;
        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.changeToDirectory(targetDirString));

        assertEquals(CD_PREFIX + targetDirString + ERROR_SEMICOLON + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test change directory with null argument.
     * Expected: Should throw CdException.
     */
    @Test
    public void changeToDirectory_nullPath_shouldThrowCdException() {
        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.changeToDirectory(null));

        assertEquals(CD_PREFIX + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Test change directory with empty path.
     * Expected: Should change working directory to home directory.
     */
    @Test
    public void changeToDirectory_emptyPath_shouldChangeToHomeDir() {
        try {
            cdApp.changeToDirectory("");

            String afterCd = Environment.currentDirectory;

            assertEquals(initDir.toString(), afterCd);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test change directory with two periods.
     * Expected: Should change working directory to parent directory.
     * Command: `cd ..`
     */
    @Test
    public void changeToDirectory_twoPeriod_shouldChangeToParentDir() {
        try {
            cdApp.changeToDirectory("..");

            String afterCd = Environment.currentDirectory;

            assertEquals(initDir.toString(), afterCd);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test change directory with one period.
     * Expected: Should not change working directory.
     * Command: `cd .`
     */
    @Test
    public void changeToDirectory_onePeriod_shouldNotChangeDir() {
        try {
            cdApp.changeToDirectory(".");

            String afterCd = Environment.currentDirectory;

            assertEquals(testDir.toString(), afterCd);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test getNormalisedAbsolutePath with relative path.
     * Expected: Should return normalised absolute path.
     */
    @Test
    public void getNormalizedAbsolutePath_relativePath_shouldReturnNormalizedAbsolutePath() {
        try {
            String processedPath = cdApp.getNormalizedAbsolutePath(TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (CdException e) {
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
            String processedPath = cdApp.getNormalizedAbsolutePath(testDir.getPath() + File.separator + TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (CdException e) {
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
            String processedPath = cdApp.getNormalizedAbsolutePath(
                    testDir.getPath() + File.separator + "././" + TEST_TARGET_DIR);
            String expectedPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            assertEquals(expectedPath, processedPath);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test getNormalisedAbsolutePath with empty string.
     * Expected: Should return path of user's home directory.
     */
    @Test
    public void getNormalizedAbsolutePath_emptyString_shouldReturnHomeDirPath() {
        try {
            String processedPath = cdApp.getNormalizedAbsolutePath("");
            String expectedPath = initDir.toString();
            assertEquals(expectedPath, processedPath);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with relative path.
     * Expected: Should throw CdException.
     */
    @Test
    public void checkPermissions_relativePath_shouldThrowCdException() {
        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.checkPermissions(Path.of("test"), "test"));

        assertEquals(CD_PREFIX + ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test checkPermissions with target path with no execute permissions.
     * Expected: Should throw CdException.
     */
    @Test
    public void checkPermissions_targetDirNoExecutePerms_shouldThrowCdException() {
        try {
            File targetDir = createTempDir(testDir + File.separator + TEST_TARGET_DIR);
            Files.setPosixFilePermissions(targetDir.toPath(), PosixFilePermissions.fromString("rw-------"));
            Throwable exception = assertThrows(CdException.class,
                    () -> cdApp.checkPermissions(targetDir.toPath(), targetDir.getPath()));

            assertEquals(CD_PREFIX + targetDir.getPath() + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with nested target path, where parent directory has no execute permissions.
     * Expected: Should throw CdException.
     */
    @Test
    public void checkPermissions_nestedPathParentDirNoExecutePerms_shouldThrowCdException() {
        try {
            File parentDir = createTempDir(testDir + File.separator + "testParentDir");
            File targetDir = createTempDir(parentDir + File.separator + TEST_TARGET_DIR);
            Files.setPosixFilePermissions(parentDir.toPath(), PosixFilePermissions.fromString("rw-------"));
            Throwable exception = assertThrows(CdException.class,
                    () -> cdApp.checkPermissions(targetDir.toPath(), targetDir.getPath()));

            assertEquals(CD_PREFIX + targetDir.getPath() + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test checkPermissions with path where target is not a directory but a file.
     * Expected: Should throw CdException.
     */
    @Test
    public void checkPermissions_targetPathNotDir_shouldThrowCdException() {
        try {
            File targetDir = createTempFile(testDir + File.separator + TEST_TARGET_DIR);
            Files.setPosixFilePermissions(targetDir.toPath(), PosixFilePermissions.fromString("rw-------"));
            Throwable exception = assertThrows(CdException.class,
                    () -> cdApp.checkPermissions(targetDir.toPath(), targetDir.getPath()));

            assertEquals(CD_PREFIX + targetDir.getPath() + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }
}
