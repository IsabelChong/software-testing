package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;

@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument {
    private StringBuilder plaintext;
    private StringBuilder regex;
    private boolean isReg;

    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.isReg = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }

    // Used for `find` command.
    // `text` here corresponds to the folder that we want to look in.
    public RegexArgument(String str, String text, boolean isRegex) {
        this();
        this.plaintext.append(text);
        this.isReg = isRegex;
        this.regex.append(".*"); // We want to match filenames
        for (char c : str.toCharArray()) {
            if (c == CHAR_ASTERISK) {
                regex.append("[^");
                regex.append(StringUtils.fileSeparator());
                regex.append("]*");
            } else {
                this.regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
    }

    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }

    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append("[^");
        regex.append(StringUtils.fileSeparator());
        regex.append("]*");
        isReg = true;
    }

    public void merge(RegexArgument other) {
        plaintext.append(other.plaintext);
        regex.append(other.regex);
        isReg = isReg || other.isReg;
    }

    public void merge(String str) {
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (isReg) {
            Pattern regexPattern = Pattern.compile(regex.toString());
            String dir = "";
            String tokens[] = plaintext.toString().replaceAll("\\\\", "/").split("/");
            for (int i = 0; i < tokens.length - 1; i++) {
                dir += tokens[i] + File.separator;
            }

            Path currPath = new File(dir).toPath();
            File currentDir = new File("");
            if (currPath.isAbsolute()) {
                currentDir = currPath.toFile();
            } else {
                currentDir = Paths.get(Environment.currentDirectory + File.separator + dir).toFile();
            }

            if (tokens[0].equals("*") && !plaintext.toString().equals("*")) {
                for (String fileName : currentDir.list()) {
                    File file = new File(currentDir + File.separator + fileName);
                    if (file.isDirectory()) {
                        globbedFiles.add(fileName);
                    }
                }
                return globbedFiles;
            } else {
                for (String candidate : currentDir.list()) {
                    if (regexPattern.matcher(dir + candidate).matches()) {
                        globbedFiles.add(dir + candidate);
                    }
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }


    /**
     * Traverses a given File node and returns a list of absolute path that match the given regexPattern.
     * <p>
     * Assumptions:
     * - ignores files and folders that we do not have access to (insufficient read permissions)
     * - regexPattern should not be null
     *
     * @param regexPattern    Pattern object
     * @param node            File object
     * @param isAbsolute      Boolean option to indicate that the regexPattern refers to an absolute path
     * @param onlyDirectories Boolean option to list only the directories
     */
    private List<String> traverseAndFilter(Pattern regexPattern, File node, boolean isAbsolute, boolean onlyDirectories) {
        List<String> matches = new ArrayList<>();
        if (regexPattern == null || !node.canRead() || !node.isDirectory()) {
            return matches;
        }
        for (String current : node.list()) {
            File nextNode = new File(node, current);
            String match = isAbsolute
                    ? nextNode.getPath()
                    : nextNode.getPath().substring(Environment.currentDirectory.length() + 1);
            // TODO: Find a better way to handle this.
            if (onlyDirectories && nextNode.isDirectory()) {
                match += File.separator;
            }
            if (!nextNode.isHidden() && regexPattern.matcher(match).matches()) {
                matches.add(nextNode.getAbsolutePath());
            }
            matches.addAll(traverseAndFilter(regexPattern, nextNode, isAbsolute, onlyDirectories));
        }
        return matches;
    }

    public boolean isRegex() {
        return isReg;
    }

    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    public String toString() {
        return plaintext.toString();
    }
}
