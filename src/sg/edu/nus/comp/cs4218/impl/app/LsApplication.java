package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;

public class LsApplication implements LsInterface { //NOPMD

    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;

    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
                                    String... folderName) throws LsException {
        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isSortByExt);
        }

        List<Path> paths;
        if (folderName.length == 0 && isRecursive) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderName);
        }

        return buildResult(paths, isRecursive, isSortByExt).trim();
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws LsException {

        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }


        // Filter out hidden files from args
        List<String> filteredArgs = new ArrayList<>();
        for (String arg : args) {
            if (!arg.startsWith(".")) { // Check if the argument is not a hidden file
                filteredArgs.add(arg);
            }
        }

        LsArgsParser parser = new LsArgsParser();
        try {
            parser.parse(filteredArgs.toArray(new String[0])); // Pass filteredArgs to parser
        } catch (InvalidArgsException e) {
            LsException lsException = new LsException(e.getMessage());
            lsException.initCause(e);
            throw lsException;
        }

        Boolean recursive = parser.isRecursive();
        Boolean sortByExt = parser.isSortByExt();
        String[] directories = parser.getDirectories()
                .toArray(new String[parser.getDirectories().size()]);
        String result = listFolderContent(recursive, sortByExt, directories);

        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            LsException lsException = new LsException(ERR_WRITE_STREAM);
            lsException.initCause(e);
            throw lsException;
        }
    }

    /**
     * Lists only the current directory's content and RETURNS. This does not account for recursive
     * mode in cwd.
     *
     * @param isSortByExt
     * @return
     */
    private String listCwdContent(Boolean isSortByExt) throws LsException {
        String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryException e) {
            LsException lsException = new LsException("Unexpected error occurred!");
            lsException.initCause(e);
            throw lsException;
        }
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths       - list of java.nio.Path objects to list
     * @param isRecursive - recursive mode, repeatedly ls the child directories
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return String to be written to output stream.
     */
    private String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt) {
        StringBuilder result = new StringBuilder();
        for (Path path : paths) {
            try {
                List<Path> contents = getContents(path);
                String formatted = formatContents(contents, isSortByExt);
                String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
                result.append(":\n");
                result.append(formatted);

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(StringUtils.STRING_NEWLINE);
                }

                result.append(StringUtils.STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isRecursive, isSortByExt));
                }
            } catch (InvalidDirectoryException e) {
                // NOTE: This is pretty hackish IMO - we should find a way to change this
                // If the user is in recursive mode, and if we resolve a file that isn't a directory
                // we should not spew the error message.
                //
                // However the user might have written a command like `ls invalid1 valid1 -R`, what
                // do we do then?
                if (!isRecursive) {
                    if (path.toFile().exists()) {
                        result.append(getRelativeToCwd(path).toString());
                    } else {
                        result.append(e.getMessage());
                    }
                    result.append(StringUtils.STRING_NEWLINE);
                }
            }
        }

        return result.toString();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    - list of items in a directory
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return
     */
    private String formatContents(List<Path> contents, Boolean isSortByExt) {
        // TODO: To implement sorting by extension
        List<String> fileNames = new ArrayList<>();
        for (Path path : contents) {
            fileNames.add(path.getFileName().toString());
        }

        if (isSortByExt) {
            Collections.sort(fileNames, (a, b) -> {
                String extA = getExtension(a);
                String extB = getExtension(b);
                if (extA.isEmpty() && !extB.isEmpty()) {
                    return -1;
                } else if (!extA.isEmpty() && extB.isEmpty()) {
                    return 1;
                } else {
                    return extA.compareTo(extB);
                }
            });
        } else {
            Collections.sort(fileNames);
        }

        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            result.append(fileName);
            result.append('\n');
        }

        return result.toString().trim();
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory
     * @return List of files + directories in the passed directory.
     */
    private List<Path> getContents(Path directory)
            throws InvalidDirectoryException {
        if (!Files.exists(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        if (!Files.isDirectory(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        for (File f : Objects.requireNonNull(pwd.listFiles())) {
            if (!f.isHidden()) {
                result.add(f.toPath());
            }
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories
     * @return List of java.nio.Path objects
     */
    private List<Path> resolvePaths(String... directories) throws LsException{
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < directories.length; i++) {
            try {
                paths.add(resolvePath(directories[i]));
            } catch (InvalidPathException e) {
                throw new LsException(ERR_FILE_NOT_FOUND);//NOPMD
            }
        }

        return paths;
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory
     * @return
     */
    private Path resolvePath(String directory) {
        if (directory.charAt(0) == CHAR_FILE_SEP || (directory.length() > 1 && directory.charAt(1) == ':')) {
            // This is an absolute path
            return Paths.get(directory).normalize();
        }

        return Paths.get(Environment.currentDirectory, directory).normalize();
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path
     * @return
     */
    private Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1);
    }

    private class InvalidDirectoryException extends Exception {
        InvalidDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory));
        }

        InvalidDirectoryException(String directory, Throwable cause) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory),
                    cause);
        }
    }
}
