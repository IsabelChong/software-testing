package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;

public class MvApplicationPublicTest {
    private MvApplication application;
    private static final String TEMP = "temp-mv" + File.separator;
    private static final String TEXT_A = "textA.txt";
    private static final String TEXT_A_PATH = TEMP + TEXT_A;
    private static final String TEXT_B = "textB.txt";
    private static final String TEXT_B_PATH = TEMP + TEXT_B;
    private static final String FOLDER = "folder" + File.separator;
    private static final String FOLDER_PATH = TEMP + FOLDER;
    private static final String MOVED_TEXT_TXT = "movedText.txt";
    private static final String MOVED_TXT_PATH = TEMP + MOVED_TEXT_TXT;
    private static final String TARGET_FOLDER = "targetFolder" + File.separator;
    private static final String TARGET_FDR_PATH = TEMP + TARGET_FOLDER;

    private static final String TEXT_C = "textC.txt";
    private static final String TEXT_C_PATH = TEMP + TEXT_C;


    void createAndWriteFile(String filePath) throws Exception {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Content inside " + filePath);
        }
    }

    void createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        Files.createDirectories(path);
    }

    void createMvResourcesAndFolders() throws Exception {
        createFolder(TEMP);
        createFolder(TARGET_FDR_PATH);
        createFolder(FOLDER_PATH);
    }

    @BeforeEach
    void setup() throws Exception {
        application = new MvApplication();
        createMvResourcesAndFolders();
        createAndWriteFile(TEXT_A_PATH);
        createAndWriteFile(TEXT_B_PATH);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Test case with mvSrcFileToDestFile where source file is moved to a folder
     */
    @Test
    void moveSrcFileToDestFile_ValidFile_FileRenamed() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvSrcFileToDestFile(true, TEXT_A_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

   /**
    * Test case with mvSrcFileToDestFile where source folder is moved to a folder and the folder is renamed
    */
    @Test
    void moveSrcFileToDestFile_ValidFolder_FolderRenamed() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvSrcFileToDestFile(true, FOLDER_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }

    /*
     * Test case with mvSrcFileToDestFile where source file does not exist and nothing happens
     */
    @Test
    void mvFilesToFolder_SourceFileNotExisting_FileMoved() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH);
        File target = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    /**
     * Test case with mvFilesToFolder where source folder does not exist and 
     */
    @Test
    void mvFilesToFolder_SourceFolderNotExisting_FolderMoved() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvFilesToFolder(true, TARGET_FDR_PATH, FOLDER_PATH);
        File target = new File(TARGET_FDR_PATH + FOLDER);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }
    
    /*
     * Test case with mvFilesToFolder where multiple source files are moved to a folder
     */
    @Test
    void mvFilesToFolder_MultipleSourceFile_FilesMoved() throws Exception {
        File sourceA = new File(TEXT_A_PATH);
        List<String> sourceAContent = Files.readAllLines(sourceA.toPath());
        File sourceB = new File(TEXT_B_PATH);
        List<String> sourceBContent = Files.readAllLines(sourceB.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH, TEXT_B_PATH);
        File targetA = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetAContent = Files.readAllLines(targetA.toPath());
        File targetB = new File(TARGET_FDR_PATH + TEXT_B);
        List<String> targetBContent = Files.readAllLines(targetB.toPath());

        assertTrue(targetA.exists());
        assertTrue(targetB.exists());
        assertFalse(sourceA.exists());
        assertFalse(sourceB.exists());
        assertEquals(sourceAContent, targetAContent);
        assertEquals(sourceBContent, targetBContent);
    }

   /*
     * Test case with mvFilesToFolder where source file does not exist and throws an exceptipn
     */
    @Test
    void mvFilesToFolder_NoSourceFiles_ErrorThrown() throws Exception {
        assertThrows(MvException.class, () -> application.mvFilesToFolder(true, TARGET_FDR_PATH, new String[]{}));
    }
    /*
     * Test case with mvSrcFileToDestFile where source file does not exist and throws an exceptipn
     */
    @Test
    void mvSrcFileToDestFile_NoSourceFiles_ErrorThrown() throws Exception {
        assertThrows(MvException.class, () -> application.mvSrcFileToDestFile(true, TEXT_C_PATH, TARGET_FDR_PATH));
    }
}
