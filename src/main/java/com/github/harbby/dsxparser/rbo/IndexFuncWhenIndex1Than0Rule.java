package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.function.IndexFunc;
import com.github.harbby.dsxparser.tree.*;

import java.util.List;

import static com.github.harbby.dsxparser.tree.ComparisonExpression.Operator.EQUAL;
import static com.github.harbby.dsxparser.tree.ComparisonExpression.Operator.GREATER_THAN;

public class IndexFuncWhenIndex1Than0Rule implements RboRule<ComparisonExpression> {

    @Override
    public Class<ComparisonExpression> bind() {
        return ComparisonExpression.class;
    }

    @Override
    public boolean isMatch(ComparisonExpression expression) {
        Expression left = expression.getLeft();
        var operator = expression.getOperator();
        Expression right = expression.getRight();

        if (right instanceof IndexFunc) {
            Expression temp = left;
            left = right;
            right = temp;
            operator = operator.flip();
        }
        if (!(left instanceof IndexFunc x && x.indexIsOne())) {
            return false;
        }
        if (operator != GREATER_THAN && operator != EQUAL) {
            return false;
        }
        return right instanceof LongLiteral lt && lt.getValue() == 0;
    }

    @Override
    public Expression optimize(Optimizer optimizer, ComparisonExpression expression) {
        Expression left = expression.getLeft();
        var operator = expression.getOperator();
        Expression right = expression.getRight();

        if (right instanceof IndexFunc) {
            left = right;
            operator = operator.flip();
        }
        List<Expression> arguments = ((IndexFunc) left).getArguments();
        Expression column = optimizer.optimize(arguments.get(0));
        Expression delimiter = arguments.get(1);
        var optimizedExp = new FunctionCall(QualifiedName.of("contains"), List.of(column, delimiter));
        return switch (operator) {
            case GREATER_THAN -> optimizedExp;
            case EQUAL -> new NotExpression(optimizedExp);
            default -> throw new UnsupportedOperationException("Error in `isMatch`");
        };
    }
}
