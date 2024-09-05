package sg.edu.nus.comp.cs4218.exception;

public class MkdirException extends AbstractApplicationException {

    private static final long serialVersionUID = -7005801205007805286L;

    public MkdirException(String message) {
        super("mkdir: " + message);
    }
}
