package zkstrata.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.compiler.Arguments;
import zkstrata.domain.Statement;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The exposure analyzer performs a check on the prover side to ensure there is no data accidentally being exposed.
 * <p>
 * If there is a witness and instance variable for the same field of the same source (e.g. file), the exposure analyzer
 * will throw an exception.
 */
public class ExposureAnalyzer {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private ExposureAnalyzer() {
        throw new IllegalStateException("Utility class");
    }

    public static void process(Statement statement, Arguments args) {
        Map<String, ValueAccessor> witnessData = args.getWitnessData();
        Map<String, ValueAccessor> instanceData = args.getInstanceData();

        List<String> susceptibleData = getSusceptibleData(witnessData, instanceData);

        if (susceptibleData.isEmpty())
            return;

        Map<String, VariableExposure> checkList = new HashMap<>();

        List<Gadget> gadgets = new ArrayList<>(statement.getGadgets());
        statement.getPremises().forEach(premise -> gadgets.addAll(premise.getGadgets()));
        for (Gadget gadget : gadgets) {
            for (Object variable : gadget.getVariables()) {
                if (variable instanceof Variable)
                    markVariable((Variable) variable, susceptibleData, checkList, witnessData, instanceData);
            }
        }
    }

    /**
     * Checks whether the given {@code variable} is part of a source listed in {@code susceptibleData} and marks the
     * variable in the {@code checkList} if this is the case.
     *
     * @param variable        {@link Variable} to check
     * @param susceptibleData {@link List} of aliases used as confidential and public data source at the same time
     * @param checkList       {@link Map} of source to {@link VariableExposure} objects
     * @param witnessData     {@link Map} of source to {@link ValueAccessor} of confidential data
     * @param instanceData    {@link Map} of source to {@link ValueAccessor} of public data
     */
    private static void markVariable(
            Variable variable,
            List<String> susceptibleData,
            Map<String, VariableExposure> checkList,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData
    ) {
        if (variable.getReference() != null) {
            String alias = variable.getReference().getSubject();
            Selector selector = variable.getReference().getSelector();
            if (variable instanceof WitnessVariable) {
                ValueAccessor witness = witnessData.getOrDefault(alias, null);
                String source = witness.getSource();
                if (susceptibleData.contains(source))
                    checkList.computeIfAbsent(String.format("%s-%s", source, selector), e ->
                            new VariableExposure()).mark(variable);
            }

            if (variable instanceof InstanceVariable) {
                ValueAccessor instance = instanceData.getOrDefault(alias, null);
                if (instance != null) {
                    String source = instance.getSource();
                    if (susceptibleData.contains(source))
                        checkList.computeIfAbsent(String.format("%s-%s", source, selector), e ->
                                new VariableExposure()).mark(variable);
                }
            }
        }
    }

    /**
     * Checks the given lists of witness and instance sources for overlap (sources that are being used as confidential
     * and public data source at the same time).
     *
     * @param witnessData  {@link Map} of source to {@link ValueAccessor} of confidential data
     * @param instanceData {@link Map} of source to {@link ValueAccessor} of public data
     * @return {@link List} of sources used as confidential and public data source at the same time
     */
    private static List<String> getSusceptibleData(
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData
    ) {
        List<String> susceptibleData = new ArrayList<>();

        for (Map.Entry<String, ValueAccessor> witness : witnessData.entrySet()) {
            for (ValueAccessor instanceAccessor : instanceData.values()) {
                if (witness.getValue().getSource().equals(instanceAccessor.getSource()))
                    LOGGER.warn("The provided `{}` is being used as reference for witness and instance data " +
                                    "simultaneously, which could lead to accidental exposure of confidential data.",
                            witness.getValue().getSource());
                susceptibleData.add(witness.getValue().getSource());
            }
        }

        return susceptibleData;
    }
}
