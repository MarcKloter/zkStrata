package zkstrata.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.reflections.Reflections;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrataLexer;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.Statement;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.HexLiteral;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.parser.ast.types.StringLiteral;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.zkStrata;
import zkstrata.zkStrataBaseVisitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Visitor oriented zkStrata parser
 * Statement (String) -> Parse Tree -> Abstract Syntax Tree
 */
public class ParseTreeVisitor {

    public Statement parse(String statement) {
        ANTLRErrorListener errorListener = new ErrorListener();

        zkStrataLexer lexer = new zkStrataLexer(CharStreams.fromString(statement));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        zkStrata parser = new zkStrata(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.setErrorHandler(new ErrorStrategy());

        ParseTree tree = parser.statement();

        StatementVisitor visitor = new StatementVisitor(parser.getRuleNames());
        return visitor.visit(tree);
    }

    public static class TypeVisitor extends zkStrataBaseVisitor<Value> {
        @Override
        public Value visitWitness_var(zkStrata.Witness_varContext ctx) {
            if (ctx.getChildCount() != 1)
                throw new InternalCompilerException("Expected a single witness variable, found %d.", ctx.getChildCount());

            return ctx.getChild(0).accept(new TypeVisitor());
        }

        @Override
        public Value visitInstance_var(zkStrata.Instance_varContext ctx) {
            if (ctx.getChildCount() != 1)
                throw new InternalCompilerException("Expected a single instance variable, found %d.", ctx.getChildCount());

            return ctx.getChild(0).accept(new TypeVisitor());
        }

        @Override
        public Value visitReferenced_value(zkStrata.Referenced_valueContext ctx) {
            if (ctx.getChildCount() == 0)
                throw new InternalCompilerException("Expected a reference, found nothing.");

            String subject = ctx.alias().getText();
            List<String> accessors = ctx.property().stream()
                    .map(RuleContext::getText)
                    .collect(Collectors.toList());
            return new Identifier(subject, accessors, ParserUtils.getPosition(ctx.alias().getStart()));
        }

        @Override
        public Value visitLiteral_value(zkStrata.Literal_valueContext ctx) {
            if (ctx.getChildCount() != 1)
                throw new InternalCompilerException("Expected 1 literal value, found %d.", ctx.getChildCount());

            TerminalNode node = (TerminalNode) ctx.getChild(0);

            switch (node.getSymbol().getType()) {
                case zkStrataLexer.STRING_LITERAL:
                    return new StringLiteral(node.getText(), ParserUtils.getPosition(ctx.getStart()));
                case zkStrataLexer.HEX_LITERAL:
                    return new HexLiteral(node.getText(), ParserUtils.getPosition(ctx.getStart()));
                case zkStrataLexer.INTEGER_LITERAL:
                    return new IntegerLiteral(node.getText(), ParserUtils.getPosition(ctx.getStart()));
                default:
                    throw new InternalCompilerException("The literal with type index %s is defined in the grammar but not implemented by the parse tree visitor.", node.getSymbol().getType());
            }
        }
    }

    public class StatementVisitor extends zkStrataBaseVisitor<Statement> {
        private String[] rules;

        StatementVisitor(String[] rules) {
            this.rules = rules;
        }

        @Override
        public Statement visitStatement(zkStrata.StatementContext ctx) {
            SubjectVisitor subjectVisitor = new SubjectVisitor();
            List<Subject> subjects = ctx.subjects().subject()
                    .stream()
                    .map(subject -> subject.accept(subjectVisitor))
                    .collect(Collectors.toList());

            PredicateVisitor predicateVisitor = new PredicateVisitor(this.rules);
            List<Predicate> predicates = ctx.predicates().predicate_clause()
                    .stream()
                    .map(predicate -> predicate.accept(predicateVisitor))
                    .collect(Collectors.toList());

            return new Statement(subjects, predicates);
        }
    }

    private class SubjectVisitor extends zkStrataBaseVisitor<Subject> {
        @Override
        public Subject visitSubject(zkStrata.SubjectContext ctx) {
            // check whether the instance keyword is present
            boolean isWitness = ctx.K_INSTANCE() == null;
            Subject subject = new Subject(isWitness);
            subject.setSchema(ctx.schema_name().getText(), ParserUtils.getPosition(ctx.schema_name().getStart()));
            subject.setAlias(ctx.alias().getText(), ParserUtils.getPosition(ctx.alias().getStart()));
            return subject;
        }
    }

    private class PredicateVisitor extends zkStrataBaseVisitor<Predicate> {
        private String[] rules;
        private Set<Class<?>> parsers;

        PredicateVisitor(String[] rules) {
            this.rules = rules;

            Reflections reflections = new Reflections("zkstrata.parser.predicates.impl");
            this.parsers = reflections.getTypesAnnotatedWith(ParserRule.class);
        }

        private PredicateParser getParser(String name) {
            for (Class<?> parser : this.parsers) {
                ParserRule annotation = parser.getAnnotation(ParserRule.class);
                if (annotation.name().equals(name)) {
                    try {
                        return (PredicateParser) parser.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new InternalCompilerException("Invalid implementation of parser %s.", name);
                    }
                }
            }

            throw new InternalCompilerException("Could not find a parser implementation for rule: %s.", name);
        }

        @Override
        public Predicate visitPredicate_clause(zkStrata.Predicate_clauseContext ctx) {
            if (ctx.getChildCount() != 1)
                throw new InternalCompilerException("Expected 1 predicate in clause, found %d.", ctx.getChildCount());

            ParseTree child = ctx.getChild(0);
            if (!(child instanceof ParserRuleContext))
                throw new InternalCompilerException("Expected predicate to be a parser rule, found %s.", child.getClass());

            ParserRuleContext gadget = (ParserRuleContext) child;
            String name = this.rules[gadget.getRuleIndex()];

            PredicateParser parser = getParser(name);

            return parser.parse(gadget);
        }
    }
}
