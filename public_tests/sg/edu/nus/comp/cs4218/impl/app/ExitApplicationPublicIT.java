package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;

@SuppressWarnings("PMD")
public class ExitApplicationPublicIT {

    public static final String EXIT_MSG = "exit: 0";
    
    /**
     * Test case to verify that the `run` method of the `ExitApplication` class exits successfully
     * when provided with arguments.
     *
     * @throws AbstractApplicationException If an error occurs while running the application.
     */
    @Test
    @ExpectSystemExitWithStatus(0)
    public void run_WithArguments_ExitsSuccessfully() throws AbstractApplicationException {
        ExitApplication app = new ExitApplication();
        app.run(new String[]{"anything", "here"}, System.in, System.out);
    }
}
