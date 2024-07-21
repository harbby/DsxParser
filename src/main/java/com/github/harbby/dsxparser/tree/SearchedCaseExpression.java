package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SearchedCaseExpression extends Expression {
    private final List<WhenClause> whenClauses;
    private final Expression defaultValue;

    public SearchedCaseExpression(List<WhenClause> whenClauses, Expression defaultValue) {
        this(null, whenClauses, defaultValue);
    }

    public SearchedCaseExpression(NodeLocation location, List<WhenClause> whenClauses, Expression defaultValue) {
        super(location);
        requireNonNull(whenClauses, "whenClauses is null");

        this.whenClauses = List.copyOf(whenClauses);
        this.defaultValue = requireNonNull(defaultValue, "defaultValue is null");
    }

    public List<WhenClause> getWhenClauses() {
        return whenClauses;
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> childs = new ArrayList<>(whenClauses);
        childs.add(defaultValue);
        return childs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchedCaseExpression that = (SearchedCaseExpression) o;
        return Objects.equals(whenClauses, that.whenClauses) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whenClauses, defaultValue);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new SearchedCaseExpression(whenClauses.stream().map(x -> x.visit(optimizer)).toList(), optimizer.optimize(defaultValue));
    }

    @Override
    public String doGenSql() {
        if (whenClauses.size() == 1) {
            WhenClause whenClause = whenClauses.get(0);
            return String.format("if(%s, %s, %s)", whenClause.getOperand().doGenSql(), whenClause.getResult().doGenSql(), defaultValue.doGenSql());
        } else {
            StringBuilder builder = new StringBuilder("case");
            for (WhenClause whenClause : whenClauses) {
                builder.append(String.format(" when %s then %s", whenClause.getOperand().doGenSql(), whenClause.getResult().doGenSql()));
            }
            builder.append(" else ").append(defaultValue.doGenSql());
            builder.append(" end");
            return builder.toString();
        }
    }
}
