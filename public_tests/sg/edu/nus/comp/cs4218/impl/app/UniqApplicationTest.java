package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.deleteFilesAndDirectoriesFrom;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.generateExpectedOutput;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.writeToFiles;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.fileSeparator;

/**
 * Unit tests:
 * 1. removeDuplicates(InputStream input, Boolean isCount) throws UniqException
 * 2. getDuplicatesOnOption(List<String> tokens, Boolean isRepeated, Boolean isAllRepeated) throws UniqException
 * 3. getInputOutputFiles(List<String> files) throws UniqException
 * 4. writeToOutputFile(String lines, String outputFile) throws UniqException
 * 5. uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String
 * outputFileName) throws UniqException
 * 6. uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String
 * outputFileName) throws UniqException
 */

public class UniqApplicationTest {
    private static UniqApplication uniqApp;
    private static OutputStream stdout;
    private static File testDir;
    private static final String CURR_DIR = System.getProperty("user.dir");

    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";
    private static final String TXT_POSTFIX = ".txt";
    private static final String NO_READ_PERM_FILE = "noPermissionFile";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");

    // Test files for UniqCommandTest
    public static final String UNIQ_TEXT1 = "Hello World\nHello World\nAlice\nAlice\nBob\nAlice\nBob\n";

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
            noReadPermFile = Files.createTempFile(testDir.toPath(), NO_READ_PERM_FILE, TXT_POSTFIX);
            Files.setPosixFilePermissions(noReadPermFile, NR_PERM);

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
     * Test case for the removeDuplicates method when there are no duplicate lines in the input.
     * The method should return a list with the same lines as the input.
     */
    @Test
    public void removeDuplicates_WithNoDuplicates_ReturnsSameList() throws UniqException {
        String input = "Hello\nWorld\nAlice\nBob\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        List<String> result = uniqApp.removeDuplicates(inputStream, false);
        assertEquals(4, result.size());
        assertEquals("Hello", result.get(0));
        assertEquals("World", result.get(1));
        assertEquals("Alice", result.get(2));
        assertEquals("Bob", result.get(3));
    }

    /**
     * Test case for the removeDuplicates method when there are duplicate lines in the input.
     * The method should return a list with duplicate lines removed.
     */
    @Test
    public void removeDuplicates_WithDuplicates_ReturnsListWithoutDuplicates() throws UniqException {
        String input = "Hello\nHello\nWorld\nWorld\nAlice\nAlice\nBob\nBob\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        List<String> result = uniqApp.removeDuplicates(inputStream, false);
        assertEquals(4, result.size());
        assertEquals("Hello", result.get(0));
        assertEquals("World", result.get(1));
        assertEquals("Alice", result.get(2));
        assertEquals("Bob", result.get(3));
    }

    /**
     * Test case for the removeDuplicates method when there are duplicate lines in the input and isCount is true.
     * The method should return a list with duplicate lines removed and the count of each line.
     */
    @Test
    public void removeDuplicates_WithCount_ReturnsListWithCount() throws UniqException {
        String input = "Hello\nHello\nWorld\nWorld\nAlice\nAlice\nBob\nBob\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        List<String> result = uniqApp.removeDuplicates(inputStream, true);
        assertEquals(4, result.size());
        assertEquals("2 Hello", result.get(0)); //NOPMD
        assertEquals("2 World", result.get(1)); //NOPMD
        assertEquals("2 Alice", result.get(2));
        assertEquals("2 Bob", result.get(3));
    }

    /**
     * Test case for the removeDuplicates method when the input is empty.
     * The method should return a list with one empty string.
     */
    @Test
    public void removeDuplicates_EmptyInput_ReturnsEmptyList() throws UniqException {
        String input = "";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        List<String> result = uniqApp.removeDuplicates(inputStream, false);
        assertEquals(1, result.size());
    }

    /**
     * Test case for the getDuplicatesOnOption method when there are no duplicate lines in the input.
     * The method should return an empty list.
     */
    @Test
    public void getDuplicatesOnOption_WithNoDuplicates_ReturnsEmptyList() throws UniqException {
        List<String> input = Arrays.asList("1 Hello", "1 World", "1 Alice", "1 Bob");
        List<String> result = UniqApplication.getDuplicatesOnOption(input, true, false);
        assertTrue(result.isEmpty());
    }

