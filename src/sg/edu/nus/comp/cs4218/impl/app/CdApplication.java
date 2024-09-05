package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CdApplication implements CdInterface {
    private static final String ERROR_SEMICOLON = ": ";


    /**
     * Changes working directory to a specified path.
     *
     * @param path String of the path to a directory
     * @throws CdException if path is null, or if target directory doesn't exist.
     */
    @Override
    public void changeToDirectory(String path) throws CdException {
        if (path == null) {
            throw new CdException(ERR_NULL_ARGS);
        }
        String normalizedPath = getNormalizedAbsolutePath(path);
        if (!Files.exists(Path.of(normalizedPath))) {
            throw new CdException(path + ERROR_SEMICOLON + ERR_FILE_NOT_FOUND);
        }
        checkPermissions(Path.of(normalizedPath), path);
        Environment.currentDirectory = normalizedPath;
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException If an error occurs during path normalisation and validation, or if input argument is null.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws CdException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }
        if (stdin == null || stdout == null) {
            throw new CdException(ERR_NULL_STREAMS);
        }
        if (args.length == 0) {
            Environment.currentDirectory = System.getProperty("user.home");
        } else {
            changeToDirectory(args[0]);
        }
    }

    /**
     * Returns the normalized absolute path from the specified path string.
     *
     * @param pathStr Input path string to be normalized into absolute path.
     * @return Normalized absolute path string.
     * @throws CdException If input string is empty, if the specified path already exists,
     *                     if the specified path is not a directory, or if any of the directories in the path from
     *                     the root does not have owner execute permissions.
     */
    protected String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            return System.getProperty("user.dir");
        }

        Path path = new File(pathStr).toPath();


        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        Path normalizedPath = path.normalize();

        return normalizedPath.toString();
    }

    /**
     * Checks if the given path has execute permissions in every level, and if the
     * separate parts of the path are all directories.
     *
     * @param path         Absolute path to check
     * @param originalPath Original path argument passed into shell, used for error message.
     * @throws CdException if any level in the absolute path doesn't have execute permissions, or
     *                     is not a directory.
     */
    protected void checkPermissions(Path path, String originalPath) throws CdException {
        if (!path.isAbsolute()) {
            throw new CdException(ERR_SYNTAX);
        }
        Path currPath = Paths.get("/");
        for (Path dir : path) {
            currPath = currPath.resolve(dir);
            if (!Files.isExecutable(currPath)) {
                throw new CdException(originalPath + ERROR_SEMICOLON + ERR_NO_PERM);
            }
            if (!Files.isDirectory(currPath)) {
                throw new CdException(originalPath + ERROR_SEMICOLON + ERR_IS_NOT_DIR);
            }
        }
    }
}
