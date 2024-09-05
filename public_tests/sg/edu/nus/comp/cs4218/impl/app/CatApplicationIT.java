package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.deleteFilesAndDirectoriesFrom;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.writeToFiles;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
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
import java.util.Set;

import org.junit.jupiter.api.*;

import sg.edu.nus.comp.cs4218.exception.CatException;

/**
 * Positive Cases:
 * 1. Using no flags and returning concatenated stdin.
 * 2. Having an empty stdin should keep waiting until non-empty stdin is given.
 * 2. Using the "-n" flag to add line numbers to each file.
 * 3. Supplying more than one file to be concatenated.
 * 4. Supplying a file ending with a new line should ignore that new line.
 * 5. Supplying a file ending with >1 new lines should ignore only 1 new line.
 * 6. Supplying a file not ending with a new line should not output a new line at the end.
 *      (shell prompt ">" will be on the RHS of output)
 *
 * Negative Cases:
 * 1. Providing a null output stream.
 * 2. Specifying a file that does not exist.
 * 3. Specifying a directory instead of a file.
 * 4. Providing empty input via stdin.
 */

public class CatApplicationIT {
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

    private static Path text1File;
    private static Path text2File;
    private static Path text3File;
    private static Path text4File;
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
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{text1File, text2File, text3File, text4File},
                    new String[]{CAT_TEXT1, CAT_TEXT2, CAT_TEXT3, CAT_TEXT4});

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

    /*************************** THROW EXCEPTION TESTING ***************************/
    /**
     * This tests for a CatException thrown when the input stream is null.
     */
    @Test
    public void run_nullInputStream_ShouldThrowCatException() {
        Throwable exception = assertThrows(CatException.class, () -> catApp.run(new String[]{"a"}, null, stdout));
        assertEquals(CAT_PREFIX + ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * This tests for a CatException thrown when the output stream is null.
     */
    @Test
    public void run_nullOutputStream_ShouldThrowCatException() {
        Throwable exception = assertThrows(CatException.class, () -> catApp.run(new String[]{"a"}, System.in, null));
        assertEquals(CAT_PREFIX + ERR_NULL_STREAMS, exception.getMessage());
    }

    /**
     * This tests for a CatException thrown when a directory input is given to sort.
     */
    @Test
    public void run_directoryInput_ShouldThrowCatException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(CatException.class, () ->
                catApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(CAT_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests for a CatException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_invalidFile_ShouldThrowCatException() {
        String invalidFile = "invalidFile.txt";
        String pathName = testDir + File.separator + invalidFile;

        assertFalse(Files.exists(Paths.get(pathName)),
                "The file '" + pathName + "' should not exist for this test to work");

        Throwable result = assertThrows(CatException.class, () ->
                catApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(CAT_PREFIX + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    /**
     * This tests for a CatException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_fileWithNoReadPerms_ShouldThrowCatException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(CatException.class, () ->
                catApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(CAT_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests for a CatException thrown when an illegal flag is given to the sort command.
     */
    @Test
    public void run_IllegalFlag_ShouldThrowSortException() {
        String illegalFlag = "b";
        Throwable result = assertThrows(CatException.class, () ->
                catApp.run(new String[]{CHAR_FLAG_PREFIX + illegalFlag}, System.in, stdout));
        assertEquals(CAT_PREFIX + ILLEGAL_FLAG_MSG + illegalFlag, result.getMessage());
    }

    /**
     * This tests that stdin would be returned to output.
     */
    @Test
    public void run_Stdin_ShouldReturnStdin() {
        try {
            String[] input = {"123456\n"};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String[] args = {};
            catApp.run(args, stdin, stdout);
            assertEquals("123456\n", stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that one file's content would be returned to output.
     */
    @Test
    public void run_catSingleFile_ShouldReturnFileContents() {
        try {
            String[] args = {text1File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            assertEquals(CAT_TEXT1, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that one file's content would be returned to output.
     */
    @Test
    public void run_catSingleFileWithOneLineWithNewLineEOF_ShouldReturnFileContents() {
        try {
            String[] args = {text2File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            assertEquals(CAT_TEXT2, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that one file's content with no new lines would be returned to output correctly.
     */
    @Test
    public void run_catSingleFileWithNoNewLineEOF_ShouldReturnCorrectOutput() {
        try {
            String[] args = {text4File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            assertEquals(CAT_TEXT4, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that one file's content with multiple lines would be returned to output.
     */
    @Test
    public void run_catSingleFileWithMultipleNewLines_ShouldReturnCorrectOutput() {
        try {
            String[] args = {text3File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            assertEquals(CAT_TEXT3, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that one file's content would be returned to output numbered when "-n" flag is supplied.
     */
    @Test
    public void run_catSingleFileWithNFlag_ShouldReturnNumberedFileContents() {
        try {
            String[] args = {LINE_FLAG, text1File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            String[] splitText = CAT_TEXT1.split("\n");
            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + splitText[0] + "\n" +
                    String.format(NUMBER_FORMAT, 2) + "\t" + splitText[1] + "\n", stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that two supplied files for concatenation would concat correctly.
     */
    @Test
    public void run_catMultipleFiles_ShouldReturnConcatFileContents() {
        try {
            String[] args = {text1File.toFile().getPath(), text2File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            assertEquals(CAT_TEXT1 + CAT_TEXT2, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

    /**
     * This tests that two supplied files for concatenation would concat correctly and be numbered when
     * the "-n" flag is supplied..
     */
    @Test
    public void run_catMultipleFilesWithNFlag_ShouldReturnNumberedConcatFileContents() {
        try {
            String[] args = {LINE_FLAG, text1File.toFile().getPath(), text2File.toFile().getPath()};
            catApp.run(args, System.in, stdout);
            String[] splitText = CAT_TEXT1.split(STRING_NEWLINE);
            assertEquals(String.format(NUMBER_FORMAT, 1) + "\t" + splitText[0] +
                    "\n" + String.format(NUMBER_FORMAT, 2) + "\t" + splitText[1] +
                    "\n" + String.format(NUMBER_FORMAT, 1) + "\t" + CAT_TEXT2, stdout.toString());
        } catch (CatException e) {
            fail();
        }
    }

}
