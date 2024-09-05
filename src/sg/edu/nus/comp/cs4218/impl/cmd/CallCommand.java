package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;

    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Performs IO Redirection if needed, then run the specified application with the given command line
     * arguments and redirection streams.
     *
     * @param stdin  Original input stream.
     * @param stdout Original output stream.
     * @throws AbstractApplicationException if errors occur during application run.
     * @throws ShellException               if argument list supplied is null, or has length 0.
     * @throws FileNotFoundException        if trying to access path that doesn't exist.
     */
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout, argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream(); //NOPMD
        OutputStream outputStream = redirHandler.getOutputStream(); //NOPMD
        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }
    }


    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }
}
