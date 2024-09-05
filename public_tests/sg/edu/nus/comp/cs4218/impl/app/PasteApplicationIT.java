package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import sg.edu.nus.comp.cs4218.exception.PasteException;

// Method to test
// public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException

// Test case:
// Parallel mode
// - Run with null args
// - Run with null stdin
// - Run with null stdout
// - Run with null args, stdin, and stdout
// - Run with empty args
// - Run with empty args, stdin, and stdout
// - Run with empty args, null stdin, and null stdout
// - Run with single file
// - Run with multiple files
// - Run with multiple files and stdin
// - Run with multiple files and two "-" from stdin
// - Run with one "-" from stdin
// - Run with empty file
// - Run with unequal number of lines
// - Run with unequal number of lines and stdin
// - Run with unequal number of lines and two "-" from stdin
// - Run with non existent file
// - Run with non existent file and stdin
// - Run with non existent file and two "-" from stdin
// - Run with non existent file and one "-" from stdin
// - Run with permission denied file
// - Run with permission denied file and stdin
// - Run with permission denied file and two "-" from stdin

// Serial mode
// - Run with null args
// - Run with null stdin
// - Run with null stdout
// - Run with null args, stdin, and stdout
// - Run with empty args
// - Run with empty args, stdin, and stdout
// - Run with single file
// - Run with multiple files
// - Run with multiple files and stdin
// - Run with multiple files and two "-" from stdin
// - Run with one "-" from stdin
// - Run with empty file
// - Run with unequal number of lines
// - Run with unequal number of lines and stdin
// - Run with unequal number of lines and three "-" from stdin
// - Run with non existent file
// - Run with non existent file and stdin
// - Run with non existent file and two "-" from stdin
// - Run with permission denied file
// - Run with permission denied file and stdin
// - Run with permission denied file and two "-" from stdin

// Directory??

// serial with unreadeable file
// parallel with unreadeable file

public class PasteApplicationIT {

    public static final String PASTE_PREFIX = "paste: ";
    public static final String ERR_NULL_ARG = PASTE_PREFIX + ERR_NULL_ARGS;
    public static final String ERR_NULL_STREAM = PASTE_PREFIX + ERR_NULL_STREAMS;

    public static final String STDIN_STRING = "Hello world!" + STRING_NEWLINE
            + "Welcome to CS4218!" + STRING_NEWLINE;
    public static final String SERIAL_STDIN = "Hello world!\tWelcome to CS4218!\n";

    // Create 3 files of same length
    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";
    private static final String FILE3 = "file3.txt";
    private static final String FILE1_CONTENT = "1\n2\n3\n4\n5\n";
    private static final String FILE2_CONTENT = "a\nb\nc\nd\ne\n";
    private static final String FILE3_CONTENT = "A\nB\nC\nD\nE\n";

    // Create 2 files of different length
    private static final String FILE4 = "file4.txt";
    private static final String FILE5 = "file5.txt";
    private static final String FILE4_CONTENT = "AA\nBB\nCC\n";
    private static final String FILE5_CONTENT = "11\n22\n33\n44\n";

    // Empty File
    private static final String FILE_EMPTY = "file_empty.txt";

    // Non existent file
    private static final String NON_EXISTENT_FILE = "nonexistent.txt";

    // Unable to read file
    private static final String NO_READ_PERM_FILE = "noreadperm.txt";
    private static final String NRP_CONTENT = "you\ncant\nread\nme\n";

    // Temporary directory
    private static final String TEMP_DIR = "temp-paste";
    public static final String FILE1_SERIAL = "1\t2\t3\t4\t5\n";
    public static final String FILE2_SERIAL = "a\tb\tc\td\te\n";
    public static final String FILE3_SERIAL = "A\tB\tC\tD\tE\n";

    private PasteApplication pasteApp;

    private InputStream inputStream;
    private OutputStream outputStream;


