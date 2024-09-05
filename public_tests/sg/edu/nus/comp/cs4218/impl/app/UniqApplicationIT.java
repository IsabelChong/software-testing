package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.UniqException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.*;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;

/**
 * Negative Cases:
 * 1. Providing a null output stream.
 * 2. Specifying a file that does not exist.
 * 3. Specifying a directory instead of a file.
 * 4. Providing empty input via stdin.
 * 5. Providing wrong flag.
 * <p>
 * Positive Cases:
 * 1. Using no flags.
 * 2. Using the "-c" flag.
 * 3. Using the "-d" flag.
 */

public class UniqApplicationIT { //NOPMD
    private static UniqApplication uniqApp;
    private static OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = System.getProperty("user.dir");

    private static final String UNIQ_PREFIX = "uniq: ";
    private static final String TXT_POSTFIX = ".txt";
    private static final String TEXT1_FILE = "mixed";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for UniqCommandTest
    public static final String UNIQ_TEXT1 = "Hello World\nHello World\nAlice\nAlice\nBob\nAlice\nBob\n";

    private static Path text1File;
    private static Path noReadPermFile;

    @BeforeEach
    public void setUp() {
        uniqApp = new UniqApplication();
        stdout = new ByteArrayOutputStream();

        // Make a test directory containing test files
        testDir = new File(CURR_DIR + fileSeparator() +
                "public_tests" + fileSeparator() +
                "tempUniqTestDir");
        testDir.mkdir();

        try {
            // Create temporary files used across various tests
            text1File = Files.createTempFile(testDir.toPath(), UNIQ_TEXT1, TXT_POSTFIX);
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

            // Add content into files
            writeToFiles(new Path[]{text1File}, new String[]{UNIQ_TEXT1});
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
     * This tests for a UniqException thrown when the input stream is null.
     */
    @Test
    public void run_nullInputStream_ShouldThrowUniqException() {
        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{"a"}, null, stdout));
        assertEquals(UNIQ_PREFIX + ERR_NULL_STREAMS, result.getMessage());
    }

    /**
     * This tests for a UniqException thrown when the output stream is null.
     */
    @Test
    public void run_nullOutputStream_ShouldThrowUniqException() {
        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{"a"}, System.in, null));
        assertEquals(UNIQ_PREFIX + ERR_NULL_STREAMS, result.getMessage());
    }

    /**
     * This tests for a UniqException thrown when a directory input is given.
     */
    @Test
    public void run_DirectoryInput_ShouldThrowUniqException() {
        String pathName = testDir.getPath();
        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(UNIQ_PREFIX + ERR_IS_DIR, result.getMessage());
    }

    /**
     * This tests for a UniqException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_invalidFile_ShouldThrowUniqException() {
        String invalidFile = "invalidFile.txt";
        String pathName = testDir + File.separator + invalidFile;

        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(UNIQ_PREFIX + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    /**
     * This tests for a UniqException thrown when the file given is invalid, or does not exist.
     */
    @Test
    public void run_fileWithNoReadPerms_ShouldThrowCatException() {
        String pathName = noReadPermFile.toFile().getPath();
        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{pathName}, System.in, stdout));
        assertEquals(UNIQ_PREFIX + ERR_NO_PERM, result.getMessage());
    }

    /**
     * This tests for a UniqException thrown when an illegal flag is given.
     */
    @Test
    public void run_IllegalFlag_ShouldThrowUniqException() {
        String illegalFlag = "b";
        Throwable result = assertThrows(UniqException.class, () ->
                uniqApp.run(new String[]{CHAR_FLAG_PREFIX + illegalFlag}, System.in, stdout));
        assertEquals(UNIQ_PREFIX + ILLEGAL_FLAG_MSG + illegalFlag, result.getMessage());
    }

    /**
     * This tests if uniq is working correctly with no flags.
     */
    @Test
    public void run_uniqWithNoFlag_ShouldGiveCorrectOutput() {
        try {
            String[] args = new String[]{text1File.toFile().getPath()};
            uniqApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"Hello World", "Alice", "Bob", "Alice", "Bob"}); //NOPMD
            assertEquals(stdout.toString(), output);
        } catch (UniqException e) {
            fail();
        }
    }

    /**
     * This tests if uniq is working correctly with "-c" flags.
     */
    @Test
    public void run_uniqWithCFlag_ShouldGiveCorrectOutput() {
        try {
            String[] args = new String[]{"-c", text1File.toFile().getPath()};
            uniqApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"   2 Hello World", "   2 Alice", "   1 Bob",
                    "   1 Alice", "   1 Bob"});
            assertEquals(stdout.toString(), output);
        } catch (UniqException e) {
            fail();
        }
    }

    /**
     * This tests if uniq is working correctly with "-d" flag.
     */
    @Test
    public void run_uniqWithdFlag_ShouldGiveCorrectOutput() {
        try {
            String[] args = new String[]{"-d", text1File.toFile().getPath()};
            uniqApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"Hello World", "Alice"});
            assertEquals(stdout.toString(), output);
        } catch (UniqException e) {
            fail();
        }
    }

    /**
     * This tests if uniq is working correctly with "-D" flag.
     */
    @Test
    public void run_uniqWithDFlag_ShouldGiveCorrectOutput() {
        try {
            String[] args = new String[]{"-D", text1File.toFile().getPath()};
            uniqApp.run(args, System.in, stdout);
            String output = generateExpectedOutput(new String[]{"Hello World", "Hello World", "Alice", "Alice"});
            assertEquals(stdout.toString(), output);
        } catch (UniqException e) {
            fail();
        }
    }
}
