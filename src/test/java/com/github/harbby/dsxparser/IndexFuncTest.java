package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IndexFuncTest {
    private final DSXParser parser = DSXParserHelper.create();
    private final Optimizer optimizer = Optimizer.empty();

    @Test
    void index_func_distinct_optimizer_rule_test() {
        String exp = """
                If
                Index(svFirstname, '#',1) >0 Or
                 0< Index(svFirstname, char(34),1) or
                 Index(svFirstname, '@',1) =0
                Then 'N'
                Else 'Y'
                """;
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("if(instr(svFirstname, '#') > 0 or 0 < instr(svFirstname, chr(34)) or instr(svFirstname, '@') = 0, 'N', 'Y')", sql);
    }

    @Test
    void index_func_test0() {
        String exp = "INDEX('AAA11122ABB1619MM',1,4)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('AAA11122ABB1619MM',1))<=4,0,len(substring_index('AAA11122ABB1619MM',1,4))+1)", sql);
        //test spark.sql: // result = 12
    }

    @Test
    void index_func_test() {
        String exp = "index('P1234XXOO1299XX00P', 'XX', 2)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('P1234XXOO1299XX00P','XX'))<=2,0,len(substring_index('P1234XXOO1299XX00P','XX',2))+1)", sql);
        //test spark.sql: // result = 14
    }

    @Test
    void index_func_test2() {
        String exp = "INDEX('222','2',4)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('222','2'))<=4,0,len(substring_index('222','2',4))+1)", sql);
        // result = 0
    }

    @Test
    void index_func_test3() {
        String exp = "INDEX(\"1234\",'A',1)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("instr('1234', 'A')", sql);
        // result = 0
    }

    @Test
    void index_func_test4() {
        String exp = "INDEX('1,2',',',2)";
        Expression expression = parser.parseExpression(exp);
        String sql = Optimizer.empty().optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('1,2',','))<=2,0,len(substring_index('1,2',',',2))+1)", sql);
        // result = 0
    }

    @Test
    void count_test() {
        String exp = "count('1,2,3',CHAR(44))";
        Expression expression = parser.parseExpression(exp);
        String sql = expression.doGenSql();
        Assertions.assertEquals("array_size(split('1,2,3',chr(44)))-1", sql);
        // rs = 2
    }

    @Test
    void index_func_test5() {
        String exp = "index('1,2,3',CHAR(44),2)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('1,2,3',chr(44)))<=2,0,len(substring_index('1,2,3',chr(44),2))+1)", sql);
        // rs = 4
    }

    @Test
    void index_func_test6() {
        String exp = "index('1,2,3',CHAR(44),count('1,2,3',CHAR(44)))";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(array_size(split('1,2,3',chr(44)))<=array_size(split('1,2,3',chr(44)))-1,0,len(substring_index('1,2,3',chr(44),array_size(split('1,2,3',chr(44)))-1))+1)", sql);
        // rs = 4
    }
}
