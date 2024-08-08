package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.tree.ArithmeticUnaryExpression;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.LongLiteral;

public class ArithmeticUnaryRule implements RboRule<ArithmeticUnaryExpression> {

    @Override
    public Class<ArithmeticUnaryExpression> bind() {
        return ArithmeticUnaryExpression.class;
    }

    @Override
    public boolean isMatch(ArithmeticUnaryExpression expression) {
        return expression.getValue() instanceof LongLiteral && expression.getSign() == ArithmeticUnaryExpression.Sign.MINUS;
    }

    @Override
    public Expression optimize(ArithmeticUnaryExpression expression) {
        long value = ((LongLiteral) expression.getValue()).getValue();
        return new LongLiteral(-1 * value);
    }
}
