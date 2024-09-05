package sg.edu.nus.comp.cs4218.impl.app;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

@SuppressWarnings("PMD")
public class ExitApplicationPublicTest {
    private ExitApplication app;

    @BeforeEach
    public void renewApplication() {
        app = new ExitApplication();
    }

    /**
     * Test case to verify that the `terminateExecution` method exits the application with status code 0,
     *
     * @throws AbstractApplicationException if an error occurs while executing the application
     */
    @Test
    @ExpectSystemExitWithStatus(0)
    public void terminateExecution_GivenAnything_ShouldExitWithStatusCode0() throws AbstractApplicationException {
        app.terminateExecution();
    }
}
