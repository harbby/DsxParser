package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.tree.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ColumnAnalyzeTest {
    private final DSXParser parser = DSXParserHelper.createParser();

    @Test
    void baseTest() {
        String exp = "IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'N' THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_FIR,US_DMS_SALES_NAM_DMS_CUS_LST|' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FIR,US_DMS_SALES_NAM_DMS_CUS_LST' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_LST|' ELSE IF trim(SvWarnFullN) = 'Y' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FIR|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'N' AND trim(SvWarnLN) = 'Y'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_FIR|' ELSE IF trim(SvWarnFullN) = 'N' AND trim(SvWarnFN) = 'Y' AND trim(SvWarnLN) = 'N'  THEN 'US_DMS_SALES_NAM_DMS_CUS_FULL,US_DMS_SALES_NAM_DMS_CUS_LST|'  ELSE SetNull()";
        Expression expression = parser.parseExpression(exp);
        Set<String> columns = DSXParserHelper.analyzeFromFields(expression);
        Assertions.assertEquals(Set.of("SvWarnLN", "SvWarnFullN", "SvWarnFN"), columns);
    }

    @Test
    void baseTest2() {
        String exp = "IF (svADDRESSLINE1 = 'Y' OR EMAIL1 = 'Y') THEN '0' ELSE '1'";
        Expression expression = parser.parseExpression(exp);
        Set<String> columns = DSXParserHelper.analyzeFromFields(expression);
        Assertions.assertEquals(Set.of("svADDRESSLINE1", "EMAIL1"), columns);
    }

    @Test
    void baseTest3() {
        String exp = "if (Field(svVin,'|',1) ='Y') and ((len(trim(Field(svVin,'|',4))))>=8 and (len(trim(Field(svVin,'|',4))))<17)  then '1'  else '0'";
        Expression expression = parser.parseExpression(exp);
        Set<String> columns = DSXParserHelper.analyzeFromFields(expression);
        Assertions.assertEquals(Set.of("svVin"), columns);
    }

    @Test
    void baseTest4() {
        String exp = "IF (resEMAIL1 > '0') Then SetNull()  Else  lnk_EmailRes.EMAIL1";
        Expression expression = parser.parseExpression(exp);
        Set<String> columns = DSXParserHelper.analyzeFromFields(expression);
        Assertions.assertEquals(Set.of("resEMAIL1", "lnk_EmailRes.EMAIL1"), columns);
    }

    @Test
    void baseTest5() {
        String exp = "IF (CND1='1' OR CND2='1' OR CND3='1' Or CND4='1' Or CND5='1' Or CND6='1' Or CND7='1' Or CND8='1'  Or CND9='1' Or CND10='1' Or CND11='1' OR CND12='1' Or CND13='1' OR address1error = '1'  OR SvTrDtrange = '1' OR svVin = '1' OR svBadVin = '1' ) THEN '1' ELSE '0'";
        Expression expression = parser.parseExpression(exp);
        Set<String> columns = DSXParserHelper.analyzeFromFields(expression);
        Assertions.assertEquals(Set.of("svBadVin", "svVin", "SvTrDtrange", "address1error", "CND13", "CND12", "CND11", "CND10", "CND9", "CND8", "CND7", "CND6", "CND5", "CND4", "CND3", "CND2", "CND1"), columns);
    }
}
