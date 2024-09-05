package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_IS_COUNT = 'c';
    public static final char FLAG_IS_REPEATED = 'd';
    public static final char FLAG_ALL_REPEATED = 'D';

    private final static int INDEX_FILES = 0;

    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_REPEATED);
        legalFlags.add(FLAG_ALL_REPEATED);
    }

    public Boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    public Boolean isRepeated() {
        return flags.contains(FLAG_IS_REPEATED);
    }

    public Boolean isAllRepeated() {
        return flags.contains(FLAG_ALL_REPEATED);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
