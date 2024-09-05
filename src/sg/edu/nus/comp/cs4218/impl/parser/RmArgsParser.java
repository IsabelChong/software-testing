package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.ArrayList;
import java.util.List;

public class RmArgsParser extends ArgsParser {
    private static final char FLAG_EMPTY_FOLDER = 'd';
    private static final char FLAG_IS_RECURSIVE = 'r';

    private static final int INDEX_FILE_NAMES = 0;

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_EMPTY_FOLDER);
        legalFlags.add(FLAG_IS_RECURSIVE);
    }

    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_EMPTY_FOLDER);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public String[] getFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (String arg : nonFlagArgs) {
            fileNames.add(arg);
        }
        return fileNames.toArray(new String[0]);
    }
}
