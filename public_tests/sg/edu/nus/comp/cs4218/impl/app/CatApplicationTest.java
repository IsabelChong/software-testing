package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.deleteFilesAndDirectoriesFrom;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.writeToFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.*;

import sg.edu.nus.comp.cs4218.exception.CatException;

/**
 * Bottom up order of units under test:
 * 1. validateFilePath(String file) throws CatException {
 * 2. addLineNumbers(List<String> lines)
 * 3. catOutput(List<String> input)
 * 4. catStdin(Boolean isLnumSpecified, InputStream stdin) throws CatException
 * 5. catFiles(Boolean isLnumSpecified, String... fileNames) throws CatException
 * 6. catFileAndStdin(Boolean isLnumSpecified, InputStream stdin, String... fileNames) throws CatException
 */

public class CatApplicationTest {
    private static final String NUMBER_FORMAT = "%6d";
    private static CatApplication catApp;
    private OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = System.getProperty("user.dir");;

    private static final String CAT_PREFIX = "cat: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String TEXT1_FILE = "text1";
    private static final String TEXT2_FILE = "text2";
    private static final String TEXT3_FILE = "text3";
    private static final String TEXT4_FILE = "text4";
    private static final String TEXTA_FILE = "a";
    private static final String TEXTB_FILE = "b";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final String LINE_FLAG = "-n";

    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for CatCommandTest
    public static final String CAT_TEXT1 = "Lorem Ipsum is simply dummy text of the printing and typesetting " +
            "industry.\nLorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown " +
            "printer took a galley of type and scrambled it to make a type specimen book.\n";
    public static final String CAT_TEXT2 = "Contrary to popular belief, Lorem Ipsum is not simply random text. " +
            "It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.\n";
    public static final String CAT_TEXT3 = "Lorem Ipsum is simply dummy text of the printing and typesetting " +
            "industry.\n\nLorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown " +
            "printer took a galley of type and scrambled it to make a type specimen book.\n\n\n";
    public static final String CAT_TEXT4 = "Contrary to popular belief, Lorem Ipsum is not simply random text. " +
            "It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.";

    public static final String CAT_TEXTA = "a";

    public static final String CAT_TEXTB = "b\n";
    private static Path text1File;
    private static Path text2File;
    private static Path text3File;
    private static Path text4File;
    private static Path textAFile;
    private static Path textBFile;
    private static Path noReadPermFile;

