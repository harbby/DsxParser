package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;
import com.github.harbby.dsxparser.tree.StringLiteral;

import java.util.List;

import static com.github.harbby.dsxparser.function.StringToTimestampFunc.dateFormatCast;

@FuncInfo(value = "StringToDate", argsNumber = {1, 2})
public class StringToDateFunc extends DsxFunc {

    public StringToDateFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(0).doGenSql();
        String format;
        if (arguments.size() == 1) {
            format = "yyyy-MM-dd";
        } else {
            format = ((StringLiteral) arguments.get(1)).getValue();
            format = dateFormatCast(format);
        }
        if ("yyyy-MM-dd".equals(format)) {
            return String.format("to_date(%s)", column);
        } else {
            return String.format("to_date(%s,'%s')", column, format);
        }
    }
}
