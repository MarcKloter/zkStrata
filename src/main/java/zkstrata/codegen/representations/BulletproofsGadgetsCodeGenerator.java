package zkstrata.codegen.representations;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.InternalCompilerException;

import java.util.*;
import java.util.stream.Collectors;

public class BulletproofsGadgetsCodeGenerator implements CodeGenerator<BulletproofsGadgets, BulletproofsGadgetsStructure> {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private String name;

    private Map<InstanceVariable, String> instanceVariables = new HashMap<>();
    private Map<WitnessVariable, String> witnessVariables = new HashMap<>();

    public BulletproofsGadgetsCodeGenerator(String name) {
        this.name = name;
    }

    @Override
    public BulletproofsGadgetsStructure generateProverTargetStructure(BulletproofsGadgets sourceRepresentation) {
        LOGGER.debug("Starting prover target code generation");

        List<String> gadgets = generateGadgets(sourceRepresentation);
        List<String> instances = generateInstances(instanceVariables);
        List<String> witnesses = generateWitnesses(witnessVariables);

        return new BulletproofsGadgetsStructure(name, gadgets, instances, witnesses);
    }

    @Override
    public BulletproofsGadgetsStructure generateVerifierTargetStructure(BulletproofsGadgets sourceRepresentation) {
        LOGGER.debug("Starting verifier target code generation");

        List<String> gadgets = generateGadgets(sourceRepresentation);
        List<String> instances = generateInstances(instanceVariables);

        return new BulletproofsGadgetsStructure(name, gadgets, instances, Collections.emptyList());
    }

    private List<String> generateGadgets(BulletproofsGadgets sourceRepresentation) {
        List<String> gadgets = new ArrayList<>();

        for (BulletproofsGadgetsCodeLine targetFormat : sourceRepresentation.toBulletproofsGadgets()) {
            Map<String, String> args = process(targetFormat.getVariables());
            StringSubstitutor substitutor = new StringSubstitutor(args, "%(", ")");
            String gadget = substitutor.replace(targetFormat.getFormat());

            LOGGER.debug("Generated gadget: {}", gadget);
            gadgets.add(gadget);
        }

        return gadgets;
    }

    /**
     * Replaces the variables in the given map by their label in the target format.
     *
     * @param args {@link Map} of key to variables
     * @return {@link Map} of key to label
     */
    private Map<String, String> process(Map<String, Variable> args) {
        return args.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getLabel(entry.getValue())));
    }

    /**
     * Returns the label of the given {@link Variable}. The label consists of a type identifier (witness/instance) and
     * an index, which is determined by the variables occurrence in the target format.
     *
     * @param var {@link Variable} variable to get an index for
     * @return label for the given variable
     */
    private String getLabel(Variable var) {
        if (var instanceof WitnessVariable) {
            witnessVariables.putIfAbsent((WitnessVariable) var, String.format("W%d", witnessVariables.size()));
            return witnessVariables.get(var);
        }

        if (var instanceof InstanceVariable) {
            instanceVariables.putIfAbsent((InstanceVariable) var, String.format("I%d", instanceVariables.size()));
            return instanceVariables.get(var);
        }

        throw new InternalCompilerException("Invalid Variable instance: %s.", var.getClass());
    }

    private List<String> generateInstances(Map<InstanceVariable, String> variables) {
        List<String> instances = new ArrayList<>();

        for (Map.Entry<InstanceVariable, String> entry : variables.entrySet()) {
            String instance = String.format("%s = 0x%s", entry.getValue(), entry.getKey().getValue().toHex());

            LOGGER.debug("Generated instance data: {}", instance);
            instances.add(instance);
        }

        return instances;
    }

    private List<String> generateWitnesses(Map<WitnessVariable, String> variables) {
        List<String> witnesses = new ArrayList<>();

        for (Map.Entry<WitnessVariable, String> entry : variables.entrySet()) {
            String witness = String.format("%s = 0x%s", entry.getValue(), entry.getKey().getValue().toHex());

            LOGGER.debug("Generated witness data: {}", witness);
            witnesses.add(witness);
        }

        return witnesses;
    }
}
