package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.PasteException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;


/**
 * Tests for PasteApplication
 * <p>
 * Positive test cases:
 * - Paste 1 file
 * - Paste 1 file with option
 * - Paste 2 files
 * - Paste 2 files with option
 * - Paste 3 files
 * - Paste 3 files with option
 * - Paste 2 files from stdin and file
 * - Paste 2 files from stdin and file with option
 * - Paste 3 files from stdin and file(s)
 * - Paste 3 files from stdin and file(s) with option
 * - Paste 2 files from stdin
 * - Paste 2 files from stdin with option
 * - Paste 3 files from stdin
 * - Paste 3 files from stdin with option
 * - Paste files with different number of lines
 * - Paste files with different number of lines with option
 * - Paste files with different number of lines from stdin
 * - (Not tested) Paste from different file types
 * - (Not tested) Paste from different file types with option
 * <p>
 * Negative test cases:
 * - Paste with one empty file and one non-empty file
 * - Paste with one empty file and one non-empty file with option
 * - Paste with two empty files and one non-empty file
 * - Paste with two empty files and one non-empty file with option
 * - Paste with one non-existing file
 * - Paste with empty stdin
 * - No read access
 * - Paste non-existing file
 * - Paste non-existing file with option
 * - Paste non-existing file from stdin
 * <p>
 * // Below this to discuss with team
 * - Paste with empty argument (supposed to show usage?)
 * - Paste with empty argument with option
 * - Paste with empty argument from stdin
 * - Paste with invalid option
 * - Paste with directory
 * - Paste with directory with option
 */

/**
 * Tests for PasteApplication
 */
public class PasteApplicationTest {

    // CONSTANTS
    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";
    private static final String FILE3 = "file3.txt";
    private static final String LONGER_FILE = "longerFile.txt";
    private static final String FILE1_CONTENT = "line1-file1\nline2-file1\nline3-file1";
    private static final String FILE2_CONTENT = "line1-file2\nline2-file2\nline3-file2";
    private static final String FILE3_CONTENT = "line1-file3\nline2-file3\nline3-file3";
    private static final String LONGER_CONTENT = "line1-longerFile\nline2-longerFile\nline3-longerFile\nline4-longerFile";
    private static final String INVALID_FILE = "invalidFile.txt";
    private static final String NO_PERM_TXT = "NoPermissionInput.txt";
    private static final String NO_PERM_O_TXT = "NoPermissionOutput.txt";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");
    private static final Set<PosixFilePermission> NW_PERM = PosixFilePermissions.fromString("r--r--r--");
    private static final String TEST_FOLDER = "TestFolder";

    // VARIABLES
    private PasteApplication pasteApplication;

    /**
     * Create files for testing
     */
    @BeforeEach
    public void setUp() throws IOException {
        pasteApplication = new PasteApplication();

        Files.createFile(Paths.get(FILE1));
        Files.createFile(Paths.get(FILE2));
        Files.createFile(Paths.get(FILE3));
        Files.writeString(Paths.get(FILE1), FILE1_CONTENT);
        Files.writeString(Paths.get(FILE2), FILE2_CONTENT);
        Files.writeString(Paths.get(FILE3), FILE3_CONTENT);
        Files.createFile(Paths.get(LONGER_FILE));
        Files.writeString(Paths.get(LONGER_FILE), LONGER_CONTENT);
        Files.createFile(Paths.get(NO_PERM_TXT)); // Ensure temp output file exists for tests
        Files.setPosixFilePermissions(Paths.get(NO_PERM_TXT), NR_PERM);
        Files.createFile(Paths.get(NO_PERM_O_TXT)); // Ensure temp output file exists for tests
        Files.setPosixFilePermissions(Paths.get(NO_PERM_O_TXT), NW_PERM);
        Files.createDirectory(Paths.get(TEST_FOLDER));
    }

