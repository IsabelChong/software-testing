package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class WcApplicationPublicTest {
    private static final String TEMP = "temp-wc";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final String LABEL_TOTAL = "total";
    private static final String FORMAT_SINGLE = CHAR_TAB + "%s ";
    private static final String FORMAT_TRIPLE = CHAR_TAB + "%s" + CHAR_TAB + "%s" + CHAR_TAB + "%s ";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String NUMBER_FORMAT_TRIPLE = " %7d %7d %7d";
    private static final String MULTI_LINE_TEXT = "This is a test.\nThis is still a test.";
    private static final String STDIN_FILENAME = "-";
    private static final String SINGLE_LINE_TEXT = "This is a test.";
    private static final long BYTECOUNT_SINGLE = SINGLE_LINE_TEXT.getBytes().length;
    private static final long BYTECOUNT_MULTI = MULTI_LINE_TEXT.getBytes().length;
    private static final long BYTESUM_SINGLE = BYTECOUNT_SINGLE + BYTECOUNT_MULTI;

    private static String initialDir;
    private static final Deque<Path> files = new ArrayDeque<>();

    private WcApplication wcApplication;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        wcApplication = new WcApplication();
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
        Files.createDirectory(TEMP_PATH);
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
    }

    @AfterEach
    void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TestEnvironmentUtil.setCurrentDirectory(initialDir);
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private void createFile(String name, String content) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        files.push(path);
    }

    @Test
    void countFromFiles_NullFile_ThrowsException() {
        assertThrows(WcException.class, () -> wcApplication.countFromFiles(
                true, true, true, null));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromFiles. It is done after the function call.
    @Test
    void countFromFiles_EmptyFile_ReturnsAllZeros() throws Exception {
        String[] fileNames = new String[]{"file1.txt"};
        String testFileContent = "";
        createFile("file1.txt", testFileContent);
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 0, 0, 0) + " file1.txt";

        String result = wcApplication.countFromFiles(true, true, true, fileNames);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromFiles. It is done after the function call.
    @Test
    void countFromFiles_SingleFileWithSingleLineText_ReturnsLinesWordsBytesCount() throws Exception {
        String[] fileNames = new String[]{"file2.txt"};
        createFile("file2.txt", SINGLE_LINE_TEXT);
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 0, 4, BYTECOUNT_SINGLE) + " file2.txt";

        String result = wcApplication.countFromFiles(true, true, true, fileNames);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromFiles. It is done after the function call.
    @Test
    void countFromFiles_SingleFileWithSingleLineTextBytesOption_ReturnBytesCount() throws Exception {
        String[] fileNames = new String[]{"file3.txt"};
        createFile("file3.txt", SINGLE_LINE_TEXT);
        String expected = String.format(NUMBER_FORMAT, BYTECOUNT_SINGLE) + " file3.txt";

        String result = wcApplication.countFromFiles(true, false, false, fileNames);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromFiles. It is done after the function call.
    @Test
    void countFromFiles_SingleFileWithSingleLineTextLinesOption_ReturnLinesCount() throws Exception {
        String[] fileNames = new String[]{"file4.txt"};
        createFile("file4.txt", SINGLE_LINE_TEXT);
        String expected = String.format(NUMBER_FORMAT, 0) + " file4.txt";

        String result = wcApplication.countFromFiles(false, true, false, fileNames);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromFiles. It is done after the function call.
    @Test
    void countFromFiles_SingleFileWithSingleLineTextWordsOption_ReturnWordsCount() throws Exception {
        String[] fileNames = new String[]{"file5.txt"};
        createFile("file5.txt", SINGLE_LINE_TEXT);
        String expected = String.format(NUMBER_FORMAT, 4) + " file5.txt";

        String result = wcApplication.countFromFiles(false, false, true, fileNames);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromStdin. It is done after the function call.
    @Test
    void countFromStdin_EmptyFile_ReturnsAllZeros() throws Exception {
        String testFileContent = "";
        InputStream stdin = new ByteArrayInputStream(testFileContent.getBytes());
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 0, 0, 0);

        String result = wcApplication.countFromStdin(true, true, true, stdin);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromStdin. It is done after the function call.
    @Test
    void countFromStdin_SingleFileWithSingleLineText_ReturnsLinesWordsBytesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 0, 4, BYTECOUNT_SINGLE);

        String result = wcApplication.countFromStdin(true, true, true, stdin);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromStdin. It is done after the function call.
    @Test
    void countFromStdin_SingleFileWithSingleLineTextBytesOption_ReturnBytesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = String.format(NUMBER_FORMAT, BYTECOUNT_SINGLE).stripTrailing();

        String result = wcApplication.countFromStdin(true, false, false, stdin);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended in countFromStdin. It is done after the function call.
    @Test
    void countFromStdin_SingleFileWithSingleLineTextLinesOption_ReturnLinesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = String.format(NUMBER_FORMAT, 0);

        String result = wcApplication.countFromStdin(false, true, false, stdin);

        assertEquals(expected, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended as the SINGLE_LINE_TEXT does not end with a new line.
    // The output formatting has been changed to suit the behaviour of our application.
    @Test
    void countFromStdin_SingleFileWithSingleLineTextWordsOption_ReturnWordsCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = String.format(NUMBER_FORMAT, 4);

        String result = wcApplication.countFromStdin(false, false, true, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextAndStdin_ReturnsLinesWordsBytesTotalCount() throws Exception {

        createFile("file13.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file13.txt", STDIN_FILENAME};
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 0, 4, BYTECOUNT_SINGLE) + " file13.txt" + STRING_NEWLINE +
                String.format(NUMBER_FORMAT_TRIPLE, 1, 9, BYTECOUNT_MULTI) + " " + STDIN_FILENAME + STRING_NEWLINE +
                String.format(NUMBER_FORMAT_TRIPLE, 1, 13, BYTESUM_SINGLE) + " " + LABEL_TOTAL;

        String result = wcApplication.countFromFileAndStdin(true, true, true, stdin, fileNames);

        assertEquals(expected + STRING_NEWLINE, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended as the SINGLE_LINE_TEXT does not end with a new line.
    // The output formatting has been changed to suit the behaviour of our application.
    @Test
    void countFromFileAndStdin_StdinAndSingleFileWithSingleLineText_ReturnsLinesWordsBytesTotalCount() throws Exception {
        createFile("file14.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{STDIN_FILENAME, "file14.txt"};
        String test = String.format(NUMBER_FORMAT_TRIPLE, 1, 9, BYTECOUNT_MULTI);
        String expected = String.format(NUMBER_FORMAT_TRIPLE, 1, 9, BYTECOUNT_MULTI) +  " " + STDIN_FILENAME + STRING_NEWLINE +
                String.format(NUMBER_FORMAT_TRIPLE, 0, 4, BYTECOUNT_SINGLE) + " file14.txt" + STRING_NEWLINE +
                String.format(NUMBER_FORMAT_TRIPLE, 1, 13, BYTESUM_SINGLE) + " " + LABEL_TOTAL;

        String result = wcApplication.countFromFileAndStdin(true, true, true, stdin, fileNames);

        assertEquals(expected + STRING_NEWLINE, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextBytesOptionAndStdin_ReturnBytesTotalCount() throws Exception {

        createFile("file15.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file15.txt", STDIN_FILENAME};
        String expected = String.format(NUMBER_FORMAT, BYTECOUNT_SINGLE) + " file15.txt" + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, BYTECOUNT_MULTI) + " " + STDIN_FILENAME + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, BYTESUM_SINGLE) + " " + LABEL_TOTAL;

        String result = wcApplication.countFromFileAndStdin(true, false, false, stdin, fileNames);

        assertEquals(expected + STRING_NEWLINE, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended as the SINGLE_LINE_TEXT does not end with a new line.
    // The output formatting has been changed to suit the behaviour of our application.
    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextLinesOptionAndStdin_ReturnLinesTotalCount() throws Exception {

        createFile("file16.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file16.txt", STDIN_FILENAME};
        String expected = String.format(NUMBER_FORMAT, 0) + " file16.txt" + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, 1) + " " + STDIN_FILENAME + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, 1) + " " + LABEL_TOTAL;

        String result = wcApplication.countFromFileAndStdin(false, true, false, stdin, fileNames);

        assertEquals(expected + STRING_NEWLINE, result);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended as the SINGLE_LINE_TEXT does not end with a new line.
    // The output formatting has been changed to suit the behaviour of our application.
    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextWordsOption_ReturnWordsTotalCount() throws Exception {
        createFile("file17.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file17.txt", STDIN_FILENAME};
        String expected = String.format(NUMBER_FORMAT, 4) + " file17.txt" + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, 9) + " " + STDIN_FILENAME + STRING_NEWLINE +
                String.format(NUMBER_FORMAT, 13) + " " + LABEL_TOTAL;

        String result = wcApplication.countFromFileAndStdin(false, false, true, stdin, fileNames);
        assertEquals(expected + STRING_NEWLINE, result);
    }
}