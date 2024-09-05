package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class SortArgsParser extends ArgsParser {
    public static final char N_FLAG = 'n';
    public static final char R_FLAG = 'r';
    public static final char F_FLAG = 'f';
    private final static int INDEX_FILES = 0;

    public SortArgsParser() {
        super();
        legalFlags.add(N_FLAG);
        legalFlags.add(R_FLAG);
        legalFlags.add(F_FLAG);
    }

    public Boolean isFirstWordNumber() {
        return flags.contains(N_FLAG);
    }

    public Boolean isReverseOrder() {
        return flags.contains(R_FLAG);
    }

    public boolean isCaseIndependent() {
        return flags.contains(F_FLAG);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
