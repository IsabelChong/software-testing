package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.ExitException;


import java.io.InputStream;
import java.io.OutputStream;

public class ExitApplication implements ExitInterface {

    /**
     * Runs the exit application.
     *
     * @param args   Array of arguments for the application, not used.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws ExitException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws ExitException {
        // Format: exit
        if (stdout == null || stdin == null) {
            throw new ExitException(ERR_NULL_STREAMS);
        }
        terminateExecution();
    }

    /**
     * Terminate shell.
     *
     * @throws ExitException
     */
    @Override
    public void terminateExecution() throws ExitException {
        try {
            System.exit(0);
        } catch (Error e) {
            ExitException exitException = new ExitException(e.getMessage());
            exitException.initCause(e);
            throw exitException;
        }
    }
}
