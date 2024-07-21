package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;

public abstract class Expression extends Node {
    protected Expression(NodeLocation location) {
        super(location);
    }

    /**
     * Please do not call this method from a subclass, as this will disable the optimizer.
     */
    public abstract String doGenSql();

    public abstract List<? extends Expression> getChildren();

    public abstract Expression visit(Optimizer optimizer);

    @Override
    public String toString() {
        //throw new UnsupportedOperationException(format("not yet implemented: %s.visit%s", getClass().getName(), this.getClass().getSimpleName()));
        return this.doGenSql();
    }
}