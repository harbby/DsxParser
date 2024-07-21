package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ArithmeticUnaryExpression extends Expression {
    public enum Sign {
        PLUS("+"),
        MINUS("-");
        private final String value;

        Sign(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final Expression value;
    private final Sign sign;

    private ArithmeticUnaryExpression(Sign sign, Expression value) {
        this(null, sign, value);
    }

    private ArithmeticUnaryExpression(NodeLocation location, Sign sign, Expression value) {
        super(location);
        requireNonNull(value, "value is null");
        requireNonNull(sign, "sign is null");

        this.value = value;
        this.sign = sign;
    }

    public static ArithmeticUnaryExpression positive(NodeLocation location, Expression value) {
        return new ArithmeticUnaryExpression(location, Sign.PLUS, value);
    }

    public static ArithmeticUnaryExpression negative(NodeLocation location, Expression value) {
        return new ArithmeticUnaryExpression(location, Sign.MINUS, value);
    }

    public Expression getValue() {
        return value;
    }

    public Sign getSign() {
        return sign;
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

        ArithmeticUnaryExpression that = (ArithmeticUnaryExpression) o;
        return Objects.equals(value, that.value) &&
                (sign == that.sign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, sign);
    }

    @Override
    public String doGenSql() {
        return String.format("%s%s", sign.getValue(), value.doGenSql());
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new ArithmeticUnaryExpression(sign, optimizer.optimize(value));
    }
}
