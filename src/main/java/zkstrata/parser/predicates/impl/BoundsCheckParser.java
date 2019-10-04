package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ParserRule(name = "bounds_check")
public class BoundsCheckParser implements PredicateParser {
    @Override
    public BoundsCheck parse(ParserRuleContext ctx) {
        ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
        List<Value> values = ctx.children.stream()
                .map(child -> child.accept(typeVisitor))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new BoundsCheck(values.get(0), values.get(1), values.get(2), ParserUtils.getPosition(ctx.getStart()));
    }
}
