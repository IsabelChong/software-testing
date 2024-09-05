package sg.edu.nus.comp.cs4218.impl.parser;
import java.util.Iterator;

public class MvArgsParser extends ArgsParser {
    private static final char FLAG_NO_OVERWRITE = 'n';
    public static final String INSUFFICIENT_ARGS = "us";
    private String[] filesToMove;
    private String destFolder;

    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_NO_OVERWRITE);
    }

    public boolean isOverwrite() {
        return !flags.contains(FLAG_NO_OVERWRITE);
    }

    public String[] getFilesToMove() {
        if (filesToMove == null) {
            separateArguments();
        }
        return filesToMove;
    }

    public String getDestFolder() {
        if (destFolder == null) {
            separateArguments();
        }
        return destFolder;
    }
    
    private void separateArguments() {
        int argSize = nonFlagArgs.size();
        if (argSize < 2) {
            // if 0 args then throw exception? Should be caught in MvApplication
            filesToMove = new String[0];
            return;
        }
        // store here
        filesToMove = new String[argSize - 1];

        Iterator<String> nonFlagsIterator = nonFlagArgs.iterator();
        for (int i = 0; i < argSize - 1; i++) {
            filesToMove[i] = nonFlagsIterator.next();
        }
        destFolder = nonFlagsIterator.next();
    }
    
}
