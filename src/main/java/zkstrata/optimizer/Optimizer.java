package zkstrata.optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.utils.CombinatoricsUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Optimizer {
    private static final Logger LOGGER = LogManager.getLogger(Optimizer.class);

    public static List<Gadget> run(List<Gadget> input) {
        LOGGER.debug(String.format("Starting optimization on %s gadgets", input.size()));

        List<Gadget> state = new ArrayList<>(input);

        List<Gadget> context = new ArrayList<>(state);

        for (Gadget gadget : state) {
            context.remove(gadget);
            applyLocalOptimizations(gadget, context).ifPresent(context::add);
        }

        state = new ArrayList<>(context);

        LOGGER.debug(String.format("Finishing optimization with %s gadgets", state.size()));
        return state;
    }

    private static Optional<Gadget> applyLocalOptimizations(Gadget gadget, List<Gadget> others) {
        Set<Method> optimizationRules = Arrays.stream(gadget.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(LocalOptimizationRule.class))
                .collect(Collectors.toSet());

        // TODO move loop from applyOptimizationRules up here

        return applyOptimizationRules(gadget, others, optimizationRules);
    }

    /**
     * Determines whether the provided gadget should be included in the optimizer output by applying the given optimization methods.
     *
     * @param gadget            {@link Gadget} as target to apply optimization methods on
     * @param others            {@link List} of {@link Gadget} for using clauses of optimization rules
     * @param optimizationRules {@link Set} of {@link Method} annotated with {@link LocalOptimizationRule} to apply on the gadget
     * @return indicator to include the given gadget in the output of the optimizer or whether it can be safely removed
     */
    private static Optional<Gadget> applyOptimizationRules(Gadget gadget, List<Gadget> others, Set<Method> optimizationRules) {
        // TODO: finish implementing this method
        LOGGER.debug(String.format("Found %d @LocalOptimizationRule for %s", optimizationRules.size(), gadget.getClass().getSimpleName()));
        for (Method optimizationRule : optimizationRules) {
            Class<? extends Gadget>[] context = optimizationRule.getAnnotation(LocalOptimizationRule.class).context();
            try {
                if (context.length > 0) {
                    List<Deque<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(List.of(context), others);
                    for (Deque<Gadget> contextCombination : contextCombinations) {
                        Optional<Gadget> result = (Optional<Gadget>) optimizationRule.invoke(gadget, contextCombination.toArray());
                    }
                } else
                    if (((Optional<Gadget>) optimizationRule.invoke(gadget)).isPresent())
                        ;// return Optional.empty();
            } catch (ReflectiveOperationException e) {
            } catch (ClassCastException e) {
                LOGGER.fatal(e.toString());
                String msg = String.format("Invalid implementation of @LocalOptimizationRule annotated method %s in %s. Return type must be Optional<Gadget>.", optimizationRule.getName(), optimizationRule.getDeclaringClass());
                throw new IllegalStateException(msg);
            }
        }

        return Optional.of(gadget);
    }
}
