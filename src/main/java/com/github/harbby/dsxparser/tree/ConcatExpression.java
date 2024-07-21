package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.function.CharFunc;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ConcatExpression extends Expression {

    private final Expression left;
    private final Expression right;

    public ConcatExpression(Expression left, Expression right) {
        this(null, left, right);
    }

    public ConcatExpression(NodeLocation location, Expression left, Expression right) {
        super(location);
        this.left = requireNonNull(left, "left is null");
        this.right = requireNonNull(right, "right is null");
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    private static boolean isOnlyCharConcat(Expression expression) {
        if (expression instanceof ConcatExpression x) {
            return isOnlyCharConcat(x.getLeft()) && isOnlyCharConcat(x.getRight());
        }
        return expression instanceof CharFunc;
    }

    @Override
    public String doGenSql() {
        if (isOnlyCharConcat(this)) {
            return String.format("%s||%s", left.doGenSql(), right.doGenSql());
        } else {
            return String.format("%s || %s", left.doGenSql(), right.doGenSql());
        }
    }

    @Override
    public List<? extends Expression> getChildren() {
        return List.of(left, right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ConcatExpression o = (ConcatExpression) obj;
        return Objects.equals(left, o.left) &&
                Objects.equals(right, o.right);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new ConcatExpression(optimizer.optimize(left), optimizer.optimize(right));
    }
}
