package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.NodeLocation;

public class ParsingException
        extends RuntimeException {
    private final int line;
    private final int charPositionInLine;

    public ParsingException(String message, Exception cause, int line, int charPositionInLine) {
        super(message, cause);

        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public ParsingException(String message) {
        this(message, null, 1, 0);
    }

    public ParsingException(String message, NodeLocation nodeLocation) {
        this(message, null, nodeLocation.getLineNumber(), nodeLocation.getColumnNumber());
    }

    public ParsingException(String message, Exception cause, NodeLocation nodeLocation) {
        this(message, cause, nodeLocation.getLineNumber(), nodeLocation.getColumnNumber());
    }

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return charPositionInLine + 1;
    }

    public String getErrorMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        return String.format("line %s:%s: %s", getLineNumber(), getColumnNumber(), getErrorMessage());
    }
}
