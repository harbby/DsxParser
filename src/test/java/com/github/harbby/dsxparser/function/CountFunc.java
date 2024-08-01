package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "count", argsNumber = 2)
public class CountFunc extends DsxFunc {
    public CountFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(0).doGenSql();
        String delimiter = arguments.get(1).doGenSql();
        return String.format("array_size(split(%s,%s))-1", column, delimiter);
    }
}
