package sg.edu.nus.comp.cs4218.impl.helper;

import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

public final class TestHelper {
    private TestHelper() {
    }

    public static String generateExpectedOutput(String... elements) {
        StringBuilder expectedOutput = new StringBuilder();
        for (String element : elements) {
            expectedOutput.append(element).append(StringUtils.STRING_NEWLINE);
        }
        return expectedOutput.toString();
    }

    public static File createTempDir(String path) {
        File targetDir = new File(path);
        targetDir.mkdir();
        targetDir.deleteOnExit();

        return targetDir;
    }

    public static File createTempFile(String path) throws IOException {
        File targetFile = new File(path);
        targetFile.createNewFile();
        targetFile.deleteOnExit();

        return targetFile;
    }

    /**
     * Writes multiple contents into files of matching array indexes.
     *
     * @param paths    An array of paths to write content of its matching index into
     * @param contents An array of contents to write into the path of its matching index
     */
    public static void writeToFiles(Path[] paths, String... contents) {
        for (int i = 0; i < paths.length; i++) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(paths[i].toFile().getPath()))) {
                    writer.write(contents[i]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deletes all files and directories in the given path.
     *
     * @param path Path to delete files and directories from
     */
    public static void deleteFilesAndDirectoriesFrom(File path) {
        File filesList[] = path.listFiles();

        for (File file : filesList) {
            if (file.isDirectory()) {
                deleteFilesAndDirectoriesFrom(file.getAbsoluteFile());
            }
            file.delete();
        }
    }

    /**
     * Sets owner read, write and execute permissions to all directories and files within a specified directory,
     * and deleting it afterward.
     *
     * @param path Path to directory to start recursively setting permissions and deleting all files and directories
     *             within it
     * @throws IOException If errors occur while setting permissions for the directory.
     */
    public static void setPermissionsAndDelete(File path) throws IOException {
        File filesList[] = path.listFiles();

        for (File file : filesList) {
            if (file.isDirectory()) {
                Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwx------"));
                setPermissionsAndDelete(file.getAbsoluteFile());
            }
            file.delete();
        }
    }
}
