package sg.edu.nus.comp.cs4218.exception;

public class CutException extends AbstractApplicationException {

    private static final long serialVersionUID = 1113796686823942499L;

    public CutException(String message) {
        super("cut: " + message);
    }
}