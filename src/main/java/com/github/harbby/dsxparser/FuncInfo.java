package com.github.harbby.dsxparser;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({TYPE})  //add METHOD
@Retention(RUNTIME)
@Documented
public @interface FuncInfo {
    String value();

    int[] argsNumber();
}
