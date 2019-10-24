package zkstrata.optimizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.Statement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.utils.CombinatoricsUtils;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Optimizer {
    private static final Logger LOGGER = LogManager.getLogger(Optimizer.class);

    public static List<Gadget> run(Statement statement) {
        LOGGER.debug("Starting optimization on {} gadgets", statement.getGadgets().size());


        List<Gadget> state = new ArrayList<>(statement.getGadgets());
        List<Gadget> context = new ArrayList<>(state);

        for (Gadget gadget : state) {
            context.remove(gadget);
            applyLocalOptimizations(gadget, context).ifPresent(context::add);
        }

        state = new ArrayList<>(context);

        LOGGER.debug("Finishing optimization with {} gadgets", state.size());
        return state;
    }

    private static Optional<Gadget> applyLocalOptimizations(Gadget gadget, List<Gadget> others) {
        Set<Method> optimizationRules = Arrays.stream(gadget.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Substitution.class))
                .collect(Collectors.toSet());

        // TODO move loop from applyOptimizationRules up here

        return applyOptimizationRules(gadget, others, optimizationRules);
    }

    private static Optional<Gadget> applyOptimizationRules(Gadget gadget, List<Gadget> others, Set<Method> optimizationRules) {
        LOGGER.debug("Found {} methods annotated as @Substitution for {}",
                optimizationRules.size(), gadget.getClass().getSimpleName());
        for (Method optimizationRule : optimizationRules) {
            if (!ReflectionHelper.checkReturnType(optimizationRule, Optional.class, Gadget.class))
                throw new InternalCompilerException("Invalid implementation of @Substitution annotated method %s in %s. "
                        + "Return type must be Optional<Gadget>.", optimizationRule.getName(), optimizationRule.getDeclaringClass());

            Class<? extends Gadget>[] context = optimizationRule.getAnnotation(Substitution.class).context();
            try {
                if (context.length > 0) {
                    List<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(List.of(context), others);
                    for (List<Gadget> contextCombination : contextCombinations) {
                        @SuppressWarnings("unchecked")
                        Optional<Gadget> result = (Optional<Gadget>) optimizationRule.invoke(gadget, contextCombination.toArray());
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    Optional<Gadget> result = (Optional<Gadget>) optimizationRule.invoke(gadget);
                    if (result.isPresent())
                        ;// return Optional.empty();
                }
            } catch (ReflectiveOperationException e) {
            }
        }

        return Optional.of(gadget);
    }
}
