package sg.edu.nus.comp.cs4218.impl.parser;

public class GrepArgsParser extends ArgsParser {
    private final static char FLAG_IS_INVERT = 'v';
    private final static char FLAG_CASE_IN = 'i';
    private final static char FLAG_COUNT_LINES = 'c';
    private final static char FLAG_PREFIX_FNAME = 'H';
    private final static int INDEX_PATTERN = 0;
    private final static int INDEX_FILES = 1;

    public GrepArgsParser() {
        super();
        legalFlags.add(FLAG_IS_INVERT);
        legalFlags.add(FLAG_CASE_IN);
        legalFlags.add(FLAG_COUNT_LINES);
        legalFlags.add(FLAG_PREFIX_FNAME);
    }

    public Boolean isInvert() {
        return flags.contains(FLAG_IS_INVERT);
    }

    public Boolean isCaseInsensitive() {
        return flags.contains(FLAG_CASE_IN);
    }

    public Boolean isCountLines() {
        return flags.contains(FLAG_COUNT_LINES);
    }

    public Boolean isPrefixFileName() {
        return flags.contains(FLAG_PREFIX_FNAME);
    }

    public String getPattern() {
        return nonFlagArgs.isEmpty() ? null : nonFlagArgs.get(INDEX_PATTERN);
    }

    public String[] getFileNames() {
        return nonFlagArgs.size() <= 1 ? null : nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size())
                .toArray(new String[0]);
    }
}
