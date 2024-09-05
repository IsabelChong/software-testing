package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// PMD suppressed for TDD testcases given
@SuppressWarnings("PMD")
public class PasteApplicationPublicTest {
    private static final File DIRECTORY = new File("pasteTestDirectory");
    private static final File NONEXISTENT = new File("paste_nonexistent.txt");
    private static final File FILE_EMPTY = new File("paste_empty.txt");

    private static final File FILE_1 = new File("paste_1.txt");
    private static final String TEXT_FILE_1 = "A\nB\nC\nD\nE";

    private static final File FILE_2 = new File("paste_2.txt");
    private static final String TEXT_FILE_2 = "1\n2\n3\n4\n5";
    private static final String ERR_IS_DIR = String.format("paste: %s: Is a directory", DIRECTORY);
    private static final String ERR_NO_SUCH_FILE = String.format("paste: %s: No such file or directory", NONEXISTENT);

    private static PasteApplication pasteApplication;

    private void assertEqualsReplacingNewlines(String expected, String actual) {
        assertEquals(expected.replaceAll("\r\n", "\n"), actual.replaceAll("\r\n", "\n"));
    }

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_1, TEXT_FILE_1);
        writeToFileWithText(FILE_2, TEXT_FILE_2);

        DIRECTORY.mkdirs();
    }

    public static void writeToFileWithText(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file); //NOPMD

        if (text == null || text.isBlank()) {
            writer.close();
            return;
        }

        writer.write(text);
        writer.close();
    }

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication();
    }

    @AfterAll
    static void tearDownAfterAll() {
        FILE_EMPTY.delete();
        FILE_1.delete();
        FILE_2.delete();

        DIRECTORY.delete();
    }

    @Test
    void mergeFile_FileNotFound_ThrowsException() throws AbstractApplicationException {
        assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, NONEXISTENT.toString()));
    }

    // Bash does not throw an error when pasting to directory, this testcase assume that it does
    @Test
    void mergeFile_FileIsDirectory_ThrowsException() throws AbstractApplicationException {
        assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, DIRECTORY.toString()));
    }

    @Test
    void mergeFileAndStdin_NullInputStream_ThrowsException() {
        assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(true, null));
    }

    @Test
    void mergeFileAndStdin_NullFilename_ThrowsException() {
        assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(true, System.in, null));
    }

    @Test
    void mergeStdin_NullStream_ThrowsException() {
        assertThrows(PasteException.class, () -> pasteApplication.mergeStdin(true, null));
    }

    @Test
    void mergeStdin_NoSerial_ReturnsItself() throws AbstractApplicationException {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());
        String result = pasteApplication.mergeStdin(false, stream);
        assertEquals(TEXT_FILE_1, result);
    }

    @Test
    void mergeStdin_Serial_ReturnsNewlinesReplacedByTabs() throws AbstractApplicationException {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());
        String result = pasteApplication.mergeStdin(true, stream);
        assertEquals(TEXT_FILE_1.replaceAll(TestStringUtils.STRING_NEWLINE, String.valueOf(TestStringUtils.CHAR_TAB)), result);
    }

    @Test
    void mergeFile_NullFilename_ThrowsException() {
        assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, null));
    }

    @Test
    void mergeFile_NoSerialOneFile_ReturnsItself() throws AbstractApplicationException {
        String result = pasteApplication.mergeFile(false, FILE_1.toString());
        assertEqualsReplacingNewlines(TEXT_FILE_1, result);
    }


    @Test
    void mergeFile_NoSerialTwoFiles_ReturnsInterleaving() throws AbstractApplicationException {
        String expected = "A\t1\nB\t2\nC\t3\nD\t4\nE\t5";
        String result = pasteApplication.mergeFile(false, FILE_1.toString(), FILE_2.toString());
        assertEqualsReplacingNewlines(expected, result);
    }

    @Test
    void mergeFile_SerialTwoFiles_ReturnsParallel() throws AbstractApplicationException {
        String expected = "A\tB\tC\tD\tE\n1\t2\t3\t4\t5";
        String result = pasteApplication.mergeFile(true, FILE_1.toString(), FILE_2.toString());
        assertEqualsReplacingNewlines(expected, result);
    }
}
