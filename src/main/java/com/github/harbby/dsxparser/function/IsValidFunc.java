package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;
import com.github.harbby.dsxparser.tree.StringLiteral;

import java.util.List;
import java.util.Locale;

import static com.github.harbby.dsxparser.function.StringToTimestampFunc.dateFormatCast;

@FuncInfo(value = "IsValid", argsNumber = {2, 3})
public class IsValidFunc extends DsxFunc {
    public IsValidFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        String mode = ((StringLiteral) arguments.get(0)).getValue().toLowerCase(Locale.ROOT);
        if (!"date".equals(mode)) {
            return defaultGenSql(this);
        }
        String column = arguments.get(1).doGenSql();
        String format;
        if (arguments.size() == 2) {
            format = "%yyyy-%mm-%dd";
        } else {
            format = ((StringLiteral) arguments.get(2)).getValue();
        }

        format = dateFormatCast(format);
        return String.format("to_date(%s,'%s')!=null", column, format);
    }
}
