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

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempDir;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.setPermissionsAndDelete;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

@SuppressWarnings("PMD")
public class MkdirApplicationTestIT {

    private MkdirApplication mkdirApp;
    private File testDir;
    private final Path initDir = Paths.get(System.getProperty("user.dir"));
    private static final String MKDIR_PREFIX = "mkdir: ";
    private static final String TEST_TARGET_DIR = "testTargetDir";
    private static final String NESTED_TARGET_DIR = "testParentDir/testTargetDir";
    private static final String NO_WRITE_EXEC_PERMS = "r--------"; //NOPMD
    private static final String NO_EXEC_PERMS = "rw-------";
    private static final String NO_WRITE_PERMS = "r-x------";
    private static final String ERROR_SEMICOLON = ": ";


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
     * Test mkdir with null argument.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_nullArg_shouldThrowMkdirException() {
        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(null, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ERR_NULL_ARGS, exception.getMessage());
    }

    /**
     * Test mkdir with empty argument list.
     * Expected: Should throw MkdirException.
     * Command: `mkdir`
     */
    @Test
    public void run_emptyArgs_shouldThrowMkdirException() {
        String[] args = new String[]{};

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));
        assertEquals(MKDIR_PREFIX + ERR_NO_FOLDERS, exception.getMessage());
    }

    /**
     * Test mkdir with existing directory.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_directoryExists_shouldThrowMkdirException() {
        String targetPath = TEST_TARGET_DIR;
        String[] args = new String[]{targetPath};

        File dir = createTempDir(testDir + File.separator + targetPath);

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));
        assertEquals(MKDIR_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_FILE_EXISTS, exception.getMessage());
    }

    /**
     * Test mkdir on nested target directory within parent directory, without -p flag.
     * Parent directory doesn't exist.
     * Expected: Should throw MkdirException.
     * Command: `mkdir testParentDir/testTargetDir`
     */
    @Test
    public void run_parentDirWithoutPFlag_shouldThrowMkdirException() {
        String targetPath = NESTED_TARGET_DIR;
        String[] args = new String[]{targetPath};

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));
        assertEquals(MKDIR_PREFIX + NESTED_TARGET_DIR + ERROR_SEMICOLON + ERR_TOP_LEVEL_MISSING, exception.getMessage());
    }

    /**
     * Test mkdir with existing target directory, nested in parent directory, with -p flag.
     * Expected: Should throw MkdirException.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_targetDirectoryExistsWithParent_shouldThrowMkdirException() {
        String targetPath = NESTED_TARGET_DIR;
        String[] args = new String[]{"-p", targetPath};

        File parentDir = createTempDir(testDir + File.separator + "testParentDir");
        File targetDir = createTempDir(testDir + File.separator + targetPath);

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));
        assertEquals(MKDIR_PREFIX + NESTED_TARGET_DIR + ERROR_SEMICOLON + ERR_FILE_EXISTS, exception.getMessage());
    }

    /**
     * Test mkdir with multiple arguments, where one is nested and one is not nested, without -p flag.
     * For nested argument, parent directory doesn't exist.
     * Expected: Should throw MkdirException.
     * Command: `mkdir testTargetDir1 testParentDir/testTargetDir2`
     */
    @Test
    public void run_multipleArgsParentDirWithoutPFlag_shouldThrowMkdirException() {
        String targetPath1 = TEST_TARGET_DIR;
        String targetPath2 = "testParentDir/testTargetDir2";
        String[] args = new String[]{targetPath1, targetPath2};

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));
        assertEquals(MKDIR_PREFIX + targetPath2 + ERROR_SEMICOLON + ERR_TOP_LEVEL_MISSING, exception.getMessage());
    }

    /**
     * Test mkdir where parent directory doesn't have write permissions.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_noParentDirectoryWritePerms_shouldThrowMkdirException() {
        try {
            String pathName = TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + pathName + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir where parent directory doesn't have execute permissions.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_noParentDirectoryExecPerms_shouldThrowMkdirException() {
        try {
            String pathName = TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + TEST_TARGET_DIR + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir where parent directory doesn't have write and execute permissions.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_noParentDirectoryWriteAndExecPerms_shouldThrowMkdirException() {
        try {
            String pathName = "testTargetDir1";
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + pathName + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with absolute path where parent directory doesn't have write and execute permissions.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_noParentDirectoryWriteAndExecPermsAbsolutePath_shouldThrowMkdirException() {
        try {
            String pathName = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + pathName + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with nested argument of target directory within parent directory, where none of them exist.
     * Directory where we are trying to create nested directories in doesn't have write permissions.
     * Tested with p flag.
     * Expected: Should throw MkdirException.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_noParentDirectoryWritePermsWithPFlag_shouldThrowMkdirException() {
        try {
            String pathName = NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + NESTED_TARGET_DIR + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with nested argument of target directory within parent directory, where none of them exist.
     * Directory where we are trying to create nested directories in doesn't have execute permissions.
     * Tested with p flag.
     * Expected: Should throw MkdirException.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_noParentDirectoryExecPermsWithPFlag_shouldThrowMkdirException() {
        try {
            String pathName = NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + pathName + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with nested argument of target directory within parent directory, where none of them exist.
     * Directory where we are trying to create nested directories in doesn't have write and execute permissions.
     * Tested with p flag.
     * Expected: Should throw MkdirException.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_noParentDirectoryWriteAndExecPermsWithPFlag_shouldThrowMkdirException() {
        try {
            String pathName = NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + NESTED_TARGET_DIR + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with nested argument of target directory within parent directory, where none of them exist.
     * Directory where we are trying to create nested directories in doesn't have write and execute permissions.
     * Tested with p flag, and absolute path used.
     * Expected: Should throw MkdirException.
     */
    @Test
    public void run_noParentDirectoryWriteAndExecPermsWithPFlagAbsolutePath_shouldThrowMkdirException() {
        try {
            String pathName = testDir.getPath() + File.separator + NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", pathName};

            Files.setPosixFilePermissions(testDir.toPath(), PosixFilePermissions.fromString(NO_WRITE_EXEC_PERMS));

            Throwable exception = assertThrows(MkdirException.class,
                    () -> mkdirApp.run(args, System.in, System.out));

            assertEquals(MKDIR_PREFIX + pathName + ERROR_SEMICOLON + ERR_NO_PERM, exception.getMessage());
        } catch (IOException e) {
            fail(e);
        }
    }


    /**
     * Test mkdir with one valid argument.
     * Expected: Should create target directory.
     * Command: `mkdir testTargetDir`
     */
    @Test
    public void run_oneValidArg_shouldCreateDirectory() {
        try {
            String targetPath = TEST_TARGET_DIR;
            String[] args = new String[]{targetPath};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);
            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with multiple valid arguments.
     * Expected: Should create specified multiple target directories.
     * Command: `mkdir testTargetDir1 testTargetDir2`
     */
    @Test
    public void run_multipleValidArgs_shouldCreateDirectories() {
        try {
            String targetPath1 = "testTargetDir1";
            String targetPath2 = "testTargetDir2";
            String[] args = new String[]{targetPath1, targetPath2};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir1 = new File(testDir + File.separator + targetPath1);
            File expectedDir2 = new File(testDir + File.separator + targetPath2);
            assertTrue(expectedDir1.exists() && expectedDir1.isDirectory());
            assertTrue(expectedDir2.exists() && expectedDir2.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with nested target directory within parent directory,
     * where parent directory exists, without -p flag.
     * Expected: Should create target directory within parent directory.
     * Command: `mkdir testParentDir/testTargetDir`
     */
    @Test
    public void run_nestedDirParentDirExist_shouldCreateTargetDirectory() {
        try {
            String targetPath = NESTED_TARGET_DIR;
            String[] args = new String[]{targetPath};

            File parentDir = createTempDir(testDir + File.separator + "testParentDir");

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);
            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir using absolute path with target directory,
     * where all top level paths exist.
     * Expected: Should create target directory.
     * Command: `mkdir -p <absolute-path>/testTargetDir`
     */
    @Test
    public void run_absolutePathTopLevelsExist_shouldCreateTargetDirectory() {
        try {
            String targetPath = testDir.getPath() + File.separator + TEST_TARGET_DIR;
            String[] args = new String[]{targetPath};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir.getPath() + File.separator + TEST_TARGET_DIR);
            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir using absolute path with target directory nested within parent directory,
     * where parent directory doesn't exist.
     * Expected: Should throw exception.
     * Command: `mkdir <absolute-path>/testParentDir/testTargetDir`
     */
    @Test
    public void run_absolutePathTopLevelDontExist_shouldThrowMkdirException() {
        String targetPath = testDir + File.separator + NESTED_TARGET_DIR;
        String[] args = new String[]{targetPath};

        Throwable exception = assertThrows(MkdirException.class,
                () -> mkdirApp.run(args, System.in, System.out));

        assertEquals(MKDIR_PREFIX + targetPath + ERROR_SEMICOLON + ERR_TOP_LEVEL_MISSING, exception.getMessage());
    }

    /**
     * Test mkdir with target directory nested within parent directory, where parent directory doesn't exist.
     * Tested with -p flag.
     * Expected: Should create target directory, along with parent directory.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_oneValidParentDontExist_shouldCreateTargetAndParentDirectory() {
        try {
            String targetPath = NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", targetPath};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);

            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with target directory nested within parent directory, where parent directory exists.
     * Tested with -p flag.
     * Expected: Should create target directory within existing parent directory.
     * Command: `mkdir -p testParentDir/testTargetDir`
     */
    @Test
    public void run_oneValidParentExist_shouldCreateTargetDirectory() {
        try {
            String targetPath = NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", targetPath};

            File parentDir = createTempDir(testDir + File.separator + "testParentDir");

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);

            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with multiple nested levels with -p flag.
     * Target directory nested within two levels of parent directories,
     * where none of the parent directories exists.
     * Expected: Should create target directory, along with both parent directories.
     * Command: `mkdir -p testParentDir1/testParentDir2/testTargetDir`
     */
    @Test
    public void run_multipleValidParentNoneExist_shouldCreateTargetAndParentDirectory() {
        try {
            String targetPath = "testParentDir1/testParentDir2/testTargetDir";
            String[] args = new String[]{"-p", targetPath};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);

            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with multiple nested levels with -p flag.
     * Target directory nested within two levels of parent directories,
     * where some of the parent directories exist.
     * Expected: Should create target directory, along with the non-existing parent.
     * Command: `mkdir -p testParentDir1/testParentDir2/testTargetDir`
     */
    @Test
    public void run_multipleValidParentSomeExist_shouldCreateTargetAndParentDirectory() {
        try {
            String targetPath = "testParentDir1/testParentDir2/testTargetDir";
            String[] args = new String[]{"-p", targetPath};

            File parentDir1 = createTempDir(testDir + File.separator + "testParentDir1");

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);

            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with multiple nested levels with -p flag.
     * Target directory nested within two levels of parent directories,
     * where all the parent directories exist.
     * Expected: Should create target directory, within the two levels of parent directories.
     * Command: `mkdir -p testParentDir1/testParentDir2/testTargetDir`
     */
    @Test
    public void run_multipleValidParentAllExist_shouldCreateTargetDiretory() {
        try {
            String targetPath = "testParentDir1/testParentDir2/testTargetDir";
            String[] args = new String[]{"-p", targetPath};

            File parentDir1 = createTempDir(testDir + File.separator + "testParentDir1");
            File parentDir2 = createTempDir(testDir + File.separator + "testParentDir2");

            mkdirApp.run(args, System.in, System.out);

            File expectedDir = new File(testDir + File.separator + targetPath);

            assertTrue(expectedDir.exists() && expectedDir.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with multiple arguments of nested levels with -p flag.
     * All parent and target directories specified don't exist.
     * Expected: Should create parent and target directory for all arguments.
     * Command: `mkdir -p testParentDir1/testTargetDir1 testParentDir2/testTargetDir2`
     */
    @Test
    public void run_multipleArgAllFoldersDontExistWithPFlag_shouldCreateTargetDirectories() {
        try {
            String targetPath1 = "testParentDir1/testTargetDir1";
            String targetPath2 = "testParentDir2/testTargetDir2";
            String[] args = new String[]{"-p", targetPath1, targetPath2};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir1 = new File(testDir + File.separator + targetPath1);
            File expectedDir2 = new File(testDir + File.separator + targetPath2);

            assertTrue(expectedDir1.exists() && expectedDir1.isDirectory());
            assertTrue(expectedDir2.exists() && expectedDir2.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }

    /**
     * Test mkdir with absolute path and nested directories with -p flag.
     * All parent and target directories specified don't exist.
     * Expected: Should create parent and target directory for all arguments.
     * Command: `mkdir -p <absolute-path>/testParentDir/testTargetDir`
     */
    @Test
    public void run_absolutePathWithPFlag_shouldCreateTargetDirectories() {
        try {
            String targetPath = testDir.getPath() + File.separator + NESTED_TARGET_DIR;
            String[] args = new String[]{"-p", targetPath};

            mkdirApp.run(args, System.in, System.out);

            File expectedDir1 = new File(targetPath);

            assertTrue(expectedDir1.exists() && expectedDir1.isDirectory());
        } catch (MkdirException e) {
            fail(e);
        }
    }
}
