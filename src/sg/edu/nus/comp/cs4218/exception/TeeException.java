
package sg.edu.nus.comp.cs4218.exception;

public class TeeException extends AbstractApplicationException {
    private static final long serialVersionUID = 6517503342362314995L;

    /**
     * Constructs a TeeException with the specified error message.
     *
     * @param message the error message
     */
    public TeeException(String message) {
        super("tee: " + message);
    }
}
