package zkstrata.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.utils.CombinatoricsUtils;
import zkstrata.utils.ImplicationHelper;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticAnalyzer {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private SemanticAnalyzer() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Combines the given claim and premises to draw all inferences that can be made from the provided information.
     * Based on this, check for any contradiction between any inference and throw a {@link CompileTimeException} in case
     * of a positive check.
     *
     * @param claim    {@link Proposition}
     * @param premises list of {@link Proposition} of premises
     */
    public static void process(Proposition claim, Proposition premises) {
        LOGGER.debug("Starting semantic analysis");

        Proposition allPropositions = claim.combine(premises);

        // get all possible evaluation paths to prove the combined statement
        List<List<Gadget>> evaluationPaths = allPropositions.getEvaluationPaths();

        LOGGER.debug("Found {} logically distinct paths to evaluate the given statement", evaluationPaths.size());

        // under the assumption that all claims are correct, check any contradictions that would make this impossible
        for (int i = 0; i < evaluationPaths.size(); i++) {
            Set<Inference> inferences = ImplicationHelper.drawInferences(evaluationPaths.get(i));

            LOGGER.debug("Drew {} inferences for evaluation path {}", inferences.size(), i);

            // check for semantic errors by executing methods annotated with @Contradiction
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
                invokeContradictionCheck(contradictionCheck, contextCombination.toArray());
        }
    }

    /**
     * Invokes the given {@link Method} using the provided {@code args}.
     * The method is expected to be a {@link Contradiction} annotated method that takes {@code args} and throws an
     * exception in case the check is positive.
     *
     * @param contradictionCheck {@link Method} to invoke
     * @param args               arguments to pass to the method
     */
    private static void invokeContradictionCheck(Method contradictionCheck, Object[] args) {
        try {
            contradictionCheck.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompileTimeException)
                throw (CompileTimeException) cause;
            else
                throw new InternalCompilerException(cause, "Invalid exception %s thrown by %s in %s.",
                        cause.getClass().getSimpleName(), contradictionCheck.getName(), contradictionCheck.getDeclaringClass());
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new InternalCompilerException(e, "Invalid implementation of @Contradiction annotated method %s in %s: "
                    + "Ensure the method is static and its parameters are matching the annotation.",
                    contradictionCheck.getName(), contradictionCheck.getDeclaringClass());
        }
    }
}
