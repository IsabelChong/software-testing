package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * A class that implements the {@link RmInterface} interface, providing functionality to remove files and directories.
 */
public class RmApplication implements RmInterface {

    /**
     * Removes files and directories based on the provided arguments.
     *
     * @param isEmptyFolder Flag indicating if empty folders should be removed.
     * @param isRecursive   Flag indicating if removal should be done recursively.
     * @param fileNames     Array of file and directory names to be removed.
     * @throws RmException If an error occurs during file removal.
     */
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames)
            throws RmException {
        // Check if file names array is null or empty
        if (fileNames == null || fileNames.length == 0) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        // Iterate over each file name
        for (String fileName : fileNames) {
            String currDir = Environment.currentDirectory;
            File file = IOUtils.resolveFilePath(fileName).toFile();
            try {
                // Check if file or directory exists
                if (!file.exists()) {
                    throw new IOException(fileName + ": " + ERR_FILE_NOT_FOUND);
                }
                // If the path is a directory
                if (file.isDirectory()) {
                    // If removal is to be done recursively
                    if (isRecursive) {
                        deleteDirectory(file);
                    }
                    // If removal should only be for empty directories
                    else if (isEmptyFolder) {
                        if (Objects.requireNonNull(file.listFiles()).length == 0) {
                            if (!file.delete()) {
                                throw new IOException(fileName + ": Failed to delete empty directory");
                            }
                        } else {
                            throw new IOException(fileName + ": Directory is not empty");
                        }
                    } else {
                        throw new IOException(fileName + ": " + ERR_IS_DIR);
                    }
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + fileName);
                    }
                }
            } catch (IOException e) {
                RmException rmException = new RmException("rm: " + e.getMessage());
                rmException.initCause(e);
                throw rmException;
            }
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param directory The directory to be deleted.
     * @throws IOException If an error occurs during directory deletion.
     */
    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                // If the file is a directory, recursively call deleteDirectory
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                // If the file is a regular file, delete it
                else {
                    if (!file.delete()) {
                        throw new IOException(file.getName() + ": Failed to delete file");
                    }
                }
            }
        }
        // After deleting all files and subdirectories, delete the directory itself
        if (!directory.delete()) {
            throw new IOException(directory.getName() + ": Failed to delete directory");
        }
    }

    /**
     * Runs the RmApplication with the given arguments, stdin, and stdout.
     *
     * @param args   Command-line arguments.
     * @param stdin  Input stream.
     * @param stdout Output stream.
     * @throws RmException If an error occurs during application
     *                     execution.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        try {
            RmArgsParser parser = new RmArgsParser();
            parser.parse(args);

            Boolean isEmptyFolder = parser.isEmptyFolder();
            Boolean isRecursive = parser.isRecursive();
            String[] fileNames = parser.getFileNames();

            // Remove files and directories based on processed arguments
            remove(isEmptyFolder, isRecursive, fileNames);
        } catch (Exception e) {
            RmException rmException = new RmException(e.getMessage());
            rmException.initCause(e);
            throw rmException;//NOPMD
        }
    }
}
