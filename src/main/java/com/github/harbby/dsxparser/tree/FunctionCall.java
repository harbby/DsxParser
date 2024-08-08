package com.github.harbby.dsxparser.tree;

import com.github.harbby.dsxparser.Optimizer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class FunctionCall extends Expression {
    protected final QualifiedName name;
    protected final List<Expression> arguments;

    public FunctionCall(QualifiedName name, List<Expression> arguments) {
        this(null, name, arguments);
    }

    public FunctionCall(NodeLocation location, QualifiedName name, List<Expression> arguments) {
        super(location);
        requireNonNull(name, "name is null");
        requireNonNull(arguments, "arguments is null");

        this.name = name;
        this.arguments = arguments;
    }

    public String getFuncName() {
        return getName().toString();
    }

    public QualifiedName getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public Expression getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    public List<Expression> getChildren() {
        return List.copyOf(arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        FunctionCall o = (FunctionCall) obj;
        return Objects.equals(name, o.name) &&
                Objects.equals(arguments, o.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    @Override
    public String doGenSql() {
        return defaultGenSql(this);
    }

    protected static String defaultGenSql(FunctionCall func) {
        String args = func.arguments.stream().map(Expression::doGenSql).collect(Collectors.joining(", "));
        return String.format("%s(%s)", func.getFuncName(), args);
    }

    @Override
    public Expression visit(Optimizer optimizer) {
        return new FunctionCall(name, arguments.stream().map(optimizer::optimize).toList());
    }
}
