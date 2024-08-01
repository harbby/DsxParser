package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;
import com.github.harbby.dsxparser.tree.StringLiteral;

import java.util.List;

@FuncInfo(value = "Trim", argsNumber = {1, 2, 3})
public class TrimFunc extends DsxFunc {

    public TrimFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
    }

    @Override
    public String doGenSql() {
        if (arguments.size() == 1) {
            return defaultGenSql(this);
        }
        String column = arguments.get(0).doGenSql();
        String flag = arguments.get(1).doGenSql();
        if (arguments.size() == 3) {
            String mode = ((StringLiteral) arguments.get(2)).getValue();
            if (!"A".equals(mode)) {
                throw new UnsupportedOperationException("not support trim(column, flag, " + mode + ")");
            }
        }
        return String.format("replace(%s, %s, '')", column, flag);
    }
}
