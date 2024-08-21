package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;

/**
 * Rule-Based Optimizer
 */
public interface RboRule<T extends Expression> {

    Class<T> bind();

    boolean isMatch(T expression);

    default Expression optimize(T expression) {
        return expression;
    }

    @Deprecated
    default Expression optimize(Optimizer optimizer, T expression) {
        return expression;
    }
}
