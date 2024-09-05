package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the PipeCommand class.
 */
class PipeCommandTest {
    private PipeCommand pipeCommand;
    private ByteArrayOutputStream outputStream;
    private static final String FIRST_STRING = "Hello";
    private static final String SECOND_STRING = "World!";

    /**
     * Sets up the test environment before each test method runs.
     */
    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * Tests the behavior of PipeCommand when an empty command list is provided.
     * It expects no output.
     */
    @Test
    void evaluate_WhenEmptyCommandList_ExpectNoOutput() {
        // Create an empty list of commands
        List<CallCommand> commandList = new ArrayList<>();

        // Create a PipeCommand with the empty list of commands
        pipeCommand = new PipeCommand(commandList);

        // Set up input stream (stdin) for the PipeCommand
        InputStream inputStream = new ByteArrayInputStream("".getBytes());

        // Execute the PipeCommand
        try {
            pipeCommand.evaluate(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Verify the output
        assertEquals("", outputStream.toString());
    }

    /**
     * Tests the behavior of PipeCommand when EchoApplication and GrepApplication are executed in sequence.
     * It expects correct output based on the given input.
     */
    @Test
    void evaluate_WhenEchoAndGrepExecuted_ExpectCorrectOutput() {

        // Create instances of EchoApplication and GrepApplication
        EchoApplication echoApp = new EchoApplication();
        GrepApplication grepApp = new GrepApplication();


        ApplicationRunner appRunner = new ApplicationRunner();
        ArgumentResolver argResolver = new ArgumentResolver();

        // Create CallCommands for echo and grep
        CallCommand echoCommand = new CallCommand(Arrays.asList("echo", FIRST_STRING + " " + SECOND_STRING), appRunner, argResolver);
        CallCommand grepCommand = new CallCommand(Arrays.asList("grep", SECOND_STRING), appRunner, argResolver);

        List<CallCommand> commandList = new ArrayList<>();
        commandList.add(echoCommand);
        commandList.add(grepCommand);

        // Create a PipeCommand with the list of CallCommands
        pipeCommand = new PipeCommand(commandList);

        // Set up input stream (stdin) for the PipeCommand
        InputStream inputStream = new ByteArrayInputStream("".getBytes());

        // Execute the PipeCommand
        try {
            pipeCommand.evaluate(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Verify the output
        String expectedOutput = "Hello World!" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    /**
     * Tests the behavior of PipeCommand when only a single command is provided.
     * It expects the same output as the command execution.
     */
    @Test
    void evaluate_WhenSingleCommand_ExpectSameOutput() {
        // Create an instance of EchoApplication
        EchoApplication echoApp = new EchoApplication();

        ApplicationRunner appRunner = new ApplicationRunner();
        ArgumentResolver argResolver = new ArgumentResolver();

        // Create a CallCommand for echo
        CallCommand echoCommand = new CallCommand(Arrays.asList("echo", "Hello World!"), appRunner, argResolver);

        List<CallCommand> commandList = new ArrayList<>();
        commandList.add(echoCommand);

        // Create a PipeCommand with the single command
        pipeCommand = new PipeCommand(commandList);

        // Set up input stream (stdin) for the PipeCommand
        InputStream inputStream = new ByteArrayInputStream("".getBytes());

        // Execute the PipeCommand
        try {
            pipeCommand.evaluate(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Verify the output
        String expectedOutput = "Hello World!" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }
}