    /**
     * Delete files after testing
     */
    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(LONGER_FILE));
        Files.deleteIfExists(Paths.get(NO_PERM_TXT));
        Files.deleteIfExists(Paths.get(NO_PERM_O_TXT));
        Files.deleteIfExists(Paths.get(TEST_FOLDER));
    }

    /**
     * Paste 2 files
     */
    @Test
    public void mergeFile_Paste2Files_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2};
        String expected = "line1-file1\tline1-file2\nline2-file1\tline2-file2\nline3-file1\tline3-file2";
        try {
            String actual = pasteApplication.mergeFile(false, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 1 file
     */
    @Test
    public void mergeFile_Paste1File_ReturnsPastedContent() {
        String[] args = {FILE1};
        String expected = "line1-file1\nline2-file1\nline3-file1";
        try {
            String actual = pasteApplication.mergeFile(false, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 1 file with option
     */
    @Test
    public void mergeFile_Paste1FileWithOption_ReturnsPastedContent() {
        String[] args = {FILE1};
        String expected = "line1-file1\tline2-file1\tline3-file1";
        try {
            String actual = pasteApplication.mergeFile(true, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 2 files with option
     */
    @Test
    public void mergeFile_Paste2FilesWithOption_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2};
        String expected = "line1-file1\tline2-file1\tline3-file1\nline1-file2\tline2-file2\tline3-file2";
        try {
            String actual = pasteApplication.mergeFile(true, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 3 files
     */
    @Test
    public void mergeFile_Paste3Files_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2, FILE3};
        String expected = "line1-file1\tline1-file2\tline1-file3\nline2-file1\tline2-file2\tline2-file3\nline3-file1\tline3-file2\tline3-file3";
        try {
            String actual = pasteApplication.mergeFile(false, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 3 files with option
     */
    @Test
    public void mergeFile_Paste3FilesWithOption_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2, FILE3};
        String expected = "line1-file1\tline2-file1\tline3-file1\nline1-file2\tline2-file2\tline3-file2\nline1-file3\tline2-file3\tline3-file3";
        try {
            String actual = pasteApplication.mergeFile(true, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 2 files from stdin and file
     */
    @Test
    public void mergeFileAndStdin_Paste2FilesFromStdinAndFile_ReturnsPastedContent() {
        String[] args = {FILE1, "-"};
        String expected = "line1-file1\tline1-stdin\nline2-file1\tline2-stdin\nline3-file1\tline3-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args); //NOPMD
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 2 files from stdin and file with option
     */
    @Test
    public void mergeFileAndStdin_Paste2FilesFromStdinAndFileWithOption_ReturnsPastedContent() {
        String[] args = {FILE1, "-"};
        String expected = "line1-file1\tline2-file1\tline3-file1\nline1-stdin\tline2-stdin\tline3-stdin";
        try {
        String actual = pasteApplication.mergeFileAndStdin(true, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
        assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 3 files from stdin and file(s)
     */
    @Test
    public void mergeFileAndStdin_Paste3FilesFromStdinAndFiles_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2, "-"};
        String expected = "line1-file1\tline1-file2\tline1-stdin\nline2-file1\tline2-file2\tline2-stdin\nline3-file1\tline3-file2\tline3-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 3 files from stdin and file(s) with option
     */
    @Test
    public void mergeFileAndStdin_Paste3FilesFromStdinAndFilesWithOption_ReturnsPastedContent() {
        String[] args = {FILE1, FILE2, "-"};
        String expected = "line1-file1\tline2-file1\tline3-file1\nline1-file2\tline2-file2\tline3-file2\nline1-stdin\tline2-stdin\tline3-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(true, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 2 files from stdin
     */
    @Test
    public void mergeFileAndStdin_Paste2FilesFromStdin_ReturnsPastedContent() {
        String[] args = {"-", "-"};
        String expected = "line1-stdin\tline2-stdin\n" +
                "line3-stdin\t";
        try {
            String actual = pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 2 files from stdin with option
     */
    @Test
    public void mergeFileAndStdin_Paste2FilesFromStdinWithOption_ReturnsPastedContent() {
        String[] args = {"-", "-"};
        String expected = "line1-stdin\tline2-stdin\tline3-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(true, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste 3 files from stdin with option
     */
    @Test
    public void mergeFileAndStdin_Paste3FilesFromStdinWithOption_ReturnsPastedContent() {
        String[] args = {"-", "-", "-"};
        String expected = "line1-stdin\tline2-stdin\tline3-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(true, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste files with different number of lines
     */
    @Test
    public void mergeFile_PasteFilesWithDifferentNumberOfLines_ReturnsPastedContent() {
        String[] args = {FILE1, LONGER_FILE};
        String expected = "line1-file1\tline1-longerFile\nline2-file1\tline2-longerFile\nline3-file1\tline3-longerFile\n\tline4-longerFile";
        try {
            String actual = pasteApplication.mergeFile(false, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste files with different number of lines with option
     */
    @Test
    public void mergeFile_PasteFilesWithDifferentNumberOfLinesWithOption_ReturnsPastedContent() {
        String[] args = {FILE1, LONGER_FILE};
        String expected = "line1-file1\tline2-file1\tline3-file1\nline1-longerFile\tline2-longerFile\tline3-longerFile\tline4-longerFile";
        try {
            String actual = pasteApplication.mergeFile(true, args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * Paste files with different number of lines from stdin
     */
    @Test
    public void mergeFileAndStdin_PasteFilesWithDifferentNumberOfLinesFromStdin_ReturnsPastedContent() {
        String[] args = {LONGER_FILE, "-"};
        String expected = "line1-longerFile\tline1-stdin\nline2-longerFile\tline2-stdin\nline3-longerFile\tline3-stdin\nline4-longerFile\tline4-stdin";
        try {
            String actual = pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\nline4-stdin\n".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }


    /**
     * Paste with one empty file and one non-empty file
     */
    @Test
    public void mergeFile_PasteWithOneEmptyFileAndOneNonEmptyFile_ShouldThrowPasteException() {
        String[] args = {FILE1, INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage()); //NOPMD
    }

    /**
     * Paste with one empty file and one non-empty file with option
     */
    @Test
    public void mergeFile_PasteWithOneEmptyFileAndOneNonEmptyFileWithOption_ShouldThrowPasteException() {
        String[] args = {FILE1, INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste with two empty files and one non-empty file
     */
    @Test
    public void mergeFile_PasteWithTwoEmptyFilesAndOneNonEmptyFile_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE, FILE1, FILE2};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste with two empty files and one non-empty file with option
     */
    @Test
    public void mergeFile_PasteWithTwoEmptyFilesAndOneNonEmptyFileWithOption_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE, FILE1, FILE2};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste with one non-existing file
     */
    @Test
    public void mergeFile_PasteWithOneNonExistingFile_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste with empty stdin
     */
    @Test
    public void mergeFileAndStdin_PasteWithEmptyStdin_ShouldOutput() {
        String[] args = {FILE1, "-"};
        String expected = "line1-file1\t\nline2-file1\t\nline3-file1\t";
        try {
            String actual = pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("".getBytes()), args);
            assertEquals(expected, actual);
        } catch (PasteException e) {
            fail(e);
        }
    }

    /**
     * No read access
     */
    @Test
    public void mergeFile_PasteWithNoReadAccess_ShouldThrowPasteException() {
        String[] args = {NO_PERM_TXT, FILE1};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + NO_PERM_TXT + ": Permission denied", exception.getMessage());
    }

    /**
     * Paste non-existing file
     */
    @Test
    public void mergeFile_PasteNonExistingFile_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste non-existing file with option
     */
    @Test
    public void mergeFile_PasteNonExistingFileWithOption_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste non-existing file from stdin
     * Not sure if this is possible since STDIN redirection is done by the shell and should
     * already throw an exception if the file does not exist
     */
    @Test
    public void mergeFileAndStdin_PasteNonExistingFileFromStdin_ShouldThrowPasteException() {
        String[] args = {INVALID_FILE};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFileAndStdin(false, new ByteArrayInputStream("line1-stdin\nline2-stdin\nline3-stdin\n".getBytes()), args));
        assertEquals("paste: " + INVALID_FILE + ": No such file or directory", exception.getMessage());
    }

    /**
     * Paste with directory
     */
    @Test
    public void mergeFile_PasteWithDirectory_ShouldThrowPasteException() {
        String[] args = {TEST_FOLDER};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(false, args));
        assertEquals("paste: " + TEST_FOLDER + ": This is a directory", exception.getMessage());
    }

    /**
     * Paste with directory with option
     */
    @Test
    public void mergeFile_PasteWithDirectoryWithOption_ShouldThrowPasteException() {
        String[] args = {TEST_FOLDER};
        Throwable exception = assertThrows(PasteException.class, () -> pasteApplication.mergeFile(true, args));
        assertEquals("paste: " + TEST_FOLDER + ": This is a directory", exception.getMessage());
    }
}
