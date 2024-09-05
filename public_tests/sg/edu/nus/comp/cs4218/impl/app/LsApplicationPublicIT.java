package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class LsApplicationPublicIT {
    private static final String TEMP = "temp-ls";
    private static final String FOLDER = "folder";
    private static final String TEXT_IN_FDR = "file_in_folder.txt";
    private static final String JPG_IN_FDR = "image_in_folder.jpg";
    private static final String DOC_IN_FDR = "document_in_folder.doc";
    private static final String SUBFOLDER_A = "subfolderA";
    private static final String TXT_IN_SUBFDR_A = "file_in_subfolderA.txt";
    private static final String JPG_IN_SUBFDR_A = "image_in_subfolderA.jpg";
    private static final String DOC_IN_SUBFDR_A = "document_in_subfolderA.doc";
    private static final String SUBFOLDER_B = "subfolderB";
    private static final String TXT_IN_SUBFDR_B = "file_in_subfolderB.txt";
    private static final String JPG_IN_SUBFDR_B = "image_in_subfolderB.jpg";
    private static final String DOC_IN_SUBFDR_B = "document_in_subfolderB.doc";
    private static final String FILE_A_TXT = "fileA.txt";
    private static final String FOLDER_B = "folderB";
    private static final String IMAGE_B_JPG = "imageB.jpg";
    private static final String DOCUMENT_C_DOC = "documentC.doc";
    private static final String FOLDER_D = "folderD";
    private static final String EMPTY_FOLDER = "empty_folder";
    private static final String EMPTY_FOLDER_A = "empty_folderA";
    private static final String EMPTY_FOLDER_B = "empty_folderB";
    private static final String FIRST_FOLDER = "first_folder";
    private static final String SECOND_FOLDER = "second_folder";
    private static final String TXT_IN_FIRST_FDR = "file_in_first_folder.txt";
    private static final String JPG_IN_FIRST_FDR = "image_in_first_folder.jpg";
    private static final String DOC_IN_FIRST_FDR = "document_in_first_folder.doc";
    private static final String TXT_IN_SECOND_FDR = "file_in_second_folder.txt";
    private static final String JPG_IN_SECOND_FDR = "image_in_second_folder.jpg";
    private static final String DOC_IN_SECOND_FDR = "document_in_second_folder.doc";
    private static final String COLON = ":";

    private static final Deque<Path> files = new ArrayDeque<>();

    private static Path TEMP_PATH;
    private LsApplication lsApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        Path originalPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
        TEMP_PATH = Paths.get(originalPath.toString(), TEMP);
    }

    @BeforeEach
    void init() throws IOException {
        lsApplication = new LsApplication();
        Files.createDirectory(TEMP_PATH);
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    private void createFile(String name) throws IOException {
        createFile(name, TEMP_PATH);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, TEMP_PATH);
    }

    private void createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        files.push(path);
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
        return path;
    }

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

    @Test
    void run_NoDirectoriesNoFlags_DisplaysFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = TestEnvironmentUtil.getCurrentDirectory();
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
        createFile(FILE_A_TXT);
        createDirectory(FOLDER_B);
        lsApplication.run(toArgs(""), System.in, output);
        TestEnvironmentUtil.setCurrentDirectory(currPathString);
        assertEquals((FILE_A_TXT + STRING_NEWLINE + FOLDER_B + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_NoDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = TestEnvironmentUtil.getCurrentDirectory();
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
        createFile(FILE_A_TXT);
        Path folderBPath = createDirectory(FOLDER_B);
        createFile("file_in_folderB.txt", folderBPath);
        lsApplication.run(toArgs("R"), System.in, output);
        TestEnvironmentUtil.setCurrentDirectory(currPathString);
        assertEquals(("." + CHAR_FILE_SEP + COLON + STRING_NEWLINE + FILE_A_TXT + STRING_NEWLINE + FOLDER_B + STRING_NEWLINE + STRING_NEWLINE +
                FOLDER_B + COLON + STRING_NEWLINE + "file_in_folderB.txt" + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_NoDirectoriesSortFlag_DisplaysSortedFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = TestEnvironmentUtil.getCurrentDirectory();
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
        createFile(FILE_A_TXT);
        createFile(IMAGE_B_JPG);
        createFile(DOCUMENT_C_DOC);
        createDirectory(FOLDER_D);
        lsApplication.run(toArgs("X"), System.in, output);
        TestEnvironmentUtil.setCurrentDirectory(currPathString);
        assertEquals((FOLDER_D + STRING_NEWLINE + DOCUMENT_C_DOC + STRING_NEWLINE + IMAGE_B_JPG + STRING_NEWLINE +
                FILE_A_TXT + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyDirectoryNoFlags_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory(EMPTY_FOLDER);
        lsApplication.run(toArgs("", EMPTY_FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + EMPTY_FOLDER + COLON + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleDirectoryWithFilesNoFlags_DisplaysFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory(FOLDER);
        createFile(FILE_A_TXT, dir);
        createFile(IMAGE_B_JPG, dir);
        createFile(DOCUMENT_C_DOC, dir);
        lsApplication.run(toArgs("", FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FOLDER + COLON + STRING_NEWLINE + DOCUMENT_C_DOC + STRING_NEWLINE + FILE_A_TXT +
                STRING_NEWLINE + IMAGE_B_JPG + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleDirectoryWithFilesSortFlag_DisplaysSortedFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory(FOLDER);
        createFile(FILE_A_TXT, dir);
        createFile(IMAGE_B_JPG, dir);
        createFile(DOCUMENT_C_DOC, dir);
        lsApplication.run(toArgs("X", FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FOLDER + COLON + STRING_NEWLINE + DOCUMENT_C_DOC + STRING_NEWLINE + IMAGE_B_JPG +
                STRING_NEWLINE + FILE_A_TXT + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleDirectoryWithEmptyDirectoryNoFlags_DisplaysDirectory() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory(FOLDER);
        createDirectory(EMPTY_FOLDER, dir);
        lsApplication.run(toArgs("", FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FOLDER + COLON + STRING_NEWLINE + EMPTY_FOLDER + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesNoFlags_DisplaysFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = createNestedFolders();
        lsApplication.run(toArgs("", FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FOLDER + COLON + STRING_NEWLINE + DOC_IN_FDR + STRING_NEWLINE +
                TEXT_IN_FDR + STRING_NEWLINE + JPG_IN_FDR + STRING_NEWLINE + SUBFOLDER_A + STRING_NEWLINE +
                SUBFOLDER_B + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    private ByteArrayOutputStream createNestedFolders() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory(FOLDER);
        createFile(TEXT_IN_FDR, dir);
        createFile(JPG_IN_FDR, dir);
        createFile(DOC_IN_FDR, dir);
        Path dirA = createDirectory(SUBFOLDER_A, dir);
        createFile(TXT_IN_SUBFDR_A, dirA);
        createFile(JPG_IN_SUBFDR_A, dirA);
        createFile(DOC_IN_SUBFDR_A, dirA);
        Path dirB = createDirectory(SUBFOLDER_B, dir);
        createFile(TXT_IN_SUBFDR_B, dirB);
        createFile(JPG_IN_SUBFDR_B, dirB);
        createFile(DOC_IN_SUBFDR_B, dirB);
        return output;
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws Exception {
        ByteArrayOutputStream output = createNestedFolders();
        lsApplication.run(toArgs("R", FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FOLDER + COLON + STRING_NEWLINE + DOC_IN_FDR + STRING_NEWLINE + TEXT_IN_FDR +
                STRING_NEWLINE + JPG_IN_FDR + STRING_NEWLINE + SUBFOLDER_A + STRING_NEWLINE + SUBFOLDER_B + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + CHAR_FILE_SEP + FOLDER + CHAR_FILE_SEP + SUBFOLDER_A + COLON + STRING_NEWLINE + DOC_IN_SUBFDR_A +
                STRING_NEWLINE + TXT_IN_SUBFDR_A + STRING_NEWLINE + JPG_IN_SUBFDR_A + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + CHAR_FILE_SEP + FOLDER + CHAR_FILE_SEP + SUBFOLDER_B + COLON + STRING_NEWLINE + DOC_IN_SUBFDR_B + STRING_NEWLINE +
                TXT_IN_SUBFDR_B + STRING_NEWLINE + JPG_IN_SUBFDR_B + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyDirectoriesNoFlags_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory(EMPTY_FOLDER_A);
        createDirectory(EMPTY_FOLDER_B);
        lsApplication.run(toArgs("", EMPTY_FOLDER_A, EMPTY_FOLDER_B), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + EMPTY_FOLDER_A + COLON + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + CHAR_FILE_SEP + EMPTY_FOLDER_B + COLON + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesNoFlags_DisplaysFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = createDoublyNestedFolders();
        lsApplication.run(toArgs("", FIRST_FOLDER, SECOND_FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FIRST_FOLDER + COLON + STRING_NEWLINE + DOC_IN_FIRST_FDR + STRING_NEWLINE +
                TXT_IN_FIRST_FDR + STRING_NEWLINE + JPG_IN_FIRST_FDR + STRING_NEWLINE + SUBFOLDER_A + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + COLON + STRING_NEWLINE + DOC_IN_SECOND_FDR + STRING_NEWLINE +
                TXT_IN_SECOND_FDR + STRING_NEWLINE + JPG_IN_SECOND_FDR + STRING_NEWLINE + SUBFOLDER_B +
                STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    private ByteArrayOutputStream createDoublyNestedFolders() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory(FIRST_FOLDER);
        createFile(TXT_IN_FIRST_FDR, firstDir);
        createFile(JPG_IN_FIRST_FDR, firstDir);
        createFile(DOC_IN_FIRST_FDR, firstDir);

        Path dirA = createDirectory(SUBFOLDER_A, firstDir);
        createFile(TXT_IN_SUBFDR_A, dirA);
        createFile(JPG_IN_SUBFDR_A, dirA);
        createFile(DOC_IN_SUBFDR_A, dirA);

        Path secondDir = createDirectory(SECOND_FOLDER);
        createFile(TXT_IN_SECOND_FDR, secondDir);
        createFile(JPG_IN_SECOND_FDR, secondDir);
        createFile(DOC_IN_SECOND_FDR, secondDir);

        Path dirB = createDirectory(SUBFOLDER_B, secondDir);
        createFile(TXT_IN_SUBFDR_B, dirB);
        createFile(JPG_IN_SUBFDR_B, dirB);
        createFile(DOC_IN_SUBFDR_B, dirB);
        return output;
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws Exception {
        ByteArrayOutputStream output = createDoublyNestedFolders();
        lsApplication.run(toArgs("R", FIRST_FOLDER, SECOND_FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FIRST_FOLDER + COLON + STRING_NEWLINE + DOC_IN_FIRST_FDR + STRING_NEWLINE +
                TXT_IN_FIRST_FDR + STRING_NEWLINE + JPG_IN_FIRST_FDR + STRING_NEWLINE + SUBFOLDER_A + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + CHAR_FILE_SEP + FIRST_FOLDER + CHAR_FILE_SEP + SUBFOLDER_A + COLON + STRING_NEWLINE +
                DOC_IN_SUBFDR_A + STRING_NEWLINE + TXT_IN_SUBFDR_A + STRING_NEWLINE + JPG_IN_SUBFDR_A +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + COLON + STRING_NEWLINE + DOC_IN_SECOND_FDR +
                STRING_NEWLINE + TXT_IN_SECOND_FDR + STRING_NEWLINE + JPG_IN_SECOND_FDR + STRING_NEWLINE + SUBFOLDER_B +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + CHAR_FILE_SEP + SUBFOLDER_B + COLON + STRING_NEWLINE +
                DOC_IN_SUBFDR_B + STRING_NEWLINE + TXT_IN_SUBFDR_B + STRING_NEWLINE + JPG_IN_SUBFDR_B +
                STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesSortFlag_DisplaysSortedFilesAndDirectories() throws Exception {
        ByteArrayOutputStream output = createDoublyNestedFolders();
        lsApplication.run(toArgs("X", FIRST_FOLDER, SECOND_FOLDER), System.in, output);
        assertEquals((TEMP + CHAR_FILE_SEP + FIRST_FOLDER + COLON + STRING_NEWLINE + SUBFOLDER_A + STRING_NEWLINE +
                DOC_IN_FIRST_FDR + STRING_NEWLINE + JPG_IN_FIRST_FDR + STRING_NEWLINE +
                TXT_IN_FIRST_FDR + STRING_NEWLINE + STRING_NEWLINE + TEMP + CHAR_FILE_SEP + SECOND_FOLDER + COLON +
                STRING_NEWLINE + SUBFOLDER_B + STRING_NEWLINE + DOC_IN_SECOND_FDR + STRING_NEWLINE +
                JPG_IN_SECOND_FDR + STRING_NEWLINE + TXT_IN_SECOND_FDR + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_UnknownFlag_Throws() {
        assertThrows(Exception.class, () -> lsApplication.run(toArgs("q", ""), System.in, System.out));
    }
}
