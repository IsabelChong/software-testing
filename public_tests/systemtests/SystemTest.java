package systemtests;

import java.io.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pairwise Testing with the following commands:
 * (echo, exit, redirection, wc, grep, cut, mkdir, cd, tee) [sort, rm] {IO,
 * semicolon}
 * <p>
 * Positive test case (echo, exit, redirection, wc, grep, cut, mkdir, cd, tee)
 * [sort, rm] {IO, semicolon}
 * - echo Hello World! "Hello World!" 'Hello World!'
 * - echo "This is a sample text file." > sample.txt
 * - echo wc -l sample.txt
 * - echo “This is\n a test\n” | grep -H “test”
 * - cut -c 1-9 example.txt | grep -H “example”
 * - mkdir nonexistentdir; cd nonexistentdir
 * - echo “hello” > file1.txt; echo “world” > file2.txt
 * - echo “hello” > file1.txt; echo “world” > file2.txt; sort file1.txt
 * file2.txt
 * - cd echo TestFolder
 * - echo "Line 1\nLine 2" | wc -l
 * - echo "This is a test" > test.txt; grep "test" test.txt
 * - echo "Sample text" > sample.txt; wc -l sample.txt | grep "1"
 * - echo "example text" > example.txt; cut -c 1-7 example.txt | grep "example"
 * - echo "hello" | tee file2.txt
 * - echo "hello world" | tee output.txt > redirection.txt
 * - echo "search me" | grep "search"
 * - echo "one\ntwo\nthree" > lines.txt; wc -l lines.txt | grep "3"
 * - mkdir parent; mkdir parent/child; cd parent/child
 * - echo "findme" > a.txt; echo "also findme" > b.txt; grep "findme" a.txt
 * b.txt
 * - echo "hello" | tee -a 1.txt 2.txt
 * - echo "alpha" > a.txt; sort a.txt | tee b.txt; grep "alpha" b.txt
 * - echo -e "c\nb\na\na" > list.txt; sort list.txt | cut -c 1
 * - mkdir newDir; cd newDir; cd ..
 * - echo "first line of file1" > file1.txt; echo "first line of file2" >
 * file2.txt; paste file1.txt file2.txt
 * - echo "file1 line1" > file1.txt; echo "file2 line1" > file2.txt; echo "file3
 * line1" > file3.txt; paste file1.txt
 * file2.txt file3.txt
 * - echo "stdin line1 stdin line2" | paste -
 * - echo -e "line1\nline2" > file.txt; paste -s file.txt
 * - echo "from stdin" | paste file1.txt - file2.txt
 * - echo "" > empty.txt; echo "non-empty" > nonempty.txt; paste empty.txt nonempty.txt
 * - cat file1.txt file2.txt > merged_file.txt; sort merged_file.txt
 * - cat file1.txt | grep "pattern" | wc -w
 * - cat file1.txt file2.txt | sort | uniq | grep "pattern"
 * - uniq -c file1.txt file2.txt | sort | cat | wc -w
 * - uniq -c file1.txt | sort | uniq -c
 * - wc -l file1.txt file2.txt | cat | sort | uniq
 * - wc -l file1.txt | sort | uniq | wc -l
 * - sort file1.txt file2.txt | uniq -c | cat > output.txt ; wc -l output.txt
 * - sort file1.txt file2.txt | wc -w
 * - echo "" > empty.txt; echo "non-empty" > nonempty.txt; paste empty.txt
 * nonempty.txt
 *
 *
 * <p>
 * <p>
 * NEGATIVE TESTCASES
 * - echo "test" | grep "no match"
 * - echo "text" > /invalid/path/file.txt
 * - echo "data" | tee /invalid/path/output.txt
 * - echo "test" | grep "["
 * - mkdir dir; echo "data" > dir
 * - ; echo "misplaced semicolon"
 * - echo "ambiguous" | tee >
 * paste non_existent_file.txt
 * paste no_read_permission.txt
 * paste -z file1.txt
 */

public class SystemTest {
    // Constants
    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";// NOPMD
    private static final String FILE3 = "file3.txt";
    private static final String FILE4 = "file4.txt";

    private static final String TEST_FOLDER = "TestFolder";
    private static final String TEST_FOLDER1 = "TestFolder1";

    private static final String TEMP_FILE = "tmpfile.txt";
    private static final String EXAMPLE_FILE = "example.txt";
    private static final String EXAMPLE_CONTENT = "example text";

