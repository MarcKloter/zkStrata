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

    private Map<String, ValueAccessor> witnessData;
    private Map<String, ValueAccessor> instanceData;

    public ExposureAnalyzer(Arguments.SubjectData subjectData) {
        this.witnessData = subjectData.getWitnessData();
        this.instanceData = subjectData.getInstanceData();
    }

    public void process(Statement statement) {
        List<String> susceptibleData = getSusceptibleData();

        if (susceptibleData.isEmpty())
            return;

        Map<String, VariableExposure> checkList = new HashMap<>();

        List<Gadget> gadgets = statement.getClaim().combine(statement.getPremise()).listAllGadgets();

        for (Gadget gadget : gadgets)
            for (Variable variable : gadget.getVariables().values())
                markVariable(variable, susceptibleData, checkList);
    }

    /**
     * Checks whether the given {@code variable} is part of a source listed in {@code susceptibleData} and marks the
     * variable in the {@code checkList} if this is the case.
     *
     * @param variable        {@link Variable} to check
     * @param susceptibleData {@link List} of aliases used as confidential and public data source at the same time
     * @param checkList       {@link Map} of source to {@link VariableExposure} objects
     */
    private void markVariable(Variable variable, List<String> susceptibleData, Map<String, VariableExposure> checkList) {
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
     * Checks the known lists of witness {@link ExposureAnalyzer#witnessData} and instance
     * {@link ExposureAnalyzer#instanceData} sources for overlap (sources that are being used to retrieve confidential
     * and public data from at the same time).
     *
     * @return {@link List} of sources used as confidential and public data source at the same time
     */
    private List<String> getSusceptibleData() {
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
