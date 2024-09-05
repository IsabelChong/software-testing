package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.File;
import java.io.IOException;

import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD")
class CdApplicationPublicIT {

    @TempDir
    private static File tempDir;

    private static CdApplication cdApplication;
    private static final String FOLDER = "folder";
    private static final String SUBFOLDER = "folder" + TestStringUtils.CHAR_FILE_SEP + "subfolder";
    private static final String BLOCKED_FOLDER = "blocked";
    private static final String VALID_FILE = "file.txt";

    @BeforeAll
    static void setupAll() throws IOException {
        new File(tempDir, FOLDER).mkdir();
        new File(tempDir, SUBFOLDER).mkdir();
        new File(tempDir, VALID_FILE).createNewFile();
        File blockedFolder = new File(tempDir, BLOCKED_FOLDER);
        blockedFolder.mkdir();
        blockedFolder.setExecutable(false);
    }

    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        cdApplication = new CdApplication();
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
    }

    // Cd into valid relative path
    @Test
    public void run_CdIntoValidRelativePath_Success() throws Exception {
        String finalPath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{FOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdIntoNestedFolder_Success() throws Exception {
        String finalPath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + SUBFOLDER;
        String[] argList = new String[]{SUBFOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdOutFromFolder_Success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        TestEnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    @Test
    public void run_CdOutFromNestedFolder_Success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + SUBFOLDER;
        TestEnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{"../../"};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    // Cd with blank arg
    @Test
    public void run_CdIntoBlankPath_Success() throws Exception {
        String finalPath = System.getProperty("user.dir");
        String[] argList = new String[]{""};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(finalPath, currDirectory);
    }

    // Cd into invalid relative path
    @Test
    public void run_InvalidRelativePath_ThrowsException() {
        String[] argList = new String[]{"invalid"};
        assertThrows(CdException.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into valid absolute path
    @Test
    public void run_CdIntoValidAbsolutePath_Success() throws Exception {
        String absolutePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + FOLDER;
        String[] argList = new String[]{absolutePath};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = TestEnvironmentUtil.getCurrentDirectory();
        assertEquals(absolutePath, currDirectory);
    }

    // Cd into invalid absolute path
    @Test
    public void run_CdIntoInvalidAbsolutePath_ThrowsException() {
        String absolutePath = tempDir.getAbsolutePath() + TestStringUtils.CHAR_FILE_SEP + "invalid";
        String[] argList = new String[]{absolutePath};
        assertThrows(CdException.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into non directory
    @Test
    public void run_CdIntoFile_ThrowsException() {
        String[] argList = new String[]{VALID_FILE};
        assertThrows(CdException.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd into folder with no permissions
    @Test
    @DisabledOnOs(WINDOWS)
    public void run_BlockedFolder_ThrowsException() {
        String[] argList = new String[]{BLOCKED_FOLDER};
        assertThrows(CdException.class, () -> cdApplication.run(argList, System.in, System.out));
    }

    // Cd with null args
    @Test
    public void run_CdWithNullArgs_ThrowsException() {
        assertThrows(CdException.class, () -> cdApplication.run(null, System.in, System.out));
    }

    // Cd with null input stream
    @Test
    public void run_CdWithNullInputStream_ThrowsException() {
        String[] argList = new String[]{};
        assertThrows(CdException.class, () -> cdApplication.run(argList, null, System.out));
    }

    // Cd with null output stream
    @Test
    public void run_CdWithNullOutputStream_ThrowsException() {
        String[] argList = new String[]{};
        assertThrows(CdException.class, () -> cdApplication.run(argList, System.in, null));
    }
}
