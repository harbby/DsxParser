package com.github.harbby.dsxparser.rbo;

import com.github.harbby.dsxparser.Optimizer;
import com.github.harbby.dsxparser.RboRule;
import com.github.harbby.dsxparser.function.CurrentDateFunc;
import com.github.harbby.dsxparser.function.CurrentTimeFunc;
import com.github.harbby.dsxparser.function.DateFromDaysSinceFunc;
import com.github.harbby.dsxparser.function.TimestampFromDateTimeFunc;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.FunctionCall;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.util.List;

public class DaysTimestampFromDateTimeRule implements RboRule<TimestampFromDateTimeFunc> {

    @Override
    public Class<TimestampFromDateTimeFunc> bind() {
        return TimestampFromDateTimeFunc.class;
    }

    @Override
    public boolean isMatch(TimestampFromDateTimeFunc exp) {
        List<Expression> arguments = exp.getArguments();
        Expression dateExp = arguments.get(0);
        Expression timeExp = arguments.get(1);
        return dateExp instanceof DateFromDaysSinceFunc it &&
                it.getArguments().get(1) instanceof CurrentDateFunc &&
                timeExp instanceof CurrentTimeFunc;
    }

    @Override
    public Expression optimize(Optimizer optimizer, TimestampFromDateTimeFunc expression) {
        Expression dateExp = expression.getArguments().get(0);
        Expression number = ((DateFromDaysSinceFunc) dateExp).getNumber();
        var f1 = new FunctionCall(null, QualifiedName.of("current_timestamp"), List.of());
        return new FunctionCall(null, QualifiedName.of("date_add"), List.of(f1, number));
    }
}
