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

    private Arguments arguments;

    public Compiler(Arguments arguments) {
        this.arguments = arguments;
    }

    public void run() {
        Statement statement = parseStatement();
        statement.setValidationRules(parseAllValidationRules(statement.getSubjects()));
        statement.setPremises(parseAllPremises());

        if (arguments.hasWitnessData())
            new ExposureAnalyzer(arguments.getSubjectData()).process(statement);

        SemanticAnalyzer.process(statement.getClaim(), statement.getPremises());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Statement claim structure before optimization:{}{}",
                    System.lineSeparator(), statement.getClaim().toDebugString());

        Proposition optimizedProposition = new Optimizer(statement.getPremises()).process(statement.getClaim());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Compiled the claim of the given statement into the following structure:{}{}",
                    System.lineSeparator(), optimizedProposition.toDebugString());

        CodeGenerator codeGenerator = new CodeGenerator(arguments.getName());

        if (arguments.hasWitnessData())
            codeGenerator.generateProverTarget(optimizedProposition);
        else
            codeGenerator.generateVerifierTarget(optimizedProposition);
    }

    private Statement parseStatement() {
        ParseTreeVisitor parseTreeVisitor = new ParseTreeVisitor(arguments.getStatement().getSource());
        AbstractSyntaxTree ast = parseTreeVisitor.visit(arguments.getStatement().getValue());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Parsed the statement `{}` into the following AST:{}{}",
                    arguments.getStatement().getSource(), System.lineSeparator(), ast.getRoot().toDebugString());

        return new ASTVisitor(arguments.getSubjectData()).visit(ast);
    }

    /**
     * Checks the schemas declared within the given {@code subjects} for validation rules that apply to the usage of
     * such. Returns a list of {@link Statement} objects representing all validation rules found.
     *
     * @param subjects map of alias to {@link StructuredData} to check
     * @return list of {@link Statement} objects containing all validation rules found
     */
    private List<Statement> parseAllValidationRules(Map<String, StructuredData> subjects) {
        List<Statement> validationRules = new ArrayList<>();

        for (Map.Entry<String, StructuredData> subject : subjects.entrySet()) {
            if (subject.getValue().isWitness() && subject.getValue().getSchema().hasValidationRule()) {
                String parentAlias = subject.getKey();
                String source = subject.getValue().getSchema().getSource();
                String parentSchema = subject.getValue().getSchema().getIdentifier();
                String validationRule = subject.getValue().getSchema().getValidationRule();

                LOGGER.debug("Processing validation rule of alias {} (schema: {}, source: {})",
                        parentAlias, parentSchema, source);

                ParseTreeVisitor parseTreeVisitor = new ParseTreeVisitor(source, parentSchema);
                AbstractSyntaxTree ast = parseTreeVisitor.visit(validationRule);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Parsed the validation rule `{}` into the following AST:{}{}",
                            source, System.lineSeparator(), ast.getRoot().toDebugString());

                ASTVisitor astVisitor = new ASTVisitor(arguments.getSubjectData(), parentAlias);
                validationRules.add(astVisitor.visit(ast));
            }
        }

        return validationRules;
    }

    /**
     * Parses all statement files listed as premises within the {@link Compiler#arguments}.
     *
     * @return {@link Proposition} of all premises found
     */
    private Proposition parseAllPremises() {
        Proposition premises = Proposition.trueProposition();

        for (Arguments.Statement premise : arguments.getPremises()) {
            LOGGER.debug("Processing premise {}", premise.getSource());

            premises = premises.combine(parsePremise(premise).getClaim());
        }

        return premises;
    }

    private Statement parsePremise(Arguments.Statement premise) {
        AbstractSyntaxTree ast = new ParseTreeVisitor(premise.getSource()).visit(premise.getValue());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Parsed the premise `{}` into the following AST:{}{}",
                    premise.getSource(), System.lineSeparator(), ast.getRoot().toDebugString());

        return new ASTVisitor(arguments.getSubjectData()).visit(ast);
    }
}
