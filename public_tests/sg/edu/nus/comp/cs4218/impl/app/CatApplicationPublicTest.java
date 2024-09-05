package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class CatApplicationPublicTest {
    private static final String NUMBER_FORMAT = "%6d";
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static final String EXPECT_ONE_NUM = String.format(NUMBER_FORMAT, 1) + "\tTest line 1" + STRING_NEWLINE +
            String.format(NUMBER_FORMAT, 2) + "\tTest line 2" + STRING_NEWLINE + String.format(NUMBER_FORMAT, 3) +
            "\tTest line 3";
    private static final String TEST_DIR = "temp-cat";
    private static final String TEST_FILE = "fileA.txt";
    private static Path TEST_DIR_PATH;
    private static Path TEST_FILE_PATH;

    private CatApplication catApplication;

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        TEST_DIR_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEST_DIR);
        Files.createDirectory(TEST_DIR_PATH);

        TEST_FILE_PATH = TEST_DIR_PATH.resolve(TEST_FILE);
        Files.createFile(TEST_FILE_PATH);
        Files.write(TEST_FILE_PATH, TEXT_ONE.getBytes());
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        Files.delete(TEST_FILE_PATH);
        Files.delete(TEST_DIR_PATH);
    }

    @Test
    void catFiles_SingleFileSpecifiedNoFlagAbsolutePath_ReturnsFileContentString() throws Exception {
        String actual = catApplication.catFiles(false, TEST_FILE_PATH.toString());
        assertEquals(TEXT_ONE, actual);
    }

    @Test
    void catStdin_NoFlag_ReturnsStdinString() throws AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String actual = catApplication.catStdin(false, inputStream);
        assertEquals(TEXT_ONE + STRING_NEWLINE, actual);
    }

    @Test
    void catStdin_EmptyStringNoFlag_ReturnsEmptyString() throws AbstractApplicationException {
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        String actual = catApplication.catStdin(false, inputStream);
        assertEquals(text, actual);
    }

    // Updated the expected outcome as assumption of spaces and formatting of expected output differs
    @Test
    void catStdin_IsLineNumberFlag_ReturnsStdinStringLineNo() throws AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String actual = catApplication.catStdin(true, inputStream);
        assertEquals(EXPECT_ONE_NUM + STRING_NEWLINE, actual);
    }
}