    /**
     * Test case for the getDuplicatesOnOption method when there are duplicate lines in the input.
     * The method should return a list with duplicate lines.
     */
    @Test
    public void getDuplicatesOnOption_WithDuplicatesAndIsRepeated_ReturnsListWithDuplicates() throws UniqException {
        List<String> input = Arrays.asList("2 Hello", "2 World", "1 Alice", "1 Bob");
        List<String> result = UniqApplication.getDuplicatesOnOption(input, true, false);
        assertEquals(2, result.size());
        assertEquals("2 Hello", result.get(0));
        assertEquals("2 World", result.get(1));
    }

    /**
     * Test case for the getDuplicatesOnOption method when there are duplicate lines in the input and isAllRepeated is true.
     * The method should return a list with all duplicate lines.
     */
    @Test
    public void getDuplicatesOnOption_WithDuplicatesAndIsAllRepeated_ReturnsListWithAllDuplicates() throws UniqException {
        List<String> input = Arrays.asList("2 Hello", "2 World", "1 Alice", "1 Bob");
        List<String> result = UniqApplication.getDuplicatesOnOption(input, false, true);
        assertEquals(4, result.size());
        assertEquals("2 Hello", result.get(0));
        assertEquals("2 Hello", result.get(1));
        assertEquals("2 World", result.get(2));
        assertEquals("2 World", result.get(3));
    }

    /**
     * Test case for the getDuplicatesOnOption method when the input is empty.
     * The method should return an empty list.
     */
    @Test
    public void getDuplicatesOnOption_WithEmptyInput_ReturnsEmptyList() throws UniqException {
        List<String> input = new ArrayList<>();
        List<String> result = UniqApplication.getDuplicatesOnOption(input, true, false);
        assertTrue(result.isEmpty());
    }

