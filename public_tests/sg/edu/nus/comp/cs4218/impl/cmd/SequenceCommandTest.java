package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * Tests for the SequenceCommand class
 * The SequenceCommand class is responsible for executing a sequence of commands separated by a semicolon.
 *  Command format: <Command> ; <Command>
 *
 *  Positive test cases:
 *  - Two commands are executed in sequence
 *  - Two commands are executed in sequence with the first command having no output
 *  - Two commands are executed in sequence with the second command having no output
 *  - Two commands are executed in sequence with both commands having no output
 *  - Two commands are executed in sequence with the first command throwing an exception
 *  - Two commands are executed in sequence with the second command throwing an exception
 *  - Two commands are executed in sequence with both commands throwing an exception
 *
 */

/**
 * Tests for the SequenceCommand class
 */
public class SequenceCommandTest {

    private Command outputCmd1;
    private Command outputCmd2;
    private Command noOutputCmd1;
    private Command noOutputCmd2;
    private Command shellExCmd;
    private Command fileNfExCmd;
    private OutputStream stdout;
    private InputStream stdin;
    private SequenceCommand sequenceCommand;

    /**
     * Initialize stdout and stdin for each test
     */
    @BeforeEach
    public void setUp() throws Exception {
        outputCmd1 = mock(Command.class);
        outputCmd2 = mock(Command.class);
        noOutputCmd1 = mock(Command.class);
        noOutputCmd2 = mock(Command.class);
        shellExCmd = mock(Command.class);
        fileNfExCmd = mock(Command.class);
        stdout = new ByteArrayOutputStream();
        stdin = new ByteArrayInputStream("".getBytes());
        doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(1);
            outputStream.write("command1 output\n".getBytes());
            return null;
        }).when(outputCmd1).evaluate(any(InputStream.class), any(OutputStream.class));
        doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(1);
            outputStream.write("command2 output\n".getBytes());
            return null;
        }).when(outputCmd2).evaluate(any(InputStream.class), any(OutputStream.class));
        doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(1);
            return null;
        }).when(noOutputCmd1).evaluate(any(InputStream.class), any(OutputStream.class));
        doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(1);
            return null;
        }).when(noOutputCmd2).evaluate(any(InputStream.class), any(OutputStream.class));
        doThrow(new ShellException("ShellException")).when(shellExCmd).evaluate(any(InputStream.class), any(OutputStream.class));
        doThrow(new ShellException("FileNotFoundException")).when(fileNfExCmd).evaluate(any(InputStream.class), any(OutputStream.class));
    }

    /**
     * Tests that two commands are executed in sequence and both produce expected output.
     * Verifies that the output from the first command is followed by the output from the second command
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequence_ShouldExecuteInOrder() {
        try {
            sequenceCommand = new SequenceCommand(List.of(outputCmd1, outputCmd2));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("command1 output\ncommand2 output\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests that when the first command produces no output, only the output from the second command is captured.
     * Verifies the ability of the SequenceCommand to handle command sequences where some commands may not produce output.
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequenceFirstCommandNoOutput_ShouldExecuteInOrder() {
        try {
            sequenceCommand = new SequenceCommand(List.of(noOutputCmd1, outputCmd2));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("command2 output\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests that when the second command produces no output, only the output from the first command is captured.
     * Ensures that the absence of output from the latter command in a sequence does not affect the execution or output of preceding commands.
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequenceSecondCommandNoOutput_ShouldExecuteInOrder() {
        try {
            sequenceCommand = new SequenceCommand(List.of(outputCmd1, noOutputCmd2));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("command1 output\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests execution of two commands where neither produces output.
     * Verifies that the SequenceCommand can handle sequences of commands without output without causing errors or unexpected behavior.
    */
 @Test
    public void evaluate_TwoCommandsExecutedInSequenceBothCommandsNoOutput_ShouldExecuteInOrder() {
         try {
             sequenceCommand = new SequenceCommand(List.of(noOutputCmd1, noOutputCmd2));
             sequenceCommand.evaluate(stdin, stdout);
             assertEquals("", stdout.toString());
         } catch (Exception e) {
             fail(e);
         }
    }

    /**
     * Tests that the sequence continues with the second command even if the first command throws an exception.
     * Ensures that exceptions do not halt the execution of subsequent commands in the sequence.
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequenceFirstCommandThrowsException_ShouldExecuteProperly() {
        try {
            sequenceCommand = new SequenceCommand(List.of(shellExCmd, outputCmd2));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("shell: ShellException\ncommand2 output\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests that the sequence can execute the first command successfully even if the second command throws an exception.
     * Demonstrates that an exception in later commands does not retroactively affect the output or execution of earlier commands.
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequenceSecondCommandThrowsException_ShouldExecuteProperly() {
        try {
            sequenceCommand = new SequenceCommand(List.of(outputCmd1, shellExCmd));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("command1 output\nshell: ShellException\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests that exceptions thrown by both commands in a sequence are handled correctly.
     * Verifies that the SequenceCommand properly captures and handles multiple exceptions.
     */
    @Test
    public void evaluate_TwoCommandsExecutedInSequenceBothCommandsThrowException_ShouldExecuteInOrder() {
        try {
            sequenceCommand = new SequenceCommand(List.of(shellExCmd, fileNfExCmd));
            sequenceCommand.evaluate(stdin, stdout);
            assertEquals("shell: ShellException\nshell: FileNotFoundException\n", stdout.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Tests the execution order of commands in a sequence to ensure they are run in the specified order.
     * Utilizes Mockito's InOrder verification to assert the execution order of the commands.
     */
    @Test
    public void evaluate_TwoCommands_ShouldExecuteInOrder() {
        try {
            sequenceCommand = new SequenceCommand(List.of(outputCmd1, outputCmd2));
            sequenceCommand.evaluate(stdin, stdout);
            InOrder inOrder = inOrder(outputCmd1, outputCmd2);
            inOrder.verify(outputCmd1).evaluate(any(InputStream.class), any(OutputStream.class));
            inOrder.verify(outputCmd2).evaluate(any(InputStream.class), any(OutputStream.class));
        } catch (Exception e) {
            fail(e);
        }
    }
}
