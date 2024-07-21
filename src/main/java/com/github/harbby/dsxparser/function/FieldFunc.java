package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "field", argsNumber = 3)
public class FieldFunc extends DsxFunc {
    public FieldFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(0).doGenSql();
        String delimiter = arguments.get(1).doGenSql();
        String index = arguments.get(2).doGenSql();
        return String.format("split_part(%s,%s,%s)", column, delimiter, index);
    }
}
