package zkstrata.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.Statement;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Compiler {
    private static final Logger LOGGER = LogManager.getLogger(Compiler.class);

    private Compiler() {
        throw new IllegalStateException("Utility class");
    }

    public static void run(Arguments args) {
        ParseTreeVisitor grammarParser = new ParseTreeVisitor();
        AbstractSyntaxTree ast = grammarParser.parse(args.getSource(), args.getStatement());
        ASTVisitor astVisitor = new ASTVisitor(
                ast,
                null,
                args.getWitnessData(),
                args.getInstanceData(),
                args.getSchemas()
        );
        Statement statement = astVisitor.visitStatement();

        List<Statement> supplementaryStatements = checkSupplementaryStatements(statement.getSubjects(), args);
        supplementaryStatements.forEach(supp -> statement.addGadgets(supp.getGadgets()));

        new SemanticAnalyzer().process(statement);
        new CodeGenerator(args.getName()).run(statement.getGadgets(), args.hasWitnessData());
    }

    private static List<Statement> checkSupplementaryStatements(Map<String, StructuredData> subjects, Arguments args) {
        List<Statement> supplementaryStatements = new ArrayList<>();

        for (Map.Entry<String, StructuredData> subject : subjects.entrySet()) {
            if (subject.getValue().isWitness() && subject.getValue().getSchema().getStatement() != null) {
                String self = subject.getKey();
                String schema = subject.getValue().getSchema().getIdentifier();

                LOGGER.debug("Processing supplementary statement of {} (schema {})", self, schema);

                ParseTreeVisitor grammarParser = new ParseTreeVisitor();
                AbstractSyntaxTree ast = grammarParser.parse(schema, subject.getValue().getSchema().getStatement());
                ASTVisitor astVisitor = new ASTVisitor(
                        ast,
                        self,
                        args.getWitnessData(),
                        args.getInstanceData(),
                        args.getSchemas()
                );
                supplementaryStatements.add(astVisitor.visitStatement());
            }
        }

        return supplementaryStatements;
    }
}
