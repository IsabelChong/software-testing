package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempDir;
import static sg.edu.nus.comp.cs4218.impl.helper.TestHelper.createTempFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

/**
 * Tests for QuotingTest and Command Substitution and Globbing
 * <p>
 * Positive test cases:
 * - Single quote
 * - Multiple arguments
 * - Space
 * - Empty
 * - Backquotes
 * - Command substituition
 * - Special characters
 * <p>
 * Negative test cases:
 * - Null input
 */

public class ArgumentResolverTest {
    private static Path initDir = Paths.get(System.getProperty("user.dir"));
    private static File rootTestDir, testDir, testFile, testTxtFile, innerTestDir = null;
    private ArgumentResolver argumentResolver;

    /*
   rootTestDir
       testDir
           testFile
           innerTestDir
       testTxtFile.txt
    */
    @BeforeAll
    static void init() throws IOException {
        rootTestDir = createTempDir(initDir + File.separator + "rootTestDir");
        testDir = createTempDir(rootTestDir + File.separator + "testDir");
        testFile = createTempFile(testDir + File.separator + "testFile");
        testTxtFile = createTempFile(rootTestDir + File.separator + "testTxtFile.txt");
        innerTestDir = createTempDir(testDir + File.separator + "innerTestDir");
    }

    @BeforeEach
    public void setUp() throws IOException {
        argumentResolver = new ArgumentResolver();
        Environment.currentDirectory = rootTestDir.toString();
    }

    @AfterAll
    static void tearDown() {
        innerTestDir.delete();
        testTxtFile.delete();
        testFile.delete();
        testDir.delete();
        rootTestDir.delete();
        Environment.currentDirectory = System.getProperty("user.dir");
    }

    /**
     * Test case for single quote with special character.
     * Input: 'Travel time 15`'
     * Expected output: Travel time 15`
     */
    @Test
    void parseArguments_SingleQuoteWithSpecial_ShouldOutputSpecialCharacter() {
        List<String> args = Arrays.asList("'Travel time 15`'");
        List<String> expected = Arrays.asList("Travel time 15`");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(actual, expected);
    }

    /**
     * Test case for multiple arguments.
     * Input: arg1 'arg2' arg3
     * Expected output: arg1 arg2 arg3
     */
    @Test
    void parseArguments_MultipleArguments_ShouldOutputMultipleArguments() {
        List<String> args = Arrays.asList("arg1", "\'arg2\'", "arg3");
        List<String> expected = Arrays.asList("arg1", "arg2", "arg3");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(actual, expected);
    }

    /**
     * Test case for blank input.
     * Input: " "
     * Expected output: " "
     */
    @Test
    void parseArguments_SpaceInput_ShouldOutputSingularSpace() {
        List<String> args = Arrays.asList("\" \"");
        List<String> expected = Arrays.asList(" ");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(actual, expected);
    }

    /**
     * Test case for empty input.
     * Input: ""
     * Expected output: ""
     */
    @Test
    void parseArguments_ShouldEmptyArgumentsInput_EmptyOutput() {
        List<String> args = Arrays.asList("");
        List<String> expected = new LinkedList<String>();
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(actual, expected);
    }

    /**
     * Test case for null input.
     * Input: null
     * Expected output: NullPointerException
     */
    @Test
    void parseArguments_NullInput_ShouldThrowNullPointerException() {
        List<String> args = null;
        assertThrows(NullPointerException.class, () -> {
            argumentResolver.parseArguments(args);
        });
    }

    /**
     * Test case for backquotes.
     * Input: echo "This is space:`echo ' '`."
     * Expected output: echo This is space: .
     * Assumption: The additional space is handled in a
     * command builder and trimmed before it is output to stdout.
     */
    @Test
    void parseArguments_RemoveBackquotes_ShouldOutputSpecialCharacter() {
        List<String> args = Arrays.asList("echo \"This is space:`echo \' \'`.\"");
        List<String> expected = Arrays.asList("echo This is space:  .");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(actual, expected);
    }

