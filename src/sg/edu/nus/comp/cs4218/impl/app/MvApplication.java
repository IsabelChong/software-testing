package sg.edu.nus.comp.cs4218.impl.app; // Update package declaration

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException; // Import missing exception class
import sg.edu.nus.comp.cs4218.exception.MvException; // Import MvException class
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class MvApplication implements MvInterface { //NOPMD
    /**
     * renames the file named by the source operand to the destination path named by
     * the target operand
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param srcFile     of path to source file
     * @param destFile    of path to destination file
     * @throws Exception
     */
    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile)
            throws AbstractApplicationException {
        try {
            Path sourcePath = IOUtils.resolveFilePath(srcFile);
            Path destinationPath = IOUtils.resolveFilePath(destFile);

            // if source files dont exist throw exception
            if (!sourcePath.toFile().exists()) {
                throw new MvException(ERR_FILE_NOT_FOUND);
            }
            // unwritable source file then throw exception
            if (!sourcePath.toFile().canWrite()) {
                throw new MvException(ERR_NO_PERM);
            }
            // if we have no perms then dont overwrite
            if (!isOverwrite && destinationPath.toFile().exists()) {
                return "cannot overwrite existing file";
            }
            // do we need if same folder?

            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // check if should remove cause not in implementation, but good for feedback
            return String.format("File moved successfully to %s", destFile);
        } catch(MvException e) {
            throw e;
        } catch (Exception e) {
            MvException mvException = new MvException(e.getMessage());
            mvException.initCause(e);
            throw mvException;
        } 
    }

    /**
     * move files to destination folder
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param destFolder  of path to destination folder
     * @param fileNames    Array of String of file names
     * @throws Exception
     */
    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileNames)//NOPMD
            throws AbstractApplicationException {
        try {
            StringBuilder sameFolderErrors = new StringBuilder();
            if (fileNames.length == 0) {
                throw new MvException(ERR_NO_ARGS);
            }
            for (String fileName : fileNames) {
                Path sourcePath = IOUtils.resolveFilePath(fileName);
                Path destinationPath = Paths.get(IOUtils.resolveFilePath(destFolder).normalize().toString(), sourcePath.getFileName().toString());
                // if src files invalid throw exception, but shift if they are not in the same folder -> need to handle this separately
                if (isSameFolder(sourcePath, destinationPath)) {
                    sameFolderErrors.append(sourcePath);
                    sameFolderErrors.append(" and ");
                    sameFolderErrors.append(destinationPath);
                    sameFolderErrors.append(": ");
                    sameFolderErrors.append(ERR_SAME_FOLDER);
                    sameFolderErrors.append(StringUtils.STRING_NEWLINE);
                    sameFolderErrors.append("mv: ");
//                    throw new MvException("cannot move file to same folder");
                    continue;
                }

                if (!sourcePath.toFile().exists()) {
                    sameFolderErrors.append(sourcePath);
                    sameFolderErrors.append(": ");
                    sameFolderErrors.append(ERR_FILE_NOT_FOUND);
                    sameFolderErrors.append(StringUtils.STRING_NEWLINE);
                    sameFolderErrors.append("mv: ");
//                    throw new MvException(ERR_FILE_NOT_FOUND);
                    continue;
                }

                if (!isOverwrite && destinationPath.toFile().exists()) {
                    continue;
                }
                // is overwrite (okay)
                // is not overwrite and dest path does not exist (create a new file there)
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
            if (sameFolderErrors.length() > 0) {
                sameFolderErrors.delete(sameFolderErrors.length() - 5, sameFolderErrors.length());
                throw new MvException(sameFolderErrors.toString());
            }

            return String.format("Files moved successfully to %s", destFolder);
        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            // might want to rewrite this in the future to be more specific
            MvException mvException = new MvException(e.getMessage());
            mvException.initCause(e);
            throw mvException;
        } 
    }

    /**
     * Runs application with specified input data and specified output stream.
     *
     * @param args
     * @param stdin
     * @param stdout
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {//NOPMD
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }
        if (args.length == 0) {
            throw new MvException(ERR_NO_FOLDERS);
        }

        if (stdin == null || stdout == null) {
            throw new MvException(ERR_NULL_STREAMS);
        }
        MvArgsParser mvArgsParser = new MvArgsParser();
        
        try {
            mvArgsParser.parse(args);
            String[] filesToMove = mvArgsParser.getFilesToMove();
            if (filesToMove.length == 0) {
                throw new MvException(ERR_NO_ARGS);
            }
            String destFolder = mvArgsParser.getDestFolder();
            Boolean isOverwrite = mvArgsParser.isOverwrite();


            // use IOUTILS.resolvefilepath
            File fileToCheck = IOUtils.resolveFilePath(destFolder).toFile();
            // check if dest exists, then is writable then is overwritten
            if (fileToCheck.exists() && !fileToCheck.canWrite() && isOverwrite) {
                throw new MvException(ERR_NO_PERM);
            }
            // if it's meant to be a directory but does not exist and no overwrite
            if (!fileToCheck.exists() && !isOverwrite && filesToMove.length > 1) {
                throw new MvException(ERR_FILE_NOT_FOUND);
            }
            // check if folder is a valid directory
            if (filesToMove.length > 1 && !fileToCheck.isDirectory()) {
                throw new MvException(ERR_IS_NOT_DIR);
            }

            // check if file is a directory
            if (fileToCheck.isDirectory()) {
                mvFilesToFolder(isOverwrite, destFolder, filesToMove);
            } else {
                mvSrcFileToDestFile(isOverwrite, filesToMove[0], destFolder);
            }
        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            MvException mvException = new MvException(e.getMessage());
            mvException.initCause(e);
            throw mvException;
        } 
    }
    // write a function to check if file is in folder
    private static boolean isSameFolder(Path sourcePath, Path destinationPath) {
//        String sourcePathStr = sourcePath.getParent();
        return sourcePath.toString().equals(destinationPath.toString());
    }
    
}