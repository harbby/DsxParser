package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.tree.ConcatExpression;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.Literal;
import com.github.harbby.dsxparser.tree.StringLiteral;

import java.util.LinkedList;
import java.util.Queue;

public class LiteralConcatConstantFoldingRule implements RboRule<ConcatExpression> {

    @Override
    public Class<ConcatExpression> bind() {
        return ConcatExpression.class;
    }

    private static boolean isMatch0(Expression expression) {
        if (expression instanceof ConcatExpression x) {
            return isMatch0(x.getLeft()) && isMatch0(x.getRight());
        }
        return expression instanceof Literal;
    }

    @Override
    public boolean isMatch(ConcatExpression expression) {
        return isMatch0(expression.getLeft()) && isMatch0(expression.getRight());
    }

    @Override
    public Expression optimize(Optimizer optimizer, ConcatExpression expression) {
        StringBuilder builder = new StringBuilder();
        Queue<Expression> stack = new LinkedList<>();
        stack.add(expression);
        Expression it;
        while ((it = stack.poll()) != null) {
            if (it instanceof ConcatExpression x) {
                stack.add(x.getLeft());
                stack.add(x.getRight());
            } else {
                String value = ((Literal) it).getValueAsString();
                builder.append(value);
            }
        }
        return new StringLiteral(builder.toString());
    }
}
