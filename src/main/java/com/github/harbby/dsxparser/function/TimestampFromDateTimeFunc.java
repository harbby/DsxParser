package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "TimestampFromDateTime", argsNumber = 2)
public class TimestampFromDateTimeFunc extends DsxFunc {

    public TimestampFromDateTimeFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String date = arguments.get(0).doGenSql();
        String time = arguments.get(1).doGenSql();
        return String.format("to_timestamp(%s||' '||%s)", date, time);
    }
}
