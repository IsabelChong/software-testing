package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.MkdirException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD")
public class MkdirApplicationPublicTest {
    MkdirApplication mkdirApplication;
    String pathToTestDir = "TestResources" + File.separator + "mkdirTestDir" + File.separator;
    String tempDir = pathToTestDir + "mkdirTest";
    String tempDir2 = pathToTestDir + "mkdirTest2";
    String tempParent = pathToTestDir + "mkdirTestParent";
    String tempChild = tempParent + File.separator + "mkdirTestChild";

    @BeforeEach
    void setUp() {
        mkdirApplication = new MkdirApplication();
        File testDir = new File(pathToTestDir);
        testDir.mkdirs();
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File(pathToTestDir + File.separator + "EmptyFileForGitTracking.txt");
        file.createNewFile();
        deleteDirectory(null, new File("TestResources").listFiles());
        deleteDirectory(new File("TestResources"), null);
    }

    public static void deleteDirectory(File directory, File... files) {
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i], files[i].listFiles());
                } else {
                    files[i].delete();
                }
            }
        }
        if (directory != null) {
            directory.delete();
        }
    }

    @Test
    void createFolder_NullInput_ThrowsException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.createFolder(null);
        });
    }

    @Test
    void createFolder_EmptyInput_ThrowsException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.createFolder("");
        });
        assertEquals(0, new File(pathToTestDir).list().length);
    }

    @Test
    void createFolder_OneNewDirectoryInput_Success() throws Exception {
        mkdirApplication.createFolder(tempDir);
        assertTrue(new File(tempDir).exists());
    }

    @Test
    void createFolder_TwoNewDirectoryInput_Success() throws Exception {
        mkdirApplication.createFolder(tempDir, tempDir2);
        assertTrue(new File(tempDir).exists());
        assertTrue(new File(tempDir2).exists());
    }

    @Test
    void createFolder_DirectoryInDirectoryInput_Success() throws Exception {
        mkdirApplication.createFolder(tempParent, tempChild);
        assertTrue(new File(tempChild).exists());
    }
}
