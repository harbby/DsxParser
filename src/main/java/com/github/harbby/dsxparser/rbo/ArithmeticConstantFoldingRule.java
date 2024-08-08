package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.tree.ArithmeticBinaryExpression;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.LongLiteral;

public class ArithmeticConstantFoldingRule implements RboRule<ArithmeticBinaryExpression> {
    @Override
    public Class<ArithmeticBinaryExpression> bind() {
        return ArithmeticBinaryExpression.class;
    }

    @Override
    public boolean isMatch(ArithmeticBinaryExpression expression) {
        Expression left = expression.getLeft();
        Expression right = expression.getRight();
        return left instanceof LongLiteral && right instanceof LongLiteral;
    }

    @Override
    public Expression optimize(ArithmeticBinaryExpression expression) {
        long left = ((LongLiteral) expression.getLeft()).getValue();
        long right = ((LongLiteral) expression.getRight()).getValue();
        ArithmeticBinaryExpression.Operator operator = expression.getOperator();
        return switch (operator) {
            case ADD -> LongLiteral.of(left + right);
            case SUBTRACT -> LongLiteral.of(left - right);
            case MULTIPLY -> LongLiteral.of(left * right);
            case DIVIDE -> LongLiteral.of(left / right);
            case MODULUS -> LongLiteral.of(left % right);
            default -> throw new UnsupportedOperationException();
        };
    }
}
