package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class PasteArgsParser extends ArgsParser {
    public static final char S_FLAG = 's';

    public PasteArgsParser() {
        super();

        legalFlags.add(S_FLAG);
    }

    public Boolean isSerial() {
        return flags.contains(S_FLAG);
    }

    private Boolean noFlags() {
        return !flags.contains(S_FLAG);
    }
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}