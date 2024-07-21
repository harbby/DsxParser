package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DSXParserTest {
    private final DSXParser parser = DSXParserHelper.createParser();
    private final Optimizer optimizer = Optimizer.empty();

    @Test
    void simpleExpression1() {
        String exp = "(trim(123 + 1) + 1)";
        Expression expression = parser.parseExpression(exp);
        String sql = expression.doGenSql();
        Assertions.assertEquals("trim(123 + 1) + 1", sql);
    }

    @Test
    void parseSimpleCaseExpression() {
        String exp = "IF (resEMAIL1 > '0') Then SetNull()  Else  lnk_EmailRes.EMAIL1";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(resEMAIL1 > '0', null, lnk_EmailRes.EMAIL1)", sql);
    }

    @Test
    void LogicalBinaryExpression_test1() {
        String exp = "not (resEMAIL1 > 0) and true";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("!(resEMAIL1 > 0) and true", sql);
    }

    @Test
    void parseExpression() {
        String exp = "IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'N' THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_FIR,US_DMS_SALES_NAM_DMS_CUS_LST|' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FIR,US_DMS_SALES_NAM_DMS_CUS_LST' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_LST|' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FIR|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_FIR|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_LST|'  ELSE SetNull()";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertNotNull(sql);
    }

    @Test
    void convert_expression1() {
        String exp = "Convert(Char(0):char(9):Char(34),'',Trim(lnk_toValidate.NAM_DMS_CUS_LST))";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("replace(trim(lnk_toValidate.NAM_DMS_CUS_LST), chr(0)||chr(9)||chr(34), '')", sql);
    }

    @Test
    void trim3_expression() {
        String exp = "Trim(Trim(Trim(TRANSACTION_ID,Char(0),\"A\"),Char(9),\"A\"),Char(34),\"A\")";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("replace(replace(replace(TRANSACTION_ID, chr(0), ''), chr(9), ''), chr(34), '')", sql);
    }

    @Test
    void upCase_NullToEmpty_func_test() {
        String exp = "IF UpCase(Trim(NullToEmpty(FIRSTNAME))) = 'ORPHANED' AND UpCase(Trim(NullToEmpty(LASTNAME))) = 'VEHICLES' THEN '1' ELSE '0'";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(upper(trim(nvl(FIRSTNAME,''))) = 'ORPHANED' and upper(trim(nvl(LASTNAME,''))) = 'VEHICLES', '1', '0')", sql);
    }

    @Test
    void SetNull_test() {
        String exp = "If lnk_EmailRes.resEMAIL1>'0' Then SetNull()  Else  lnk_EmailRes.EMAIL1";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(lnk_EmailRes.resEMAIL1 > '0', null, lnk_EmailRes.EMAIL1)", sql);
    }

    @Test
    void is_null_test() {
        String exp = "If (IsNull(FIRSTNAME) OR Trim(NullToEmpty(FIRSTNAME))=''  or FIRSTNAME='N/A') Then 'N' Else 'Y'";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(isnull(FIRSTNAME) or trim(nvl(FIRSTNAME,'')) = '' or FIRSTNAME = 'N/A', 'N', 'Y')", sql);
    }

    @Test
    void casen_when_base_test1() {
        String exp = "If (ADDRESSLINE1='Y' And ZIP1='1') Then 'Y' Else If (ADDRESSLINE1='Y' And ((STATE1+CITY1)>'1')) Then 'Y'  else 'N'";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("case when ADDRESSLINE1 = 'Y' and ZIP1 = '1' then 'Y' when ADDRESSLINE1 = 'Y' and STATE1 + CITY1 > '1' then 'Y' else 'N' end", sql);
    }

    @Test
    void field_test() {
        String exp = "IF Field(svVehslsDlvyDt,'|',1) ='Y'  THEN svDelivaryDt ELSE IF Field(svVehslsContrDt,'|',1) ='Y' THEN  svContrilDt ELSE SetNull()";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("case when split_part(svVehslsDlvyDt,'|',1) = 'Y' then svDelivaryDt when split_part(svVehslsContrDt,'|',1) = 'Y' then svContrilDt else null end", sql);
    }

    @Test
    void IsValid_date_test() {
        String exp = "If IsNotNull(svDelivaryDt) and Trim(svDelivaryDt)<>'' and IsValid('Date',svDelivaryDt,'%yyyy%mm%dd') Then 'Y|||' Else SetNull()";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(isnotnull(svDelivaryDt) and trim(svDelivaryDt) <> '' and isNotNull(to_date(svDelivaryDt,'yyyyMMdd')), 'Y|||', null)", sql);
    }

    @Test
    void stringToTimestamp_test() {
        String exp = "IF Field(svTransDt1,'|',1) ='1' THEN StringToTimestamp(svTransDt,'%yyyy%mm%dd') ELSE SetNull()";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("if(split_part(svTransDt1,'|',1) = '1', to_timestamp(svTransDt,'yyyyMMdd'), null)", sql);
    }

    @Test
    void count_func_test() {
        String exp = "count('1,2,3', ',')";
        Expression expression = parser.parseExpression(exp);
        String sql = optimizer.optimize(expression).doGenSql();
        Assertions.assertEquals("array_size(split('1,2,3',','))-1", sql);
        // resoult = 2
    }
}