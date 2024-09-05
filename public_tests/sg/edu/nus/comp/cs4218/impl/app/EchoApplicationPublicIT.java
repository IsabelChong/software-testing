package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

// PMD suppressed for TDD testcases given
@SuppressWarnings("PMD")
public class EchoApplicationPublicIT {
    private EchoApplication echoApplication;

    @BeforeEach
    void setUp() {
        echoApplication = new EchoApplication();
    }
    
    @Test
    public void run_SingleArgument_OutputsArgument() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[] {"A*B*C"}, System.in, output);
        assertArrayEquals(("A*B*C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_MultipleArgument_SpaceSeparated() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[] {"A", "B", "C"}, System.in, output);
        assertArrayEquals(("A B C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_MultipleArgumentWithSpace_SpaceSeparated() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[] {"A B", "C D"}, System.in, output);
        assertArrayEquals(("A B C D" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_ZeroArguments_OutputsNewline() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoApplication.run(new String[] {}, System.in, output);
        assertArrayEquals(STRING_NEWLINE.getBytes(), output.toByteArray());
    }
}
