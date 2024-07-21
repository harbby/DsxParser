package com.github.harbby.dsxparser.function;

import com.github.harbby.dsxparser.DsxFunc;
import com.github.harbby.dsxparser.FuncInfo;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.LongLiteral;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

@FuncInfo(value = "Index", argsNumber = 3)
public class IndexFunc extends DsxFunc {
    private final Expression indexExpression;
    private final boolean indexIsOne;

    public IndexFunc(QualifiedName name, List<Expression> arguments) {
        super(name, arguments);
        this.indexExpression = arguments.get(2);
        boolean isOne = false;
        if (indexExpression instanceof LongLiteral lt) {
            isOne = lt.getValue() == 1;
            checkState(lt.getValue() > 0, "index number should > 0");
        }
        this.indexIsOne = isOne;
    }

    public boolean indexIsOne() {
        return indexIsOne;
    }

    @Override
    public String doGenSql() {
        String column = arguments.get(0).doGenSql();
        String delimiter = arguments.get(1).doGenSql();
        if (indexIsOne) {
            return String.format("instr(%s, %s)", column, delimiter);
        } else {
            String index = arguments.get(2).doGenSql();
            return String.format("if(array_size(split(%s,%s))<=%s,0,len(substring_index(%s,%s,%s))+1)",
                    column, delimiter, index,
                    column, delimiter, index);
        }
    }
}
