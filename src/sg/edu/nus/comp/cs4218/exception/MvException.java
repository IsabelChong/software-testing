package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = 1254567890L;

    public MvException(String message) {
        super("mv: " + message);
    }

}
