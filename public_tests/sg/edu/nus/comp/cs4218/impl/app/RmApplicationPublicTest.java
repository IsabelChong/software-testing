package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SuppressWarnings("PMD")
public class RmApplicationPublicTest {
    private static final String TEMP = "temp-rm" + File.separator;
    private static final String DIR_A = "rmDirectoryA";
    private static final String DIR_B = "rmDirectoryB";
    private static final String FILE_A = "rmA";
    private static final String FILE_B = "rmB";
    private static final String FILE_C = "rmC";
    private static final String FILE_D = "rmD";

    private static Path TEMP_PATH;
    private RmApplication rmApplication;


    @BeforeAll
    static void createTemp() throws NoSuchFieldException, IllegalAccessException {
        TEMP_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }

    @BeforeEach
    public void setUp() throws Exception {
        rmApplication = new RmApplication();
        Files.createDirectory(TEMP_PATH);
        Files.createFile(Paths.get(TEMP + FILE_A));
        Files.createFile(Paths.get(TEMP + FILE_B));
        Files.createFile(Paths.get(TEMP + FILE_C));
        Files.createDirectory(Paths.get(TEMP + DIR_A));
        Files.createDirectory(Paths.get(TEMP + DIR_B));
        Files.createFile(Paths.get(TEMP + DIR_A + File.separator + FILE_D));
    }

    @AfterEach
    public void clean() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void removeTest_RemoveSingleFileNoFlag_RemovesFile() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A);
        assertFalse(Files.exists(Paths.get(TEMP + FILE_A)));
    }

    @Test
    public void removeTest_RemoveEmptyFolderNoFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false, TEMP + DIR_B));
    }

    @Test
    public void removeTest_RemoveEmptyFolderEmptyFlag_RemovesFolder() throws Exception {
        rmApplication.remove(true, false, TEMP + DIR_B);
        assertFalse(Files.exists(Paths.get(TEMP + DIR_B)));
    }

    @Test
    public void removeTest_RemoveFolderNoFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_RemoveFolderEmptyFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(true, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_RemoveMultipleFilesNoFlag_RemovesSelectedFiles() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A, TEMP + FILE_B, TEMP + FILE_C);
        assertFalse(Files.exists(Paths.get(TEMP + FILE_A)));
        assertFalse(Files.exists(Paths.get(TEMP + FILE_B)));
        assertFalse(Files.exists(Paths.get(TEMP + FILE_C)));
    }

    @Test
    public void removeTest_RemoveFolderRecurseFlag_RemovesAllFoldersAndFiles() throws Exception {
        rmApplication.remove(false, true, TEMP + DIR_A);
        assertFalse(Files.exists(Paths.get(TEMP + DIR_A)));
        assertFalse(Files.exists(Paths.get(TEMP + DIR_A + File.separator + FILE_D)));
    }
}
