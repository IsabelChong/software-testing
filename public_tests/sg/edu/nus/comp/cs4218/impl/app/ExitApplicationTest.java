package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.security.Permission;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;

@SuppressWarnings("PMD")
class ExitApplicationTest {
    private final ExitApplication exitApp = new ExitApplication();
    private final String[] emptyArgs = {};

    @BeforeAll
    static void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @AfterAll
    static void tearDown() {
        System.setSecurityManager(null);
    }

    /**
     * Test case for security manager to block exit
     * Input: exitApp run command, with SecurityManagerSetup
     * Expected output: TestException caught
     */
    @Test
    void terminateExitApp_BlockedExit_TestingExceptionCaught() {
        try {
            exitApp.terminateExecution();
        } catch (TestException e) {
            assertEquals(0, e.status, "Exit status");
        } catch (AbstractApplicationException e) {
            fail(e);
        }
    }


    /**
     * Exception to be thrown when System.exit is called
     */
    protected static class TestException extends SecurityException {
        final int status;

        TestException(int status) {
            super("No exit");
            this.status = status;
        }
    }

    /**
     * Security manager that prevents System.exit from being called and throws exception instead
     */
    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm)
        {
            // allow anything.
        }
        @Override
        public void checkPermission(Permission perm, Object context)
        {
            // allow anything.

        }
        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new TestException(status);
        }
    }

}
