package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CutException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
public class CutApplicationPublicTest {
    CutApplication cutApplication;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    public void setUp() {
        cutApplication = new CutApplication();
    }

    @Test
    void cutFromStdin_NullContent_ThrowsException() {
        int[] ranges = new int[]{1, 2};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(CutException.class, () -> cutApplication.cutFromStdin(false, true, List.of(ranges), null));
    }

    @Test
    void cutFromStdin_SingleLineByCharRange_ReturnCutByLine() throws Exception {
        int[] ranges = new int[]{1, 3};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        String actual = cutApplication.cutFromStdin(true, false, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE, actual);
    }

    @Test
    void cutFromStdin_SingleLineByByteRange_ReturnCutByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = generateInputStreamFromStrings("hello world");
        String actual = cutApplication.cutFromStdin(false, true, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE, actual);
    }

    @Test
    void cutFromStdin_MultipleLinesByCharRange_ReturnCutContentAtEachLineByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        String actual = cutApplication.cutFromStdin(true, false, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, actual);
    }

    @Test
    void cutFromStdin_MultipleLinesByByteRange_ReturnCutContentAtEachLineByByte() throws Exception {
        int[] ranges = new int[]{1, 3};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        String actual = cutApplication.cutFromStdin(false, true, List.of(ranges), stdin);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, actual);
    }


    @Test
    void cutFromFile_InvalidFile_ThrowsException() {
        int[] ranges = new int[]{1, 3};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(CutException.class,
                () -> cutApplication.cutFromFiles(false, true, List.of(ranges), "invalidFile"));
    }

}