    @BeforeAll
    static void setUpBeforeAll() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(FILE4));
        Files.deleteIfExists(Paths.get(FILE5));
        Files.deleteIfExists(Paths.get(FILE_EMPTY));
        Files.deleteIfExists(Paths.get(NON_EXISTENT_FILE));
        Files.deleteIfExists(Paths.get(NO_READ_PERM_FILE));
        Files.deleteIfExists(Paths.get(TEMP_DIR));

    }

    @BeforeEach
    void setUp() throws IOException {
        // Create files
        Files.createFile(Paths.get(FILE1));
        Files.createFile(Paths.get(FILE2));
        Files.createFile(Paths.get(FILE3));
        Files.createFile(Paths.get(FILE4));
        Files.createFile(Paths.get(FILE5));
        Files.createFile(Paths.get(FILE_EMPTY));
        Files.createFile(Paths.get(NO_READ_PERM_FILE));

        // Write content to files
        Files.write(Paths.get(FILE1), FILE1_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE2), FILE2_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE3), FILE3_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE4), FILE4_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(FILE5), FILE5_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(NO_READ_PERM_FILE), NRP_CONTENT.getBytes(StandardCharsets.UTF_8));

        // Set read permission to false
        File file = new File(NO_READ_PERM_FILE);
        file.setReadable(false);

        inputStream = new ByteArrayInputStream(STDIN_STRING.getBytes(StandardCharsets.UTF_8));
        outputStream = new ByteArrayOutputStream();

        pasteApp = new PasteApplication();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(FILE4));
        Files.deleteIfExists(Paths.get(FILE5));
        Files.deleteIfExists(Paths.get(FILE_EMPTY));
        Files.deleteIfExists(Paths.get(NON_EXISTENT_FILE));
        Files.deleteIfExists(Paths.get(NO_READ_PERM_FILE));
    }

    /**
     * Run with null args
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullArgs_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(null, inputStream, outputStream));
        assertEquals(ERR_NULL_ARG, exception.getMessage());
    }

    /**
     * Run with null stdin
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullStdin_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{}, null, outputStream));
        assertEquals(ERR_NULL_STREAM, exception.getMessage());
    }

    /**
     * Run with null stdout
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullStdout_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{}, inputStream, null));
        assertEquals(ERR_NULL_STREAM, exception.getMessage());
    }

    /**
     * Run with null args, stdin, and stdout
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullArgsStdinStdout_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(null, null, null));
        assertEquals(ERR_NULL_ARG, exception.getMessage());
    }

    /**
     * Run with empty args
     * Expected: Print stdin content.
     */
    @Test
    public void run_EmptyArgs_PrintNewLine() {
        try {
            pasteApp.run(new String[]{}, inputStream, outputStream);
            assertEquals(STDIN_STRING, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with empty args, stdin, and stdout
     * Expected: Print nothing
     */
    @Test
    public void run_EmptyArgsStdinStdout_PrintNothing() {
        try {
            pasteApp.run(new String[]{}, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), outputStream);
            assertEquals("", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with empty args, null stdin, and null stdout
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_EmptyArgsNullStdinNullStdout_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{}, null, null));
        assertEquals(ERR_NULL_STREAM, exception.getMessage());
    }

    /**
     * Run with single file
     * Expected: Print content of file.
     */
    @Test
    public void run_SingleFile_PrintFileContent() {
        try {
            pasteApp.run(new String[]{FILE1}, inputStream, outputStream);
            assertEquals(FILE1_CONTENT, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files
     * Expected: Print content of files in parallel.
     */
    @Test
    public void run_MultipleFiles_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE1, FILE2, FILE3}, inputStream, outputStream);
            assertEquals("1\ta\tA\n2\tb\tB\n3\tc\tC\n4\td\tD\n5\te\tE\n", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files and stdin
     * Expected: Print content of files in parallel and stdin.
     */
    @Test
    public void run_MultipleFilesStdin_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE1, FILE2, FILE3, "-"}, inputStream, outputStream);
            assertEquals("1\ta\tA\tHello world!\n2\tb\tB\tWelcome to CS4218!\n3\tc\tC\t\n4\td\tD\t\n5\te\tE\t\n",
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files and two "-" from stdin
     * Expected: Print content of files in parallel and two new lines.
     */
    @Test
    public void run_MultipleFilesTwoStdin_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE1, FILE2, FILE3, "-", "-"}, inputStream, outputStream);
            assertEquals(
                    "1\ta\tA\tHello world!\tWelcome to CS4218!\n" +
                            "2\tb\tB\t\t\n" +
                            "3\tc\tC\t\t\n" +
                            "4\td\tD\t\t\n" +
                            "5\te\tE\t\t\n",
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with one "-" from stdin
     * Expected: Print content of files in parallel and one new line.
     */
    @Test
    public void run_MultipleFilesOneStdin_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE1, FILE2, FILE3, "-"}, inputStream, outputStream);
            assertEquals(
                    "1\ta\tA\tHello world!\n" +
                            "2\tb\tB\tWelcome to CS4218!\n" +
                            "3\tc\tC\t\n" +
                            "4\td\tD\t\n" +
                            "5\te\tE\t\n",
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with empty file
     * Expected: Print nothing.
     */
    @Test
    public void run_EmptyFile_PrintNothing() {
        try {
            pasteApp.run(new String[]{FILE_EMPTY}, inputStream, outputStream);
            assertEquals("", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines
     * Expected: Print content of files in parallel.
     */
    @Test
    public void run_UnequalLines_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE4, FILE5}, inputStream, outputStream);
            assertEquals("AA\t11\nBB\t22\nCC\t33\n\t44\n", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines and stdin
     * Expected: Print content of files in parallel and stdin.
     */
    @Test
    public void run_UnequalLinesStdin_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE4, FILE5, "-"}, inputStream, outputStream);
            assertEquals("AA\t11\tHello world!\nBB\t22\tWelcome to CS4218!\nCC\t33\t\n\t44\t\n", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines and two "-" from stdin
     * Expected: Print content of files in parallel and two new lines.
     */
    @Test
    public void run_UnequalLinesTwoStdin_PrintFilesContentParallel() {
        try {
            pasteApp.run(new String[]{FILE4, FILE5, "-", "-"}, inputStream, outputStream);
            assertEquals("AA\t11\tHello world!\tWelcome to CS4218!\nBB\t22\t\t\nCC\t33\t\t\n\t44\t\t\n", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with non existent file
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFile_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NON_EXISTENT_FILE}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with non existent file and stdin
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFileStdin_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NON_EXISTENT_FILE, "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with non existent file and two "-" from stdin
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFileTwoStdin_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NON_EXISTENT_FILE, "-", "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with permission denied file
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFile_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NO_READ_PERM_FILE}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Run with permission denied file and stdin
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFileStdin_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NO_READ_PERM_FILE, "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Run with permission denied file and two "-" from stdin
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFileTwoStdin_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{NO_READ_PERM_FILE, "-", "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Run with null stdin in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullStdinSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s"}, null, outputStream));
        assertEquals(ERR_NULL_STREAM, exception.getMessage());
    }

    /**
     * Run with null stdout in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullStdoutSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s"}, inputStream, null));
        assertEquals(ERR_NULL_STREAM, exception.getMessage());
    }

    /**
     * Run with null args in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullArgsSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(null, inputStream, outputStream));
        assertEquals(ERR_NULL_ARG, exception.getMessage());
    }

    /**
     * Run with null args, stdin, and stdout in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NullArgsStdinStdoutSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(null, null, null));
        assertEquals(ERR_NULL_ARG, exception.getMessage());
    }

    /**
     * Run with empty args in serial mode
     * Expected: Print stdin content.
     */
    @Test
    public void run_EmptyArgsSerial_PrintStdin() {
        try {
            pasteApp.run(new String[]{"-s"}, inputStream, outputStream);
            assertEquals(SERIAL_STDIN, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with single file in serial mode
     * Expected: Print content of file.
     */
    @Test
    public void run_SingleFileSerial_PrintFileContent() {
        try {
            pasteApp.run(new String[]{"-s", FILE1}, inputStream, outputStream);
            assertEquals(FILE1_SERIAL, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files in serial mode
     * Expected: Print content of files in serial.
     */
    @Test
    public void run_MultipleFilesSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE1, FILE2, FILE3}, inputStream, outputStream);
            assertEquals(FILE1_SERIAL +
                    FILE2_SERIAL +
                    FILE3_SERIAL, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files and stdin in serial mode
     * Expected: Print content of files in serial and stdin.
     */
    @Test
    public void run_MultipleFilesStdinSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE1, FILE2, FILE3, "-"}, inputStream, outputStream);
            assertEquals(FILE1_SERIAL +
                            FILE2_SERIAL +
                            FILE3_SERIAL +
                            SERIAL_STDIN,
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with multiple files and two "-" from stdin in serial mode
     * Expected: Print content of files in serial and two new lines.
     */
    @Test
    public void run_MultipleFilesTwoStdinSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE1, FILE2, FILE3, "-", "-"}, inputStream, outputStream);
            assertEquals(FILE1_SERIAL +
                            FILE2_SERIAL +
                            FILE3_SERIAL +
                            SERIAL_STDIN,
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with one "-" from stdin in serial mode
     * Expected: Print content of files in serial and one new line.
     */
    @Test
    public void run_MultipleFilesOneStdinSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE1, FILE2, FILE3, "-"}, inputStream, outputStream);
            assertEquals(FILE1_SERIAL +
                            FILE2_SERIAL +
                            FILE3_SERIAL +
                            SERIAL_STDIN,
                    outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with empty file in serial mode
     * Expected: Print nothing.
     */
    @Test
    public void run_EmptyFileSerial_PrintNothing() {
        try {
            pasteApp.run(new String[]{"-s", FILE_EMPTY}, inputStream, outputStream);
            assertEquals("", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines in serial mode
     * Expected: Print content of files in serial.
     */
    @Test
    public void run_UnequalLinesSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE4, FILE5}, inputStream, outputStream);
            assertEquals("AA\tBB\tCC\n" +
                    "11\t22\t33\t44\n", outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines and stdin in serial mode
     * Expected: Print content of files in serial and stdin.
     */
    @Test
    public void run_UnequalLinesStdinSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE4, FILE5, "-"}, inputStream, outputStream);
            assertEquals("AA\tBB\tCC\n" +
                    "11\t22\t33\t44\n" +
                    SERIAL_STDIN, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with unequal number of lines and three "-" from stdin in serial mode
     * Expected: Print content of files in serial and one new line only.
     */
    @Test
    public void run_UnequalLinesThreeStdinSerial_PrintFilesContentSerial() {
        try {
            pasteApp.run(new String[]{"-s", FILE4, FILE5, "-", "-", "-"}, inputStream, outputStream);
            assertEquals("AA\tBB\tCC\n" +
                    "11\t22\t33\t44\n" +
                    SERIAL_STDIN, outputStream.toString());
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Run with non existent file in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFileSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NON_EXISTENT_FILE}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with non existent file and stdin in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFileStdinSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NON_EXISTENT_FILE, "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with non existent file and two "-" from stdin in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NonExistentFileTwoStdinSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NON_EXISTENT_FILE, "-", "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Run with permission denied file in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFileSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NO_READ_PERM_FILE}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Run with permission denied file and stdin in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFileStdinSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NO_READ_PERM_FILE, "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Run with permission denied file and two "-" from stdin in serial mode
     * Expected: Throw PasteException with error message.
     */
    @Test
    public void run_NoReadPermFileTwoStdinSerial_ThrowException() {
        Throwable exception = assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"-s", NO_READ_PERM_FILE, "-", "-"}, inputStream, outputStream));
        assertEquals(PASTE_PREFIX + NO_READ_PERM_FILE + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // Directory testcases not added yet
}
