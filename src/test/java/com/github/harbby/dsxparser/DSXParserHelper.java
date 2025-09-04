package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.function.*;
import com.github.harbby.dsxparser.rbo.DaysTimestampFromDateTimeRule;
import com.github.harbby.dsxparser.rbo.IndexFuncWhenIndex1Than0Rule;

import java.util.List;

public class DSXParserHelper {
    private DSXParserHelper() {
    }

    private static final List<Class<? extends DsxFunc>> builtinFunctions = List.of(
            CharFunc.class,
            ConvertFunc.class,
            TrimFunc.class,
            NullToEmptyFunc.class,
            NullToZeroFunc.class,
            UpCaseFunc.class,
            SetNullFunc.class,
            FieldFunc.class,
            IsValidFunc.class,
            StringToTimestampFunc.class,
            CurrentDateFunc.class,
            CurrentTimeFunc.class,
            CurrentTimestampFunc.class,
            TimestampFromDateTimeFunc.class,
            DateFromDaysSinceFunc.class,
            IndexFunc.class,
            LeftFunc.class,
            StringToDecimalFunc.class,
            StringToDateFunc.class,
            CountFunc.class);

    public static List<Class<? extends DsxFunc>> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public static DSXParser create() {
        return DSXParser.builder()
                .register(builtinFunctions)
                .build();
    }

    public static Optimizer createOptimizer() {
        return Optimizer.builder()
                .add(DaysTimestampFromDateTimeRule.class)
                .add(IndexFuncWhenIndex1Than0Rule.class)
                .build();
    }
}
