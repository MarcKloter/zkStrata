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

// TODO: Quality of Life Feature to help the prover
public class ExposureAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger(ExposureAnalyzer.class);


    private ExposureAnalyzer() {
        throw new IllegalStateException("Utility class");
    }

    public static void process(Statement statement, Arguments args) {
        Map<String, ValueAccessor> witnessData = args.getWitnessData();
        Map<String, ValueAccessor> instanceData = args.getInstanceData();

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

        if (!susceptibleData.isEmpty()) {
            Map<String, VariableExposure> checkList = new HashMap<>();

            for (Gadget gadget : statement.getGadgets()) {
                for (Object variable : gadget.getVariables()) {
                    if (variable instanceof Variable
                            && ((Variable) variable).getReference() != null) {
                        String alias = ((Variable) variable).getReference().getSubject();
                        Selector selector = ((Variable) variable).getReference().getSelector();
                        if (variable instanceof WitnessVariable) {
                            ValueAccessor witness = witnessData.getOrDefault(alias, null);
                            String source = witness.getSource();
                            if (susceptibleData.contains(source))
                                checkList.computeIfAbsent(String.format("%s-%s", source, selector), e -> new VariableExposure())
                                        .markWitness((WitnessVariable) variable);
                        }

                        if (variable instanceof InstanceVariable) {
                            ValueAccessor instance = instanceData.getOrDefault(alias, null);
                            String source = instance.getSource();
                            if (susceptibleData.contains(source))
                                checkList.computeIfAbsent(String.format("%s-%s", source, selector), e -> new VariableExposure())
                                        .markInstance((InstanceVariable) variable);
                        }
                    }
                }
            }
        }
    }
}
