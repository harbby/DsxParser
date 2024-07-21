package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

public class SubStringExpression extends Expression {

    private final Expression base;
    private final Expression pos;
    private final Expression len;

    private SubStringExpression(Expression base, Expression pos, Expression len) {
        this(null, base, pos, len);
    }

    public SubStringExpression(NodeLocation location, Expression base, Expression pos, Expression len) {
        super(location);
        this.base = base;
        this.pos = pos;
        this.len = len;
    }

    public Expression getPos() {
        return pos;
    }

    public Expression getLen() {
        return len;
    }

    @Override
    public String doGenSql() {
        return String.format("substring(%s,%s,%s)", base.doGenSql(), pos.doGenSql(), len.doGenSql());
    }

    @Override
    public List<? extends Expression> getChildren() {
        return List.of(pos, len);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, len);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubStringExpression that = (SubStringExpression) o;
        return Objects.equals(pos, that.pos) &&
                Objects.equals(len, that.len);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new SubStringExpression(optimizer.optimize(base), optimizer.optimize(pos), optimizer.optimize(len));
    }
}
