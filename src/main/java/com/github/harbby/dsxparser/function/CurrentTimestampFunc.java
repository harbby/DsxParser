package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "CurrentTimestamp", argsNumber = 0)
public class CurrentTimestampFunc extends DsxFunc {

    public CurrentTimestampFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    public String doGenSql() {
        return "current_timestamp()";
    }
}
