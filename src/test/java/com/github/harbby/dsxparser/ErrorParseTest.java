package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ErrorParseTest {
    private final DSXParser parser = DSXParserHelper.create();
    private final Optimizer optimizer = DSXParserHelper.createOptimizer();

    @Test
    void error_parse1() {
        String exp = "lnk_xfm2.DTE_MODL_YR:f2";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("lnk_xfm2.DTE_MODL_YR || f2", sql);
    }

    @Test
    void error_parse2() {
        String exp = "lnk_xfm2.DTE_MODL_YR:'-01-01'";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("lnk_xfm2.DTE_MODL_YR || '-01-01'", sql);
    }

    @Test
    void error_parse3() {
        String exp = "IsValid(\"date\",lnk_xfm2.DTE_MODL_YR:'-01-01')=1";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("isNotNull(to_date(lnk_xfm2.DTE_MODL_YR || '-01-01','yyyy-MM-dd')) = 1", sql);
    }

    @Test
    void left_test() {
        String exp = "left(to_api.TRANSACTION_ID,2)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("left(to_api.TRANSACTION_ID, 2)", sql);
    }

    @Test
    void right_test() {
        String exp = "right(to_api.TRANSACTION_ID,2)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("right(to_api.TRANSACTION_ID, 2)", sql);
    }

    @Test
    void substr_parse4() {
        String exp = "lnk_BPM_Fl.TRANSACTION_ID[1,2]";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("substring(lnk_BPM_Fl.TRANSACTION_ID,1,2)", sql);
    }

    @Test
    void substr_parse4_2() {
        String exp = "TRANSACTION_ID[1,2] || '-' || TRANSACTION_ID[3,2]";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("substring(TRANSACTION_ID,1,2) || '-' || substring(TRANSACTION_ID,3,2)", sql);
    }

    @Test
    void decimal_type_test() {
        String exp = "If Trim(Ln_Read_Src.RO_HDR_CP_STOT_AMT_DMS) <> ''  Then StringToDecimal(Ln_Read_Src.RO_HDR_CP_STOT_AMT_DMS) Else 0.00";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(trim(Ln_Read_Src.RO_HDR_CP_STOT_AMT_DMS) <> '', cast(Ln_Read_Src.RO_HDR_CP_STOT_AMT_DMS as decimal), 0.00)", sql);
    }

    @Test
    void error_parse4() {
        String exp = "In.CITY + xx.group";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("In.CITY + xx.group", sql);
    }

    @Test
    void TimestampFromDateTime_test() {
        String exp = "TimestampFromDateTime(StringToDate('2024-07-20'), '12:00:00')";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("to_timestamp(to_date('2024-07-20')||' '||'12:00:00')", sql);
    }

    @Test
    void Identifier_test() {
        String exp = "@PARTITIONNUM + (@NUMPARTITIONS * (@INROWNUM - 1)) + 1";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("@PARTITIONNUM + @NUMPARTITIONS * (@INROWNUM - 1) + 1", sql);
    }

    @Test
    void Identifier_test2() {
        String exp = "LEN($ev_PROJLOC : CDMParams.source : \"/accessories_customers_\")";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("len($ev_PROJLOC || CDMParams.source || '/accessories_customers_')", sql);
    }

    @Test
    void len_func_test() {
        String exp = "LEFT('abc',LEN(from_src_Xtract.FilePath)-nLenRem-5)";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("left('abc', len(from_src_Xtract.FilePath) - nLenRem - 5)", sql);
    }

    @Test
    void windows_path_test() {
        String exp = "index('C:\\Program Files (x86)\\temp', '\\', 1) > 0";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("contains('C:\\\\Program Files (x86)\\\\temp', '\\\\')", sql);
    }

    @Test
    void unicode_str_test() {
        // 🦄
        String exp = "'你' || '好\uD83E\uDD84'";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("'你好\uD83E\uDD84'", sql);
    }

    @Test
    void str_test() {
        String exp = "contains('a\nb', '\n')";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("contains('a\nb', '\n')", sql);
    }

    @Test
    void error_parse5() {
        String exp = "TimestampToString(Extract.updated_at, \"%yyyy-%mm-%ddT%hh:%nn:%ss.3Z\")";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("timestamptostring(Extract.updated_at, '%yyyy-%mm-%ddT%hh:%nn:%ss.3Z')", sql);
    }
}
