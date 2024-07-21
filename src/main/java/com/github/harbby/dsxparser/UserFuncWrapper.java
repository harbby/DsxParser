package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.QualifiedName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class UserFuncWrapper {
    private final Class<? extends DsxFunc> funcClass;

    private final String funcName;

    private final int[] argsNumber;

    private final Constructor<? extends DsxFunc> constructor;

    public UserFuncWrapper(Class<? extends DsxFunc> funcClass,
                           String funcName,
                           int[] argsNumber,
                           Constructor<? extends DsxFunc> constructor) {
        this.funcClass = funcClass;
        this.funcName = funcName;
        this.argsNumber = argsNumber;
        this.constructor = constructor;
    }

    public static UserFuncWrapper of(Class<? extends DsxFunc> funcClass) {
        requireNonNull(funcClass, "funcClass is null");
        Constructor<? extends DsxFunc> constructor = null;
        try {
            constructor = funcClass.getConstructor(QualifiedName.class, List.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(funcClass + " not found public Constructor(QualifiedName, List<Expression>)", e);
        }
        FuncInfo funcInfo = funcClass.getAnnotation(FuncInfo.class);
        requireNonNull(funcInfo, "User Function: " + funcClass + " not add @UserFunc");
        String name = funcInfo.value().toLowerCase(Locale.ROOT);
        checkState(funcInfo.argsNumber().length > 0, "@FuncInfo(argsNumber should not Empty)");
        return new UserFuncWrapper(funcClass, name, funcInfo.argsNumber(), constructor);
    }

    public int[] getArgsNumber() {
        return argsNumber;
    }

    public String getFuncName() {
        return funcName;
    }

    public Class<? extends DsxFunc> getFuncClass() {
        return funcClass;
    }

    public DsxFunc newInstance(QualifiedName qualifiedName, List<Expression> arguments)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String name = qualifiedName.toString();
        checkArgument(this.funcName.equals(name), "function name should be %s, but is %s", this.funcName, name);
        checkArgumentsNumber(name, arguments, this.argsNumber);
        return constructor.newInstance(qualifiedName, arguments);
    }

    private static void checkArgumentsNumber(String funcName, List<Expression> arguments, int... numberArray) {
        int size = arguments.size();
        for (int num : numberArray) {
            if (num == size) {
                return;
            }
        }
        String requiresArray = IntStream.of(numberArray)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("/"));
        throw new ParsingException(String.format("The `%s` requires %s parameters but the actual number is %s",
                funcName, requiresArray, size));
    }
}
