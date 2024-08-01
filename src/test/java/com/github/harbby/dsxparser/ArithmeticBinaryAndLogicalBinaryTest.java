package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArithmeticBinaryAndLogicalBinaryTest {
    private final DSXParser parser = DSXParserHelper.create();

    @Test
    void simpleArithmeticBinaryExpression1() {
        String exp = "1 + ((5 + 1) * 2)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("1 + (5 + 1) * 2", sql);
    }

    @Test
    void simpleArithmeticBinaryExpression2() {
        String exp = "1 + (5 + 1) * 2";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("1 + (5 + 1) * 2", sql);
    }

    @Test
    void simpleArithmeticBinaryExpression3() {
        String exp = "1 + 5 * 2 - 1";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("1 + 5 * 2 - 1", sql);
    }

    @Test
    void simpleArithmeticBinaryExpression4() {
        String exp = "5 * (3 - 1) % 2";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("5 * (3 - 1) % 2", sql);
    }

    @Test
    void simpleArithmeticBinaryExpression5() {
        String exp = "1 + -1 * -2";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("1 + -1 * -2", sql);
    }

    @Test
    void LogicalBinary_test1() {
        String exp = "(a and b) or not (c and d)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("(a and b) or !(c and d)", sql);
    }

    @Test
    void LogicalBinary_test2() {
        String exp = "(a and b) or (c and d)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("(a and b) or (c and d)", sql);
    }

    @Test
    void LogicalBinary_test3() {
        String exp = "(a and b) and (c and d)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("a and b and c and d", sql);
    }

    @Test
    void LogicalBinary_test4() {
        String exp = "(a and b) and (c or d)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("a and b and (c or d)", sql);
    }

    @Test
    void LogicalBinary_test5() {
        String exp = "(a and b) or (c or d)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("(a and b) or c or d", sql);
    }

    @Test
    void LogicalBinary_test6() {
        String exp = "a and b or c";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("(a and b) or c", sql);
    }
}
