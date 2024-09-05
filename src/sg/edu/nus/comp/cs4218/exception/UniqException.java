package sg.edu.nus.comp.cs4218.exception;

public class UniqException extends AbstractApplicationException {

    private static final long serialVersionUID = 2333367686823942499L;

    public UniqException(String message) {
        super("uniq: " + message);
    }
}