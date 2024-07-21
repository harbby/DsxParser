package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.rbo.*;
import com.github.harbby.dsxparser.tree.Expression;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Optimizer {

    private static final Optimizer EMPTY_OPTIMIZER = new Optimizer(Collections.emptyList()) {
        @Override
        public void registerRBO(Class<? extends RboRule<?>> ruleClass) {
            throw new UnsupportedOperationException("Empty Optimizer do not support registerRBO rele");
        }
    };

    private final Map<Class<? extends Expression>, Set<RboRule<?>>> ruleMap = new HashMap<>();

    private static final List<Class<? extends RboRule<?>>> INTERNAL_RBO_RULES = Arrays.asList(
            ArithmeticUnaryRule.class,
            DaysTimestampFromDateTimeRule.class,
            IndexFuncWhenIndex1Than0Rule.class,
            ArithmeticConstantFoldingRule.class,
            LiteralConcatConstantFoldingRule.class);

    private Optimizer(List<Class<? extends RboRule<?>>> ruleList) {
        for (Class<? extends RboRule<?>> ruleClass : ruleList) {
            registerRBO(ruleClass);
        }
    }

    public void registerRBO(Class<? extends RboRule<?>> ruleClass) {
        RboRule<?> rboRule = instanceRule(ruleClass);
        ruleMap.computeIfAbsent(rboRule.bind(), k -> new HashSet<>()).add(rboRule);
    }

    public <T extends Expression> Optional<RboRule<T>> match(T exp) {
        Set<? extends RboRule<T>> rules = lookupRule(exp);
        for (RboRule<T> rboRule : rules) {
            if (rboRule.isMatch(exp)) {
                return Optional.of(rboRule);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T extends Expression> Set<? extends RboRule<T>> lookupRule(T exp) {
        Set<? extends RboRule<?>> rules = ruleMap.getOrDefault(exp.getClass(), Collections.emptySet());
        return (Set<? extends RboRule<T>>) rules;
    }

    private static RboRule<?> instanceRule(Class<? extends RboRule<?>> ruleClass) {
        try {
            return ruleClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("instance rule class failed", e);
        }
    }

    public Expression optimize(Expression e) {
        Expression childFirst = e.visit(this);
        return this.match(childFirst)
                .map(r -> r.optimize(this, childFirst))
                .orElse(childFirst);
    }

    public static Optimizer create() {
        return new Optimizer(INTERNAL_RBO_RULES);
    }

    public static Optimizer create(List<Class<? extends RboRule<?>>> ruleList) {
        return new Optimizer(ruleList);
    }

    public static Optimizer empty() {
        return EMPTY_OPTIMIZER;
    }
}
