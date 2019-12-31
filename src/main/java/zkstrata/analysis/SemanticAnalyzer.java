package zkstrata.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.CombinatoricsUtils;
import zkstrata.utils.InferencesTableBuilder;
import zkstrata.utils.ImplicationHelper;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticAnalyzer {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private SemanticAnalyzer() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Draw all inferences that can be made from the provided information (given {@link Statement}) to check for any
     * contradictions. Throws a {@link CompileTimeException} in case of a contradiction.
     */
    public static void process(Statement statement) {
        LOGGER.debug("Starting semantic analysis");

        Proposition allPropositions = statement.getClaim().combine(statement.getPremise()).combine(statement.getValidationRule());

        List<List<Gadget>> evaluationPaths = allPropositions.getEvaluationPaths();

        LOGGER.debug("Found {} logically distinct paths to evaluate the given statement", evaluationPaths.size());

        for (int i = 0; i < evaluationPaths.size(); i++) {
            Set<Inference> inferences = ImplicationHelper.drawInferences(evaluationPaths.get(i));

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Drew {} inferences for evaluation path {}:{}{}", inferences.size(), i,
                        System.lineSeparator(), new InferencesTableBuilder().buildTable(inferences));

            checkContradictions(inferences);
        }

        LOGGER.debug("Finishing semantic analysis");
    }

    /**
     * Executes all methods annotated as {@link Contradiction} on gadget combinations formed from the provided
     * set of inferences.
     *
     * @param inferences set of {@link Inference} to check contradictions on
     */
    private static void checkContradictions(Set<Inference> inferences) {
        Set<Method> contradictionChecks = ReflectionHelper.getMethodsAnnotatedWith(Contradiction.class);
        for (Method contradictionCheck : contradictionChecks) {
            Class<? extends Gadget>[] context = contradictionCheck.getAnnotation(Contradiction.class).propositions();

            Set<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(
                    List.of(context),
                    inferences.stream().map(Inference::getConclusion).collect(Collectors.toSet())
            );

            for (List<Gadget> contextCombination : contextCombinations)
                ReflectionHelper.invokeStaticMethod(contradictionCheck, contextCombination.toArray());
        }
    }
}
