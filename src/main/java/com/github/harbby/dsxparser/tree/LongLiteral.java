package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.ParsingException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class LongLiteral extends Literal {
    private final long value;

    public LongLiteral(long value) throws ParsingException {
        super(null);
        this.value = value;
    }

    public LongLiteral(NodeLocation location, String value) throws ParsingException {
        super(location);
        requireNonNull(value, "value is null");
        try {
            this.value = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ParsingException("Invalid numeric literal: " + value);
        }
    }

    public static LongLiteral of(String value) {
        try {
            return of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new ParsingException("Invalid numeric literal: " + value);
        }
    }

    public static LongLiteral of(long value) {
        return new LongLiteral(value);
    }

    public long getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LongLiteral other = (LongLiteral) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.getValue());
    }

    @Override
    public String doGenSql() {
        return String.valueOf(this.getValue());
    }
}
