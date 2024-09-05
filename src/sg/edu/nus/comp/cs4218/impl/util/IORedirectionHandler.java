package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

/**
 * IORedirectionHandler handles input and output redirection for commands.
 */
public class IORedirectionHandler {
    private final List<String> argsList;
    private final ArgumentResolver argumentResolver;
    private List<String> noRedirArgsList;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Constructor for IORedirectionHandler.
     *
     * @param argsList         List of arguments.
     * @param origInputStream  Original InputStream.
     * @param origOutputStream Original OutputStream.
     * @param argumentResolver ArgumentResolver to resolve arguments.
     */
    public IORedirectionHandler(List<String> argsList, InputStream origInputStream,
                                OutputStream origOutputStream, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Extracts the input and output redirection options from the list of arguments.
     *
     * @throws AbstractApplicationException If an exception happens while running an application.
     * @throws ShellException               If an unsupported or invalid application command is
     *                                      detected.
     * @throws FileNotFoundException        If a file specified in the arguments is not found.
     */
    public void extractRedirOptions() throws AbstractApplicationException, ShellException, FileNotFoundException {//NOPMD

        // Fix logic error: || instead of &&
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        noRedirArgsList = new LinkedList<>();

        // Used to handle multiple input and output redirection

        String lastInputRedir = null;
        String lastOutputRedir = null;

        // extract redirection operators (with their corresponding files) from argsList
        ListIterator<String> argsIterator = argsList.listIterator();

        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();

            // leave the other args untouched
            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
                continue;
            }

            // If there is no file specified after the redirection operator
            if (!argsIterator.hasNext()) {
                throw new ShellException(ERR_NO_FILE_ARGS);
            }

            // if current arg is < or >, fast-forward to the next arg to extract the specified file
            String file = argsIterator.next();

            if (isRedirOperator(file)) {
                throw new ShellException(ERR_SYNTAX);
            }

            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = argumentResolver.resolveOneArgument(file);
            if (fileSegment.size() > 1) {
                // ambiguous redirect if file resolves to more than one parsed arg
                throw new ShellException(ERR_SYNTAX);
            }
            file = fileSegment.get(0);
            if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
                lastInputRedir = file;
            } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
                lastOutputRedir = file;
            }

            if (lastOutputRedir != null) {
                IOUtils.closeOutputStream(outputStream); // Close the previous outputStream if it's not the original one.
                File fileToWrite = new File(lastOutputRedir);
                // Exist and directory will throw "Is a directory" error
                if (fileToWrite.exists() && fileToWrite.isDirectory()) {
                    throw new ShellException(file + ": " +ERR_IS_DIR);
                }
                // Do not exist and directory will throw "No such file or directory" error
                if (lastOutputRedir.charAt(lastOutputRedir.length() - 1) == File.separatorChar) {
                    throw new ShellException(file + ": " +ERR_FILE_NOT_FOUND);
                }
                // Exist and no write permission will throw "Permission denied" error
                if (fileToWrite.exists() && !fileToWrite.canWrite()) {
                    throw new ShellException(file + ": " +ERR_NO_PERM);
                }
                try {
                    outputStream = IOUtils.openOutputStream(lastOutputRedir);
                } catch (FileNotFoundException e) {
                    ShellException shellException = new ShellException(ERR_FILE_NOT_FOUND);
                    shellException.initCause(e);
                    throw shellException;
                }
            }

            if (lastInputRedir != null) {
                IOUtils.closeInputStream(inputStream); // Close the previous inputStream if it's not the original one.
                File fileToRead = new File(lastInputRedir);
                if (fileToRead.exists() && !fileToRead.canRead()) {
                    throw new ShellException(file + ": " + ERR_NO_PERM);
                }
                if (fileToRead.isDirectory()) {
                    inputStream = new ByteArrayInputStream("".getBytes());
                } else {
                    inputStream = IOUtils.openInputStream(lastInputRedir);
                }
            }
        }
    }

    /**
     * Returns the list of arguments without the input and output redirection options.
     *
     * @return List of arguments without the input and output redirection options.
     */
    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    /**
     * Returns the InputStream after redirection.
     *
     * @return InputStream after redirection.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the OutputStream after redirection.
     *
     * @return OutputStream after redirection.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Checks if the given string is a redirection operator.
     *
     * @param str String to check.
     * @return True if the given string is a redirection operator, false otherwise.
     */
    protected boolean isRedirOperator(String str) {
        // Fixed bug: added a check for output as well
        return str.equals(String.valueOf(CHAR_REDIR_INPUT)) || str.equals(String.valueOf(CHAR_REDIR_OUTPUT));
    }
}