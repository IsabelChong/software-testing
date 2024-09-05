package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
class SortApplicationPublicIT {
    private static final String TEMP = "temp-sort";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final String TEST_FILE = "file.txt";
    private static final String NUMBER_FLAG = "-n";
    private static final String REVERSE_FLAG = "-r";
    private static final String CASE_INSENSITIVE_FLAG = "-f";
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
        Path path = TEMP_PATH.resolve(SortApplicationPublicIT.TEST_FILE);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        files.push(path);
    }

    @Test
    void sortFromStdin_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("10 b", "5 c", "1 a");
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b") + STRING_NEWLINE;
        String[] argList = new String[]{NUMBER_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, stdin, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromStdin_ReverseOrder_ReverseSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("c", "b", "a") + STRING_NEWLINE;
        String[] argList = new String[]{REVERSE_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, stdin, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromStdin_CaseIndependent_CaseIndependentSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("A", "C", "b");
        String expected = joinStringsByLineSeparator("A", "b", "C") + STRING_NEWLINE;
        String[] argList = new String[]{CASE_INSENSITIVE_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, stdin, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    // File

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("a", "b", "c") + STRING_NEWLINE;
        String[] argList = new String[]{TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, System.in, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("10 b", "5 c", "1 a"));
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b") + STRING_NEWLINE;
        String[] argList = new String[]{NUMBER_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, System.in, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_ReverseOrder_ReverseSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("c", "b", "a") + STRING_NEWLINE;
        String[] argList = new String[]{REVERSE_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, System.in, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_CaseIndependent_CaseIndependentSortedList() throws Exception {
        createFile(joinStringsByLineSeparator("A", "C", "b"));
        String expected = joinStringsByLineSeparator("A", "b", "C") + STRING_NEWLINE;
        String[] argList = new String[]{CASE_INSENSITIVE_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sortApplication.run(argList, System.in, output);
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }
}