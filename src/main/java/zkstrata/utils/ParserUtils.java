package zkstrata.utils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import zkstrata.exceptions.Position;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.types.Value;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParserUtils {
    private ParserUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Position getPosition(Token token) {
        return new Position.Relative(token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    /**
     * Visits all children of the given {@code ctx} using a {@link ParseTreeVisitor.TypeVisitor} and returns a list of
     * all returned {@link Value} that are non-null.
     *
     * @param ctx {@link ParserRuleContext} to visit children for
     * @return {@link List} of {@link Value} returned from visited children
     */
    public static List<Value> getValues(ParserRuleContext ctx) {
        ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
        return ctx.children.stream()
                .map(child -> child.accept(typeVisitor))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
