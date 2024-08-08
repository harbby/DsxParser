package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.rbo.ArithmeticConstantFoldingRule;
import com.github.harbby.dsxparser.rbo.ArithmeticUnaryRule;
import com.github.harbby.dsxparser.rbo.LiteralConcatConstantFoldingRule;
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
                .map(r -> r.optimize(childFirst))
                .orElse(childFirst);
    }

    public static Optimizer empty() {
        return EMPTY_OPTIMIZER;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Class<? extends RboRule<?>>> rboRules = new ArrayList<>();
        private boolean disableBuiltinRboRule = false;

        public Builder add(Class<? extends RboRule<?>> ruleClass) {
            this.rboRules.add(ruleClass);
            return this;
        }

        public Builder addAll(Collection<Class<? extends RboRule<?>>> ruleClassList) {
            this.rboRules.addAll(ruleClassList);
            return this;
        }

        public Builder disableBuiltinRboRule() {
            this.disableBuiltinRboRule = true;
            return this;
        }

        public Optimizer build() {
            if (!disableBuiltinRboRule) {
                this.addAll(INTERNAL_RBO_RULES);
            }
            return new Optimizer(rboRules);
        }
    }
}
