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
    private final String mode;
    private final String dateFormat;

    public IsValidFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
        String mode = ((StringLiteral) arguments.get(0)).getValue().toLowerCase(Locale.ROOT);
        String format;
        switch (mode) {
            case "date" -> {
                if (arguments.size() == 2) {
                    format = "%yyyy-%mm-%dd";
                } else {
                    format = ((StringLiteral) arguments.get(2)).getValue();
                }
            }
            case "timestamp" -> {
                if (arguments.size() == 2) {
                    format = "%yyyy-%mm-%dd %hh:%nn:%ss";
                } else {
                    format = ((StringLiteral) arguments.get(2)).getValue();
                }
            }
            default -> throw new UnsupportedOperationException("not support IsValid('" + mode + "', arg2, arg3)");
        }
        this.dateFormat = format;
        this.mode = mode;
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(1).doGenSql();
        String format = dateFormatCast(this.dateFormat);
        String func = switch (mode) {
            case "date" -> "to_date";
            case "timestamp" -> "to_timestamp";
            default -> throw new UnsupportedOperationException();
        };
        return String.format("isNotNull(%s(%s,'%s'))", func, column, format);
    }
}
