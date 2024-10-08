package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ComparisonExpression extends Expression {
    public enum Operator {
        EQUAL("="),
        NOT_EQUAL("<>"),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        IS_DISTINCT_FROM("IS DISTINCT FROM");

        private final String value;

        Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Operator flip() {
            return switch (this) {
                case EQUAL -> EQUAL;
                case NOT_EQUAL -> NOT_EQUAL;
                case LESS_THAN -> GREATER_THAN;
                case LESS_THAN_OR_EQUAL -> GREATER_THAN_OR_EQUAL;
                case GREATER_THAN -> LESS_THAN;
                case GREATER_THAN_OR_EQUAL -> LESS_THAN_OR_EQUAL;
                case IS_DISTINCT_FROM -> IS_DISTINCT_FROM;
                default -> throw new IllegalArgumentException("Unsupported comparison: " + this);
            };
        }

        public Operator negate() {
            return switch (this) {
                case EQUAL -> NOT_EQUAL;
                case NOT_EQUAL -> EQUAL;
                case LESS_THAN -> GREATER_THAN_OR_EQUAL;
                case LESS_THAN_OR_EQUAL -> GREATER_THAN;
                case GREATER_THAN -> LESS_THAN_OR_EQUAL;
                case GREATER_THAN_OR_EQUAL -> LESS_THAN;
                default -> throw new IllegalArgumentException("Unsupported comparison: " + this);
            };
        }
    }

    private final Operator operator;
    private final Expression left;
    private final Expression right;

    public ComparisonExpression(Operator operator, Expression left, Expression right) {
        this(null, operator, left, right);
    }

    public ComparisonExpression(NodeLocation location, Operator operator, Expression left, Expression right) {
        super(location);
        requireNonNull(operator, "type is null");
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

        ComparisonExpression that = (ComparisonExpression) o;
        return (operator == that.operator) &&
                Objects.equals(left, that.left) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }

    @Override
    public String doGenSql() {
        return String.format("%s %s %s", left.doGenSql(), operator.value, right.doGenSql());
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        Expression left1 = optimizer.optimize(left);
        Expression right1 = optimizer.optimize(right);
        return new ComparisonExpression(operator, left1, right1);
    }
}
