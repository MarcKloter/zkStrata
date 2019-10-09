package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrata;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ParserRule(name = "bounds_check")
public class BoundsCheckParser implements PredicateParser {
    @Override
    public BoundsCheck parse(ParserRuleContext ctx) {
        zkStrata.Bounds_checkContext boundsCheckContext = (zkStrata.Bounds_checkContext) ctx;

        if (boundsCheckContext.bounds() != null) {
            List<Value> values = getValues(boundsCheckContext.bounds());
            return new BoundsCheck(values.get(0), values.get(1), values.get(2), ParserUtils.getPosition(ctx.getStart()));
        }

        if (boundsCheckContext.less_than() != null) {
            List<Value> values = getValues(boundsCheckContext.less_than());
            return new BoundsCheck(values.get(0), values.get(1), null, ParserUtils.getPosition(ctx.getStart()));
        }

        if (boundsCheckContext.greater_than() != null) {
            List<Value> values = getValues(boundsCheckContext.greater_than());
            return new BoundsCheck(values.get(0), null, values.get(1), ParserUtils.getPosition(ctx.getStart()));
        }

        throw new InternalCompilerException("Unknown bounds_check rule.");
    }

    private List<Value> getValues(ParserRuleContext ctx) {
        ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
        return ctx.children.stream()
                .map(child -> child.accept(typeVisitor))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
