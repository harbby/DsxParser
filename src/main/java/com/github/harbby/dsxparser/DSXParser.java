package com.github.harbby.dsxparser;

import com.github.harbby.dsxparser.antlr4.SqlBaseLexer;
import com.github.harbby.dsxparser.antlr4.SqlBaseParser;
import com.github.harbby.dsxparser.tree.Expression;
import com.github.harbby.dsxparser.tree.Node;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class DSXParser {
    private static final Logger logger = LoggerFactory.getLogger(DSXParser.class);
    private final Map<String, UserFuncWrapper> funcMap;

    private DSXParser(Map<String, UserFuncWrapper> funcMap) {
        this.funcMap = Map.copyOf(funcMap);
    }

    private static final BaseErrorListener LEXER_ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String message, RecognitionException e) {
            throw new ParsingException(message, e, line, charPositionInLine);
        }
    };

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Map<String, UserFuncWrapper> funcMap = new HashMap<>();

        public Builder register(Class<? extends DsxFunc> funcClass) {
            requireNonNull(funcClass, "funcClass is null");
            UserFuncWrapper wrapper = UserFuncWrapper.of(funcClass);
            UserFuncWrapper old = funcMap.put(wrapper.getFuncName(), wrapper);
            if (old != null) {
                logger.warn("Duplicate register func {} {}", wrapper.getFuncName(), funcClass);
            }
            return this;
        }

        public Builder register(List<Class<? extends DsxFunc>> userFuncList) {
            for (var f : userFuncList) {
                this.register(f);
            }
            return this;
        }

        public DSXParser build() {
            return new DSXParser(funcMap);
        }
    }

    public Expression parseExpression(String expression) throws ParsingException {
        return (Expression) invokeParser("expression", expression, SqlBaseParser::singleExpression);
    }

    private Node invokeParser(String name, String sql, Function<SqlBaseParser, ParserRuleContext> parseFunction) throws ParsingException {
        try {
            SqlBaseLexer lexer = new SqlBaseLexer(new CaseInsensitiveStream(CharStreams.fromString(sql)));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SqlBaseParser parser = new SqlBaseParser(tokenStream);

            //parser.addParseListener(new PostProcessor(Arrays.asList(parser.getRuleNames())));

            lexer.removeErrorListeners();
            lexer.addErrorListener(LEXER_ERROR_LISTENER);

            parser.removeErrorListeners();
            parser.addErrorListener(LEXER_ERROR_LISTENER);

            ParserRuleContext tree;
            try {
                // first, try parsing with potentially faster SLL mode
                parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
                tree = parseFunction.apply(parser);
            } catch (ParseCancellationException ex) {
                // if we fail, parse with LL mode
                tokenStream.seek(0); // rewind input stream
                parser.reset();

                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                tree = parseFunction.apply(parser);
            }

            return new AstBuilder(this.funcMap).visit(tree);
        } catch (StackOverflowError e) {
            throw new ParsingException(name + " is too large (stack overflow while parsing)");
        }
    }
}