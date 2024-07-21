package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;

public class WhenClause
        extends Expression {
    private final Expression operand;
    private final Expression result;

    public WhenClause(Expression operand, Expression result) {
        this(null, operand, result);
    }

    public WhenClause(NodeLocation location, Expression operand, Expression result) {
        super(location);
        this.operand = operand;
        this.result = result;
    }

    public Expression getOperand() {
        return operand;
    }

    public Expression getResult() {
        return result;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(operand, result);
    }

    @Override
    public WhenClause visit(Optimizer optimizer) {
        return new WhenClause(optimizer.optimize(operand), optimizer.optimize(result));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WhenClause that = (WhenClause) o;
        return Objects.equals(operand, that.operand) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand, result);
    }

    @Override
    public String doGenSql() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("when %s then %s", operand, result);
    }
}
