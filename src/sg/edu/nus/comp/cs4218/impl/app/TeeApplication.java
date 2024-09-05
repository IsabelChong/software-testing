package sg.edu.nus.comp.cs4218.impl.app;

import java.io.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

import sg.edu.nus.comp.cs4218.app.TeeInterface;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TeeApplication implements TeeInterface {
    /**
     * Runs application with specified input data and specified output stream.
     *
     * @param args
     * @param stdin
     * @param stdout
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws TeeException {//NOPMD
        if (stdout == null || stdin == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }
        TeeArgsParser teeArgsParser = new TeeArgsParser();


        try {
            teeArgsParser.parse(args);
            List<String> fileNames = teeArgsParser.getFileNames();
            List<String> workingFiles = new ArrayList<>();
            List<String> nonWorkingFiles = new ArrayList<>();

            Boolean isAppend = teeArgsParser.isAppend();

            // Check for file perms
            for (String file : fileNames) {
                Path resolvedPath = IOUtils.resolveFilePath(file);
                File fileToWrite = resolvedPath.toFile();

                // If file is exist and can write or file does not exist
                if ((fileToWrite.exists() && fileToWrite.canWrite() && fileToWrite.isFile()) ||
                        (!fileToWrite.exists())) {
                    workingFiles.add(file);
                } else {
                    nonWorkingFiles.add(file);
                }
            }
            try {
                String output = teeFromStdin(isAppend, stdin, workingFiles.toArray(new String[0]));
                stdout.write(output.getBytes());
            } catch (IOException e) {
                TeeException teeException = new TeeException(e.getMessage());
                teeException.initCause(e);
                throw teeException;
            }

            StringBuilder stringB = new StringBuilder();
            for (String file : nonWorkingFiles) {
                File fileToCheck = new File(file);
                if (fileToCheck.exists() && !fileToCheck.canWrite()) {
                    stringB.append(file);
                    stringB.append(": ");
                    stringB.append(ERR_NO_PERM);
                    stringB.append("\ntee: ");
                } else if (fileToCheck.exists() && fileToCheck.isDirectory()) {
                    stringB.append(file);
                    stringB.append(": ");
                    stringB.append(ERR_IS_DIR);
                    stringB.append("\ntee: ");
                } else {
                    stringB.append(file);
                    stringB.append(": ");
                    stringB.append(ERR_NO_PERM);
                    stringB.append("\ntee: ");
                }
            }

            if (!nonWorkingFiles.isEmpty()) {
                stringB.delete(stringB.length() - 6, stringB.length());
                throw new TeeException(stringB.toString());
            }

        } catch (TeeException e) {
            throw e;
        } catch (Exception e) {
            TeeException teeException = new TeeException(e.getMessage());
            teeException.initCause(e);
            throw teeException;
        }
    }

    /**
     * Reads from standard input and write to both the standard output and files
     *
     * @param isAppend Boolean option to append the standard input to the contents
     *                 of the input files
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return
     * @throws Exception
     */
    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName)//NOPMD
            throws TeeException {

        if (stdin == null || fileName == null) {
            throw new TeeException(ERR_NULL_STREAMS);
        }

        if (isAppend == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }

        String output;

        try {

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = stdin.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            result.close();
            output = result.toString();

            for (String file : fileName) {
                OutputStream fileOutputStream;//NOPMD : Closed in line 154,155
                BufferedWriter bufferedWriter;//NOPMD : Closed in line 156

                try {
                    if (isAppend) {
                        List<String> existingContent = new ArrayList<>();
                        if (new File(IOUtils.resolveFilePath(file).toString()).exists()) {
                            existingContent = IOUtils.getLinesFromInputStream(IOUtils.openInputStream(file));
                        }
                        fileOutputStream = IOUtils.openOutputStream(file);
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
                        for (String line : existingContent) {
                            bufferedWriter.write(line + System.lineSeparator());
                        }
                        bufferedWriter.write(output);

                    } else {
                        fileOutputStream = IOUtils.openOutputStream(file);
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
                        bufferedWriter.write(output);
                    }

                    bufferedWriter.flush();
                    bufferedWriter.close();
                    IOUtils.closeOutputStream(fileOutputStream);

                } catch (IOException e) {
                    TeeException teeException = new TeeException(e.getMessage());
                    teeException.initCause(e);
                    throw teeException;
                }
            }

        } catch (IOException |ShellException e) {
            TeeException teeException = new TeeException(e.getMessage());
            teeException.initCause(e);
            throw teeException;
        }
        return output;
    }
}
