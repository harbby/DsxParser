package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "NullToEmpty", argsNumber = 1)
public class NullToEmptyFunc extends DsxFunc {
    public NullToEmptyFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        return String.format("nvl(%s,'')", arguments.get(0).doGenSql());
    }
}
