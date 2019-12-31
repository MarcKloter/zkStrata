package zkstrata.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.ExposureAnalyzer;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.codegen.CodeGenerator;
import zkstrata.codegen.TargetRepresentation;
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

    public TargetRepresentation compile() {
        Statement statement = parseStatement();
        statement.addPremise(parseAllPremises());
        statement.setValidationRule(parseAllValidationRules(statement.getSubjects()));

        if (arguments.hasWitnessData())
            new ExposureAnalyzer(arguments.getSubjectData()).process(statement);

        SemanticAnalyzer.process(statement);

        statement.setClaim(new Optimizer(statement).process());

        CodeGenerator codeGenerator = arguments.getCodeGenerator();

        if (arguments.hasWitnessData())
            return codeGenerator.generateProverTargetRepresentation(statement.getClaim());
        else
            return codeGenerator.generateVerifierTargetRepresentation(statement.getClaim());
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
     * such. Returns a {@link Proposition} representing all validation rules found.
     *
     * @param subjects list of {@link StructuredData} to check
     * @return {@link Proposition} containing all validation rules connected using logical AND
     */
    private Proposition parseAllValidationRules(List<StructuredData> subjects) {
        Proposition validationRules = Proposition.trueProposition();

        for (StructuredData subject : subjects) {
            if (isWitnessAndHasValidationRule(subject)) {
                validationRules = validationRules.combine(parseValidationRule(subject));
            }
        }

        return validationRules;
    }

    private boolean isWitnessAndHasValidationRule(StructuredData subject) {
        return subject.isWitness() && subject.getSchema().hasValidationRule();
    }

    private Proposition parseValidationRule(StructuredData subject) {
        String parentAlias = subject.getAlias();
        String source = subject.getSchema().getSource();
        String parentSchema = subject.getSchema().getIdentifier();
        String validationRule = subject.getSchema().getValidationRule();

        LOGGER.debug("Processing validation rule of alias {} (schema: {}, source: {})",
                parentAlias, parentSchema, source);

        ParseTreeVisitor parseTreeVisitor = new ParseTreeVisitor(source, parentSchema);
        AbstractSyntaxTree ast = parseTreeVisitor.visit(validationRule);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Parsed the validation rule of `{}` into the following AST:{}{}",
                    source, System.lineSeparator(), ast.getRoot().toDebugString());

        ASTVisitor astVisitor = new ASTVisitor(arguments.getSubjectData(), parentAlias);

        return astVisitor.visit(ast).getClaim();
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
