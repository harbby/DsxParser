package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class LogicalBinaryExpression extends Expression {
    public enum Operator {
        AND, OR;

        public Operator flip() {
            return switch (this) {
                case AND -> OR;
                case OR -> AND;
                default -> throw new IllegalArgumentException("Unsupported logical expression type: " + this);
            };
        }
    }

    private final Operator operator;
    private final Expression left;
    private final Expression right;

    public LogicalBinaryExpression(Operator operator, Expression left, Expression right) {
        this(null, operator, left, right);
    }

    public LogicalBinaryExpression(NodeLocation location, Operator operator, Expression left, Expression right) {
        super(location);
        requireNonNull(operator, "operator is null");
        requireNonNull(left, "left is null");
        requireNonNull(right, "right is null");

        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogicalBinaryExpression that = (LogicalBinaryExpression) o;
        return operator == that.operator &&
                Objects.equals(left, that.left) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }

    @Override
    public String doGenSql() {
        String x1 = left.doGenSql();
        String x2 = right.doGenSql();
        if (left instanceof LogicalBinaryExpression l1 && this.operator != l1.operator) {
            x1 = String.format("(%s)", x1);
        }
        if (right instanceof LogicalBinaryExpression l2 && this.operator != l2.operator) {
            x2 = String.format("(%s)", x2);
        }
        return String.format("%s %s %s", x1, operator.name().toLowerCase(Locale.ROOT), x2);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new LogicalBinaryExpression(operator, optimizer.optimize(left), optimizer.optimize(right));
    }
}