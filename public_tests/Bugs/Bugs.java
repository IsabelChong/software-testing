package Bugs; //NOPMD

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/*
    Class containing all the bugs issued to us on Github from the Hackathon.
*/
public class Bugs { //NOPMD
    public static final String FAIL_PREFIX = "Unexpected exception: ";
    private final InputStream originalIn = System.in;
    private static final String GLOB_TEST_DIR = "globTestFolder";
    private static final String GLOB_INNER_DIR = "globInnerTestFolder";
    private static final String GLOB_INNER_FILE = "globInnerTestFile.txt";
    private static final String LS_TEST_FILE = "lsTest.txt";
    private static final String LS_TEST_DIR = "lsTest";
    private static final String LS_INNER_TSTFLE = "lsInnerTestFile.txt";
    private static final String LS_TEST_FILE_A = "a.txt";
    private static final String LS_TEST_FILE_B = "b.txt";
    private static final String LS_TEST_DIR_D = "d";
    private static final String CUT_TEST_FILE = "cutTestFile.txt";
    private static final String GREP_TEST_FILE = "grepTestFile.txt";
    private static final String CAT_TEST_DIR = "catTestFolder";
    private static final String CAT_FILE_A = "a.txt";
    private static final String CAT_FILE_B = "b.txt";
    private static final String CAT_A_CONTENT = "a";
    private static final String CAT_B_CONTENT = "b\n";

    private static final String SORT_TEST_DIR = "sortTestFolder";
    private static final String SORT_FILE_NUMS = "nums.txt";
    private static final String SORT_NUMS_CONTENT = "31\n3\n96\n57\n66\n14\n75\n20\n74\n99\n-3\n55\n64\n" +
            "  -18\n  56\n48\n-1\n90\n89\n15\n85\n82\n9\n-20\n57\n87\n46\n74\n-11\n94\n91\n73\n-9\n42\n76\n-8\n" +
            "83\n65\n85\n23\n93\n68\n10\n1\n66\n1\n-5\n3\n57\n84\n12\n28\n10\n54\n29\n72\n 1\n56\n34\n-4\n-3\n-5\n" +
            "26\n86\n39\n96\n25\n22\n38\n99\n94\n-13\n5\n-11\n65\n49\n16\n47\n55\n84\n72\n66\n71\n-19\n29\n8\n15\n" +
            "-14\n93\n94\n95\n18\n97\n19\n93\n9\n66\n59\n-16\n61\n";

    private static final String UNIQ_TEST_DIR = "uniqTestFolder";
    private static final String UNIQ_FILE_ALICE = "alice-bob.txt";
    private static final String UNIQ_ALICE_CONTENT = "Hello World\nHello World\nAlice\n" + // NOPMD
            "Alice\nBob\nAlice\nBob\n";

    // variables
    private ShellImpl shell;
    private static File testDir;
    private ByteArrayOutputStream outputStream;
    private String originPath;

