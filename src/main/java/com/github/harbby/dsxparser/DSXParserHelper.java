package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.function.*;
import com.github.harbby.dsxparser.tree.DereferenceExpression;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.Identifier;
import com.github.harbby.dsxparser.tree.Node;

import java.util.*;

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

    public static DSXParser createParser() {
        return DSXParser.builder()
                .register(builtinFunctions)
                .build();
    }

    public static Set<String> analyzeFromFields(Expression expression) {
        Deque<Node> stack = new LinkedList<>();
        Set<String> set = new LinkedHashSet<>();
        stack.add(expression);
        Node it;
        while ((it = stack.poll()) != null) {
            if (it instanceof DereferenceExpression exp) {
                set.add(exp.toString());
            } else if (it instanceof Identifier exp) {
                set.add(exp.toString());
            } else {
                List<? extends Node> children = it.getChildren();
                assert !(children instanceof LinkedList<? extends Node>);
                int size = children.size();
                for (int i = 1; i <= size; i++) {
                    stack.addFirst(children.get(size - i));
                }
            }
        }
        return set;
    }
}