    /**
     * Test case for command substituition.
     * Input: "`echo 'ccp'`"
     * Expected output: ccp
     */
    @Test
    void parseArguments_CommandSubstituition_ShouldOutputText() {
        List<String> args = Arrays.asList("`echo 'ccp'`");
        List<String> expected = Arrays.asList("ccp");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(expected, actual);
    }

    /**
     * Test case for command substituition with mixing quotes.
     * Input: "knn `echo '\"nb'`"
     * Expected output: knn "nb
     */
    @Test
    void parseArguments_testMixingQuotes_ShouldOutputQuotes() {
        List<String> args = Arrays.asList("\"knn `echo '\"nb'`");
        List<String> expected = Arrays.asList("knn \"nb ");
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(expected, actual);
    }

    /**
     * Test case for command substituition with special characters.
     * Input: {special character}
     * Expected output: special character
     */
    @ParameterizedTest
    @ValueSource(strings = {"<", ">", "?", ":", "|", "&", "[", "]", "{"})
    void parseArguments_WeirdSymbolsInput_ShouldOutputCharacters(String pattern) {
        List<String> args = Arrays.asList(pattern);
        List<String> expected = Arrays.asList(pattern);
        List<String> actual = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        assertEquals(expected, actual);
    }

    /**
     * Test globbing with asterisk after existing directory.
     * Expected: Should expand argument into files within specified directory.
     * Argument: `testDir/*`
     */
    @Test
    void parseArguments_asteriskAfterDirectory_shouldExpandToMatchingFilesWithinDirectory() {
        List<String> args = Arrays.asList("testDir/*");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testDir.getName() + File.separator + innerTestDir.getName(),
                testDir.getName() + File.separator + testFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk.
     * Expected: Should expand argument into files within current directory.
     * Argument: `*`
     */
    @Test
    public void parseArguments_onlyAsterisk_shouldExpandToMatchingFiles() {
        List<String> args = Arrays.asList("*");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        List<String> expectedArgs = Arrays.asList(testDir.getName(), testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }


    /**
     * Test globbing with asterisk in between valid characters.
     * Expected: Should expand argument into files within current directory.
     * Argument: `test*.txt`
     */
    @Test
    public void parseArguments_globBetweenChar_shouldExpandToMatchingFile() {
        List<String> args = Arrays.asList("test*.txt");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        List<String> expectedArgs = Arrays.asList(testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk in between valid characters with absolute path.
     * Expected: Should expand argument into file within current directory.
     * Argument: `/Users/.../rootTestDir/test*.txt`
     */
    @Test
    public void parseArguments_globBetweenCharAbsolutePath_shouldExpandToMatchingFile() {
        List<String> args = Arrays.asList(rootTestDir.toString() + File.separator + "test*.txt");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        List<String> expectedArgs = Arrays.asList(rootTestDir.toString() + File.separator + testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk after absolute path.
     * Expected: Should expand argument into files within specified path.
     * Argument: `/Users/.../rootTestDir/*`
     */
    @Test
    public void parseArguments_globInDirectoryAbsolutePath_shouldExpandToFileInDirectory() {
        List<String> args = Arrays.asList(rootTestDir.toString() + File.separator + "*");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(rootTestDir.toString() + File.separator + testDir.getName(),
                rootTestDir.toString() + File.separator + testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk before and after valid characters.
     * Expected: Should expand argument into matching file in current directory.
     * Argument: `*Txt*`
     */
    @Test
    public void parseArguments_globBeforeAndAfterChar_shouldExpandToMatchingFile() {
        List<String> args = Arrays.asList("*Txt*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk before and in between valid characters.
     * Expected: Should expand argument into matching file in current directory.
     * Argument: `*Txt*.txt`
     */
    @Test
    public void parseArguments_globBeforeAndInBetweenChar_shouldExpandToMatchingFile() {
        List<String> args = Arrays.asList("*Txt*.txt");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk after empty directory.
     * Expected: Should leave argument unchanged.
     * Argument: `testDir/innerTestDir/*`
     */
    @Test
    public void parseArguments_globInEmptyDirectory_shouldLeaveArgumentUnchanged() {
        List<String> args = Arrays.asList("testDir/innerTestDir/*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        assertEquals(args, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters.
     * Expected: Should expand argument into files with matching prefix in current directory.
     * Argument: `test*`
     */
    @Test
    public void parseArguments_globAfterPrefix_shouldExpandToFilesWithMatchingPrefix() {
        List<String> args = Arrays.asList("test*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testDir.getName(), testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk before valid characters.
     * Expected: Should expand argument into files with matching suffix in current directory.
     * Argument: `*Dir`
     */
    @Test
    public void parseArguments_globBeforeSuffix_shouldExpandToFilesWithMatchingSuffix() {
        List<String> args = Arrays.asList("*Dir");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testDir.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk before extension.
     * Expected: Should expand argument into files with matching extension in current directory.
     * Argument: `*.txt`
     */
    @Test
    public void parseArguments_globExtension_shouldExpandToFilesWithMatchingSuffix() {
        List<String> args = Arrays.asList("*.txt");
        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in specified directory.
     * Expected: Should expand argument into files with matching prefix in specified directory.
     * Argument: `testDir/test*`
     */
    @Test
    public void parseArguments_globAfterPrefixInDirectory_shouldExpandToFilesWithPrefixInDirectory() {
        List<String> args = Arrays.asList("testDir/test*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testDir.getName() + File.separator + testFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in specified directory
     * where there are no matching files.
     * Expected: Should leave argument unchanged
     * Argument: `testDir/p*`
     */
    @Test
    public void parseArguments_globAfterPrefixInDirectoryNoMatchingFiles_shouldLeaveArgumentUnchanged() {
        List<String> args = Arrays.asList("testDir/p*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        assertEquals(args, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in current directory
     * where there are no matching files.
     * Expected: Should leave argument unchanged
     * Argument: `p*`
     */
    @Test
    public void parseArguments_globAfterPrefixNoMatchingFiles_shouldLeaveArgumentUnchanged() {
        List<String> args = Arrays.asList("p*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        assertEquals(args, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in current directory
     * where there are files with same letters but different capitalisation.
     * Expected: Should leave argument unchanged
     * Argument: `Test*`
     */
    @Test
    public void parseArguments_globWithCapitalisedPrefix_shouldLeaveArgumentUnchanged() {
        List<String> args = Arrays.asList("Test*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        assertEquals(args, parsedArgs);
    }

    /**
     * Test globbing with asterisk before and after valid characters in current directory
     * where there are files with same letters but different capitalisation.
     * Expected: Should leave argument unchanged
     * Argument: `*tdi*`
     */
    @Test
    public void parseArguments_globInBetweenCharWithDifferentCapitalisation_shouldLeaveArgumentUnchanged() {
        List<String> args = Arrays.asList("*tdi*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });

        assertEquals(args, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in current directory
     * where characters match exactly with directory name.
     * Expected: Should expand to matching directory name as forward slash is considered
     * part of directory name.
     * Argument: `testDir*`
     */
    @Test
    public void parseArguments_globAfterExactDirectoryNameMatch_shouldExpandToMatchingDirectory() {
        List<String> args = Arrays.asList("testDir*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testDir.getName());

        assertEquals(expectedArgs, parsedArgs);
    }

    /**
     * Test globbing with asterisk after valid characters in current directory
     * where characters match exactly with file name.
     * Expected: Should expand to matching file name as forward slash is considered
     * part of file name.
     * Argument: `testTxtFile.txt*`
     */
    @Test
    public void parseArguments_globAfterExactFileNameMatch_shouldExpandToMatchingFile() {
        List<String> args = Arrays.asList("testTxtFile.txt*");

        List<String> parsedArgs = assertDoesNotThrow(() -> {
            return argumentResolver.parseArguments(args);
        });
        List<String> expectedArgs = Arrays.asList(testTxtFile.getName());

        assertEquals(expectedArgs, parsedArgs);
    }
}
