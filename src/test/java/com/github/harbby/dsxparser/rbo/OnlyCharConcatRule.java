package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.tree.ConcatExpression;
import com.github.harbby.dsxparser.tree.Expression;

public class OnlyCharConcatRule implements RboRule<ConcatExpression> {

    @Override
    public Class<ConcatExpression> bind() {
        return null;
    }

    @Override
    public boolean isMatch(ConcatExpression expression) {
        return false;
    }

    @Override
    public Expression optimize( ConcatExpression expression) {
        return null;
    }
}
