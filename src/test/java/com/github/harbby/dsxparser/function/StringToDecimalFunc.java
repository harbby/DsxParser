package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "StringToDecimal", argsNumber = 1)
public class StringToDecimalFunc extends DsxFunc {

    public StringToDecimalFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String value = arguments.get(0).doGenSql();
        return String.format("cast(%s as decimal)", value);
    }
}