    @BeforeEach
    public void setUp() throws IOException {
        shell = new ShellImpl();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("Original Input Stream".getBytes());
        outputStream = new ByteArrayOutputStream();
        originPath = Environment.currentDirectory;
        ArgumentResolver argumentResolver = new ArgumentResolver();
        ApplicationRunner appRunner = new ApplicationRunner();

        Files.createDirectory(Paths.get(GLOB_TEST_DIR));
        Files.createDirectory(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR));
        Files.createFile(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_FILE));

        Files.createFile(Paths.get(LS_TEST_FILE));
        Files.createDirectory(Paths.get(LS_TEST_DIR));
        Files.createFile(Paths.get(LS_TEST_DIR + File.separator + LS_INNER_TSTFLE));

        Files.createFile(Paths.get(LS_TEST_FILE_A));
        Files.createFile(Paths.get(LS_TEST_FILE_B));
        Files.createDirectory(Paths.get(LS_TEST_DIR_D));
        Files.createFile(Paths.get(LS_TEST_DIR_D + File.separator + LS_TEST_FILE_A));
        Files.createFile(Paths.get(LS_TEST_DIR_D + File.separator + LS_TEST_FILE_B));

        Files.writeString(Paths.get(CUT_TEST_FILE), "a");

        Files.writeString(Paths.get(GREP_TEST_FILE), "apple\nbanana\norange\n");

        // Create CAT folder and files
        Files.createDirectory(Paths.get(CAT_TEST_DIR));
        Path aTxtPath = Files.createFile(Paths.get(CAT_TEST_DIR + File.separator + CAT_FILE_A));
        Path bTxtPath = Files.createFile(Paths.get(CAT_TEST_DIR + File.separator + CAT_FILE_B));

        // Write to CAT files
        Files.write(aTxtPath, CAT_A_CONTENT.getBytes(StandardCharsets.UTF_8));
        Files.write(bTxtPath, CAT_B_CONTENT.getBytes(StandardCharsets.UTF_8));

        // Create SORT folder and files
        Files.createDirectory(Paths.get(SORT_TEST_DIR));
        Path numPath = Files.createFile(Paths.get(SORT_TEST_DIR + File.separator + SORT_FILE_NUMS));

        // Write to SORT files
        Files.write(numPath, SORT_NUMS_CONTENT.getBytes(StandardCharsets.UTF_8));

        // Create UNIQ folder and files
        Files.createDirectory(Paths.get(UNIQ_TEST_DIR));
        Path alicePath = Files.createFile(Paths.get(UNIQ_TEST_DIR + File.separator + UNIQ_FILE_ALICE));

        // Write to UNIQ files
        Files.write(alicePath, UNIQ_ALICE_CONTENT.getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    public void restoreStreams() {
        System.setIn(originalIn);
    }


    @AfterEach
    public void tearDown() throws IOException {
        assertDoesNotThrow(() -> {
            outputStream.close();
        });
        Environment.currentDirectory = originPath;
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR + File.separator + GLOB_INNER_FILE));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_FILE));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR));
        Files.deleteIfExists(Paths.get(LS_TEST_FILE));
        Files.deleteIfExists(Paths.get(LS_TEST_DIR + File.separator + LS_INNER_TSTFLE));
        Files.deleteIfExists(Paths.get(LS_TEST_DIR));

        Files.deleteIfExists(Paths.get(LS_TEST_FILE_A));
        Files.deleteIfExists(Paths.get(LS_TEST_FILE_B));
        Files.deleteIfExists(Paths.get(LS_TEST_DIR_D + File.separator + LS_TEST_FILE_A));
        Files.deleteIfExists(Paths.get(LS_TEST_DIR_D + File.separator + LS_TEST_FILE_B));
        Files.deleteIfExists(Paths.get(LS_TEST_DIR_D));

        Files.deleteIfExists(Paths.get(CUT_TEST_FILE));

        Files.deleteIfExists(Paths.get(GREP_TEST_FILE));

        Files.deleteIfExists(Paths.get(CAT_TEST_DIR + File.separator + CAT_FILE_A));
        Files.deleteIfExists(Paths.get(CAT_TEST_DIR + File.separator + CAT_FILE_B));
        Files.deleteIfExists(Paths.get(CAT_TEST_DIR));

        Files.deleteIfExists(Paths.get(SORT_TEST_DIR + File.separator + SORT_FILE_NUMS));
        Files.deleteIfExists(Paths.get(SORT_TEST_DIR));

        Files.deleteIfExists(Paths.get(UNIQ_TEST_DIR + File.separator + UNIQ_FILE_ALICE));
        Files.deleteIfExists(Paths.get(UNIQ_TEST_DIR));
    }

    // Issue #107 - Fixed
    @Test
    public void parseAndEvaluate_catWithStdinBetweenFiles_shouldConcatenateCorrectSequence() {
        try {
            Environment.currentDirectory = CAT_TEST_DIR;
            String input = "hello world\nhello mars";
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            String expectedOutput = "ahello world\nhello mars\nb\n";

            shell.parseAndEvaluate("cat a.txt - b.txt", outputStream);
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    // Issue #115 - Fixed
    @Test
    public void parseAndEvaluate_singleCommandWithSemicolon_shouldEchoHello() {
        try {
            shell.parseAndEvaluate("echo hello;", outputStream);
            String expectedOutput = "hello" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    // Issue #120 - Fixed
    @Test
    public void parseAndEvaluate_globBeforeForwardSlash_shouldExpandToDirectoriesOnly() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("ls */", outputStream);
            String expectedOutput = GLOB_INNER_DIR + ":" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    // Issue #126 - Fixed
    @Test
    public void parseAndEvaluate_cutOutOfBoundsByteRangeWithFileInput_ShouldPrintAllInput() {
        try {
            // Run the cut command on the CUT_TEST_FILE with an out-of-bounds byte range
            shell.parseAndEvaluate("cut -b 1-2 " + CUT_TEST_FILE, outputStream);

            // Assert that the output matches the full input
            String expectedOutput = "a" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(FAIL_PREFIX + e);
        }
    }

    // Issue #127 - Fixed
    @Test
    public void parseAndEvaluate_grepEmptyPattern_shouldReturnAllLines() {
        try {
            shell.parseAndEvaluate("grep '' " + GREP_TEST_FILE, outputStream);
            String expectedOutput = "apple\nbanana\norange\n";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * The bugs below here are all disabled because they are part of the bugs filed in our repo but are not fixed.
     * The bugs we have fixed are above this section.
     */

    // Issue #114 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_globBeforeForwardSlashAndPostfix_shouldExpandToAllDirectoriesWithPostfix() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("echo */.", outputStream);
            String expectedOutput = GLOB_INNER_DIR + "/." + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException | NullPointerException e) {
            fail(e);
        }
    }

    // Issue #116 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_cutSpecificCharsSmallFileWithOutOfBounds_ShouldPrintNothing() {
        try {
            // Run a cut command for specific out-of-bounds character positions
            shell.parseAndEvaluate("cut -c 100,200 " + CUT_TEST_FILE, outputStream);

            // Assert that the output is empty because those positions do not exist in the file
            assertEquals("", outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail("Unexpected exception: " + e);
        }
    }

    // Issue #117 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_cutLargeCharPosition_ShouldPrintNothing() {
        try {
            // Simulate a cut command with an extremely large character position
            shell.parseAndEvaluate("cut -c 5294967296 " + CUT_TEST_FILE, outputStream);

            // Assert that the output is empty because no such character position exists
            assertEquals("", outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(FAIL_PREFIX + e);
        }
    }

    // Issue #121 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_lsWildcardMatchFilesStartingWithLs_ShouldOutputCorrectOrder() {
        try {
            // Execute the ls command with a wildcard pattern that matches filenames starting with 'ls'
            shell.parseAndEvaluate("ls ls*", outputStream);

            // Construct the expected output
            String expectedOutput = LS_TEST_FILE + System.lineSeparator() + System.lineSeparator() +
                    LS_TEST_DIR + ":" + System.lineSeparator() +
                    LS_INNER_TSTFLE + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(FAIL_PREFIX + e);
        }
    }

    // Issue #123 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_lsWithFileAndFolder_ShouldOutputAllFiles() {
        try {
            // Execute the ls command with a wildcard pattern that matches filenames starting with 'ls'
            shell.parseAndEvaluate("ls -R a.txt b.txt d", outputStream);

            // Construct the expected output
            String expectedOutput = LS_TEST_FILE_A + System.lineSeparator() +
                    LS_TEST_FILE_B + System.lineSeparator() + System.lineSeparator() +
                    LS_TEST_DIR_D + ":" + System.lineSeparator() +
                    LS_TEST_FILE_A + System.lineSeparator() +
                    LS_TEST_FILE_B + System.lineSeparator();

            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(FAIL_PREFIX + e);
        }
    }

    // Issue #124 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_sortNumsWithSpacesBeforeNums_shouldSortCorrectly() {
        try {
            Environment.currentDirectory = SORT_TEST_DIR;
            String expectedOutput = "-20\n-19\n  -18\n-16\n-14\n-13\n-11\n-11\n-9\n-8\n-5\n-5\n-4\n-3\n-3\n-1\n" +
                    "1\n1\n1\n3\n3\n5\n8\n9\n9\n10\n10\n12\n14\n15\n15\n16\n18\n19\n20\n22\n23\n" +
                    "25\n26\n28\n29\n29\n31\n34\n38\n39\n42\n46\n47\n48\n49\n54\n55\n55\n  56\n56\n57\n57\n57\n59\n61\n" +
                    "64\n65\n65\n66\n66\n66\n66\n68\n71\n72\n72\n73\n74\n74\n75\n76\n82\n83\n84\n84\n85\n85\n86\n" +
                    "87\n89\n90\n91\n93\n93\n93\n94\n94\n94\n95\n96\n96\n97\n99\n99";

            // Note: The command has been altered from issue #124 for the path to obtain the nums file, but the
            // input is the same and hence a valid and related test case to test the correctness of sorting.
            shell.parseAndEvaluate("sort -n nums.txt", outputStream);
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    // Issue #133 - Not Fixed
    @Disabled
    @Test
    public void parseAndEvaluate_uniqWithDdcFlag_shouldReturnCorrectOutput() {
        try {
            Environment.currentDirectory = UNIQ_TEST_DIR;
            String expectedOutput = "   1 Hello World\n" +
                    "   1 Hello World\n" +
                    "   1 Alice\n" +
                    "   1 Alice\n";

            // Note: The command has been altered from issue #133 for the path to obtain the alice-bob file, but the
            // input is the same and hence a valid and related test case to test the correctness of this call.
            shell.parseAndEvaluate("uniq -D -d -c alice-bob.txt", outputStream);
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }
}

