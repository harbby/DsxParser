package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.FunctionCall;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class DsxFunc extends FunctionCall {
    protected final QualifiedName name;
    protected final List<Expression> arguments;

    public DsxFunc(QualifiedName name, List<Expression> arguments) {
        super(null, name, arguments);
        requireNonNull(name, "name is null");
        requireNonNull(arguments, "arguments is null");
        this.name = name;
        this.arguments = arguments;
    }

    public String getFuncName() {
        return name.toString();
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public abstract String doGenSql();

    @Override
    public final Expression visit(Optimizer optimizer) {
        var optimized = this.arguments.stream().map(optimizer::optimize).toList();
        try {
            return UserFuncWrapper.of(this.getClass()).newInstance(name, optimized);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("new Instance func failed", e);
        }
    }
}
