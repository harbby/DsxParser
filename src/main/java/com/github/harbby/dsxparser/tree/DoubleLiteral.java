package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.ParsingException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DoubleLiteral
        extends Literal {
    private final double value;

    public DoubleLiteral(NodeLocation location, String value) throws ParsingException {
        super(location);
        requireNonNull(value, "value is null");
        try {
            this.value = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParsingException("Invalid numeric literal: " + value);
        }
    }

    public double getValue() {
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
        DoubleLiteral other = (DoubleLiteral) obj;
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
