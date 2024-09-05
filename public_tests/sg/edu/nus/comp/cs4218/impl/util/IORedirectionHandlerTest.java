package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

/**
 * Tests for IORedirectionHandler
 *
 * Assumptions: We do not use the same file for input and output redirection
 * Current bash implementation will just make the file blank if we do so
 * e.g. cat < test.txt > test.txt will make test.txt blank\
 * Invalid input is when the file does not exist
 * Invalid output is when the file is a directory / in a directory that does not exist
 * Non-existing input file is when the file does not exist, but can be written to
 *
 * Positive test cases:
 * - Redirecting valid input
 * - Redirecting valid output
 * - Redirecting to existing output file (should overwrite)
 * - Redirecting valid input and valid output (different file)
 * - input redirection from two files (ignore preceding)
 * - output redirection to two files (ignore preceding)
 * - input redirection from two files (ignore preceding) and output redirection to two files (ignore preceding)
 *
 * Negative test cases:
 * - Null argument list
 * - Empty argument list
 * - Unspecified input file
 * - Unspecified output file
 * - valid input and invalid output redirection
 * - valid output and invalid input redirection
 * - Invalid input file
 * - Invalid output file
 * - Invalid input and valid output file
 * - Invalid output and valid input file
 * - invalid input and invalid output file
 * - Improper use of redirection
 * - Output redirection to file with no permissions
 * - Input redirection from file with no permissions
 * - Output redirection to existing directory
 * - Input redirection from existing directory
 * - Output redirection to non-existing directory
 * - Input redirection from non-existing directory
 * - Input redirection from directory
 * - Output redirection to two files, first file has no permissions
 * - Input redirection from two files, first file has no permissions
 * - Output redirection to two files, second file has no permissions
 * - Input redirection from two files, second file has no permissions
 *
 * Edge cases:
 * - Empty input file
 * - Special characters in file name
 * - Special characters in file content
 * - Large file
 *
 */

/**
 * Test for IORedirectionHandler
 */
public class IORedirectionHandlerTest {

    private static final String TEST_STRING = "HELLO CS4218!!";
    private static final String INPUT_TXT = "TestInput.txt";
    private static final String INPUT_FILE_1 = "Test Input File 1";
    private static final String INPUT_2_TXT = "TestInput2.txt";
    private static final String INPUT_FILE_2 = "Test Input File 2";
    private static final String OUTPUT_TXT = "TestOutput.txt";
    private static final String OUTPUT_FILE = "Test Output File";
    private static final String OUTPUT_2_TXT = "TestOutput2.txt";
    private static final String INVALID_INPUT_TXT = "InvalidInput.txt";
    private static final String INVALID_OUT_TXT = "nonexistant/InvalidOutput.txt";
    private static final String NON_EXISTING_TXT = "NonExistingOutput.txt";
    private static final String EMPTY_INPUT_TXT = "EmptyInput.txt";
    private static final String NO_PERM_TXT = "NoPermissionInput.txt";
    private static final String NO_PERM_O_TXT = "NoPermissionOutput.txt";
    private static final String SHELL_STRING = "shell: ";
    private static final Set<PosixFilePermission> NR_PERM = PosixFilePermissions.fromString("-wx-wx-wx");
    private static final Set<PosixFilePermission> NW_PERM = PosixFilePermissions.fromString("r--r--r--");
    private static final String TEST_FOLDER = "TestFolder";

    private IORedirectionHandler ioRH;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ArgumentResolver argumentResolver;

    /**
     * Set up the input stream, output stream, and argument resolver
     * @throws IOException
     */
    @BeforeEach
    public void setUp() throws IOException {
        inputStream = new ByteArrayInputStream("Original Input Stream".getBytes());
        outputStream = new ByteArrayOutputStream();
        argumentResolver = new ArgumentResolver();
        Files.createFile(Paths.get(INPUT_TXT)); // Ensure temp input file exists for tests
        Files.writeString(Paths.get(INPUT_TXT), INPUT_FILE_1);
        Files.createFile(Paths.get(INPUT_2_TXT)); // Ensure temp input file exists for tests
        Files.writeString(Paths.get(INPUT_2_TXT), INPUT_FILE_2);
        Files.createFile(Paths.get(OUTPUT_TXT)); // Ensure temp output file exists for tests
        Files.writeString(Paths.get(OUTPUT_TXT), OUTPUT_FILE);
        Files.createFile(Paths.get(EMPTY_INPUT_TXT)); // Ensure temp output file exists for tests
        Files.createFile(Paths.get(NO_PERM_TXT)); // Ensure temp output file exists for tests
        Files.setPosixFilePermissions(Paths.get(NO_PERM_TXT), NR_PERM);
        Files.createFile(Paths.get(NO_PERM_O_TXT)); // Ensure temp output file exists for tests
        Files.setPosixFilePermissions(Paths.get(NO_PERM_O_TXT), NW_PERM);
        Files.createDirectory(Paths.get(TEST_FOLDER)); // Ensure temp directory exists for tests
    }

