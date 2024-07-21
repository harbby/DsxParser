package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class Identifier extends Expression {
    private static final Pattern NAME_PATTERN = Pattern.compile("[@$]?[a-zA-Z_]([a-zA-Z0-9_:@])*");

    private final String value;
    private final boolean delimited;

    public Identifier(String value) {
        this(null, value, false);
    }

    public Identifier(NodeLocation location, String value, boolean delimited) {
        super(location);
        this.value = value;
        this.delimited = delimited;

        checkState(delimited || NAME_PATTERN.matcher(value).matches(), "value contains illegal characters: " + value);
    }

    public String getValue() {
        return value;
    }

    public boolean isDelimited() {
        return delimited;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Identifier that = (Identifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        if (!this.isDelimited()) {
            return this.getValue();
        } else {
            return '"' + this.getValue().replace("\"", "\"\"") + '"';
        }
    }

    @Override
    public String doGenSql() {
        return toString();
    }

    @Override
    public List<? extends Expression> getChildren() {
        return List.of();
    }

    @Override
    public Identifier visit(Optimizer optimizer) {
        return this;
    }
}
