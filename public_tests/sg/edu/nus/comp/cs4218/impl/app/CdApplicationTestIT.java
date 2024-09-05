package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempDir;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

@SuppressWarnings("PMD")
public class CdApplicationTestIT {
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
        Environment.currentDirectory = System.getProperty("user.dir");
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
     * Test cd with null argument.
     * Expected: Throw CdException with error message.
     */
    @Test
    public void run_nullArg_shouldThrowCdException() {
        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.run(null, System.in, System.out));
        assertEquals(CD_PREFIX + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Test cd into folder without owner execute permissions.
     * Expected: Throw CdException with error message.
     */
    @Test
    public void run_noTargetPerm_shouldThrowCdException() {
        try {
            String pathName = TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            File targetDir = createTempDir(testDir + File.separator + pathName);
            Files.setPosixFilePermissions(targetDir.toPath(), PosixFilePermissions.fromString("rw-------"));

            Throwable exception = assertThrows(CdException.class,
                    () -> cdApp.run(args, System.in, System.out));

            assertEquals(CD_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test cd into file.
     * Expected: Throw CdException with error message.
     */
    @Test
    public void run_notDirectory_shouldThrowCdException() {
        try {
            String targetPath = TEST_TARGET_DIR;
            String[] args = new String[]{targetPath};

            //creating file with same name as argument
            File file = new File(testDir + File.separator + "testTargetDir");
            file.createNewFile();
            Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwx------"));

            Throwable exception = assertThrows(CdException.class,
                    () -> cdApp.run(args, System.in, System.out));

            assertEquals(CD_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_IS_NOT_DIR, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test cd into nested target folder where parent folder does not exist.
     * Expected: Throw CdException with error message.
     */
    @Test
    public void run_parentDirectoryDoesntExist_shouldThrowCdException() {
        String path = "testParentDir/testTargetDir";
        String[] args = new String[]{path};

        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.run(args, System.in, System.out));

        assertEquals(CD_PREFIX + path + ERROR_SEMICOLON + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test cd into directory that doesn't exist.
     * Expected: Throw CdException with error message.
     */
    @Test
    public void run_directoryDoesntExist_shouldThrowCdException() {
        String targetPath = TEST_TARGET_DIR;
        String[] args = new String[]{targetPath};

        Throwable exception = assertThrows(CdException.class,
                () -> cdApp.run(args, System.in, System.out));

        assertEquals(CD_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test cd with no arguments.
     * Expected: Change working directory to home directory.
     * Command: `cd`
     */
    @Test
    public void run_noArguments_shouldChangeToHomeDirectory() {
        String[] args = new String[]{};

        try {
            cdApp.run(args, System.in, System.out);
            String afterCdDirectory = Environment.currentDirectory;
            assertEquals(System.getProperty("user.home"), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with one period.
     * Expected: Working directory should remain the same.
     * Command: `cd .`
     */
    @Test
    public void run_onePeriod_shouldNotChangeDirectory() {
        String[] args = new String[]{"."};

        try {
            cdApp.run(args, System.in, System.out);
            String afterCdDirectory = Environment.currentDirectory;
            assertEquals(testDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with two periods.
     * Expected: Should change working directory to parent directory.
     * Command: `cd ..`
     */
    @Test
    public void run_twoPeriod_shouldChangeToParentDirectory() {
        String[] args = new String[]{".."};

        try {
            cdApp.run(args, System.in, System.out);
            String afterCdDirectory = Environment.currentDirectory;
            assertEquals(initDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with multiple arguments.
     * Expected: Should change directory to first specified directory.
     * Command: `cd testTargetDir1 testTargetDir2`
     */
    @Test
    public void run_multipleArguments_shouldChangeToFirstDirectory() {
        try {
            String[] args = new String[]{"testTargetDir1", "testTargetDir2"};
            File targetDir1 = createTempDir(testDir + File.separator + "testTargetDir1");
            File targetDir2 = createTempDir(testDir + File.separator + "testTargetDir2");

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir1.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with one target directory with owner execute permissions.
     * Expected: Should change working directory to target directory.
     */
    @Test
    public void run_haveTargetPerm_shouldChangeToTargetDirectory() {

        try {
            String pathName = TEST_TARGET_DIR;
            String[] args = new String[]{pathName};
            File targetDir = createTempDir(testDir + File.separator + pathName);
            Files.setPosixFilePermissions(targetDir.toPath(), PosixFilePermissions.fromString("rwx------"));

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir.toString(), afterCdDirectory);
        } catch (CdException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test cd with absolute path of one target directory.
     * Expected: Should change working directory to target directory.
     */
    @Test
    public void run_absoluteTargetPath_shouldChangeToTargetDirectory() {
        try {
            String pathName = testDir.toString() + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};
            File targetDir = createTempDir(pathName);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with nested level: one parent directory and one target directory.
     * Expected: Should change working directory to nested target directory.
     * Command: `cd testParentDir/testTargetDir`
     */
    @Test
    public void run_oneParentDir_shouldChangeToTargetDirectory() {
        try {
            String parentPath = "testParentDir";
            String targetPath = TEST_TARGET_DIR;
            String[] args = new String[]{parentPath + File.separator + targetPath};

            // Creating parent directory
            File parentDir = createTempDir(testDir + File.separator + parentPath);
            // Creating target directory
            File targetDir = createTempDir(parentDir.toString() + File.separator + targetPath);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with multiple nested levels: two parent directories and one target directory.
     * Expected: Should change working directory to nested target directory.
     * Command: `cd testParentDir1/testParentDir2/testTargetDir`
     */
    @Test
    public void run_moreThanOneParentDir_shouldChangeToTargetDirectory() {
        try {
            String parentPath1 = "testParentDir1";
            String parentPath2 = "testParentDir2";
            String targetPath = TEST_TARGET_DIR;
            String[] args = new String[]{parentPath1 + File.separator + parentPath2 + File.separator + targetPath};

            //creating parent directory
            File parentDir = createTempDir(testDir + File.separator + parentPath1);
            File parentDir2 = createTempDir(parentDir.toString() + File.separator + parentPath2);
            //creating target directory
            File targetDir = createTempDir(parentDir2.toString() + File.separator + targetPath);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with nested levels: one parent directory and one target directory, using absolute path.
     * Expected: Should change working directory to nested target directory.
     */
    @Test
    public void run_absoluteParentPath_shouldChangeToTargetDirectory() {
        try {
            String parentPath = "testParentDir";
            String targetPath = TEST_TARGET_DIR;
            String[] args = new String[]{testDir.toString() + File.separator + parentPath + File.separator + targetPath};

            //creating parent directory
            File parentDir = createTempDir(testDir + File.separator + parentPath);
            //creating target directory
            File targetDir = createTempDir(parentDir.toString() + File.separator + targetPath);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(targetDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with multiple levels of . and .. from current directory
     * Should change to parent directory
     */
    @Test
    public void run_nestedLevelsOfPeriods_shouldChangeToParentDirectory() {
        try {
            String path = "././..";
            String[] args = new String[]{path};

            // Creating parent directory

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(initDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with multiple levels of . and .. after specified target directory.
     * Should change to parent directory of target directory.
     */
    @Test
    public void run_nestedLevelsOfPeriodsAfterTargetDirectory_shouldChangeToParentDirectory() {
        try {
            String path = TEST_TARGET_DIR + "/././..";
            String[] args = new String[]{path};

            // Creating parent directory
            File dir = createTempDir(testDir + File.separator + path);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(testDir.toString(), afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }

    /**
     * Test cd with multiple levels of . and .. after specified target directory with absolute path.
     * Should change to target directory.
     */
    @Test
    public void run_nestedLevelsOfPeriodsAfterTargetDirectoryAbsolutePath_shouldChangeToTargetDirectory() {
        try {
            String path = testDir.getPath() + "/././" + TEST_TARGET_DIR;
            String[] args = new String[]{path};

            // Creating target directory
            File dir = createTempDir(testDir + File.separator + TEST_TARGET_DIR);

            cdApp.run(args, System.in, System.out);

            String afterCdDirectory = Environment.currentDirectory;

            assertEquals(testDir + File.separator + TEST_TARGET_DIR, afterCdDirectory);
        } catch (CdException e) {
            fail(e);
        }
    }
}
