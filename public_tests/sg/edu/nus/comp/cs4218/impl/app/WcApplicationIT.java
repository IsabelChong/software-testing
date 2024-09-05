package sg.edu.nus.comp.cs4218.impl.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.junit.jupiter.api.*;

import sg.edu.nus.comp.cs4218.exception.WcException;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.deleteFilesAndDirectoriesFrom;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.writeToFiles;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;

public class WcApplicationIT {
    private static WcApplication wcApp;
    private OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = System.getProperty("user.dir");;

    private static final String NUMBER_FORMAT = " %7d";
    private static final String WC_PREFIX = "wc: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String TEXT1_FILE = "text1";
    private static final String TEXT2_FILE = "text2";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for WcCommandTest
    public static final String WC_TEXT1 = "What is Lorem Ipsum?\n";
    public static final String WC_TEXT2 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer " +
            "took a galley of type and scrambled it to make a type specimen book.\n";

    private static Path text1File;
    private static Path text2File;
    private static Path noReadPermFile;

    @BeforeEach
    public void setUp() {
        wcApp = new WcApplication();
        stdout = new ByteArrayOutputStream();

        // Make a test directory containing test files
        testDir = new File(CURR_DIR + fileSeparator() +
                "public_tests" + fileSeparator() +
                "tempWcTestDir");
        testDir.mkdir();

        try {
            // Create temporary files used across various tests
            text1File = Files.createTempFile(testDir.toPath(), TEXT1_FILE, TXT_POSTFIX);
            text2File = Files.createTempFile(testDir.toPath(), TEXT2_FILE, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{text1File, text2File}, new String[]{WC_TEXT1, WC_TEXT2});

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
     * This tests for a WcException thrown when the input stream is null.
     */
    @Test
    public void run_nullInputStream_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{}, null, stdout));
        assertEquals(WC_PREFIX + ERR_NULL_STREAMS, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when the output stream is null.
     */
    @Test
    public void run_nullOutputStream_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{}, System.in, null));
        assertEquals(WC_PREFIX + ERR_NULL_ARGS, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when a directory input is given to wc.
     */
    @Test
    public void run_directoryInput_ShouldThrowWcException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(WC_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_invalidFile_ShouldThrowWcException() {
        String invalidFile = "invalidFile.txt";
        String pathName = testDir + File.separator + invalidFile;

        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(WC_PREFIX + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when the file given does not have read permissions.
     */
    @Test
    public void run_fileWithNoReadPerms_ShouldThrowWcException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(WC_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests for a WcException thrown when an illegal flag is given to the wc command.
     */
    @Test
    public void run_IllegalFlag_ShouldThrowWcException() {
        String illegalFlag = "a";
        Throwable result = assertThrows(WcException.class, () ->
                wcApp.run(new String[] {CHAR_FLAG_PREFIX + illegalFlag}, System.in, stdout));
        assertEquals(WC_PREFIX + ILLEGAL_FLAG_MSG + illegalFlag, result.getMessage());
    }

    /**
     * This tests if the contents are returned to output when only one file is given with no flags.
     */
    @Test
    public void run_SingleFileWithNoFlag_ShouldReturnLWB() {
        try {
            String[] args = new String[]{text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithLFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-l", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the words in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithWFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-w", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 4) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the bytes in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithCFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-c", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines and words in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithLWFlag_ShouldReturnLWOnly() {
        try {
            String[] args = {"-lw", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines and bytes in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithLCFlag_ShouldReturnWBOnly() {
        try {
            String[] args = {"-lc", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the words and bytes in the given file is outputted correctly.
     */
    @Test
    public void run_SingleFileWithWCFlag_ShouldReturnWBOnly() {
        try {
            String[] args = {"-wc", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the bytes and words in the given file is outputted correctly when supplied in different orders
     */
    @Test
    public void run_SingleFileWithCWFlag_ShouldReturnLWOnly() {
        try {
            String[] args = {"-cw", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines, words and bytes in the given file is outputted correctly when all flags are supplied.
     */
    @Test
    public void run_SingleFileWithLWCFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-lwc", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines, words and bytes in the given file is outputted correctly when all flags are supplied
     * in different orders.
     */
    @Test
    public void run_SingleFileWithCWLFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-cwl", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if the lines, words and bytes in the given file is outputted correctly when all flags are supplied
     * in different orders.
     */
    @Test
    public void run_SingleFileWithCLWFlag_ShouldReturnLOnly() {
        try {
            String[] args = {"-clw", text1File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) +
                    " " + text1File.toFile().getPath() + STRING_NEWLINE;
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc would be outputted correctly.
     */
    @Test
    public void run_MultipleFilesWithNoFlag_ShouldReturnIndividualLWBWithTotal() {
        try {
            String[] args = {text1File.toFile().getPath(), text2File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = (
                    String.format(NUMBER_FORMAT, 1) +
                            String.format(NUMBER_FORMAT, 4) +
                            String.format(NUMBER_FORMAT, 21) +
                            " " + text1File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 2) +
                            String.format(NUMBER_FORMAT, 43) +
                            String.format(NUMBER_FORMAT, 246) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 3) +
                            String.format(NUMBER_FORMAT, 47) +
                            String.format(NUMBER_FORMAT, 267) +
                            " " + "total" + STRING_NEWLINE
            );
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc with the '-l' flag supplied would be outputted correctly.
     */
    @Test
    public void run_MultipleFilesWithLFlag_ShouldReturnIndividualLWithTotalLOnly() {
        try {
            String[] args = {"-w", text1File.toFile().getPath(), text2File.toFile().getPath()};
            wcApp.run(args, System.in, stdout);
            String output = (
                    String.format(NUMBER_FORMAT, 4) +
                            " " + text1File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 43) +
                            " " + text2File.toFile().getPath() + STRING_NEWLINE +

                            String.format(NUMBER_FORMAT, 47) +
                            " " + "total" + STRING_NEWLINE
            );
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly.
     */
    @Test
    public void run_StdinNoFlag_ShouldReturnLWB() {
        try {
            String[] input = {WC_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String[] args = {};
            String output = String.format(NUMBER_FORMAT, 1) +
                    String.format(NUMBER_FORMAT, 4) +
                    String.format(NUMBER_FORMAT, 21) + STRING_NEWLINE;
            wcApp.run(args, stdin, stdout);
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly with the "-l" flag supplied.
     */
    @Test
    public void run_StdinLFlag_ShouldReturnLOnly() {
        try {
            String[] input = {WC_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String[] args = {"-l"};
            String output = String.format(NUMBER_FORMAT, 1) + STRING_NEWLINE;
            wcApp.run(args, stdin, stdout);
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly with the "-w" flag supplied.
     */
    @Test
    public void run_StdinWFlag_ShouldReturnWOnly() {
        try {
            String[] input = {WC_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String[] args = {"-w"};
            String output = String.format(NUMBER_FORMAT, 4) + STRING_NEWLINE;
            wcApp.run(args, stdin, stdout);
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }

    /**
     * This tests if multiple files given to wc using stdin would be outputted correctly with the "-c" flag supplied.
     */
    @Test
    public void run_StdinCFlag_ShouldReturnBOnly() {
        try {
            String[] input = {WC_TEXT1};
            String concatenatedInput = String.join("", input);
            InputStream stdin = new ByteArrayInputStream(concatenatedInput.getBytes());
            String[] args = {"-c"};
            String output = String.format(NUMBER_FORMAT, 21) + STRING_NEWLINE;
            wcApp.run(args, stdin, stdout);
            assertEquals(output, stdout.toString());
        } catch (WcException e) {
            fail();
        }
    }
}
