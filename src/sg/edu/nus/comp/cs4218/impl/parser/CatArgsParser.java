package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CatArgsParser extends ArgsParser {
    public static final char LINE_FLAG = 'n';
    private final static int INDEX_FILES = 0;

    public CatArgsParser() {
        super();
        legalFlags.add(LINE_FLAG);
    }

    public Boolean isLineNumberSpecified() {
        return flags.contains(LINE_FLAG);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
