package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;

public abstract class Literal extends Expression {
    protected Literal(NodeLocation location) {
        super(location);
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    public final Expression visit(Optimizer optimizer) {
        return this;
    }

    public abstract String getValueAsString();
}
