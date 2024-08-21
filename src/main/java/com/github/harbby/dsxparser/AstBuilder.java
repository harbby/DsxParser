package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.antlr4.SqlBaseBaseVisitor;
import com.github.harbby.dsxparser.antlr4.SqlBaseLexer;
import com.github.harbby.dsxparser.antlr4.SqlBaseParser;
import com.github.harbby.dsxparser.tree.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AstBuilder extends SqlBaseBaseVisitor<Node> {
    private static final Logger logger = LoggerFactory.getLogger(AstBuilder.class);
    private final Map<String, UserFuncWrapper> funcMap;

    Consumer<RuntimeException> warningConsumer = e -> logger.warn("parse warning: {}", e.getMessage());

    AstBuilder(Map<String, UserFuncWrapper> funcMap) {
        this.funcMap = funcMap;
    }

    @Override
    public Node visitSingleExpression(SqlBaseParser.SingleExpressionContext ctx) {
        return this.visit(ctx.expression());
    }

    @Override
    public Expression visitExpression(SqlBaseParser.ExpressionContext ctx) {
        return (Expression) this.visit(ctx.booleanExpression());
    }

    @Override
    public Node visitSimpleIfThen(SqlBaseParser.SimpleIfThenContext ctx) {
        Expression condition = this.visit(ctx.condition, Expression.class);
        Expression ifValue = visitExpression(ctx.result);
        List<WhenClause> whenClauses = new ArrayList<>();
        whenClauses.add(new WhenClause(getLocation(ctx.condition), condition, ifValue));
        whenClauses.addAll(visit(ctx.whenClause(), WhenClause.class));
        return new SearchedCaseExpression(
                getLocation(ctx),
                whenClauses,
                this.visit(ctx.elseExpression, Expression.class));
    }

    @Override
    public Node visitWhenClause(SqlBaseParser.WhenClauseContext ctx) {
        return new WhenClause(getLocation(ctx), (Expression) visit(ctx.condition), (Expression) visit(ctx.result));
    }

    @Override
    public Node visitComparison(SqlBaseParser.ComparisonContext context) {
        return new ComparisonExpression(
                getLocation(context.comparisonOperator()),
                getComparisonOperator(((TerminalNode) context.comparisonOperator().getChild(0)).getSymbol()),
                (Expression) visit(context.value),
                (Expression) visit(context.right));
    }

    @Override
    public Node visitLogicalNot(SqlBaseParser.LogicalNotContext context) {
        return new NotExpression(getLocation(context), visit(context.booleanExpression(), Expression.class));
    }

    @Override
    public Node visitLogicalBinary(SqlBaseParser.LogicalBinaryContext context) {
        LogicalBinaryExpression.Operator operator = getLogicalBinaryOperator(context.operator);
        boolean warningForMixedAndOr = false;
        Expression left = (Expression) visit(context.left);
        Expression right = (Expression) visit(context.right);

        if (operator.equals(LogicalBinaryExpression.Operator.OR) &&
                (mixedAndOrOperatorParenthesisCheck(right, context.right, LogicalBinaryExpression.Operator.AND) ||
                        mixedAndOrOperatorParenthesisCheck(left, context.left, LogicalBinaryExpression.Operator.AND))) {
            warningForMixedAndOr = true;
        }

        if (operator.equals(LogicalBinaryExpression.Operator.AND) &&
                (mixedAndOrOperatorParenthesisCheck(right, context.right, LogicalBinaryExpression.Operator.OR) ||
                        mixedAndOrOperatorParenthesisCheck(left, context.left, LogicalBinaryExpression.Operator.OR))) {
            warningForMixedAndOr = true;
        }

        if (warningForMixedAndOr) {
            warningConsumer.accept(new ParsingWarning(
                    "The query contains OR and AND operators without proper parentheses. "
                            + "Make sure the operators are guarded by parentheses in order "
                            + "to fetch logically correct results.",
                    context.getStart().getLine(), context.getStart().getCharPositionInLine()));
        }

        return new LogicalBinaryExpression(
                getLocation(context.operator),
                operator,
                left,
                right);
    }

    @Override
    public Node visitParenthesizedExpression(SqlBaseParser.ParenthesizedExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Node visitFunctionCall(SqlBaseParser.FunctionCallContext ctx) {
        QualifiedName qualifiedName = getQualifiedName(ctx.qualifiedName());
        List<Expression> arguments = this.visit(ctx.expression(), Expression.class);
        String funcName = qualifiedName.toString();

        checkState(ctx.ASTERISK() == null, "d'not support func(*)");
        checkState(ctx.setQuantifier() == null, "d'not support func(distinct exp)");
        UserFuncWrapper funcWrapper = funcMap.get(funcName);
        if (funcWrapper != null) {
            try {
                return funcWrapper.newInstance(qualifiedName, arguments);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ParsingException("Instance func " + funcWrapper.getFuncClass() + " failed", e, getLocation(ctx));
            }
        }
        return new FunctionCall(getLocation(ctx), qualifiedName, arguments);
    }

    @Override
    public Node visitArithmeticUnary(SqlBaseParser.ArithmeticUnaryContext ctx) {
        // operater = + or -
        Expression child = (Expression) visit(ctx.valueExpression());
        return switch (ctx.operator.getType()) {
            case SqlBaseLexer.MINUS -> ArithmeticUnaryExpression.negative(getLocation(ctx), child);
            case SqlBaseLexer.PLUS -> ArithmeticUnaryExpression.positive(getLocation(ctx), child);
            default -> throw new UnsupportedOperationException("Unsupported sign: " + ctx.operator.getText());
        };
    }

    @Override
    public Node visitArithmeticBinary(SqlBaseParser.ArithmeticBinaryContext context) {
        return new ArithmeticBinaryExpression(
                getLocation(context.operator),
                getArithmeticBinaryOperator(context.operator),
                (Expression) visit(context.left),
                (Expression) visit(context.right));
    }

    @Override
    public Node visitConcatenation(SqlBaseParser.ConcatenationContext context) {
        return new ConcatExpression(
                getLocation(context.CONCAT().getSymbol()),
                (Expression) visit(context.left),
                (Expression) visit(context.right));
    }

    @Override
    public Node visitDereference(SqlBaseParser.DereferenceContext context) {
        return new DereferenceExpression(
                getLocation(context),
                (Expression) visit(context.base),
                (Identifier) visit(context.fieldName));
    }

    @Override
    public Node visitBasicStringLiteral(SqlBaseParser.BasicStringLiteralContext context) {
        return new StringLiteral(getLocation(context), unquote(context.STRING().getText()));
    }

    @Override
    public Node visitUnicodeStringLiteral(SqlBaseParser.UnicodeStringLiteralContext context) {
        throw new UnsupportedOperationException();
        //return new StringLiteral(getLocation(context), decodeUnicodeLiteral(context));
    }

    @Override
    public Node visitSubstring2(SqlBaseParser.Substring2Context ctx) {
        return new SubStringExpression(getLocation(ctx),
                visit(ctx.value, Expression.class),
                visit(ctx.pos, Expression.class),
                visit(ctx.len, Expression.class));
    }

    @Override
    public Node visitDoubleLiteral(SqlBaseParser.DoubleLiteralContext context) {
        return new DoubleLiteral(getLocation(context), context.getText());
    }

    @Override
    public Node visitDecimalLiteral(SqlBaseParser.DecimalLiteralContext ctx) {
        return new DecimalLiteral(getLocation(ctx), ctx.getText());
    }

    @Override
    public Node visitBooleanValue(SqlBaseParser.BooleanValueContext context) {
        return new BooleanLiteral(getLocation(context), context.getText());
    }

    @Override
    public Node visitIntegerLiteral(SqlBaseParser.IntegerLiteralContext context) {
        return new LongLiteral(getLocation(context), context.getText());
    }

    @Override
    public Node visitUnquotedIdentifier(SqlBaseParser.UnquotedIdentifierContext ctx) {
        return new Identifier(getLocation(ctx), ctx.getText(), false);
    }

    @Override
    public Node visitQuotedIdentifier(SqlBaseParser.QuotedIdentifierContext ctx) {
        String token = ctx.getText();
        String identifier = token.substring(1, token.length() - 1)
                .replace("\"\"", "\"");

        return new StringLiteral(getLocation(ctx), identifier);
        //return new Identifier(getLocation(ctx), identifier, true);
    }

    private String getType(SqlBaseParser.TypeContext type) {
        if (type.baseType() != null) {
            String signature = type.baseType().getText();
            if (type.baseType().DOUBLE_PRECISION() != null) {
                // TODO: Temporary hack that should be removed with new planner.
                signature = "DOUBLE";
            }
            if (!type.typeParameter().isEmpty()) {
                String typeParameterSignature = type
                        .typeParameter()
                        .stream()
                        .map(this::typeParameterToString)
                        .collect(Collectors.joining(","));
                signature += "(" + typeParameterSignature + ")";
            }
            return signature;
        }

        if (type.ARRAY() != null) {
            return "ARRAY(" + getType(type.type(0)) + ")";
        }

        if (type.MAP() != null) {
            return "MAP(" + getType(type.type(0)) + "," + getType(type.type(1)) + ")";
        }

        if (type.ROW() != null) {
            StringBuilder builder = new StringBuilder("(");
            for (int i = 0; i < type.identifier().size(); i++) {
                if (i != 0) {
                    builder.append(",");
                }
                builder.append(visit(type.identifier(i)))
                        .append(" ")
                        .append(getType(type.type(i)));
            }
            builder.append(")");
            return "ROW" + builder.toString();
        }

        throw new IllegalArgumentException("Unsupported type specification: " + type.getText());
    }

    private String typeParameterToString(SqlBaseParser.TypeParameterContext typeParameter) {
        if (typeParameter.INTEGER_VALUE() != null) {
            return typeParameter.INTEGER_VALUE().toString();
        }
        if (typeParameter.type() != null) {
            return getType(typeParameter.type());
        }
        throw new IllegalArgumentException("Unsupported typeParameter: " + typeParameter.getText());
    }

    public static NodeLocation getLocation(ParserRuleContext parserRuleContext) {
        requireNonNull(parserRuleContext, "parserRuleContext is null");
        return getLocation(parserRuleContext.getStart());
    }

    public static NodeLocation getLocation(Token token) {
        requireNonNull(token, "token is null");
        return new NodeLocation(token.getLine(), token.getCharPositionInLine());
    }

    private QualifiedName getQualifiedName(SqlBaseParser.QualifiedNameContext context) {
        List<String> parts = visit(context.identifier(), Identifier.class).stream()
                .map(Identifier::getValue) // TODO: preserve quotedness
                .collect(toList());

        return QualifiedName.of(parts);
    }

    private <T> List<T> visit(List<? extends ParserRuleContext> contexts, Class<T> clazz) {
        return contexts.stream()
                .map(this::visit)
                .map(clazz::cast)
                .collect(toList());
    }

    @Override
    public final Node visit(ParseTree tree) {
        Node result = super.visit(tree);
        if (result == null) {
            throw new RuntimeException("Visit method not implemented for node: " + tree.getText());
        }
        return result;
    }

    private <T> T visit(ParserRuleContext context, Class<T> clazz) {
        return clazz.cast(visit(context));
    }

    private static String unquote(String value) {
        return value.substring(1, value.length() - 1)
                .replace("''", "'");
    }

    private static ArithmeticBinaryExpression.Operator getArithmeticBinaryOperator(Token operator) {
        return switch (operator.getType()) {
            case SqlBaseLexer.PLUS -> ArithmeticBinaryExpression.Operator.ADD;
            case SqlBaseLexer.MINUS -> ArithmeticBinaryExpression.Operator.SUBTRACT;
            case SqlBaseLexer.ASTERISK -> ArithmeticBinaryExpression.Operator.MULTIPLY;
            case SqlBaseLexer.SLASH -> ArithmeticBinaryExpression.Operator.DIVIDE;
            case SqlBaseLexer.PERCENT -> ArithmeticBinaryExpression.Operator.MODULUS;
            default -> throw new UnsupportedOperationException("Unsupported operator: " + operator.getText());
        };
    }

    private static ComparisonExpression.Operator getComparisonOperator(Token symbol) {
        return switch (symbol.getType()) {
            case SqlBaseLexer.EQ -> ComparisonExpression.Operator.EQUAL;
            case SqlBaseLexer.NEQ -> ComparisonExpression.Operator.NOT_EQUAL;
            case SqlBaseLexer.LT -> ComparisonExpression.Operator.LESS_THAN;
            case SqlBaseLexer.LTE -> ComparisonExpression.Operator.LESS_THAN_OR_EQUAL;
            case SqlBaseLexer.GT -> ComparisonExpression.Operator.GREATER_THAN;
            case SqlBaseLexer.GTE -> ComparisonExpression.Operator.GREATER_THAN_OR_EQUAL;
            default -> throw new IllegalArgumentException("Unsupported operator: " + symbol.getText());
        };

    }

    private static LogicalBinaryExpression.Operator getLogicalBinaryOperator(Token token) {
        return switch (token.getType()) {
            case SqlBaseLexer.AND -> LogicalBinaryExpression.Operator.AND;
            case SqlBaseLexer.OR -> LogicalBinaryExpression.Operator.OR;
            default -> throw new IllegalArgumentException("Unsupported operator: " + token.getText());
        };

    }

    private boolean mixedAndOrOperatorParenthesisCheck(Expression expression, SqlBaseParser.BooleanExpressionContext node, LogicalBinaryExpression.Operator operator) {
        if (expression instanceof LogicalBinaryExpression) {
            if (((LogicalBinaryExpression) expression).getOperator().equals(operator)) {
                if (node.children.get(0) instanceof SqlBaseParser.ValueExpressionDefaultContext) {
                    return !(((SqlBaseParser.PredicatedContext) node).valueExpression().getChild(0) instanceof
                            SqlBaseParser.ParenthesizedExpressionContext);
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}
