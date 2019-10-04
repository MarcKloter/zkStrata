package zkstrata.parser.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ast.predicates.Predicate;

public interface PredicateParser {
    Predicate parse(ParserRuleContext ctx);
}
