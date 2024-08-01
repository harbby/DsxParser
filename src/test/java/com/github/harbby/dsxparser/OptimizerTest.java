package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptimizerTest {
    private final DSXParser parser = DSXParserHelper.create();
    private final Optimizer optimizer = DSXParserHelper.createOptimizer();

    @Test
    void timestampfromdatetime_test() {
        String exp = "timestampfromdatetime(datefromdayssince(1, currentdate()), currenttime())";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("date_add(current_timestamp(), 1)", sql);
    }

    @Test
    void timestampfromdatetime_test2() {
        String exp = "timestampfromdatetime(datefromdayssince(-2, currentdate()), currenttime())";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("date_add(current_timestamp(), -2)", sql);
    }

    @Test
    void index_func_enable_optimizer_rule_test() {
        String exp = """
                If
                Index(svFirstname, '#',1) >0 Or
                 0< Index(svFirstname, char(34),1) or
                 Index(svFirstname, '@',1) =0
                Then 'N'
                Else 'Y'
                """;
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(contains(svFirstname, '#') or contains(svFirstname, chr(34)) or !contains(svFirstname, '@'), 'N', 'Y')", sql);
    }

    @Test
    void simpleExpression1() {
        // String exp = "Convert(Char(0):Char(9):Char(34),'',Trim(lnk_EmailRes.NAM_DMS_CUS_FIR))";
        String exp = "(trim(123 + 1) + 1)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("trim(124) + 1", sql);
    }

    @Test
    void ArithmeticConstantFolding_test1() {
        // String exp = "Convert(Char(0):Char(9):Char(34),'',Trim(lnk_EmailRes.NAM_DMS_CUS_FIR))";
        String exp = "1 + ((5 + 1) * 2)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("13", sql);
    }

    @Test
    void ArithmeticConstantFolding_test2() {
        String exp = "1 + -1 * -2";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("3", sql);
    }

    @Test
    void StringConstantFolding_test2() {
        String exp = "3.14 || '15' || 926";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("'3.1415926'", sql);
    }
}
