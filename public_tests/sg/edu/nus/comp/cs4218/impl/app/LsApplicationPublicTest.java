package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

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


@SuppressWarnings("PMD")
public class LsApplicationPublicTest {

    public static final String SRC = "src";
    private static final String TEMP = "temp-ls";
    private static final String FIRST_LEVEL_FOLDER = "firstLevelFolder";
    private static final String FIRST_LEVEL_FILE = "firstLevelFile";
    private static final String FIRST_LEVEL_HIDDEN_FILE = ".firstLevelHidden";
    private static final String SECOND_LEVEL_FOLDER = "secondLevelFolder";
    private static Path TEMP_PATH;
    private static File[] firstLevelTestFiles;
    private static File[] allTestFiles;

    private LsApplication application;

    @BeforeAll
    static void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        TEMP_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectories(Paths.get(TEMP_PATH.toString(), FIRST_LEVEL_FOLDER, SECOND_LEVEL_FOLDER));
        Files.createFile(Paths.get(TEMP_PATH.toString(), FIRST_LEVEL_FILE));
        Files.createFile(Paths.get(TEMP_PATH.toString(), FIRST_LEVEL_HIDDEN_FILE));
        firstLevelTestFiles = TEMP_PATH.toFile().listFiles();
        File[] secondLevelTestFiles = TEMP_PATH.resolve(Paths.get(FIRST_LEVEL_FOLDER)).toFile().listFiles();
        allTestFiles = Stream.concat(Arrays.stream(firstLevelTestFiles), Arrays.stream(secondLevelTestFiles))
                .toArray(size -> (File[]) Array.newInstance(firstLevelTestFiles.getClass().getComponentType(), size));
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @BeforeEach
    void createTemp() {
        application = new LsApplication();
    }

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

    @Test
    public void listFolderContent_NoFoldersSpecified_ReturnsCurrentFolderContent() throws AbstractApplicationException {
        String result =  application.listFolderContent(false, false);
        assertTrue(result.contains(SRC));
    }

    @Test
    public void listFolderContent_SingleFolderSpecified_ReturnsFolderContent() throws Exception {
        String result = application.listFolderContent(false, false, TEMP);
        assertResultContainsFiles(result, firstLevelTestFiles);
    }

    @Test
    public void listFolderContent_SingleFolderSpecifiedRecursive_ReturnsAllContent() throws Exception {
        String result = application.listFolderContent(true, false, TEMP);
        assertResultContainsFiles(result, allTestFiles);
    }

    @Test
    public void listFolderContent_MultipleFoldersSpecified_ReturnsMultipleFoldersContent() throws Exception {
        String result =  application.listFolderContent(false, false,
                                                      TestStringUtils.STRING_CURR_DIR + TestStringUtils.CHAR_FILE_SEP, TEMP);
        assertTrue(result.contains("src"));
        assertResultContainsFiles(result, firstLevelTestFiles);
    }

    @Test
    public void listFolderContent_SingleFolder2LevelsDownSpecified_ReturnsFolderContent() throws Exception {
        String result = application.listFolderContent(false, false,
                                                      Paths.get(TEMP_PATH.toString(), FIRST_LEVEL_FOLDER).toString());
        assertTrue(result.contains(SECOND_LEVEL_FOLDER));
    }
}
