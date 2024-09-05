package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class WcArgsParser extends ArgsParser {
    public static final char B_FLAG = 'c';

    public static final char LC_FLAG = 'l';

    public static final char WC_FLAG = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(B_FLAG);
        legalFlags.add(LC_FLAG);
        legalFlags.add(WC_FLAG);
    }

    public Boolean isByteCount() {
        return flags.contains(B_FLAG) || noFlags();
    }

    public Boolean isLineCount() {
        return flags.contains(LC_FLAG) || noFlags();
    }

    public Boolean isWordCount() {
        return flags.contains(WC_FLAG) || noFlags();
    }

    private Boolean noFlags() {
        return !flags.contains(B_FLAG) &&
                !flags.contains(LC_FLAG) &&
                !flags.contains(WC_FLAG);
    }
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