    @BeforeEach
    public void setUp() {
        catApp = new CatApplication();
        stdout = new ByteArrayOutputStream();

        // Make a test directory containing test files
        testDir = new File(CURR_DIR + fileSeparator() +
                "public_tests" + fileSeparator() +
                "tempSortTestDir");
        testDir.mkdir();

        try {
            // Create temporary files used across various tests
            text1File = Files.createTempFile(testDir.toPath(), TEXT1_FILE, TXT_POSTFIX);
            text2File = Files.createTempFile(testDir.toPath(), TEXT2_FILE, TXT_POSTFIX);
            text3File = Files.createTempFile(testDir.toPath(), TEXT3_FILE, TXT_POSTFIX);
            text4File = Files.createTempFile(testDir.toPath(), TEXT4_FILE, TXT_POSTFIX);
            textAFile = Files.createTempFile(testDir.toPath(), TEXTA_FILE, TXT_POSTFIX);
            textBFile = Files.createTempFile(testDir.toPath(), TEXTB_FILE, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{text1File, text2File, text3File, text4File, textAFile, textBFile},
                    new String[]{CAT_TEXT1, CAT_TEXT2, CAT_TEXT3, CAT_TEXT4, CAT_TEXTA, CAT_TEXTB});

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteFilesAndDirectoriesFrom(testDir);
        testDir.delete();
        stdout.flush();
    }

    /**
     * This tests if validateFilePath throws an exception if the file is non existent.
     */
    @Test
    public void validateFilePath_nonExistentFile_ShouldThrowFileNotFoundException() {
        String filePath = "iAmAFileWhichShouldNotExist";
        assertFalse(Files.exists(Paths.get(filePath)),
                "The file '" + filePath + "' should not exist for this test to work");
        Throwable exception = assertThrows(CatException.class, () -> catApp.validateFilePath(filePath));
        assertEquals(CAT_PREFIX + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * This tests if validateFilePath throws an exception if the path given is to a directory.
     */
    @Test
    public void validateFilePath_directoryInput_ShouldThrowDirException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(CatException.class, () ->
                catApp.validateFilePath(pathName));
        assertEquals(CAT_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests if validateFilePath throws an exception if the file exists but does not have read permissions.
     */
    @Test
    public void validateFilePath_fileWithNoReadPerms_ShouldThrowNoPermException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(CatException.class, () ->
                catApp.validateFilePath(pathName));
        assertEquals(CAT_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests if validateFilePath does nothing if the file path given is valid.
     */
    @Test
    public void validateFilePath_validPath_ShouldDoNothing() {
        String pathName = text1File.toFile().getPath();
        assertDoesNotThrow(() -> catApp.validateFilePath(pathName));
    }

    /**
     * This tests if line numbers are post-fixed beside each line correctly for a List<String>
     *     of size 1 with no new line appended at the back.
     */
    @Test
    public void addLineNumbers_oneLineWithoutNewLine_ShouldNumberOneLine() {
        List<String> lines = List.of(CAT_TEXT4);
        List<String> output = catApp.addLineNumbers(lines);
        assertEquals(List.of(String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT4), output);
    }

    /**
     * This tests if line numbers are post-fixed beside each line correctly for a List<String>
     *     of size 1 with a new line appended at the back.
     */
    @Test
    public void addLineNumbers_oneLineWithNewLine_ShouldNumberOneLine() {
        List<String> lines = List.of(CAT_TEXT2.split(STRING_NEWLINE));
        List<String> output = catApp.addLineNumbers(lines);
        assertEquals(List.of(String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT2.split(STRING_NEWLINE)[0]), output);
    }

    /**
     * This tests if line numbers are post-fixed beside each line correctly for a List<String> of size 2.
     */
    @Test
    public void addLineNumbers_twoLines_ShouldNumberTwoLines() {
        List<String> lines = List.of(CAT_TEXT1.split(STRING_NEWLINE));
        List<String> output = catApp.addLineNumbers(lines);
        assertEquals(output.size(), 2);
        assertEquals(List.of(String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT1.split(STRING_NEWLINE)[0],
                String.format(NUMBER_FORMAT, 2) + "\t" + CAT_TEXT1.split(STRING_NEWLINE)[1]), output);
    }

    /**
     * This tests if joining one line without a new line would return that line appended with a new line.
     */
    @Test
    public void catOutput_listOfOneLineWithoutNewLine_ShouldReturnOneStringWithNewLine() {
        List<String> lines = List.of(CAT_TEXT4.split(STRING_NEWLINE));
        String output = catApp.catOutput(lines);
        assertEquals(CAT_TEXT4 + STRING_NEWLINE, output);
    }

    /**
     * This tests if joining one line with a new line would return that line.
     */
    @Test
    public void catOutput_listOfOneLineWithNewLine_ShouldReturnOneString() {
        List<String> lines = List.of(CAT_TEXT2.split(STRING_NEWLINE));
        String output = catApp.catOutput(lines);
        assertEquals(CAT_TEXT2, output);
    }

    /**
     * This tests if joining multiple lines would join into one string separated by new lines correctly line.
     */
    @Test
    public void catOutput_listOfMultipleLines_ShouldReturnCorrectOneString() {
        List<String> lines = List.of(CAT_TEXT1.split(STRING_NEWLINE));
        String output = catApp.catOutput(lines);
        assertEquals(CAT_TEXT1, output);
    }

    /**
     * This tests if joining multiple lines ending with three new lines would join into one string separated by new
     * lines with only one new line at the back.
     */
    @Test
    public void catOutput_listOfLinesEndingWithThreeNewLines_ShouldReturnOneStringWithOneNewLineAtBack() {
        List<String> lines = List.of(CAT_TEXT3.split(STRING_NEWLINE));
        String output = catApp.catOutput(lines);
        int newlineCount = 0;
        int index = output.length() - 1;
        while (index >= 0 && output.charAt(index) == '\n') {
            newlineCount++;
            index--;
        }
        assertEquals(1, newlineCount);
        assertEquals(CAT_TEXT3, output + STRING_NEWLINE + STRING_NEWLINE);
    }

    /**
     * This tests for a CatException thrown when the input stream is null in catStdin.
     */
    @Test
    public void catStdin_nullInputStream_ShouldThrowCatException() {
        Throwable exception = assertThrows(CatException.class, () -> catApp.catStdin(true, null));
        assertEquals(CAT_PREFIX + ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * This tests that catStdin without the n flag provided (false for isLnumSpecified) would return the stdin.
     */
    @Test
    public void catStdin_stdinWithoutNFlag_ShouldReturnStdin() {
        try {
            String[] input = {CAT_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String output = catApp.catStdin(false, stdin);
            assertEquals(CAT_TEXT1, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catStdin with the n flag provided (true for isLnumSpecified) would return
     * the numbered stdin, split my new lines.
     */
    @Test
    public void catStdin_stdinWithNFlag_ShouldCorrectOutput() {
        try {
            String[] input = {CAT_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String output = catApp.catStdin(true, stdin);
            String[] splitText = CAT_TEXT1.split(STRING_NEWLINE);
            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + splitText[0] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 2) + "\t" + splitText[1] + STRING_NEWLINE, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles will return one file's content correctly if provided without flags.
     */
    @Test
    public void catFiles_catSingleFileWithOneLineWithNewLineEOF_ShouldReturnFileContents() {
        try {
            String[] args = {text2File.toFile().getPath()};
            String output = catApp.catFiles(false, args);
            assertEquals(CAT_TEXT2, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles will return one file's content (without new lines) correctly if provided without flags.
     */
    @Test
    public void catFiles_catSingleFileWithNoNewLineEOF_ShouldReturnCorrectOutput() {
        try {
            String[] args = {text4File.toFile().getPath()};
            String output = catApp.catFiles(false, args);
            assertEquals(CAT_TEXT4, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles will return one file's content (with multiple new lines) if provided without flags.
     */
    @Test
    public void catFiles_catSingleFileWithMultipleNewLines_ShouldReturnCorrectOutput() {
        try {
            String[] args = {text3File.toFile().getPath()};
            String output = catApp.catFiles(false, args);
            assertEquals(CAT_TEXT3, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles with the n flag provided (true for isLnumSpecified) would return
     * the numbered file content, split my new lines.
     */
    @Test
    public void catFiles_catSingleFileWithNFlag_ShouldReturnNumberedFileContents() {
        try {
            String[] args = {text1File.toFile().getPath()};
            String output = catApp.catFiles(true, args);
            String[] splitText1 = CAT_TEXT1.split(STRING_NEWLINE);
            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + splitText1[0] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 2) + "\t" + splitText1[1] + STRING_NEWLINE, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles with two supplied files for concatenation would concat correctly.
     */
    @Test
    public void catFiles_catMultipleFiles_ShouldReturnConcatFileContents() {
        try {
            String[] args = {text1File.toFile().getPath(), text2File.toFile().getPath()};
            String output = catApp.catFiles(false, args);
            assertEquals(CAT_TEXT1 + CAT_TEXT2, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFiles with two supplied files with the n flag provided (true for isLnumSpecified)
     * for concatenation would concat correctly with the correct numbered prefixes.
     */
    @Test
    public void catFiles_catMultipleFilesWithNFlag_ShouldReturnNumberedConcatFileContents() {
        try {
            String[] args = {text1File.toFile().getPath(), text2File.toFile().getPath()};
            String output = catApp.catFiles(true, args);
            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT1.split(STRING_NEWLINE)[0] +
                    STRING_NEWLINE + String.format(NUMBER_FORMAT, 2) + "\t" + CAT_TEXT1.split(STRING_NEWLINE)[1] +
                    STRING_NEWLINE + String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT2, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests for a CatException thrown when the input stream is null in catFileAndStdin.
     */
    @Test
    public void catFileAndStdin_nullInputStream_ShouldThrowCatException() {
        Throwable exception = assertThrows(CatException.class, () -> catApp.catFileAndStdin(true, null, ""));
        assertEquals(CAT_PREFIX + ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * This tests that catFileAndStdin will return one file's content correctly stdin ("-") is provided, but is
     * an empty string ("").
     */
    @Test
    public void catFileAndStdin_catEmptyStdinWithSingleFile_ShouldReturnCorrectOutput() {
        try {
            String[] input = {""};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());

            String[] args = {STRING_STDIN_DASH, text2File.toFile().getPath()};
            String output = catApp.catFileAndStdin(false, stdin, args);
            assertEquals(CAT_TEXT2, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFileAndStdin will cat stdin and one file's content in the correct order if stdin ("-") is
     * provided before the file argument.
     */
    @Test
    public void catFileAndStdin_catStdinFirstWithSingleFile_ShouldReturnCorrectOutput() {
        try {
            String input = "FIRST\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());

            String[] args = {STRING_STDIN_DASH, text2File.toFile().getPath()};
            String output = catApp.catFileAndStdin(false, stdin, args);
            assertEquals(input + CAT_TEXT2, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFileAndStdin will cat stdin and one file's content in the correct order if stdin ("-") is
     * provided after the file argument.
     */
    @Test
    public void catFileAndStdin_catStdinLastWithSingleFile_ShouldReturnCorrectOutput() {
        try {
            String input = "LAST\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());

            String[] args = {text2File.toFile().getPath(), STRING_STDIN_DASH};
            String output = catApp.catFileAndStdin(false, stdin, args);
            assertEquals(CAT_TEXT2 + input, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFileAndStdin will cat stdin and 2 files' content correctly.
     */
    @Test
    public void catFileAndStdin_catStdinFirstWithTwoFiles_ShouldReturnCorrectOutput() {
        try {
            String input = "FIRST\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());

            String[] args = {text2File.toFile().getPath(), text3File.toFile().getPath(), STRING_STDIN_DASH};
            String output = catApp.catFileAndStdin(false, stdin, args);
            assertEquals(CAT_TEXT2 + CAT_TEXT3 + input, output);
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that catFileAndStdin will cat stdin and files correctly with numbered lines.
     */
    @Test
    public void catFileAndStdin_catStdinFirstWithTwoFilesWithNFlag_ShouldReturnCorrectOutput() {
        try {
            String input = "FIRST\n";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());

            String[] args = {text2File.toFile().getPath(), text3File.toFile().getPath(), STRING_STDIN_DASH};
            String output = catApp.catFileAndStdin(true, stdin, args);
            String[] splitText1 = CAT_TEXT2.split(STRING_NEWLINE);
            String[] splitText2 = CAT_TEXT3.split(STRING_NEWLINE);

            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + splitText1[0] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 1) + "\t" + splitText2[0] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 2) + "\t" + splitText2[1] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 3) + "\t" + splitText2[2] + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 4) + "\t" + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 5) + "\t" + STRING_NEWLINE +
                    String.format(NUMBER_FORMAT, 1) + "\t" + input, output);
        } catch (CatException e) {
            fail();
        }
    }

    @Test
    public void catFileAndStdin_catFileWithStdinBetween_ShouldReturnCorrectOutput() {
        try {
            String input = "hello world\nhello mars";
            InputStream stdin = new ByteArrayInputStream(input.getBytes());

            String[] args = {textAFile.toFile().getPath(), STRING_STDIN_DASH, textBFile.toFile().getPath()};
            String output = catApp.catFileAndStdin(false, stdin, args);

            assertEquals("ahello world\nhello mars\nb\n", output);
        } catch (CatException e) {
            fail();
        }
    }
}
