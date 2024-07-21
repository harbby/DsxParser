package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

public class ArithmeticBinaryExpression extends Expression {
    public enum Operator {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULUS("%");
        private final String value;

        Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getLevel() {
            return switch (this) {
                case ADD -> 1;
                case SUBTRACT -> 1;
                case MULTIPLY -> 2;
                case DIVIDE -> 2;
                case MODULUS -> 2;
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    private final Operator operator;
    private final Expression left;
    private final Expression right;

    public ArithmeticBinaryExpression(Operator operator, Expression left, Expression right) {
        this(null, operator, left, right);
    }

    public ArithmeticBinaryExpression(NodeLocation location, Operator operator, Expression left, Expression right) {
        super(location);
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

        ArithmeticBinaryExpression that = (ArithmeticBinaryExpression) o;
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
        String x1 = left.doGenSql();
        String x2 = right.doGenSql();
        if (left instanceof ArithmeticBinaryExpression l1 && this.operator.getLevel() > l1.operator.getLevel()) {
            x1 = String.format("(%s)", x1);
        }
        if (right instanceof ArithmeticBinaryExpression l2 && this.operator.getLevel() > l2.operator.getLevel()) {
            x2 = String.format("(%s)", x2);
        }
        return String.format("%s %s %s", x1, operator.getValue(), x2);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        Expression left1 = optimizer.optimize(left);
        Expression right1 = optimizer.optimize(right);
        return new ArithmeticBinaryExpression(operator, left1, right1);
    }
}
