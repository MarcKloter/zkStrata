package zkstrata.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.ExposureAnalyzer;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.Statement;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;

import java.util.*;

public class Compiler {
    private static final Logger LOGGER = LogManager.getLogger(Compiler.class);

    private Compiler() {
        throw new IllegalStateException("Utility class");
    }

    public static void run(Arguments args) {
        Statement statement = parse(args, null, null);

        parseValidationRules(statement.getSubjects(), args).forEach(stmt -> statement.addGadgets(stmt.getGadgets()));

        statement.setPremises(parsePremises(args));



        if (args.hasWitnessData())
            ExposureAnalyzer.process(statement, args);

        new SemanticAnalyzer().process(statement);




        new CodeGenerator(args.getName()).run(statement.getGadgets(), args.hasWitnessData());
    }

    private static Statement parse(Arguments args, String parentAlias, String parentSchema) {
        ParseTreeVisitor grammarParser = new ParseTreeVisitor();

        AbstractSyntaxTree ast = grammarParser.parse(
                args.getStatement().getSource(),
                args.getStatement().getValue(),
                parentSchema
        );

        ASTVisitor astVisitor = new ASTVisitor(
                ast,
                args.getWitnessData(),
                args.getInstanceData(),
                args.getSchemas(),
                parentAlias
        );

        return astVisitor.visitStatement();
    }

    private static List<Statement> parseValidationRules(Map<String, StructuredData> subjects, Arguments args) {
        List<Statement> validationRules = new ArrayList<>();

        for (Map.Entry<String, StructuredData> subject : subjects.entrySet()) {
            if (subject.getValue().isWitness() && subject.getValue().getSchema().getValidationRule() != null) {
                String parentAlias = subject.getKey();
                String source = subject.getValue().getSchema().getSource();
                String stmt = subject.getValue().getSchema().getValidationRule();
                String parentSchema = subject.getValue().getSchema().getIdentifier();

                LOGGER.debug("Processing validation rule of alias {} (schema: {}, source: {})",
                        parentAlias, parentSchema, source);

                validationRules.add(parse(new Arguments(source, stmt, args), parentAlias, parentSchema));
            }
        }

        return validationRules;
    }

    private static Set<Statement> parsePremises(Arguments args) {
        Set<Statement> premises = new HashSet<>();

        for (Arguments.Statement premise : args.getPremises()) {
            String source = premise.getSource();
            String stmt = premise.getValue();

            LOGGER.debug("Processing premise {}", source);

            premises.add(parse(new Arguments(source, stmt, args), null, null));
        }

        return premises;
    }
}