    /**
     * Clean up the input and output files
     * @throws IOException
     */
    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(INPUT_TXT));
        Files.deleteIfExists(Paths.get(INPUT_2_TXT));
        Files.deleteIfExists(Paths.get(OUTPUT_TXT));
        Files.deleteIfExists(Paths.get(OUTPUT_2_TXT));
        Files.deleteIfExists(Paths.get(NON_EXISTING_TXT));
        Files.deleteIfExists(Paths.get(EMPTY_INPUT_TXT));
        Files.deleteIfExists(Paths.get(INVALID_OUT_TXT));
        Files.deleteIfExists(Paths.get(NO_PERM_TXT));
        Files.deleteIfExists(Paths.get(NO_PERM_O_TXT));
        Files.deleteIfExists(Paths.get(TEST_FOLDER));
    }

    // Positive test cases
    /**
     * Test for redirecting valid input
     * Expected: Should redirect
     */
    @Test
    public void extractRedirOptions_RedirectInput_ShouldRedirect() {
        ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT), inputStream, outputStream, argumentResolver);
        try {
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(INPUT_FILE_1, actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for redirecting valid output
     * Expected: Should create and redirect
     */
    @Test
    public void extractRedirOptions_RedirectOutput_ShouldCreateAndRedirect() {
        ioRH = new IORedirectionHandler(List.of(">", NON_EXISTING_TXT), inputStream, outputStream, argumentResolver);
        try {
            ioRH.extractRedirOptions();
            ioRH.getOutputStream().write(TEST_STRING.getBytes());
            ioRH.getOutputStream().flush();
            Boolean exists = Files.exists(Paths.get(NON_EXISTING_TXT));
            assertTrue(exists);
            String actualContent = Files.readString(Paths.get(NON_EXISTING_TXT));
            assertEquals(TEST_STRING, actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for redirecting to existing output file (should overwrite)
     * Expected: Should overwrite
     */
    @Test
    public void extractRedirOptions_RedirectOutputToExistingFile_ShouldOverwrite() {
        try {
        ioRH = new IORedirectionHandler(List.of(">", OUTPUT_TXT), inputStream, outputStream, argumentResolver);
        ioRH.extractRedirOptions();
        ioRH.getOutputStream().write(TEST_STRING.getBytes());
        ioRH.getOutputStream().flush();
        String actualContent = Files.readString(Paths.get(OUTPUT_TXT));
        assertEquals(TEST_STRING, actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for redirecting valid input and valid output (different file)
     * Expected: Should redirect input and create and redirect output
     */
    @Test
    public void extractRedirOptions_RedirectInputAndOutput_ShouldRedirect() {
        try {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, ">", OUTPUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(INPUT_FILE_1, actualContent);
            ioRH.getOutputStream().write(TEST_STRING.getBytes());
            ioRH.getOutputStream().flush();
            String readString = Files.readString(Paths.get(OUTPUT_TXT));
            assertEquals(TEST_STRING, readString);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for input redirection from two files
     * Expected: Should redirect second
     */
    @Test
    public void extractRedirOptions_RedirectInputFromTwoFiles_ShouldRedirectSecond() {
        try {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, "<", INPUT_2_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(INPUT_FILE_2, actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for output redirection to two files
     * Expected: Should create first file and redirect second
     */
    @Test
    public void extractRedirOptions_RedirectOutputToTwoFiles_ShouldCreateFirstFileAndRedirectSecond() {
        try {
            ioRH = new IORedirectionHandler(List.of(">", OUTPUT_TXT, ">", OUTPUT_2_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            Boolean exists = Files.exists(Paths.get(OUTPUT_TXT));
            assertTrue(exists);
            ioRH.getOutputStream().write(TEST_STRING.getBytes());
            ioRH.getOutputStream().flush();
            String actualContent = Files.readString(Paths.get(OUTPUT_2_TXT));
            assertEquals(TEST_STRING, actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for input redirection from two files (ignore preceding) and output redirection to two files (ignore preceding)
     * Expected: Should redirect second for both and create first output file
     */
    @Test
    public void extractRedirOptions_RedirectInputFromTwoFilesAndOutputToTwoFiles_ShouldRedirectSecondForBothAndCreateFirstOutputFile() {
        try {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, "<", INPUT_2_TXT, ">", OUTPUT_TXT, ">", OUTPUT_2_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(INPUT_FILE_2, actualContent);
            ioRH.getOutputStream().write(TEST_STRING.getBytes());
            ioRH.getOutputStream().flush();
            String readString = Files.readString(Paths.get(INPUT_2_TXT));
            assertEquals(INPUT_FILE_2, readString);
            Boolean exists = Files.exists(Paths.get(OUTPUT_TXT));
            assertTrue(exists);
        } catch (Exception e) {
            fail(e);
        }
    }

    // Negative test cases

    /**
     * Test for null argument list
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_NullArgumentList_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(null, inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test for empty argument list
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_EmptyArgumentList_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test for unspecified input redirection
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_UnspecifiedInputRedirection_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<"), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_NO_FILE_ARGS, exception.getMessage());
    }

    /**
     * Test for unspecified output redirection
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_UnspecifiedOutputRedirection_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">"), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_NO_FILE_ARGS, exception.getMessage());
    }

    /**
     * Test for valid input and invalid output redirection
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_ValidInputAndInvalidOutputRedirection_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, ">", INVALID_OUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for valid output and invalid input redirection
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_ValidOutputAndInvalidInputRedirection_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INVALID_INPUT_TXT, ">", OUTPUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for invalid input file
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InvalidInputFile_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INVALID_INPUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for invalid output file
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InvalidOutputFile_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", INVALID_OUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for invalid input and valid output file
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InvalidInputAndValidOutputFile_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INVALID_INPUT_TXT, ">", OUTPUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for invalid output and valid input file
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InvalidOutputAndValidInputFile_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, ">", INVALID_OUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for invalid input and invalid output file
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InvalidInputAndInvalidOutputFile_ShouldThrowFileNotFoundException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INVALID_INPUT_TXT, ">", INVALID_OUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for improper use of redirection
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_ImproperUseOfRedirection_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", "<"), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_SYNTAX, exception.getMessage());
    }

    /**
     * Test for output redirection to file with no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_OutputRedirectionToFileWithNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", NO_PERM_O_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + NO_PERM_O_TXT + ": " + ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for input redirection from file with no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InputRedirectionFromFileWithNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", NO_PERM_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + NO_PERM_TXT + ": " + ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for output redirection to existing directory
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_OutputRedirectionToExistingDirectory_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", TEST_FOLDER), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + TEST_FOLDER + ": " + ErrorConstants.ERR_IS_DIR, exception.getMessage());
    }

    /**
     * Test for input redirection from existing directory
     * Expected: Should do nothing
     */
    @Test
    public void extractRedirOptions_InputRedirectionFromExistingDirectory_ShouldDoNothing() {
        try {
            ioRH = new IORedirectionHandler(List.of("<", TEST_FOLDER), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals("", actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for output redirection to non-existing directory
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_OutputRedirectionFromNonExistingDirectory_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", "nonexistant/"), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + "nonexistant/" + ": " + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for input redirection from non-existing directory
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InputRedirectionFromNonExistingDirectory_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", "nonexistant/"), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + ErrorConstants.ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test for output redirection to two files, first file has no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_OutputRedirectionToTwoFilesFirstFileNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", NO_PERM_O_TXT, ">", OUTPUT_2_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        //File 2 should not be created
        Boolean exists = Files.exists(Paths.get(OUTPUT_2_TXT));
        assertFalse(exists);
        assertEquals(SHELL_STRING + NO_PERM_O_TXT + ": " + ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for input redirection from two files, first file has no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InputRedirectionFromTwoFilesFirstFileNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", NO_PERM_TXT, "<" , INPUT_2_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + NO_PERM_TXT + ": " + ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for output redirection to two files, second file has no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_OutputRedirectionToTwoFilesSecondFileNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of(">", OUTPUT_TXT, ">", NO_PERM_O_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        //File 1 should be created
        Boolean exists = Files.exists(Paths.get(OUTPUT_TXT));
        assertTrue(exists);
        assertEquals(SHELL_STRING + NO_PERM_O_TXT + ": " +ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for input redirection from two files, second file has no permissions
     * Expected: Should throw ShellException
     */
    @Test
    public void extractRedirOptions_InputRedirectionFromTwoFilesSecondFileNoPermissions_ShouldThrowShellException() {
        Throwable exception = assertThrows(ShellException.class, () -> {
            ioRH = new IORedirectionHandler(List.of("<", INPUT_TXT, "<" , NO_PERM_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
        });
        assertEquals(SHELL_STRING + NO_PERM_TXT + ": " + ErrorConstants.ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Test for empty input file
     * Expected: Should redirect
     */
    @Test
    public void extractRedirOptions_EmptyInputFile_ShouldRedirect() {
        try {
            ioRH = new IORedirectionHandler(List.of("<", EMPTY_INPUT_TXT), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals("", actualContent);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for special characters in file name
     * Expected: Should redirect
     */
    @Test
    public void extractRedirOptions_SpecialCharactersInFileName_ShouldRedirect() {
        try {
        String specialStr = "specialInput!@#$:,%^&*()_+{}[]|;.<>?~.txt";
        Files.createFile(Paths.get(specialStr)); // Ensure temp input file exists for tests
        Files.writeString(Paths.get(specialStr), INPUT_FILE_1);
        ioRH = new IORedirectionHandler(List.of("<", specialStr), inputStream, outputStream, argumentResolver);
        ioRH.extractRedirOptions();
        String actualContent = new String(ioRH.getInputStream().readAllBytes());
        assertEquals(INPUT_FILE_1, actualContent);
        Files.deleteIfExists(Paths.get(specialStr));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for special characters in file content
     * Expected: Should redirect
     */
    @Test
    public void extractRedirOptions_SpecialCharactersInFileContent_ShouldRedirect() {
        try {
            String fileNameStr = "specialInput.txt";
            String inputStr = "specialInput!@#$:,%^&*()_+{}[]|;.<>?~";
            Files.createFile(Paths.get(fileNameStr)); // Ensure temp input file exists for tests
            Files.writeString(Paths.get(fileNameStr), inputStr);
            ioRH = new IORedirectionHandler(List.of("<", fileNameStr), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(inputStr, actualContent);
            Files.deleteIfExists(Paths.get(fileNameStr));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test for large file
     * Expected: Should redirect
     */
    @Test
    public void extractRedirOptions_LargeFile_ShouldRedirect() {
        try {
            String txtStr = "largeInput.txt";
            String contentStr = "a".repeat(100000000);
            Files.createFile(Paths.get(txtStr)); // Ensure temp input file exists for tests
            Files.writeString(Paths.get(txtStr), contentStr);
            ioRH = new IORedirectionHandler(List.of("<", txtStr), inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            String actualContent = new String(ioRH.getInputStream().readAllBytes());
            assertEquals(contentStr, actualContent);
            Files.deleteIfExists(Paths.get(txtStr));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test getNoRedirArgsList
     * Expected: Should return list of arguments without input and output redirection options
     */
    @Test
    public void getNoRedirArgsList_OutputRedirection_ShouldReturnListWithoutRedirOptions() {
        try {
            List<String> argsList = List.of("echo", "hello", ">", OUTPUT_TXT);
            ioRH = new IORedirectionHandler(argsList, inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            List<String> noRedirArgsList = ioRH.getNoRedirArgsList();
            assertEquals(List.of("echo", "hello"), noRedirArgsList);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test getNoRedirArgsList
     * Expected: Should return list of arguments without input and output redirection options
     */
    @Test
    public void getNoRedirArgsList_InputRedirection_ShouldReturnListWithoutRedirOptions() {
        try {
            List<String> argsList = List.of("paste", "-", "<", INPUT_TXT);
            ioRH = new IORedirectionHandler(argsList, inputStream, outputStream, argumentResolver);
            ioRH.extractRedirOptions();
            List<String> noRedirArgsList = ioRH.getNoRedirArgsList();
            assertEquals(List.of("paste", "-"), noRedirArgsList);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Test isRedirOperator
     * Expected: Should return true if the given string is a redirection operator, false otherwise
     */
    @Test
    public void isRedirOperator_SpecialCharacters_ShouldReturnTrueIfGivenStringIsRedirectionOperator() {
        ioRH = new IORedirectionHandler(List.of(), inputStream, outputStream, argumentResolver);
        assertTrue(ioRH.isRedirOperator("<"));
        assertTrue(ioRH.isRedirOperator(">"));
        assertFalse(ioRH.isRedirOperator("echo"));
        assertFalse(ioRH.isRedirOperator("&"));
        assertFalse(ioRH.isRedirOperator("^"));
        assertFalse(ioRH.isRedirOperator("*"));
        assertFalse(ioRH.isRedirOperator("$"));
        assertFalse(ioRH.isRedirOperator("#"));
    }

}
