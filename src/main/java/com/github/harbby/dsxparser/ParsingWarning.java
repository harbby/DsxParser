package com.github.harbby.dsxparser;

public class ParsingWarning extends RuntimeException {
    private final int stat;
    private final int length;

    public ParsingWarning(String line, int stat, int length) {
        super(line);
        this.stat = stat;
        this.length = length;
    }

    public int getStat() {
        return stat;
    }

    public int getLength() {
        return length;
    }
}
