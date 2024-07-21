package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;
import com.github.harbby.dsxparser.tree.StringLiteral;

import java.util.List;

@FuncInfo(value = "StringToTimestamp", argsNumber = {1, 2})
public class StringToTimestampFunc extends DsxFunc {
    public StringToTimestampFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(0).doGenSql();
        String format;
        if (arguments.size() == 1) {
            format = "yyyy-MM-dd hh:mm:ss";
        } else {
            format = ((StringLiteral) arguments.get(1)).getValue();
            format = dateFormatCast(format);
        }
        return String.format("to_timestamp(%s,'%s')", column, format);
    }

    public static String dateFormatCast(String format) {
        return format.replace("%yyyy", "yyyy")
                .replace("%mm", "MM")
                .replace("%dd", "dd")
                .replace("%hh", "HH")
                .replace("%nn", "mm")
                .replace("%ss", "ss");
    }
}
