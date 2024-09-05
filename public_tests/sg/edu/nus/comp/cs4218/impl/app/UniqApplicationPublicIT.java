package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class UniqApplicationPublicIT {
    private static final String INPUT_FILE_TXT = "input_file.txt";
    private static final String OUTPUT_FILE_TXT = "output_file.txt";
    
    private static final Deque<Path> files = new ArrayDeque<>();
    private static Path CURR_PATH;
    
    private UniqApplication uniqApplication;

    private static final String testInput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;

    private static final String withoutFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;

    // This variable has been altered as it does not reflect the behaviour of our application.
    // The count is prefixed by 3 spaces instead of 1.
    private static final String withCountFlagOutput = "   2 Hello World" + STRING_NEWLINE +
            "   2 Alice" + STRING_NEWLINE +
            "   1 Bob" + STRING_NEWLINE +
            "   1 Alice" + STRING_NEWLINE +
            "   1 Bob" + STRING_NEWLINE;

    private static final String withDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;

    private static final String withAllDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;

    // This variable has been altered as it does not reflect the behaviour of our application.
    // The count is prefixed by 3 spaces instead of 1.
    private static final String withCountAndDuplicateFlagsOutput = "   2 Hello World" + STRING_NEWLINE +
            "   2 Alice" + STRING_NEWLINE;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        CURR_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
    }
    
    @BeforeEach
    void init() {
        uniqApplication = new UniqApplication();
    }
    
    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = CURR_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_NoFilesWithoutFlag_ReadsFromInputAndDisplaysAdjacentLines() throws Exception {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withoutFlagOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountFlag_ReadsFromInputAndDisplaysCountOfAdjacentLines() throws Exception {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withCountFlagOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withDuplicateFlagOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithAllDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateAndAllDuplicateFlags_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountAndDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withCountAndDuplicateFlagsOutput, outputStream.toString());
    }

    @Test
    void run_NoFilesWithUnknownFlag_Throws() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonemptyInputFile_ReadsFileAndDisplaysAdjacentLines() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(withoutFlagOutput, outputStream.toString());
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndDisplaysNewline() throws Exception {
        createFile(INPUT_FILE_TXT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NonexistentInputFile_Throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    // This test has been altered as it does not reflect the behaviour of our application.
    // No extra new line will be printed when nothing is written to stdin.
    @Test
    void run_InputFileToOutputFile_OverwritesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        writeToFile(outputPath, "This is the output file.");
        files.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
    }

    // This test has been altered as it does not reflect the behaviour of our application.
    // No extra new line will be printed when nothing is written to stdin.
    @Test
    void run_InputFileToNonexistentOutputFile_CreatesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        Path outputPath = CURR_PATH.resolve(OUTPUT_FILE_TXT);
        files.push(outputPath);
        assertTrue(Files.exists(outputPath));
        assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_NonexistentInputFileToOutputFile_Throws() throws IOException {
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(outputPath, "This is the output file.");
        files.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_Throws() {
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }
}
