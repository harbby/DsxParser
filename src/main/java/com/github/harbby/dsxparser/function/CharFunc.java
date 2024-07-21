package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.LongLiteral;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

@FuncInfo(value = "char", argsNumber = 1)
public final class CharFunc extends DsxFunc {
    private final int code;

    public CharFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
        if (arguments.get(0) instanceof LongLiteral lt) {
            long l = lt.getValue();
            this.code = Math.toIntExact(l);
        } else {
            throw new IllegalStateException("function char input type should be integer, but is " + arguments.get(0));
        }
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        CharFunc o = (CharFunc) obj;
        return this.code == o.code;
    }

    @Override
    public String doGenSql() {
        return String.format("chr(%s)", code);
    }
}
