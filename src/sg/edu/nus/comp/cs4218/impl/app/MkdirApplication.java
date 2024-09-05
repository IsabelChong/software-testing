package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class MkdirApplication implements MkdirInterface {
    String errors = "";
    private static final String MKDIR_PREFIX = "mkdir: ";

    /**
     * Runs the mkdir application with the specified arguments.
     * Assumption: The application must take in at least one argument.
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws MkdirException If errors occur during path normalisation and validation, or if input argument is null
     *                        or has length 0.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws MkdirException {
        if (args == null) {
            throw new MkdirException(ERR_NULL_ARGS);
        }
        if (args.length == 0) {
            throw new MkdirException(ERR_NO_FOLDERS);
        }

        if (args[0].equals("-p")) {
            //handle make directory w/ parent folders
            if (args.length < 2) {
                //if no directory specified after -p flag
                throw new MkdirException(ERR_MISSING_ARG);
            }
            for (int i = 1; i < args.length; i++) {
                String normalizedPath = getNormalizedAbsolutePath(args[i]);
                if (Files.exists(Path.of(normalizedPath))) {
                    errors = errors + args[i] + ": " + ERR_FILE_EXISTS + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                    continue;
//                    throw new MkdirException(String.format(ERR_FILE_EXISTS, args[i]));
                }
                Boolean permissionsPass = checkPermissions(normalizedPath, true, args[i]);
                if (!permissionsPass) {
                    continue;
                }
                File targetDirectory = new File(normalizedPath);
                targetDirectory.mkdirs();
            }
        } else {
            createFolder(args);
        }

        if (errors.length() > 0) {
            errors = errors.substring(0, errors.length() - 8);
            throw new MkdirException(errors);
        }
    }

    /**
     * Creates a single or multiple folders based on the list of folder names supplied.
     *
     * @param folderName Array of string of folder names to be created
     * @throws MkdirException If folder to be created already exists, or if arguments are null.
     */
    @Override
    public void createFolder(String... folderName) throws MkdirException {
        if (folderName == null) {
            throw new MkdirException(ERR_NULL_ARGS);
        }
        // create folders / folder

        for (String pathName : folderName) {
            String normalizedPath = getNormalizedAbsolutePath(pathName);
            if (Files.exists(Path.of(normalizedPath))) {
                errors = errors + pathName + ": " + ERR_FILE_EXISTS + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                continue;
//                throw new MkdirException(String.format(ERR_FILE_EXISTS, pathName));
            }
            Boolean permissionsPass = checkPermissions(normalizedPath, false, pathName);
            if (!permissionsPass) {
                continue;
            }
            File directory = new File(normalizedPath);
            directory.mkdir();
        }
    }

    /**
     * Returns the normalized absolute path from the specified path string.
     *
     * @param pathStr Input path string to be normalized into absolute path.
     * @return Normalized absolute path string.
     * @throws MkdirException If input string is empty.
     */
    protected String getNormalizedAbsolutePath(String pathStr) throws MkdirException {
        if (StringUtils.isBlank(pathStr)) {
            throw new MkdirException(ERR_NO_ARGS);
        }

        Path path = new File(pathStr).toPath();


        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        return path.normalize().toString();
    }

    /**
     * Checks for permissions from given path.
     * Without p flag, all levels of directories up till target directories must
     * exist and have execute permissions. Most direct parent directory must have write permissions.
     * With p flag, all levels of directories that exist must have execute permissions and most
     * direct parent directory that exists must have write permissions.
     *
     * @param path         Absolute and normalized path to check for permissions.
     * @param withPFlag    Whether the p-flag is used.
     * @param originalPath Original path argument passed into shell, used for error message.
     * @return Boolean Returns true if all permissions pass, else return false.
     * @throws MkdirException if any of the above conditions are not satisfied.
     */
    protected Boolean checkPermissions(String path, Boolean withPFlag, String originalPath) throws MkdirException {
        if (!Path.of(path).isAbsolute()) {
            throw new MkdirException(ERR_SYNTAX);
        }

        if (withPFlag) {
            if (path.contains("/")) {
                Path currPath = Paths.get("/");
                for (Path dir : Path.of(path)) {
                    currPath = currPath.resolve(dir);
                    if (currPath.toFile().exists()) {
                        if (!Files.isExecutable(currPath) && !currPath.equals(Path.of(path))) {
                            errors = errors + originalPath + ": " + ERR_NO_PERM + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                            return false;
                        }
                    } else {
                        break;
                    }
                }
                Path parentPath = currPath.getParent();
                if (!parentPath.toFile().canWrite()) {
                    errors = errors + originalPath + ": " + ERR_NO_PERM + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                    return false;
                }
            }
        } else {
            if (path.contains("/")) {
                Path currPath = Paths.get("/");
                for (Path dir : Path.of(path)) {
                    currPath = currPath.resolve(dir);
                    if (!currPath.toFile().exists() && !currPath.equals(Path.of(path))) {
                        errors = errors + originalPath + ": " + ERR_TOP_LEVEL_MISSING + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                        return false;
                    }
                    if (!Files.isExecutable(currPath) && !currPath.equals(Path.of(path))) {
                        errors = errors + originalPath + ": " + ERR_NO_PERM + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                        return false;
                    }
                }
                Path parentPath = currPath.getParent();
                if (!parentPath.toFile().canWrite()) {
                    errors = errors + originalPath + ": " + ERR_NO_PERM + StringUtils.STRING_NEWLINE + MKDIR_PREFIX;
                    return false;
                }
            }
        }
        return true;
    }
}