    private static final String FILE1_CONTENT = "line1-file1\nline2-file1\nline3-file1";
    private static final String FILE2_CONTENT = "bbc\ncca\naab";
    private static final String NON_EXISTENT_DIR = "nonexistentdir";
    private static final String NEW_DIR = "newdir2";
    private static final String SAMPLE_FILE = "sample.txt";
    private static final String SAMPLE_TEXTS = "This is a sample text file.";
    private static final String RENAMED_DIR = "newdir3";
    private static final String GLOB_TEST_DIR = "globTestFolder";
    private static final String GLOB_INNER_DIR = "globInnerTestFolder";
    private static final String GLOB_INNER_FILE = "globInnerTestFile.txt";
    public static final String SPACE_2 = "       2";

    // variables
    private ShellImpl shell;
    private ByteArrayOutputStream outputStream;
    private String originPath;
    private File file1;
    private File file2;
    private File file3;

    @BeforeEach
    public void setUp() throws IOException {
        shell = new ShellImpl();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("Original Input Stream".getBytes());
        outputStream = new ByteArrayOutputStream();
        originPath = Environment.currentDirectory;
        ArgumentResolver argumentResolver = new ArgumentResolver();
        ApplicationRunner appRunner = new ApplicationRunner();

        Files.createFile(Paths.get(FILE1));
        Files.writeString(Paths.get(FILE1), FILE1_CONTENT);
        Files.writeString(Paths.get(FILE2), FILE2_CONTENT);
        ;
        Files.createFile(Paths.get(EXAMPLE_FILE));
        Files.writeString(Paths.get(EXAMPLE_FILE), EXAMPLE_CONTENT);
        Files.createFile(Paths.get(SAMPLE_FILE));
        Files.writeString(Paths.get(SAMPLE_FILE), SAMPLE_TEXTS);
        Files.createDirectory(Paths.get(TEST_FOLDER));
        Files.createDirectory(Paths.get(GLOB_TEST_DIR));
        Files.createDirectory(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR));
        Files.createFile(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_FILE));
        Files.writeString(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_FILE),
                "b\na\nc\n");
    }

    @AfterEach
    public void tearDown() throws IOException {
        assertDoesNotThrow(() -> {
            outputStream.close();
        });
        Environment.currentDirectory = originPath;

        Files.deleteIfExists(Paths.get(FILE1));
        Files.deleteIfExists(Paths.get(FILE2));
        Files.deleteIfExists(Paths.get(FILE3));
        Files.deleteIfExists(Paths.get(FILE4));
        Files.deleteIfExists(Paths.get(EXAMPLE_FILE));
        Files.deleteIfExists(Paths.get(TEMP_FILE));
        Files.deleteIfExists(Paths.get(TEST_FOLDER));
        Files.deleteIfExists(Paths.get(TEST_FOLDER1));
        Files.deleteIfExists(Paths.get(NON_EXISTENT_DIR));
        Files.deleteIfExists(Paths.get(NEW_DIR));
        Files.deleteIfExists(Paths.get("test.txt"));
        Files.deleteIfExists(Path.of(originPath + File.separator + "parent" + File.separator + "child"));
        Files.deleteIfExists(Path.of(originPath + File.separator + "parent"));
        Files.deleteIfExists(Paths.get("a.txt"));
        Files.deleteIfExists(Paths.get("b.txt"));
        Files.deleteIfExists(Paths.get("1.txt"));
        Files.deleteIfExists(Paths.get("lines.txt"));
        Files.deleteIfExists(Paths.get("sample1.txt"));
        Files.deleteIfExists(Paths.get(SAMPLE_FILE));
        Files.deleteIfExists(Paths.get("empty.txt"));
        Files.deleteIfExists(Paths.get("nonempty.txt"));
        Files.deleteIfExists(Paths.get("file.txt"));
        Files.deleteIfExists(Paths.get(RENAMED_DIR));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR + File.separator + GLOB_INNER_FILE));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR + File.separator + GLOB_INNER_FILE));
        Files.deleteIfExists(Paths.get(GLOB_TEST_DIR));
    }

    /**
     * Test Case: echo Hello World! "Hello World!" 'Hello World!'
     * Expected: Should run the specified application, handling quoting of double
     * and single quotes.
     */
    @Test
    public void parseAndEvaluate_mixUnquotedAndQuotedArgs_shouldRemoveQuotes() {
        try {
            shell.parseAndEvaluate("echo Hello World! \"Hello World!\" 'Hello World!'", outputStream);
            String expectedOutput = "Hello World! Hello World! Hello World!" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "This is a sample text file." > sample.txt
     * Expected: Should create a file with the specified content.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirection_shouldCreateFileWithContent() {
        try {
            shell.parseAndEvaluate("echo \"This is a sample text file.\" > sample1.txt", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals("This is a sample text file." + System.lineSeparator(),
                    Files.readString(Paths.get("sample1.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo `wc -l sample.txt`
     * Expected: Should handle command substitution and echo output.
     */
    @Test
    public void parseAndEvaluate_echoWithBackQuote_shouldRunCommand() {
        try {
            shell.parseAndEvaluate("echo `wc -l sample.txt`", outputStream);
            String expectedOutput = "0 sample.txt" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file1.txt | grep -i "Line3"
     * Expected: Should run the specified application, handling quoting of double
     * and single quotes.
     */
    @Test
    public void parseAndEvaluate_catWithPipeAndGrep_shouldPrintMatchingLine() {
        try {
            shell.parseAndEvaluate("cat file1.txt | grep -i \"Line3\"", outputStream);
            String expectedOutput = "line3-file1" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cut -c 1-9 example.txt | grep -H “example”
     * Expected: Should run the specified application, handling quoting of double
     * and single quotes.
     */
    @Test
    public void parseAndEvaluate_cutWithPipeAndGrep_shouldRunCommand() {
        try {
            shell.parseAndEvaluate("cut -c 1-9 example.txt | grep -H \"example\"", outputStream);
            String expectedOutput = "(standard input): example t" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: mkdir nonexistentdir; cd nonexistentdir
     * Expected: Should create a directory and change the current directory to the
     * created directory.
     */
    @Test
    public void parseAndEvaluate_mkdirAndCd_shouldCreateAndChangeDirectory() {
        try {
            shell.parseAndEvaluate("mkdir newdir2; cd newdir2", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(Environment.currentDirectory, originPath + File.separator + "newdir2");
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo “hello” > file1.txt; echo “world” > file2.txt
     * Expected: Should create two files with the specified content.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionInSequence_shouldCreateFileWithContent() {
        try {
            shell.parseAndEvaluate("echo \"hello\" > file1.txt; echo \"world\" > file2.txt", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals("hello" + System.lineSeparator(), Files.readString(Paths.get(FILE1)));// NOPMD
            assertEquals("world" + System.lineSeparator(), Files.readString(Paths.get("file2.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo “hello” > file1.txt; echo “world” > file2.txt; sort file1.txt
     * file2.txt
     * Expected: Should create two files with the specified content and sort the
     * content of the files.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionInSequence_shouldCreateFileWithContentAndSort() {
        try {
            shell.parseAndEvaluate("echo \"hello\" > 1.txt; echo \"world\" > file2.txt; sort 1.txt file2.txt",
                    outputStream);
            String expectedOutput = "hello" + System.lineSeparator() + "world" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cd `echo TestFolder`
     * Expected: Should change the current directory to the specified directory.
     */
    @Test
    public void parseAndEvaluate_cdWithBackQuote_shouldChangeDirectory() {
        try {
            shell.parseAndEvaluate("cd `echo TestFolder`", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(Environment.currentDirectory, originPath + File.separator + "TestFolder");
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file1.txt | wc -l
     * Expected: Should print number of lines in file1.txt.
     */
    @Test
    public void parseAndEvaluate_catWithPipeAndWc_shouldRunCommand() {
        try {
            String commandString = "cat file1.txt | wc -l";
            shell.parseAndEvaluate(commandString, outputStream);
            String expectedOutput = SPACE_2 + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "This is a test" > test.txt; grep "test" test.txt
     * Expected: Should create a file with the specified content and search for the
     * specified string in the file.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndGrep_shouldCreateFileWithContentAndSearch() {
        try {
            shell.parseAndEvaluate("echo \"This is a test\" > test.txt; grep \"test\" test.txt", outputStream);
            String expectedOutput = "This is a test" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "Sample text" > sample.txt; wc -l sample.txt | grep "1"
     * Expected: Should create a file with the specified content and count the
     * number of lines in the file.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndWcAndGrep_shouldCreateFileWithContentAndCountLines() {
        try {
            shell.parseAndEvaluate("echo \"Sample text\" > sample.txt; wc -l sample.txt | grep \"1\"", outputStream);
            String expectedOutput = "       1 sample.txt" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "example text" > example.txt; cut -c 1-7 example.txt | grep
     * "example"
     * Expected: Should create a file with the specified content and cut the content
     * of the file and search for the specified string in the file.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndCutAndGrep_shouldCreateFileWithContentAndCutAndSearch() {
        try {
            shell.parseAndEvaluate("echo \"example text\" > example.txt; cut -c 1-7 example.txt | grep \"example\"",
                    outputStream);
            String expectedOutput = "example" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "hello" | tee file2.txt
     * Expected: Should create a file with the specified content.
     */
    @Test
    public void parseAndEvaluate_echoWithTee_shouldCreateFileWithContent() {
        try {
            shell.parseAndEvaluate("echo \"hello\" | tee file2.txt", outputStream);
            String expectedOutput = "hello" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals("hello" + System.lineSeparator(), Files.readString(Paths.get("file2.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "hello world" | tee output.txt > redirection.txt
     * Expected: Should create a file with the specified content.
     */
    @Test
    public void parseAndEvaluate_echoWithTeeAndRedirection_shouldCreateFileWithContent() {
        try {
            shell.parseAndEvaluate("echo \"hello world\" | tee file1.txt > file2.txt", outputStream);
            String expectedOutput = "hello world" + System.lineSeparator();
            assertEquals("hello world" + System.lineSeparator(), Files.readString(Paths.get(FILE1)));
            assertEquals("hello world" + System.lineSeparator(), Files.readString(Paths.get("file2.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "search me" | grep "search"
     * Expected: Should print the line provided to echo as argument.
     */
    @Test
    public void parseAndEvaluate_echoPipeToGrep_shouldRunCommand() {
        try {
            shell.parseAndEvaluate("echo \"search me\" | grep \"search\"", outputStream);
            String expectedOutput = "search me" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file1.txt > lines.txt; wc -l lines.txt | grep "3"
     * Expected: Should create a file with the specified content and count the
     * number of lines in the file.
     */
    // TODO: fix spacings
    @Test
    public void parseAndEvaluate_catWithRedirectionAndWcAndGrep_shouldCreateFileWithContentAndCountLines() {
        try {
            shell.parseAndEvaluate("cat file1.txt > lines.txt; wc -l lines.txt | grep \"3\"", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: mkdir parent; mkdir parent/child; cd parent/child
     * Expected: Should create a directory and change the current directory to the
     * created directory, with sequential execution of commands.
     */
    @Test
    public void parseAndEvaluate_mkdirAndCdWithSemicolon_shouldCreateAndChangeDirectory() {
        try {
            shell.parseAndEvaluate("mkdir parent; mkdir parent/child; cd parent/child", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(Environment.currentDirectory,
                    originPath + File.separator + "parent" + File.separator + "child");
        } catch (AbstractApplicationException | IOException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "findme" > a.txt; echo "also findme" > b.txt; grep "findme"
     * a.txt b.txt
     * a.txt b.txt
     * Expected: Should create two files with the specified content and search for
     * the specified string in the files.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndGrepAndSemicolon_shouldCreateFileWithContentAndSearch() {
        try {
            shell.parseAndEvaluate("echo \"findme\" > a.txt; echo \"also findme\" > b.txt; grep \"findme\" a.txt b.txt",
                    outputStream);
            String expectedOutput = "a.txt: findme" + System.lineSeparator() + "b.txt: also findme"
                    + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "hello" | tee -a 3.txt 4.txt
     * Expected: Should create two files with the specified content.
     */
    @Test
    public void parseAndEvaluate_echoWithTeeAndAppend_shouldCreateFileWithContent() {
        try {
            shell.parseAndEvaluate("echo \"hello\" | tee -a file3.txt file4.txt", outputStream);
            String expectedOutput = "hello" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals("hello" + System.lineSeparator(), Files.readString(Paths.get("file3.txt")));
            assertEquals("hello" + System.lineSeparator(), Files.readString(Paths.get("file4.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "alpha" > a.txt; sort a.txt | tee b.txt; grep "alpha" b.txt
     * Expected: Should create a file with the specified content and sort the
     * content of the file and search for the specified string in the file.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndSortAndTeeAndGrep_shouldCreateFileWithContentAndSortAndSearch() {
        try {
            shell.parseAndEvaluate("echo \"alpha\" > a.txt; sort a.txt | tee b.txt; grep \"alpha\" b.txt",
                    outputStream);
            String expectedOutput = "alpha" + System.lineSeparator() + "alpha" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file2.txt; sort file2.txt | cut -c 1
     * Expected: Should concatenate the contents of file2.txt, sort the content,
     * and then cut the first character of each line.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndSortAndCut_shouldCreateFileWithContentAndSortAndCut() {
        try {
            shell.parseAndEvaluate("cat file2.txt; sort file2.txt | cut -c 1", outputStream);
            String expectedOutput = "bbc" + System.lineSeparator() + "cca"
                    + System.lineSeparator() + "aaba" + System.lineSeparator() + "b"
                    + System.lineSeparator() + "c" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file1.txt file2.txt > merged_file.txt; sort merged_file.txt
     * Expected: Should concatenate the content of file1.txt and file2.txt into merged_file.txt,
     * and then sort the lines of merged_file.txt.
     */
    @Test
    public void parseAndEvaluate_mkdirAndCdWithSemicolon_shouldCreateAndChangeDirectoryAndChangeBack() {
        try {
            shell.parseAndEvaluate("mkdir " + TEST_FOLDER1 + "; cd " + TEST_FOLDER1 + "; cd ..", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(Environment.currentDirectory, originPath);
        } catch (AbstractApplicationException | IOException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: mkdir newDir; cd newDir; cd ..
     * Expected: Should create a directory and change the current directory to the
     * created directory, and then change the current directory back to the
     * original directory.
     */
    @Test
    public void parseAndEvaluate_catWithRedirectionAndSort_shouldCreateAndChangeDirectoryAndChangeBack() {
        try {
            shell.parseAndEvaluate("cat file1.txt file2.txt > merged_file.txt; " +
                    "sort merged_file.txt", outputStream);
            String expectedOutput = "aab\n" +
                    "cca\n" +
                    "line1-file1\n" +
                    "line2-file1\n" +
                    "line3-file1bbc\n";
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(Environment.currentDirectory, originPath);
            try {
                Files.delete(Paths.get("merged_file.txt"));
            } catch (IOException e) {
                fail("Failed to delete file: " + e.getMessage());
            }
        } catch (AbstractApplicationException | IOException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat input_file.txt | grep "pattern" | wc -w > word_count.txt
     * Expected: Should count the occurrences of the pattern "pattern" in input_file.txt and save the count in word_count.txt.
     */
    @Test
    public void parseAndEvaluate_catWithPipeAndGrepAndWcAndRedirection_shouldCountWordOccurrences() {
        try {
            shell.parseAndEvaluate("cat file1.txt | grep \"pattern\" | wc -w ", outputStream);
            String expectedOutput = "       0" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: sort file1.txt file2.txt | wc -w
     * Expected: Should concatenate the content of file1.txt and file2.txt, sort the lines, and then count the total number of words.
     */
    @Test
    public void parseAndEvaluate_sortWithRedirectionAndWc_shouldConcatenateSortAndCountWords() {
        try {
            shell.parseAndEvaluate("sort file1.txt file2.txt | wc -w", outputStream);
            String expectedOutput = "       6" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: wc -l file1.txt | sort | uniq | wc -l
     * Expected: Should count the number of lines in file1.txt, sort the count,
     * display only the unique count, and then count the number of lines again.
     */
    @Test
    public void parseAndEvaluate_wcWithPipeAndSortAndUniqAndWc_shouldCountUniqueLinesTwice() {
        try {
            shell.parseAndEvaluate("wc -l file1.txt | sort | uniq | wc -l", outputStream);
            String expectedOutput = "1";
            assertEquals(expectedOutput, outputStream.toString().trim());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: cat file1.txt file2.txt | sort | uniq | grep "pattern"
     * Expected: Should concatenate the content of file1.txt and file2.txt,
     * sort the lines, display only the unique lines, and then search for lines containing "pattern".
     */
    @Test
    public void parseAndEvaluate_catWithPipeAndSortAndUniqAndGrep_shouldConcatenateAndSortAndDisplayUniqueLinesAndSearchPattern() {
        try {
            shell.parseAndEvaluate("cat file1.txt file2.txt | sort | uniq | grep \"pattern\"", outputStream);
            String expectedOutput = ""; // No lines should match "pattern"
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: sort file1.txt file2.txt | uniq -c | cat > output.txt ; wc -l output.txt
     * Expected: Should sort the content of file1.txt and file2.txt, count the unique lines with their occurrences,
     * save the output to output.txt, and then count the lines in output.txt.
     */
    @Test
    public void parseAndEvaluate_sortAndUniqAndCatWithRedirectionAndWc_shouldSortAndCountUniqueLinesAndCountOutputLines() {
        try {
            shell.parseAndEvaluate("sort file1.txt file2.txt | uniq -c | cat > output.txt ; wc -l output.txt", outputStream);
            String expectedOutput = "6 output.txt";
            assertEquals(expectedOutput, outputStream.toString().trim());
            try {
                Files.delete(Paths.get("output.txt"));
            } catch (IOException e) {
                fail("Failed to delete file: " + e.getMessage());
            }
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: uniq -c file1.txt file2.txt | sort | cat | wc -w
     * Expected: Should count the occurrences of each unique line in file1.txt and file2.txt,
     * sort the count, concatenate the output, and then count the total number of words in the output.
     */
    @Test
    public void parseAndEvaluate_uniqAndSortAndCatWithRedirectionAndWc_shouldCountOccurrencesAndSortAndCountWords() {
        try {
            shell.parseAndEvaluate("uniq -c file1.txt file2.txt | sort | cat | wc -w", outputStream);
            String expectedOutput = "0"; // Assuming total words count
            assertEquals(expectedOutput, outputStream.toString().trim());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: wc -l file1.txt file2.txt | cat | sort | uniq
     * Expected: Should count the number of lines in file1.txt and file2.txt, concatenate the counts,
     * sort the counts, and then display only the unique count.
     */
    @Test
    public void parseAndEvaluate_wcAndCatAndSortAndUniq_shouldCountLinesAndConcatenateAndSortAndDisplayUniqueCount() {
        try {
            shell.parseAndEvaluate("wc -l file1.txt file2.txt | cat | sort | uniq", outputStream);
            String expectedOutput = "2 file1.txt\n" +
                    "2 file2.txt\n" +
                    "4 total\n";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: uniq -c file1.txt file2.txt | sort | uniq -c
     * Expected: Should count the number of occurrences of each line in file1.txt and file2.txt combined,
     * sort the counts, and then display only the unique count.
     */
    @Test
    public void parseAndEvaluate_uniqWithCountAndSortAndUniq_shouldCountOccurrencesAndSortAndDisplayUniqueCount() {
        try {
            shell.parseAndEvaluate("uniq -c file1.txt | sort | uniq -c", outputStream);
            String expectedOutput = "   1    1 line1-file1\n" +
                    "   1    1 line2-file1\n" +
                    "   1    1 line3-file1\n";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "first line of file1" > file1.txt; echo "first line of file2" > file2.txt; paste file1.txt file2.txt
     * Expected: Should print the content of file1 and file2 side by side.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPaste_shouldPrintContentOfFilesSideBySide() {
        try {
            shell.parseAndEvaluate(
                    "echo \"first line of file1\" > file1.txt; echo \"first line of file2\" > file2.txt; paste file1.txt file2.txt",
                    outputStream);
            String expectedOutput = "first line of file1\tfirst line of file2" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "file1 line1" > file1.txt; echo "file2 line1" > file2.txt;
     * echo "file3 line1" > file3.txt; paste file1.txt file2.txt file3.txt
     * Expected: Should print the content of file1, file2 and file3 side by side.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPasteMultipleFiles_shouldPrintContentOfFilesSideBySide() {
        try {
            shell.parseAndEvaluate(
                    "echo \"file1 line1\" > file1.txt; echo \"file2 line1\" > file2.txt; echo \"file3 line1\" > file3.txt; paste file1.txt file2.txt file3.txt",
                    outputStream);
            String expectedOutput = "file1 line1\tfile2 line1\tfile3 line1" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "stdin line1 stdin line2" | paste -
     * Expected: Should print the content of stdin and file1 side by side.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPasteStdin_shouldPrintContentOfFilesSideBySide() {
        try {
            shell.parseAndEvaluate("echo \"stdin line1 stdin line2\" | paste - file1.txt", outputStream);
            String expectedOutput = "stdin line1 stdin line2\tline1-file1\n" +
                    "\tline2-file1\n" +
                    "\tline3-file1" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo -e "line1\nline2" > file.txt; paste -s file.txt
     * Expected: Should print the content of file.txt in serial mode.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPasteSerial_shouldPrintContentOfFilesSerial() {
        try {
            shell.parseAndEvaluate("echo 'line1 line2' > file.txt; paste -s file.txt", outputStream);
            String expectedOutput = "line1 line2" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "from stdin" | paste file1.txt - file2.txt
     * Expected: Should print the content of stdin and file1 and file2 side by side.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPasteStdinAndFiles_shouldPrintContentOfFilesSideBySide() {
        try {
            shell.parseAndEvaluate("echo \"from stdin\" | paste file1.txt - file2.txt", outputStream);
            String expectedOutput = "line1-file1\tfrom stdin\tbbc\n" +
                    "line2-file1\t\tcca\n" +
                    "line3-file1\t\taab" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "" > empty.txt; echo "non-empty" > nonempty.txt; paste
     * empty.txt nonempty.txt
     * Expected: Should print the content of empty.txt and nonempty.txt side by
     * side.
     */
    @Test
    public void parseAndEvaluate_echoWithRedirectionAndPasteEmptyFile_shouldPrintContentOfFilesSideBySide() {
        try {
            shell.parseAndEvaluate(
                    "echo \"\" > empty.txt; echo \"non-empty\" > nonempty.txt; paste empty.txt nonempty.txt",
                    outputStream);
            String expectedOutput = "\tnon-empty" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "test" | grep "no match"
     * Expected: Should not print anything.
     */
    @Test
    public void parseAndEvaluate_echoWithPipeAndGrepNoMatch_shouldNotPrintAnything() {
        try {
            shell.parseAndEvaluate("echo \"test\" | grep \"no match\"", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo "data" | tee /invalid/path/output.txt
     * Expected: Should throw TeeException.
     */
    @Test
    public void parseAndEvaluate_echoWithTeeToInvalidPath_shouldThrowTeeException() {
        assertThrows(TeeException.class, () -> {
            shell.parseAndEvaluate("echo \"data\" | tee /invalid/path/output.txt", outputStream);
        });
    }

    /**
     * Test Case: paste non_existent_file.txt
     * Expected: Should throw FileNotFoundException.
     */
    @Test
    public void parseAndEvaluate_pasteNonExistentFile_shouldPasteException() {
        assertThrows(PasteException.class, () -> {
            shell.parseAndEvaluate("paste non_existent_file.txt", outputStream);
        });
    }

    /**
     * Test Case: paste no_read_permission.txt
     * Expected: Should throw PasteException.
     */
    @Test
    public void parseAndEvaluate_pasteNoReadPermission_shouldPasteException() {
        assertThrows(PasteException.class, () -> {
            shell.parseAndEvaluate("paste no_read_permission.txt", outputStream);
        });
    }

    /**
     * Test Case: echo * in empty directory.
     * Expected: Should print asterisk.
     */
    @Test
    public void parseAndEvaluate_echoWithWildcardInEmptyDir_shouldEchoAsterisk() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR;
            shell.parseAndEvaluate("echo *", outputStream);
            String expectedOutput = "*" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: echo * in directory with folder and file.
     * Expected: Should print name of folder and file.
     */
    @Test
    public void parseAndEvaluate_echoWithWildcardInNonEmptyDir_shouldEchoFileNames() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("echo *", outputStream);
            String expectedOutput = "globInnerTestFile.txt globInnerTestFolder" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: paste -z file1.txt
     * Expected: Should throw PasteException.
     */
    @Test
    public void parseAndEvaluate_pasteWithInvalidOption_shouldPasteException() {
        assertThrows(PasteException.class, () -> {
            shell.parseAndEvaluate("paste -z file1.txt", outputStream);
        });
    }

    /**
     * Test case: mkdir "newdir"; mv newdir newdir2
     * Expected: Should rename new directory to newdir2
     */
    @Test
    public void parseAndEvaluate_mkdirAndMv_shouldMoveDirectory() {
        try {
            shell.parseAndEvaluate("mkdir newdir; mv newdir newdir2", outputStream);
            assertEquals(Environment.currentDirectory + File.separator + RENAMED_DIR,
                    Paths.get(RENAMED_DIR).toAbsolutePath().toString());
        } catch (AbstractApplicationException | IOException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: sort *
     * Expected: Should print sorted content of all files in the current directory.
     */
    @Test
    public void parseAndEvaluate_sortWithWildcard_shouldSortAllFilesInDirectory() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("sort *.txt", outputStream);
            String expectedOutput = "a" + System.lineSeparator() + "b" + System.lineSeparator() + "c"
                    + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: rm *
     * Expected: Should delete all files in the current directory.
     */
    @Test
    public void parseAndEvaluate_rmWithWildcard_shouldDeleteAllFilesInDirectory() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("rm -r *", outputStream);
            assertEquals("", outputStream.toString());
            assertEquals(0, new File(GLOB_TEST_DIR).listFiles().length);
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /*
     * Test Case: mv *.txt dir (dir exist)
     * Expected: Should move all files with .txt extension to the specified
     * directory.
     */
    @Test
    public void parseAndEvaluate_mvWithWildcardAndExistingDir_shouldMoveFilesToDir() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("mv *.txt " + GLOB_INNER_DIR, outputStream);
            assertEquals("", outputStream.toString());
            assertEquals(1, new File(GLOB_TEST_DIR).listFiles().length);
            assertEquals(1, new File(GLOB_TEST_DIR + File.separator + GLOB_INNER_DIR).listFiles().length);
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }


    /**
     * Test Case: ls | grep "txt"
     * Expected: Should print all files with extension .txt.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrep_shouldPrintFilesWithTxtExtension() {
        try {
            shell.parseAndEvaluate("ls | grep \"txt\"", outputStream);
            String expectedOutput = "example.txt" + System.lineSeparator() + FILE1 + System.lineSeparator() + "file2.txt"
                    + System.lineSeparator() + "sample.txt" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: ls | grep "txt" | cut -c 1-3
     * Expected: Should print all files with extension .txt and cut the first 3
     * characters of each line.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndCut_shouldPrintFilesWithTxtExtensionAndCut() {
        try {
            shell.parseAndEvaluate("ls | grep \"txt\" | cut -c 1-3", outputStream);
            String expectedOutput = "exa" + System.lineSeparator() + "fil" + System.lineSeparator() + "fil" + System.lineSeparator()
                    + "sam" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: ls | grep "file" | sort | wc -l
     * Expected: Should print all files with "file" in the name, sort them, and count the number of lines.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWc_shouldPrintFilesWithFileAndSortAndCount() {
        try {
            shell.parseAndEvaluate("ls | grep \"file\" | sort | wc -l", outputStream);
            String expectedOutput = SPACE_2 + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: ls | grep "file" | sort | wc -l | grep "2"
     * Expected: Should print all files with "file" in the name, sort them, count the number of lines, and print the line with "2".
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWcAndGrep_shouldPrintFilesWithFileAndSortAndCountAndPrintLine() {
        try {
            shell.parseAndEvaluate("ls | grep \"file\" | sort | wc -l | grep \"2\"", outputStream);
            String expectedOutput = SPACE_2 + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }

    /**
     * Test Case: ls | grep "file" | sort | wc -l | tee count.txt
     * Expected: Should print all files with "file" in the name, sort them, count the number of lines, and write the output to count.txt.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWcAndTee_shouldPrintFilesWithFileAndSortAndCountAndWriteToFile() {
        try {
            shell.parseAndEvaluate("ls | grep \"file\" | sort | wc -l | tee count.txt", outputStream);
            String expectedOutput = SPACE_2 + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
            assertEquals(SPACE_2 + System.lineSeparator(), Files.readString(Paths.get("count.txt")));
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    @Test
    public void parseAndEvaluate_test3_shouldPrintFilesWithFileAndSortAndCountAndWriteToFile() {
        try {
            Environment.currentDirectory = GLOB_TEST_DIR;
            shell.parseAndEvaluate("echo hello; echo hello", outputStream);
            String expectedOutput = "hello" + System.lineSeparator() + "hello" + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | ShellException | IOException e) {
            fail(e);
        }
    }

    /**
     * Test Case: ls | grep "nonexistent"
     * Expected: Should not print anything.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepNoMatch_shouldNotPrintAnything() {
        try {
            shell.parseAndEvaluate("ls | grep \"nonexistent\"", outputStream);
            String expectedOutput = "";
            assertEquals(expectedOutput, outputStream.toString());
        } catch (AbstractApplicationException | FileNotFoundException | ShellException e) {
            fail(e);
        }
    }


    /**
     * Test Case: ls | grep "file.txt" | sort | rm
     * Expected: Should throw AbstractApplicationException.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndRm_shouldThrowAbstractApplicationException() {
        assertThrows(AbstractApplicationException.class, () -> {
            shell.parseAndEvaluate("ls | grep \"file.txt\" | sort | rm", outputStream);
        });
    }

    /**
     * Test Case: ls | grep "file.txt" | sort | wc -l | grep "2" | rm
     * Expected: Should throw AbstractApplicationException.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWcAndGrepAndRm_shouldThrowAbstractApplicationException() {
        assertThrows(AbstractApplicationException.class, () -> {
            shell.parseAndEvaluate("ls | grep \"file.txt\" | sort | wc -l | grep \"2\" | rm", outputStream);
        });
    }

    /**
     * Test Case: ls | grep "file.txt" | sort | wc -l | tee count.txt | rm count.txt | rm count.txt
     * Expected: Should throw AbstractApplicationException.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWcAndTeeAndRm_shouldThrowAbstractApplicationException() {
        assertThrows(AbstractApplicationException.class, () -> {
            shell.parseAndEvaluate("ls | grep \"file.txt\" | sort | wc -l | tee count.txt | rm count.txt | rm count.txt", outputStream);
        });
    }

    /**
     * Test Case: ls | grep "file.txt" | sort | wc -l | tee count.txt | rm count.txt | grep "2" | rm count.txt
     * Expected: Should throw AbstractApplicationException.
     */
    @Test
    public void parseAndEvaluate_lsWithPipeAndGrepAndSortAndWcAndTeeAndRmAndGrep_shouldThrowAbstractApplicationException() {
        assertThrows(AbstractApplicationException.class, () -> {
            shell.parseAndEvaluate("ls | grep \"file.txt\" | sort | wc -l | tee count.txt | rm count.txt | grep \"2\" | rm count.txt", outputStream);
        });
    }
}
