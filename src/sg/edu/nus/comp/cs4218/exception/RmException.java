package sg.edu.nus.comp.cs4218.exception;

public class RmException extends AbstractApplicationException {

    private static final long serialVersionUID = 6616752571518808461L;

    public RmException(String message) {
        super(getFormattedErrorMessage(message));
    }

    private static String getFormattedErrorMessage(String message) {
        // Check if the message already starts with "rm:"
        if (message.startsWith("rm:")) {
            return message; // Return the message as it is
        } else {
            return "rm: " + message; // Prefix "rm:" to the message
        }
    }
}