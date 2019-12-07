package zkstrata.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.ExposureAnalyzer;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.optimizer.Optimizer;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;

import java.util.*;

public class Compiler {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private Compiler() {
        throw new IllegalStateException("Utility class");
    }

    public static void run(Arguments args) {
        Statement statement = parse(args, null, null);

        parseValidationRules(statement.getSubjects(), args).forEach(stmt -> statement.addProposition(stmt.getClaim()));

        statement.setPremises(parsePremises(args));

        if (args.hasWitnessData())
            ExposureAnalyzer.process(statement, args);

        SemanticAnalyzer.process(statement.getClaim(), statement.getPremises());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Statement claim structure before optimization:{}{}",
                    System.lineSeparator(), statement.getClaim().toDebugString());

        Proposition claim = Optimizer.process(statement.getClaim(), statement.getPremises());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Compiled the claim of the given statement into the following structure:{}{}",
                    System.lineSeparator(), claim.toDebugString());

        new CodeGenerator(args.getName()).run(claim, args.hasWitnessData());
    }

    private static Statement parse(Arguments args, String parentAlias, String parentSchema) {
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(
                args.getStatement().getSource(),
                args.getStatement().getValue(),
                parentSchema
        );

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Parsed the statement `{}` into the following AST:{}{}",
                    args.getStatement().getSource(), System.lineSeparator(), ast.getRoot().toDebugString());

        return new ASTVisitor(args, parentAlias).visit(ast);
    }

    private static List<Statement> parseValidationRules(Map<String, StructuredData> subjects, Arguments args) {
        List<Statement> validationRules = new ArrayList<>();

        for (Map.Entry<String, StructuredData> subject : subjects.entrySet()) {
            if (subject.getValue().isWitness() && subject.getValue().getSchema().hasValidationRule()) {
                String source = subject.getValue().getSchema().getSource();
                String parentAlias = subject.getKey();
                String parentSchema = subject.getValue().getSchema().getIdentifier();

                LOGGER.debug("Processing validation rule of alias {} (schema: {}, source: {})",
                        parentAlias, parentSchema, source);

                Arguments arguments = new Arguments(args);
                String validationRule = subject.getValue().getSchema().getValidationRule();
                arguments.setStatement(new Arguments.Statement(source, validationRule));
                validationRules.add(parse(arguments, parentAlias, parentSchema));
            }
        }

        return validationRules;
    }

    private static Proposition parsePremises(Arguments args) {
        Proposition premises = Proposition.emptyProposition();

        for (Arguments.Statement premise : args.getPremises()) {
            LOGGER.debug("Processing premise {}", premise.getSource());

            Arguments arguments = new Arguments(args);
            arguments.setStatement(premise);
            premises = premises.combine(parse(arguments, null, null).getClaim());
        }

        return premises;
    }
}
