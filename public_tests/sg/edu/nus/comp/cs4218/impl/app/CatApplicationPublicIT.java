package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD")
public class CatApplicationPublicIT {
    private static final String NUMBER_FORMAT = "%6d";
    private static final String TEMP = "temp-cat";
    private static final String DIR = "dir";
    public static final String ERR_IS_DIR = String.format("cat: %s: Is a directory", DIR);
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static final Deque<Path> files = new ArrayDeque<>();
    public static final String ERR_NO_SUCH_FILE = "cat: %s: No such file or directory";
    private static Path TEMP_PATH;
    private static Path DIR_PATH;

    private CatApplication catApplication;

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        String initialDir = TestEnvironmentUtil.getCurrentDirectory();
        TEMP_PATH = Paths.get(initialDir, TEMP);
        DIR_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + CHAR_FILE_SEP + DIR);
        Files.createDirectory(TEMP_PATH);
        Files.createDirectory(DIR_PATH);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(DIR_PATH);
        Files.delete(TEMP_PATH);
    }

    private void createFile(String name, String text) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        files.push(path);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            if (file.equals("-")) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    //catStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    // Numbered file formatting is different and hence changed to the expected behaviour.
    // There is a new line after the last stdin even if there is no new line typed.
    @Test
    void run_SingleStdinFlag_DisplaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = String.format(NUMBER_FORMAT, 1) + "\tTest line 1\n" +
                String.format(NUMBER_FORMAT, 2) + "\tTest line 2\n" +
                String.format(NUMBER_FORMAT, 3) + "\tTest line 3\n";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // Numbered file formatting is different and hence changed to the expected behaviour.
    // There is no extra new line found after "text".
    // Single stdin input differs from expected behaviour. There should be a new line after the last line even if
    // it does not end with a new line, as it is a stdin input.
    @Test
    void run_SingleStdinDashFlag_DisplaysNumberedStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = String.format(NUMBER_FORMAT, 1) + "\tTest line 1\n" +
                String.format(NUMBER_FORMAT, 2) + "\tTest line 2\n" +
                String.format(NUMBER_FORMAT, 3) + "\tTest line 3\n";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n", "-"), inputStream, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    //catFiles cases

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = TEXT_ONE;
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    // Numbered file formatting is different and hence changed to the expected behaviour.
    @Test
    void run_SingleFileFlag_DisplaysNumberedFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        String expectedText = String.format(NUMBER_FORMAT, 1) + "\tTest line 1\n" +
                String.format(NUMBER_FORMAT, 2) + "\tTest line 2\n" +
                String.format(NUMBER_FORMAT, 3) + "\tTest line 3";
        createFile(fileName, TEXT_ONE);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        createFile(fileName, TEXT_ONE);
        assertThrows(CatException.class, () -> catApplication.run(toArgs("a", fileName), System.in, output));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "expectedText".
    // Files ending with no new line should not have an additional new line added.
    @Test
    void run_MultipleFilesNoFlag_DisplaysCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = "Test line 1.1\nTest line 1.2\nTest line 1.3Test line 2.1\nTest line 2.2";
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    // Numbered file formatting is different and hence changed to the expected behaviour.
    // Files ending with no new line should not have an additional new line added.
    @Test
    void run_MultipleFilesFlag_DisplaysNumberedCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = String.format(NUMBER_FORMAT, 1) + "\tTest line 1.1\n" +
                String.format(NUMBER_FORMAT, 2) + "\tTest line 1.2\n" +
                String.format(NUMBER_FORMAT, 3) + "\tTest line 1.3" +
                // String.format(NUMBER_FORMAT, 3) + "\tTest line 1.3\n" +
                String.format(NUMBER_FORMAT, 1) + "\tTest line 2.1\n" +
                String.format(NUMBER_FORMAT, 2) + "\tTest line 2.2";
        createFile(fileName1, text1);
        createFile(fileName2, text2);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    // This test has been altered as it does not follow the expected behaviour of our application.
    // There is no extra new line found after "text".
    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((text), output.toString(StandardCharsets.UTF_8));
    }

    //catFilesAndStdin cases

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysCatStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String fileName = "fileN.txt";
        String fileText = "Test line 2.1\nTest line 2.2";
        createFile(fileName, fileText);
        String expectedText = "Test line 1.1\nTest line 1.2\nTest line 1.3\nTest line 2.1\nTest line 2.2";
        catApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((expectedText), output.toString(StandardCharsets.UTF_8));
    }
}

