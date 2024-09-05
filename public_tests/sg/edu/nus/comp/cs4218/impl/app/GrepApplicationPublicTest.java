package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class GrepApplicationPublicTest {
    private final static String LABEL_STDIN = "(standard input)";
    private final static String PATTERN_VALID = "test";
    private final static String PATTERN_INVALID = "[";

    private final static String TEXT_ONE = "test";
    private final static String TEXT_TWO = "another test.";
    private final static byte[] BYTES_SINGLE_LINE = TEXT_ONE.getBytes();
    private final static String TEXT_MULTI_LINE = TEXT_ONE + STRING_NEWLINE + TEXT_TWO + STRING_NEWLINE + "Test";
    private final static byte[] BYTES_MULTI_LINE = TEXT_MULTI_LINE.getBytes();

    private static final String TEST_FILE = "fileA.txt";
    private static final String TEMP = "temp-grep";
    private static Path TEMP_PATH;
    private static Path TEST_FILE_PATH;

    private GrepApplication grepApplication;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException, NoClassDefFoundError,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
        grepApplication = new GrepApplication();
        TEMP_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        TEST_FILE_PATH = TEMP_PATH.resolve(TEST_FILE);
        Files.createDirectory(TEMP_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEST_FILE_PATH.toAbsolutePath());
        Files.delete(TEMP_PATH);
    }

    private Path createFile() throws IOException {
        Path path = TEMP_PATH.resolve(GrepApplicationPublicTest.TEST_FILE);
        Files.createFile(path);
        return path;
    }


    @Test
    void grepFromStdin_CountLinesOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = "2";

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, false, true, false, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CountLinesOptionInvalidPatternStdin_LinesFoundAddedToResults() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);

        assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(PATTERN_INVALID, false, true, false, stdin));
    }

    @Test
    void grepFromStdin_CaseInsensitiveCountLinesOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = "3";

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, true, true, false, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CountLinesPrefixFileNameOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = String.format("%s: %d", LABEL_STDIN, 2);

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, false, true, true, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CaseInsensitiveCountLinesPrefixFilenamePatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = String.format("%s: %d", LABEL_STDIN, 3);

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, true, true, true, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }


    @Test
    void grepFromFiles_CountLinesOptionPatternFile_LinesFoundAddedToResults() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String[] fileNames = new String[]{TEST_FILE_PATH.toString()};
        Files.write(createFile(), BYTES_MULTI_LINE);
        String expected = "2";

        String actual = grepApplication.grepFromFiles(PATTERN_VALID, false, true, false, fileNames);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromFiles_CaseInsensitivePatternFile_LinesFoundAddedToResults() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String[] fileNames = new String[]{TEST_FILE_PATH.toString()};
        Files.write(createFile(), BYTES_MULTI_LINE);

        String actual = grepApplication.grepFromFiles(PATTERN_VALID, true, false, false, fileNames);
        assertEquals(TEXT_MULTI_LINE + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromFileAndStdin_EmptyPattern_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String[] fileNames = new String[]{TEST_FILE};
        Files.write(createFile(), BYTES_MULTI_LINE);

        assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin("", false, false, false, stdin, fileNames));
    }

}
