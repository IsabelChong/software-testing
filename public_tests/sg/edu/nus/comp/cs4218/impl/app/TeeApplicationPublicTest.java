package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

// PMD suppressed for TDD testcases given
@SuppressWarnings("PMD")
public class TeeApplicationPublicTest {
    private static final String TEMP = "temp-tee";
    private static final String TEXT_A = "textA.txt";
    private static final String TEXT_B = "textB.txt";
    private static final String ONE_LINE_INPUT = "test 1";
    private static final String MULTI_LINE_INPUT = ONE_LINE_INPUT + TestStringUtils.STRING_NEWLINE + ONE_LINE_INPUT;
    private static String initialDir;

    private TeeApplication teeApplication;

    private InputStream mockInputStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    private String convertListOfStringToStrings(List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            stringBuilder.append(strings.get(i));
            if (i != strings.size() - 1) {
                stringBuilder.append(TestStringUtils.STRING_NEWLINE);
            }
        }
        return stringBuilder.toString();
    }

    private void writeToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(TeeApplicationPublicTest.ONE_LINE_INPUT);
            writer.flush();
        } catch (Exception e) {
            fail();
        }
    }

    @BeforeAll
    static void setUpAll() throws NoSuchFieldException, IllegalAccessException {
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
    }

    @BeforeEach
    void setup() throws Exception {
        teeApplication = new TeeApplication();
        Files.createDirectories(Paths.get(TEMP));
        TestEnvironmentUtil.setCurrentDirectory(TEMP);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TestEnvironmentUtil.setCurrentDirectory(initialDir);
    }

    @Test
    public void teeFromStdin_NoAppendNoFileNamesWithOneLineInput_returnsCorrectString() {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = new String[0];
            String result = teeApplication.teeFromStdin(false, inputStream, arr);

            inputStream.close();
            assertEquals(ONE_LINE_INPUT, result);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void teeFromStdin_NoAppendOneFileNameWithOneLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            String result = teeApplication.teeFromStdin(false, inputStream, arr);
            File file = new File(TEMP, TEXT_A);
            List<String> targetContent = Files.readAllLines(file.toPath());

            assertTrue(file.exists());
            assertEquals(ONE_LINE_INPUT, result );
            assertEquals(ONE_LINE_INPUT, targetContent.get(0));
        }
    }

    @Test
    public void teeFromStdin_NoAppendNoFileNamesWithMultiLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(MULTI_LINE_INPUT)) {
            String[] arr = new String[0];
            String result = teeApplication.teeFromStdin(false, inputStream, arr);

            assertEquals(MULTI_LINE_INPUT, result);
        }
    }

    @Test
    public void teeFromStdin_NoAppendOneFileNameWithMultipleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(MULTI_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            File file = new File(TEMP, TEXT_A);
            String result = teeApplication.teeFromStdin(false, inputStream, arr);
            List<String> targetContent = Files.readAllLines(file.toPath());

            assertTrue(file.exists());
            assertEquals(MULTI_LINE_INPUT, result);
            assertEquals(MULTI_LINE_INPUT, convertListOfStringToStrings(targetContent));
        }
    }

    @Test
    public void teeFromStdin_AppendSingleFilenamesWithSingleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            File fileA = new File(TEMP, TEXT_A);
            fileA.createNewFile();
            writeToFile(fileA);
            String result = teeApplication.teeFromStdin(true, inputStream, arr);
            List<String> targetContentA = Files.readAllLines(fileA.toPath());
            System.out.println("Path: " + fileA.toPath());

            assertEquals(ONE_LINE_INPUT, result);
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT,
                         convertListOfStringToStrings(targetContentA));
        }
    }

    @Test
    public void teeFromStdin_AppendMultipleFilenamesWithSingleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A, TEXT_B};
            File fileA = new File(TEMP, TEXT_A);
            fileA.createNewFile();
            writeToFile(fileA);
            File fileB = new File(TEMP, TEXT_B);
            fileB.createNewFile();
            writeToFile(fileB);

            String result = teeApplication.teeFromStdin(true, inputStream, arr);
            List<String> targetContentA = Files.readAllLines(fileA.toPath());
            List<String> targetContentB = Files.readAllLines(fileB.toPath());

            assertEquals(ONE_LINE_INPUT, result);
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT, convertListOfStringToStrings(targetContentA));
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT, convertListOfStringToStrings(targetContentB));
        }
    }
}