    /**
     * Test for getInputOutputFiles method when the list of files is empty.
     * It should return an array with two null elements.
     */
    @Test
    public void getInputOutputFiles_EmptyList_ReturnsNullArray() throws UniqException {
        List<String> files = new ArrayList<>();
        String[] result = uniqApp.getInputOutputFiles(files);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    /**
     * Test for getInputOutputFiles method when the list of files contains one file.
     * It should return an array with the input file name at index 0 and null at index 1.
     */
    @Test
    public void getInputOutputFiles_OneFile_ReturnsArrayWithInputFileAndNull() throws UniqException {
        List<String> files = Arrays.asList(FILE1);
        String[] result = uniqApp.getInputOutputFiles(files);
        assertEquals(FILE1, result[0]);
        assertNull(result[1]);
    }

    /**
     * Test for getInputOutputFiles method when the list of files contains two files.
     * It should return an array with the input file name at index 0 and the output file name at index 1.
     */
    @Test
    public void getInputOutputFiles_TwoFiles_ReturnsArrayWithInputAndOutputFiles() throws UniqException {
        List<String> files = Arrays.asList(FILE1, FILE2);
        String[] result = uniqApp.getInputOutputFiles(files);
        assertEquals(FILE1, result[0]);
        assertEquals(FILE2, result[1]);
    }

    /**
     * Test for getInputOutputFiles method when the list of files contains more than two files.
     * It should throw an UniqException.
     */
    @Test
    public void getInputOutputFiles_MoreThanTwoFiles_ThrowsUniqException() {
        List<String> files = Arrays.asList(FILE1, FILE2, "file3.txt");
        assertThrows(UniqException.class, () -> uniqApp.getInputOutputFiles(files));
    }

    /**
     * Test for writeToOutputFile method when the output file does not exist.
     * It should create the file and write the provided lines to it.
     */
    @Test
    public void writeToOutputFile_FileDoesNotExist_WritesToFile() throws UniqException, IOException {
        String lines = "Hello\nWorld\n"; //NOPMD
        String outputFile = "output.txt";
        uniqApp.writeToOutputFile(lines, outputFile);
        String result = new String(Files.readAllBytes(Paths.get(outputFile)));
        assertEquals(lines, result);
        Files.deleteIfExists(Paths.get(outputFile));
    }

    /**
     * Test for writeToOutputFile method when the output file already exists.
     * It should overwrite the file with the provided lines.
     */
    @Test
    public void writeToOutputFile_FileExists_OverwritesFile() throws UniqException, IOException {
        String lines = "Hello\nWorld\n";
        String outputFile = "output.txt";
        Files.createFile(Paths.get(outputFile));
        uniqApp.writeToOutputFile(lines, outputFile);
        String result = new String(Files.readAllBytes(Paths.get(outputFile)));
        assertEquals(lines, result);
        Files.deleteIfExists(Paths.get(outputFile));
    }

    /**
     * Test for writeToOutputFile method when the output file is a directory.
     * It should throw an UniqException.
     */
    @Test
    public void writeToOutputFile_OutputIsDirectory_ThrowsUniqException() throws IOException {
        String lines = "Hello\nWorld\n";
        String outputFile = "outputDir";
        Files.createDirectory(Paths.get(outputFile));
        assertThrows(UniqException.class, () -> uniqApp.writeToOutputFile(lines, outputFile));
        Files.deleteIfExists(Paths.get(outputFile));
    }

    /**
     * Test for writeToOutputFile method when the output file is not writable.
     * It should throw an UniqException.
     */
    @Test
    public void writeToOutputFile_FileNotWritable_ThrowsUniqException() throws IOException {
        String lines = "Hello\nWorld\n";
        String outputFile = "output.txt";
        Files.createFile(Paths.get(outputFile));
        Files.setPosixFilePermissions(Paths.get(outputFile), PosixFilePermissions.fromString("r--r--r--"));
        assertThrows(UniqException.class, () -> uniqApp.writeToOutputFile(lines, outputFile));
        Files.deleteIfExists(Paths.get(outputFile));
    }

    /**
     * Test for uniqFromFile method when the input file does not exist.
     * It should throw an UniqException.
     */
    @Test
    public void uniqFromFile_FileDoesNotExist_ThrowsUniqException() {
        assertThrows(UniqException.class, () -> uniqApp.uniqFromFile(false, false, false, "nonexistent.txt", null));
    }

    /**
     * Test for uniqFromFile method when the input file is a directory.
     * It should throw an UniqException.
     */
    @Test
    public void uniqFromFile_InputIsDirectory_ThrowsUniqException() throws IOException {
        String inputDir = "inputDir";
        Files.createDirectory(Paths.get(inputDir));
        assertThrows(UniqException.class, () -> uniqApp.uniqFromFile(false, false, false, inputDir, null));
        Files.deleteIfExists(Paths.get(inputDir));
    }

    /**
     * Test for uniqFromFile method when the input file is not readable.
     * It should throw an UniqException.
     */
    @Test
    public void uniqFromFile_FileNotReadable_ThrowsUniqException() throws IOException {
        assertThrows(UniqException.class, () -> uniqApp.uniqFromFile(false, false, false, noReadPermFile.toFile().getPath(), null));
    }

    /**
     * Test for uniqFromFile method when the input file is valid.
     * It should return the unique lines from the file.
     */
    @Test
    public void uniqFromFile_ValidFile_ReturnsUniqueLines() throws UniqException, IOException {
        String inputFile = "input.txt";
        String inputContent = "Hello\nHello\nWorld\nWorld\n";
        Files.write(Paths.get(inputFile), inputContent.getBytes());
        String result = uniqApp.uniqFromFile(false, false, false, inputFile, null);
        assertEquals("Hello\nWorld\n", result);
        Files.deleteIfExists(Paths.get(inputFile));
    }

    /**
     * Test for uniqFromStdin method when the input stream is null.
     * It should throw an UniqException.
     */
    @Test
    public void uniqFromStdin_NullInputStream_ThrowsUniqException() {
        assertThrows(UniqException.class, () -> uniqApp.uniqFromStdin(false, false, false, null, null));
    }

    /**
     * Test for uniqFromStdin method when the input stream is empty.
     * It should return an empty string.
     */
    @Test
    public void uniqFromStdin_EmptyInputStream_ReturnsEmptyString() throws UniqException {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        String result = uniqApp.uniqFromStdin(false, false, false, inputStream, null);
        assertEquals("\n", result);
    }

    /**
     * Test for uniqFromStdin method when the input stream contains duplicate lines.
     * It should return the unique lines.
     */
    @Test
    public void uniqFromStdin_DuplicateLines_ReturnsUniqueLines() throws UniqException {
        String input = "Hello\nHello\nWorld\nWorld\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String result = uniqApp.uniqFromStdin(false, false, false, inputStream, null);
        assertEquals("Hello\nWorld\n", result);
    }
}
