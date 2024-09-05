package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD")
public class GrepApplicationPublicIT {
    private static final String TEMP = "temp-grep";
    private static final byte[] BYTES_A = "First line\nSecond line\nThird line\nFourth line\n".getBytes();
    private static final byte[] BYTES_B = "Fifth line\nSixth line\nSeventh line\nEighth line\n".getBytes();

    private static final Deque<Path> files = new ArrayDeque<>();
    private static Path TEMP_PATH;

    private final GrepApplication grepApplication = new GrepApplication();

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        TEMP_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    private Path createFile(String name) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private String[] toArgs(String flags, String pattern, String... files) {
        List<String> args = new ArrayList<>();
        if (!flags.isEmpty()) {
            args.add("-" + flags);
        }
        args.add(pattern);
        for (String file : files) {
            if (file.equals("-")) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    private Path getRelativePath(String name) {
        return Paths.get(TEMP, name);
    }

    @Test
    void run_SingleFile_PrintsLine() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("a.txt"), BYTES_A);
        grepApplication.run(toArgs("", "th", "a.txt"), System.in, output);
        assertArrayEquals(("Fourth line" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileRegex_PrintsLines() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("b.txt"), BYTES_A);
        grepApplication.run(toArgs("", "[th] l", "b.txt"), System.in, output);
        assertArrayEquals(("First line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFiles_PrintsLinesWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("c.txt"), BYTES_A);
        Files.write(createFile("d.txt"), BYTES_B);
        grepApplication.run(toArgs("", "Fi", "c.txt", "d.txt"), System.in, output);
        String expected = getRelativePath("c.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("d.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFilePrintName_PrintsLinesWithName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("e.txt"), BYTES_A);
        Path path = getRelativePath("e.txt");
        grepApplication.run(toArgs("H", "F", "e.txt"), System.in, output);
        String expected = path + ": First line" + STRING_NEWLINE + path + ": Fourth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesPrintName_PrintsLinesWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("f.txt"), BYTES_A);
        Files.write(createFile("g.txt"), BYTES_B);
        grepApplication.run(toArgs("H", "Fi", "f.txt", "g.txt"), System.in, output);
        String expected = getRelativePath("f.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("g.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCaseInsensitive_PrintsLines() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("h.txt"), BYTES_A);
        grepApplication.run(toArgs("i", "th", "h.txt"), System.in, output);
        String expected = "Third line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFilePrintNameCaseInsensitive_PrintsLinesWithName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("i.txt"), BYTES_A);
        Path path = getRelativePath("i.txt");
        grepApplication.run(toArgs("Hi", "th", "i.txt"), System.in, output);
        String expected = path + ": Third line" + STRING_NEWLINE + path + ": Fourth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesPrintNameCaseInsensitive_PrintsLines() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("j.txt"), BYTES_A);
        Files.write(createFile("k.txt"), BYTES_B);
        grepApplication.run(toArgs("Hi", "fi", "j.txt", "k.txt"), System.in, output);
        String expected = getRelativePath("j.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("k.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCountLines_PrintsCount() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("l.txt"), BYTES_A);
        grepApplication.run(toArgs("c", "th", "l.txt"), System.in, output);
        assertArrayEquals(("1" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    // Flag only contain c but expected output requires H?
    @Test
    void run_MultipleFilesCountLines_PrintsCountWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("m.txt"), BYTES_A);
        Files.write(createFile("n.txt"), BYTES_B);
        grepApplication.run(toArgs("c", "th", "m.txt", "n.txt"), System.in, output);
        String expected = getRelativePath("m.txt") + ": 1" + STRING_NEWLINE +
                getRelativePath("n.txt") + ": 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCountLinesPrintName_PrintsCountWithName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("o.txt"), BYTES_A);
        grepApplication.run(toArgs("cH", "th", "o.txt"), System.in, output);
        assertArrayEquals((getRelativePath("o.txt") + ": 1" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesCountLinesPrintName_PrintsCountWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("p.txt"), BYTES_A);
        Files.write(createFile("q.txt"), BYTES_B);
        grepApplication.run(toArgs("cH", "th", "p.txt", "q.txt"), System.in, output);
        String expected = getRelativePath("p.txt") + ": 1" + STRING_NEWLINE +
                getRelativePath("q.txt") + ": 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCountLinesCaseInsensitive_PrintsCount() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("r.txt"), BYTES_A);
        grepApplication.run(toArgs("ci", "th", "r.txt"), System.in, output);
        assertArrayEquals(("2" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesCountLinesCaseInsensitive_PrintsCountWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("s.txt"), BYTES_A);
        Files.write(createFile("t.txt"), BYTES_B);
        grepApplication.run(toArgs("ci", "th", "s.txt", "t.txt"), System.in, output);
        String expected = getRelativePath("s.txt") + ": 2" + STRING_NEWLINE +
                getRelativePath("t.txt") + ": 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileCountLinesCaseInsensitivePrintName_PrintsCountWithName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("u.txt"), BYTES_A);
        grepApplication.run(toArgs("ciH", "th", "u.txt"), System.in, output);
        assertArrayEquals((getRelativePath("u.txt") + ": 2" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesCountLinesCaseInsensitivePrintName_PrintsCountWithNames() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("v.txt"), BYTES_A);
        Files.write(createFile("w.txt"), BYTES_B);
        grepApplication.run(toArgs("ciH", "th", "v.txt", "w.txt"), System.in, output);
        String expected = getRelativePath("v.txt") + ": 2" + STRING_NEWLINE +
                getRelativePath("w.txt") + ": 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_NoFiles_PrintsLinesFromStdin() throws Exception {
        String inputString = "abc" + STRING_NEWLINE + "def" + STRING_NEWLINE + "ghi" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(toArgs("", "[ci]"), input, output);
        assertArrayEquals(("abc" + STRING_NEWLINE + "ghi" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_NoFilesCaseInsensitivePrintName_PrintsLinesWithNameFromStdin() throws Exception {
        String inputString = "Abc" + STRING_NEWLINE + "aaf" + STRING_NEWLINE + "bbd" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(toArgs("Hi", "a"), input, output);
        String expected = "(standard input): Abc" + STRING_NEWLINE + "(standard input): aaf" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_NoFilesCountLinesCaseInsensitivePrintName_PrintsCountWithNameFromStdin() throws Exception {
        String inputString = "Abc" + STRING_NEWLINE + "aaf" + STRING_NEWLINE + "bbd" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(new String[]{"-c", "-Hi", "a"}, input, output);
        String expected = "(standard input): 2" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_Stdin_PrintsLinesFromStdin() throws Exception {
        String inputString = "jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE + "pqr" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(toArgs("", "[lo]", "-"), input, output);
        assertArrayEquals(("jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinCaseInsensitivePrintName_PrintsLinesWithNameFromStdin() throws Exception {
        String inputString = "Dbc" + STRING_NEWLINE + "daf" + STRING_NEWLINE + "aab" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(toArgs("Hi", "d", "-"), input, output);
        String expected = "(standard input): Dbc" + STRING_NEWLINE + "(standard input): daf" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinCountLinesCaseInsensitivePrintName_PrintsCountWithNameFromStdin() throws Exception {
        String inputString = "Bbc" + STRING_NEWLINE + "bac" + STRING_NEWLINE + "cde" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(new String[]{"-c", "-Hi", "b", "-"}, input, output);
        String expected = "(standard input): 2" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinAndFile_PrintsLines() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_A);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("aa.txt"), BYTES_B);
        grepApplication.run(toArgs("", "Fi", "-", "aa.txt"), input, output);
        String expected = "(standard input): First line" + STRING_NEWLINE +
                getRelativePath("aa.txt") + ": Fifth line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinAndFileCaseInsensitivePrintName_PrintsLinesWithName() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_B);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("ab.txt"), BYTES_A);
        grepApplication.run(toArgs("Hi", "fi", "-", "ab.txt"), input, output);
        String expected = "(standard input): Fifth line" + STRING_NEWLINE +
                getRelativePath("ab.txt") + ": First line" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_StdinAndFileCountLinesCaseInsensitivePrintName_PrintsCountWithName() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_B);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("ac.txt"), BYTES_A);
        grepApplication.run(toArgs("cHi", "th", "ac.txt", "-"), input, output);
        String expected = getRelativePath("ac.txt") + ": 2" + STRING_NEWLINE +
                "(standard input): 4" + STRING_NEWLINE;
        assertArrayEquals(expected.getBytes(), output.toByteArray());
    }

    @Test
    void run_EmptyFile_PrintsNothing() throws Exception {
        createFile("x.txt");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        grepApplication.run(toArgs("", "a", "x.txt"), System.in, output);
        assertArrayEquals(new byte[]{}, output.toByteArray());
    }

    @Test
    void run_NoMatch_PrintsNothing() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("y.txt"), BYTES_A);
        grepApplication.run(toArgs("", "a", "y.txt"), System.in, output);
        assertArrayEquals(new byte[]{}, output.toByteArray());
    }

    @Test
    void run_UnknownFlag_Throws() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("z.txt"), BYTES_A);
        assertThrows(GrepException.class, () -> grepApplication.run(toArgs("x", "l", "z.txt"), System.in, output));
    }

    @Test
    void run_ZeroArguments_Throws() {
        assertThrows(GrepException.class, () -> grepApplication.run(new String[]{}, System.in, System.out));
    }

    @Test
    void run_FlagOnly_Throws() {
        assertThrows(GrepException.class, () -> grepApplication.run(new String[]{"-c"}, System.in, System.out));
    }
}
