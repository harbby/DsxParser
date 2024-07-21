package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "convert", argsNumber = 3)
public class ConvertFunc extends DsxFunc {
    public ConvertFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        return String.format("replace(%s, %s, %s)",
                arguments.get(2).doGenSql(),
                arguments.get(0).doGenSql(),
                arguments.get(1).doGenSql());
    }
}
