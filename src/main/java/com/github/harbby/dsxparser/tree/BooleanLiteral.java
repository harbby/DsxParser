package com.github.harbby.dsxparser.tree;

import java.util.Objects;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;

public class BooleanLiteral extends Literal {
    private final boolean value;

    public BooleanLiteral(NodeLocation location, String value) {
        super(location);
        requireNonNull(value, "value is null");
        checkArgument(value.toLowerCase(ENGLISH).equals("true") || value.toLowerCase(ENGLISH).equals("false"));

        this.value = value.toLowerCase(ENGLISH).equals("true");
    }

    public boolean getValue() {
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
        BooleanLiteral other = (BooleanLiteral) obj;
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
