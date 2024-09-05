package sg.edu.nus.comp.cs4218.impl.parser;

import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;

public class TeeArgsParser extends ArgsParser {
    private static final char FLAG_IS_APPEND = 'a';

    public TeeArgsParser() {
        super();
        legalFlags.add(FLAG_IS_APPEND);
    }

    public Boolean isAppend() {
        return flags.contains(FLAG_IS_APPEND);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }

}