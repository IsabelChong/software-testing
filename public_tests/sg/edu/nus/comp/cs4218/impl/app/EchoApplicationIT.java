package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Tests for EchoApplication
 *
 * Positive test cases:
 * - echo with null args
 * - echo with empty args
 * - echo with one arg
 * - echo with multiple args
 * - echo with multiple args with spaces
 * - echo with multiple args with spaces and special characters
 * - echo with one arg with spaces
 * - echo with escape characters
 * - echo with new line
 * - echo with leading and trailing spaces
 * <p>
 * Negative test cases:
 * - echo with null stdout
 * - echo with null stdin
 * - echo with null stdin and null stdout
 * - echo with no args
 * - echo with asterisk
 */

/**
 * Tests for echo command.
 * Print the arguments to the standard output.
 * Command format: echo [args]
 */
public class EchoApplicationIT { //NOPMD
    private EchoApplication echoApp;
    private OutputStream stdOut;

    /**
     * Initialize EchoApplication and OutputStream.
     */
    @BeforeEach
    public void setUp() {
        echoApp = new EchoApplication();
        stdOut = new ByteArrayOutputStream();
    }

    /**
     * Test echo with null args.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_NullArgs_ThrowException() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(null, System.in, stdOut));
        assertEquals("echo: Null arguments", exception.getMessage());
    }

    /**
     * Test echo with empty args.
     * Expected: Print new line.
     */
    @Test
    public void run_EmptyArgs_PrintNewLine() {
        try {
            echoApp.run(new String[]{}, System.in, stdOut);
            assertEquals(System.lineSeparator(), stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }

    }

    /**
     * Test echo with one arg.
     * Expected: Print the arg.
     */
    @Test
    public void run_OneArg_PrintArg() {
        try {
            echoApp.run(new String[]{"hello"}, System.in, stdOut);//NOPMD
            assertEquals("hello\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args.
     * Expected: Print the args with spaces.
     */
    @Test
    public void run_MultipleArgs_PrintArgsWithSpaces() {
        try {
            echoApp.run(new String[]{"hello", "CS4218"}, System.in, stdOut);//NOPMD
            assertEquals("hello CS4218\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args with spaces.
     * Expected: Print the args with spaces.
     */
    @Test
    public void run_MultipleArgsWithSpaces_PrintArgsWithSpaces() {
        try {
            echoApp.run(new String[]{"hello", "CS4218", " ", "this", "is", "a", "test"}, System.in, stdOut);
            assertEquals("hello CS4218   this is a test\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args with spaces and special characters.
     * Expected: Print the args with spaces and special characters.
     */
    @Test
    public void run_MultipleArgsWithSpacesAndSpecialChars_PrintArgsWithSpacesAndSpecialChars() {
        try {
            echoApp.run(new String[]{"hello", "CS4218", "this", "is", "a", "test", "with", "special", "chars", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-", "=", "{", "}", "[", "]", "|", "\\", ":", ";", "\"", "'", "<", ">", ",", ".", "?", "/", "`", "~"}, System.in, stdOut);
            assertEquals("hello CS4218 this is a test with special chars ! @ # $ % ^ & * ( ) _ + - = { } [ ] | \\ : ; \" ' < > , . ? / ` ~\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with one arg with spaces.
     * Expected: Print the arg with spaces.
     */
    @Test
    public void run_OneArgWithSpaces_PrintArgWithSpaces() {
        try {
            echoApp.run(new String[]{"hello CS4218"}, System.in, stdOut);
            assertEquals("hello CS4218\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with escape characters.
     * Expected: Print the escape characters.
     */
    @Test
    public void run_EscapeChars_PrintEscapeChars() {
        try {
            echoApp.run(new String[]{"\n", "\t", "\r", "\f", "\b"}, System.in, stdOut);
            assertEquals("\n \t \r \f \b\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with new line.
     * Expected: Print new line.
     */
    @Test
    public void run_NewLine_PrintNewLine() {
        try {
            echoApp.run(new String[]{"\n"}, System.in, stdOut);
            assertEquals("\n\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with leading and trailing spaces.
     * Expected: Print the arg with leading and trailing spaces.
     */
    @Test
    public void run_LeadingAndTrailingSpaces_PrintArgWithSpaces() {
        try {
            echoApp.run(new String[]{"  hello CS4218  "}, System.in, stdOut);
            assertEquals("  hello CS4218  \n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with " c".
     * Expected: Print " c".
     */
    @Test
    public void run_withSpaceBeforeArg_PrintSpaceBeforeArg() {
        try {
            echoApp.run(new String[]{" c"}, System.in, stdOut);
            assertEquals(" c\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with null stdout.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_withNullStdout_ThrowException() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(new String[]{}, System.in, null));
        assertEquals("echo: OutputStream not provided", exception.getMessage());
    }

    /**
     * Test echo with null stdin.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_withNullStdin_ShouldThrowException() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(new String[]{"hello", "CS4218"}, null, stdOut));//NOPMD
        assertEquals("echo: " + ERR_NULL_STREAMS, exception.getMessage());//NOPMD
    }

    /**
     * Test echo with null stdin and null stdout.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_withNullStdinAndNullStdout_PrintArgsWithSpaces() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(new String[]{"hello", "CS4218"}, null, null));//NOPMD
        assertEquals("echo: " + ERR_NULL_STREAMS, exception.getMessage());//NOPMD
    }

    /**
     * Test echo with no args.
     * Expected: Print new line.
     */
    @Test
    public void run_withNoArgs_PrintNewLine() {
        try {
            echoApp.run(new String[]{}, System.in, stdOut);
            assertEquals(System.lineSeparator(), stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with asterisk.
     * Expected: Print with asterisk.
     */
    @Test
    public void run_withAsterisk_PrintWithAsterisk() {
        try {
            echoApp.run(new String[]{"A*B*C"}, System.in, stdOut);
            assertEquals("A*B*C\n", stdOut.toString());
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with null stdIn.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_withNullStdIn_ShouldThrowException() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(new String[]{"hello", "CS4218"}, null, stdOut));
        assertEquals("echo: " + ERR_NULL_STREAMS, exception.getMessage());
    }


    /**
     * Test echo with null stdOut.
     * Expected: Throw EchoException with error message.
     */
    @Test
    public void run_withNullStdOut_ShouldThrowException() {
        Throwable exception = assertThrows(EchoException.class, () -> echoApp.run(new String[]{"hello", "CS4218"}, System.in, null));
        assertEquals("echo: " + ERR_NO_OSTREAM, exception.getMessage());
    }

}
