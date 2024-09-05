package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import javax.swing.*;
import java.io.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CallCommandTest {
    private CallCommand callCommand;
    private ApplicationRunner appRunner;
    private ArgumentResolver argumentResolver;
    private List<String> argsList;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeEach
    public void setUp() {
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
        stdin = new ByteArrayInputStream("".getBytes());
        stdout = new ByteArrayOutputStream();
    }

    /**
     * Test Call Command with multiple valid application name.
     * Expected: Should run the specified application.
     */
    @Test
    public void evaluate_validAppNameNoArgs_shouldExecuteCorrectly()
            throws AbstractApplicationException, FileNotFoundException, ShellException {
        argsList = Arrays.asList("echo");
        callCommand = new CallCommand(argsList, appRunner, argumentResolver);

        callCommand.evaluate(stdin, stdout);

        String expectedOutput = System.lineSeparator();

        assertEquals(expectedOutput, stdout.toString());
    }

    /**
     * Test Call Command with valid application name, and valid arguments.
     * Expected: Should run the specified application with arguments.
     */
    @Test
    public void evaluate_validAppNameAndArgs_shouldExecuteCorrectly() {
        try {
            argsList = Arrays.asList("echo", "Hello World!");
            callCommand = new CallCommand(argsList, appRunner, argumentResolver);

            callCommand.evaluate(stdin, stdout);

            String expectedOutput = "Hello World!" + System.lineSeparator();

            assertEquals(expectedOutput, stdout.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Call Command with invalid application name.
     * Expected: Should throw Shell Exception.
     */
    @Test
    public void evaluate_invalidAppName_shouldThrowShellException() {
        String invalidArg = "meow";
        argsList = Arrays.asList(invalidArg);
        callCommand = new CallCommand(argsList, appRunner, argumentResolver);

        Throwable exception = assertThrows(ShellException.class,
                () -> callCommand.evaluate(stdin, stdout));
        assertEquals("shell: " + invalidArg + ": " + ERR_INVALID_APP, exception.getMessage());
    }

    /**
     * Test Call Command with null arguments.
     * Expected: Should throw Shell Exception.
     */
    @Test
    public void evaluate_nullArgs_shouldThrowShellException() {
        callCommand = new CallCommand(null, appRunner, argumentResolver);

        Throwable exception = assertThrows(ShellException.class,
                () -> callCommand.evaluate(stdin, stdout));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test Call Command with empty argument list.
     * Expected: Should throw Shell Exception.
     */
    @Test
    public void evaluate_emptyArgs_shouldThrowShellException() {
        argsList = new ArrayList<>();
        callCommand = new CallCommand(argsList, appRunner, argumentResolver);

        Throwable exception = assertThrows(ShellException.class,
                () -> callCommand.evaluate(stdin, stdout));
        assertEquals("shell: " + ERR_SYNTAX, exception.getMessage());
    }
}
