package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "DateFromDaysSince", argsNumber = 2)
public class DateFromDaysSinceFunc extends DsxFunc {

    public DateFromDaysSinceFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    public Expression getNumber() {
        return arguments.get(0);
    }

    @Override
    public String doGenSql() {
        return String.format("date_add(%s,%s)", arguments.get(1).doGenSql(), getNumber().doGenSql());
    }
}
