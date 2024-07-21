package com.github.harbby.dsxparser.tree;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DecimalLiteral
        extends Literal {
    private final String value;

    public DecimalLiteral(String value) {
        this(null, value);
    }

    public DecimalLiteral(NodeLocation location, String value) {
        super(location);
        this.value = requireNonNull(value, "value is null");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DecimalLiteral that = (DecimalLiteral) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String doGenSql() {
        return value;
    }
}
