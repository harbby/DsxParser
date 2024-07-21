package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class NotExpression extends Expression {
    private final Expression value;

    public NotExpression(Expression value) {
        this(null, value);
    }

    public NotExpression(NodeLocation location, Expression value) {
        super(location);
        requireNonNull(value, "value is null");
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }


    @Override
    public List<Expression> getChildren() {
        return List.of(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotExpression that = (NotExpression) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String doGenSql() {
        if (value instanceof BooleanLiteral) {
            return String.format("not %s", value.doGenSql());
        } else if (value instanceof FunctionCall || value instanceof Identifier || value instanceof DereferenceExpression) {
            return String.format("!%s", value.doGenSql());
        } else {
            return String.format("!(%s)", value.doGenSql());
        }
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new NotExpression(optimizer.optimize(value));
    }
}
