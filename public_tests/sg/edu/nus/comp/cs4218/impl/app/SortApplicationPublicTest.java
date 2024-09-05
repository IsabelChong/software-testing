package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
class SortApplicationPublicTest {
    private static final String TEMP = "temp-sort";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final String TEST_FILE = "file.txt";
    private static SortApplication sortApplication;
    private static final Deque<Path> files = new ArrayDeque<>();
    private static String initialDir;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(TestStringUtils.STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
        sortApplication = new SortApplication();
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

    private void createFile(String content) throws IOException {
        Path path = TEMP_PATH.resolve(SortApplicationPublicTest.TEST_FILE);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        files.push(path);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromStdin. It is added after the function call.
    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("a", "b", "c");
        String actual = sortApplication.sortFromStdin(false, false, false, stdin);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromStdin. It is added after the function call.
    @Test
    void sortFromStdin_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("10 b", "5 c", "1 a");
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b");
        String actual = sortApplication.sortFromStdin(true, false, false, stdin);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromStdin. It is added after the function call.
    @Test
    void sortFromStdin_ReverseOrder_ReverseSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("c", "b", "a");
        String actual = sortApplication.sortFromStdin(false, true, false, stdin);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromFiles. It is added after the function call.
    @Test
    void sortFromStdin_CaseIndependent_CaseIndependentSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("A", "C", "b");
        String expected = joinStringsByLineSeparator("A", "b", "C");
        String actual = sortApplication.sortFromStdin(false, false, true, stdin);
        assertEquals(expected, actual);
    }

    // File

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromFiles. It is added after the function call.
    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("a", "b", "c");
        String actual = sortApplication.sortFromFiles(false, false,
                false, TEST_FILE);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromFiles. It is added after the function call.
    @Test
    void sortFromFiles_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("10 b", "5 c", "1 a"));
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b");
        String actual = sortApplication.sortFromFiles(true, false,
                false, TEST_FILE);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromFiles. It is added after the function call.
    @Test
    void sortFromFiles_ReverseOrder_ReverseSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("c", "b", "a");
        String actual = sortApplication.sortFromFiles(false, true,
                false, TEST_FILE);
        assertEquals(expected, actual);
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no newline appended after the sorting done in sortFromFiles. It is added after the function call.
    @Test
    void sortFromFiles_CaseIndependent_CaseIndependentSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("A", "C", "b"));
        String expected = joinStringsByLineSeparator("A", "b", "C");
        String actual = sortApplication.sortFromFiles(false, false,
                true, TEST_FILE);
        assertEquals(expected, actual);
    }
}