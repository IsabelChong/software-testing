package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

// Methods to test
// public String constructResult(String[] args) throws EchoException

// Test Cases:
// - echo with null args
// - echo with empty args
// - echo with one arg but empty
// - echo with one arg
// - echo with multiple args
// - echo with multiple args with spaces
// - echo with multiple args with spaces and special characters
// - echo with one arg with spaces
// - echo with escape characters
// - echo with new line
// - echo with leading and trailing spaces

/**
 * Unit tests for EchoApplication
 */
public class EchoApplicationTest {

    // Test String
    private static final String TEST_STRING = "test";
    // Test String 2
    private static final String TEST_STRING_2 = "testing";
    // Test String with multiple spaces
    private static final String TEST_STRING_MS = "te  st";

    private EchoApplication echoApplication;

    @BeforeEach
    void setUp() {
        echoApplication = new EchoApplication();
    }

    /**
     * Test echo with null args.
     * Expected: Throw EchoException with error message.
     */
    @Test
    void constructResults_NullArguments_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(EchoException.class, () -> echoApplication.constructResult(null));
    }

    /**
     * Test echo with empty args.
     * Expected: Return new line.
     */
    @Test
    void constructResults_NoArguments_ReturnsEmptyString() {
        String[] args = new String[0];
        try {
            String actual = echoApplication.constructResult(args);
            assertEquals(STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with one arg but empty.
     * Expected: Return new line.
     */
    @Test
    void constructResults_SingleArgumentButEmpty_ReturnsEmptyString() {
        try {
            String actual = echoApplication.constructResult(new String[]{""});
            assertEquals(STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with one arg.
     * Expected: Return the argument.
     */
    @Test
    void constructResults_SingleArgument_ReturnsArgument() {
        try {
            String actual = echoApplication.constructResult(new String[]{TEST_STRING});
            assertEquals(TEST_STRING + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args.
     * Expected: Return the arguments separated by space.
     */
    @Test
    void constructResults_MultipleArguments_ReturnsArgumentsSeparatedBySpace() {
        try {
            String actual = echoApplication.constructResult(new String[]{TEST_STRING, TEST_STRING_2});
            assertEquals(TEST_STRING + " " + TEST_STRING_2 + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args with spaces.
     * Expected: Return the arguments separated by space.
     */
    @Test
    void constructResults_MultipleArgumentsWithSpaces_ReturnsArgumentsSeparatedBySpace() {
        try {
            String actual = echoApplication.constructResult(new String[]{TEST_STRING_MS, TEST_STRING_2});
            assertEquals( TEST_STRING_MS + " " + TEST_STRING_2 + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with multiple args with spaces and special characters.
     * Expected: Return the arguments separated by space.
     */
    @Test
    void constructResults_MultipleArgumentsWithSpacesAndSpecialChars_ReturnsArgumentsSeparatedBySpace() {
        try {
            String actual = echoApplication.constructResult(new String[]{TEST_STRING_MS, "testing", "CS4218", "!", "@", "#"});
            assertEquals(TEST_STRING_MS + " " + "testing CS4218 ! @ #" + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with one arg with spaces.
     * Expected: Return the argument.
     */
    @Test
    void constructResults_OneArgumentWithSpaces_ReturnsArgument() {
        try {
            String actual = echoApplication.constructResult(new String[]{TEST_STRING_MS});
            assertEquals(TEST_STRING_MS + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with escape characters.
     * Expected: Return the argument.
     */
    @Test
    void constructResults_EscapeCharacters_ReturnsArgument() {
        try {
            String actual = echoApplication.constructResult(new String[]{"\\t", "\\n", "\\r", "\\f", "\\b"});
            assertEquals("\\t \\n \\r \\f \\b" + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with new line.
     * Expected: Return the argument.
     */
    @Test
    void constructResults_NewLine_ReturnsArgument() {
        try {
            String actual = echoApplication.constructResult(new String[]{"\n"});
            assertEquals("\n" + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with leading and trailing spaces.
     * Expected: Return the argument.
     */
    @Test
    void constructResults_LeadingAndTrailingSpaces_ReturnsArgument() throws AbstractApplicationException {
        String actual = echoApplication.constructResult(new String[]{"  test  "});
        assertEquals("  test  " + STRING_NEWLINE, actual);
    }

    /**
     * Test echo with multiple args with spaces and special characters.
     * Expected: Print the args with spaces and special characters.
     */
    @Test
    public void constructResults_MultipleArgsWithSpacesAndSpecialChars_ReturnsArgsWithSpacesAndSpecialChars() {
        try {
            String actual = echoApplication.constructResult(new String[]{"hello", "CS4218", "this", "is", "a", "test", "with", "special", "chars", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-", "=", "{", "}", "[", "]", "|", "\\", ":", ";", "\"", "'", "<", ">", ",", ".", "?", "/", "`", "~"});
            assertEquals("hello CS4218 this is a test with special chars ! @ # $ % ^ & * ( ) _ + - = { } [ ] | \\ : ; \" ' < > , . ? / ` ~\n", actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }

    /**
     * Test echo with " c".
     * Expected: Print " c".
     */
    @Test
    public void constructResults_WithSpaceBeforeArg_ReturnsSpaceBeforeArg() {
        try {
            String actual = echoApplication.constructResult(new String[]{" c"});
            assertEquals(" c" + STRING_NEWLINE, actual);
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }
}
