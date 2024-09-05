package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("PMD")
public class UniqApplicationPublicTest {
    private static final File TEMP = new File("temp-uniq");
    private static final File NONEXISTENT = new File("uniq_nonexistent.txt");
    private static final File FILE_EMPTY = new File("uniq_empty.txt");
    private static final File OUTPUT = new File("output.txt");

    private static final File FILE_NO_ADJ_DUP = new File("uniq_no_duplicates.txt");
    private static final String TEST_NO_ADJ_DUP = "Hello World\nAlice\nBob\nHello World\nBob\nAlice\nCS4218";

    private static final File FILE_ALL_DUP = new File("uniq_all_duplicates.txt");
    private static final String TEST_ALL_DUP = "CS4218\n".repeat(50); // NOPMD

    private static final File FILE_MIXED_DUP = new File("uniq_interleaved_duplicates.txt");
    private static final String TEST_MIXED_DUP = "CS4218\n".repeat(10) + "CS1101S\n" + // NOPMD
            "CS4218\n".repeat(3) + "CS4218\n".repeat(3) + "CS1101S\n".repeat(20) + "CS4218\n".repeat(2);

    private static UniqApplication uniqApplication;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_NO_ADJ_DUP, TEST_NO_ADJ_DUP);
        writeToFileWithText(FILE_ALL_DUP, TEST_ALL_DUP);
        writeToFileWithText(FILE_MIXED_DUP, TEST_MIXED_DUP);

        TEMP.mkdirs();
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
        uniqApplication = new UniqApplication();
    }

    @AfterAll
    static void tearDownAfterAll() {
        FILE_EMPTY.delete();
        FILE_NO_ADJ_DUP.delete();
        FILE_ALL_DUP.delete();
        FILE_MIXED_DUP.delete();

        TEMP.delete();
        OUTPUT.delete();
    }

    @Test
    void uniqFromFile_EmptyFile_ReturnsEmpty() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, false, false, FILE_EMPTY.toString(), null);

            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesNoArguments_EqualToItself() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, false, false, FILE_NO_ADJ_DUP.toString(), null);

            assertEquals(TEST_NO_ADJ_DUP + "\n", result);
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesCountOnly_AllOneCounts() {
        assertDoesNotThrow(() -> {

            String expected = "   1 Hello World\n   1 Alice\n   " +
                    "1 Bob\n   1 Hello World\n   1 Bob\n   1 Alice\n   1 CS4218\n";
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_NO_ADJ_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesRepeatedOnly_ReturnsEmpty() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, true, false, FILE_NO_ADJ_DUP.toString(), null);

            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesAllRepeatedOnly_ReturnsEmpty() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, false, true, FILE_NO_ADJ_DUP.toString(), null);

            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesNoArguments_OnlyOneResult() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\n";
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesCountOnly_ReturnsCount() {
        assertDoesNotThrow(() -> {

            String expected = "   50 CS4218\n"; // NOPMD
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesRepeatedOnly_OnlyOneResult() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\n";
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesAllRepeatedOnly_ReturnsItself() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, false, true, FILE_ALL_DUP.toString(), null);

            assertEquals(TEST_ALL_DUP, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesCountAndRepeatedOnly_ReturnsCount() {
        assertDoesNotThrow(() -> {

            String expected = "   50 CS4218\n";
            String result = uniqApplication.uniqFromFile(true, true, false, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesCountAndAllRepeatedOnly_ReturnsCountRepeated() {
        assertDoesNotThrow(() -> {

            String expected = "   50 CS4218\n".repeat(50);
            String result = uniqApplication.uniqFromFile(true, false, true, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesRepeatedAndAllRepeatedOnly_ReturnsItself() {
        assertDoesNotThrow(() -> {

            String result = uniqApplication.uniqFromFile(false, true, true, FILE_ALL_DUP.toString(), null);

            assertEquals(TEST_ALL_DUP, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesAllArguments_ReturnsItself() {
        assertDoesNotThrow(() -> {

            String expected = "   50 CS4218\n".repeat(50);
            String result = uniqApplication.uniqFromFile(true, true, true, FILE_ALL_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesNoArguments_Success() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\nCS1101S\nCS4218\nCS1101S\nCS4218\n";
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_MIXED_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesCountOnly_Success() {
        assertDoesNotThrow(() -> {

            String expected = "   10 CS4218\n   1 CS1101S\n   6 CS4218\n   20 CS1101S\n   2 CS4218\n";
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_MIXED_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesRepeatedOnly_Success() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\nCS4218\nCS1101S\nCS4218\n";
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_MIXED_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesAllRepeatedOnly_Success() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\n".repeat(16) + "CS1101S\n".repeat(20) + "CS4218\n".repeat(2);
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_MIXED_DUP.toString(), null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_NonExistentFile_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.uniqFromFile(true, true, true,
                NONEXISTENT.toString(), null));
    }

    @Test
    void uniqFromFile_Directory_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.uniqFromFile(true, true, true,
                TEMP.toString(), null));
    }

    @Test
    void uniqFromStdIn_NullStream_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () ->
                uniqApplication.uniqFromStdin(false, false, false, null, null));

    }

    @Test
    void uniqFromStdIn_EmptyFile_ReturnsEmpty() {
        assertDoesNotThrow(() -> {

            InputStream stream = new ByteArrayInputStream("".getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);

            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromStdIn_NoAdjacentDuplicates_Success() {
        assertDoesNotThrow(() -> {

            InputStream stream = new ByteArrayInputStream(TEST_NO_ADJ_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);

            assertEquals(TEST_NO_ADJ_DUP + TestStringUtils.STRING_NEWLINE, result);
        });
    }

    @Test
    void uniqFromStdIn_AllDuplicates_Success() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\n";
            InputStream stream = new ByteArrayInputStream(TEST_ALL_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);

            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromStdIn_InterleavedDuplicates_Success() {
        assertDoesNotThrow(() -> {

            String expected = "CS4218\nCS1101S\nCS4218\nCS1101S\nCS4218\n";
            InputStream stream = new ByteArrayInputStream(TEST_MIXED_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, null);

            assertEquals(expected, result);
        });
    }

}
