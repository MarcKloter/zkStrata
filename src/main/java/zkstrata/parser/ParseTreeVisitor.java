package zkstrata.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.ParserException;
import zkstrata.exceptions.Position;
import zkstrata.utils.ParserUtils;
import zkstrata.utils.ReflectionHelper;
import zkstrata.zkStrataLexer;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.Value;
import zkstrata.zkStrata;
import zkstrata.zkStrataBaseVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static zkstrata.parser.ast.Subject.*;

/**
 * Transforms a zkStrata statement ({@link String}) into a parse tree ({@link ParseTree}) using ANTLR.
 * Then visits this parse tree to form an abstract syntax tree ({@link AbstractSyntaxTree}).
 */
public class ParseTreeVisitor {
    public AbstractSyntaxTree parse(String source, String statement, String parentSchema) {
        ANTLRErrorListener errorListener = new ErrorListener();

        zkStrataLexer lexer = new zkStrataLexer(CharStreams.fromString(statement));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        zkStrata parser = new zkStrata(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.setErrorHandler(new ErrorStrategy());

        ParseTree tree;
        try {
            tree = parser.statement();
        } catch (ParserException e) {
            throw new CompileTimeException(source, statement, e);
        }

        StatementVisitor visitor = new StatementVisitor(source, statement, parser.getRuleNames(), parentSchema);
        return visitor.visit(tree);
    }

    public static class TypeVisitor extends zkStrataBaseVisitor<Value> {
        private Set<Constructor> constructors;

        public TypeVisitor() {
            this.constructors = ReflectionHelper.getConstructorsAnnotatedWith(TokenType.class);
        }

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
            Position.Relative position = new Position.Relative(ctx.getText(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            return new Identifier(subject, accessors, position);
        }

        @Override
        public Value visitLiteral_value(zkStrata.Literal_valueContext ctx) {
            if (ctx.getChildCount() != 1)
                throw new InternalCompilerException("Expected 1 literal value, found %d.", ctx.getChildCount());

            TerminalNode node = (TerminalNode) ctx.getChild(0);
            Constructor constructor = getConstructor(node.getSymbol().getType());

            try {
                return (Value) constructor.newInstance(node.getText(), ParserUtils.getPosition(ctx.getStart()));
            } catch (ReflectiveOperationException e) {
                throw new InternalCompilerException("Error during invocation of constructor of %s. "
                        + "Ensure that the constructor takes two arguments (String value, Position.Absolute position)",
                        constructor.getDeclaringClass());
            }
        }

        private Constructor getConstructor(int tokenType) {
            for (Constructor constructor : this.constructors) {
                Annotation annotation = constructor.getAnnotation(TokenType.class);
                if (annotation instanceof TokenType && ((TokenType) annotation).type() == tokenType)
                    return constructor;
            }

            throw new InternalCompilerException("No method found annotated as @TokenType with type property `%s`.", tokenType);
        }
    }

    public class StatementVisitor extends zkStrataBaseVisitor<AbstractSyntaxTree> {
        private String source;
        private String statement;
        private String[] rules;
        private String parentSchema;

        StatementVisitor(String source, String statement, String[] rules, String parentSchema) {
            this.source = source;
            this.statement = statement;
            this.rules = rules;
            this.parentSchema = parentSchema;
        }

        @Override
        public AbstractSyntaxTree visitStatement(zkStrata.StatementContext ctx) {
            SubjectVisitor subjectVisitor = new SubjectVisitor(parentSchema);
            List<Subject> subjects = ctx.subjects().subject()
                    .stream()
                    .map(subject -> subject.accept(subjectVisitor))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            PredicateVisitor predicateVisitor = new PredicateVisitor(this.rules);
            List<Predicate> predicates = ctx.predicates().predicate_clause()
                    .stream()
                    .map(predicate -> predicate.accept(predicateVisitor))
                    .collect(Collectors.toList());

            return new AbstractSyntaxTree(source, statement, subjects, predicates);
        }
    }

    private class SubjectVisitor extends zkStrataBaseVisitor<List<Subject>> {
        private String parentSchema;

        SubjectVisitor(String parentSchema) {
            this.parentSchema = parentSchema;
        }

        @Override
        public List<Subject> visitSubject(zkStrata.SubjectContext ctx) {
            if (ctx.K_THIS() != null)
                return handleThis(ctx);

            // check whether the instance keyword is present
            boolean isWitness = ctx.K_INSTANCE() == null;
            Schema schema = new Schema(ctx.schema_name().getText(), ParserUtils.getPosition(ctx.schema_name().getStart()));
            Alias alias = new Alias(ctx.alias().getText(), ParserUtils.getPosition(ctx.alias().getStart()));

            return List.of(new Subject(schema, alias, isWitness));
        }

        private List<Subject> handleThis(zkStrata.SubjectContext ctx) {
            Schema schema = new Schema(parentSchema, ParserUtils.getPosition(ctx.K_THIS().getSymbol()));

            Alias privateAlias = new Alias("private", ParserUtils.getPosition(ctx.K_THIS().getSymbol()));
            Subject witness = new Subject(schema, privateAlias, true);

            Alias publicAlias = new Alias("public", ParserUtils.getPosition(ctx.K_THIS().getSymbol()));
            Subject instance = new Subject(schema, publicAlias, false);

            return List.of(witness, instance);
        }
    }

    private class PredicateVisitor extends zkStrataBaseVisitor<Predicate> {
        private String[] rules;
        private Set<Method> parsers;

        PredicateVisitor(String[] rules) {
            this.rules = rules;
            this.parsers = ReflectionHelper.getMethodsAnnotatedWith(ParserRule.class);
        }

        private Method getParser(String name) {
            for (Method parser : this.parsers) {
                ParserRule annotation = parser.getAnnotation(ParserRule.class);
                if (annotation.name().equals(name))
                    return parser;
            }

            throw new InternalCompilerException("No method found annotated as @ParserRule with name property `%s`.", name);
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

            Method parser = getParser(name);
            try {
                return (Predicate) parser.invoke(null, gadget);
            } catch (ReflectiveOperationException e) {
                throw new InternalCompilerException("Error during invocation of method %s in %s.",
                        parser.getName(), parser.getDeclaringClass().getSimpleName());
            }
        }
    }
}
