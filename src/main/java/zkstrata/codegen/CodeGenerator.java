package zkstrata.codegen;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.InternalCompilerException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CodeGenerator {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final String GADGETS_FILE_EXT = ".gadgets";
    private static final String INSTANCE_FILE_EXT = ".inst";
    private static final String WITNESS_FILE_EXT = ".wtns";

    private final String gadgetsFileName;
    private final String instanceFileName;
    private final String witnessFileName;

    private Map<InstanceVariable, String> instanceVariables = new HashMap<>();
    private Map<WitnessVariable, String> witnessVariables = new HashMap<>();

    public CodeGenerator(String name) {
        gadgetsFileName = name + GADGETS_FILE_EXT;
        instanceFileName = name + INSTANCE_FILE_EXT;
        witnessFileName = name + WITNESS_FILE_EXT;
    }

    public void run(List<Gadget> gadgets, boolean writeWitnesses) {
        LOGGER.debug("Starting target code generation for {} gadgets", gadgets.size());

        generateGadgetsFile(gadgets);
        generateInstanceFile(instanceVariables);
        if (writeWitnesses)
            generateWitnessFile(witnessVariables);

        // TODO: on error, clean up files
    }

    private void generateGadgetsFile(List<Gadget> gadgets) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(gadgetsFileName))) {
            LOGGER.debug("Writing to {}", gadgetsFileName);

            List<TargetFormat> target = gadgets.stream().map(Gadget::toTargetFormat).collect(Collectors.toList());

            for (TargetFormat targetFormat : target) {
                Map<String, String> args = process(targetFormat.getArgs());
                StringSubstitutor substitutor = new StringSubstitutor(args, "%(", ")");
                String line = substitutor.replace(targetFormat.getFormat());
                writer.write(line);
                writer.newLine();

                LOGGER.debug("Generated line: {}", line);
            }
        } catch (IOException e) {
            throw new InternalCompilerException("Error while writing to {}.", gadgetsFileName);
        }
    }

    private Map<String, String> process(Map<String, Variable> args) {
        return args.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getLabel(entry.getValue())));
    }

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

    private void generateInstanceFile(Map<InstanceVariable, String> variables) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(instanceFileName))) {
            LOGGER.debug("Writing to {}", instanceFileName);

            for(Map.Entry<InstanceVariable, String> entry : variables.entrySet()) {
                String line = String.format("%s = 0x%s", entry.getValue(), entry.getKey().getValue().toHex());
                writer.write(line);
                writer.newLine();
                LOGGER.debug("Generated line: {}", line);
            }
        } catch (IOException e) {
            throw new InternalCompilerException(e, "Error while writing to {}.", instanceFileName);
        }
    }

    private void generateWitnessFile(Map<WitnessVariable, String> variables) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(witnessFileName))) {
            LOGGER.debug("Writing to {}", witnessFileName);

            for(Map.Entry<WitnessVariable, String> entry : variables.entrySet()) {
                String line = String.format("%s = 0x%s", entry.getValue(), entry.getKey().getValue().toHex());
                writer.write(line);
                writer.newLine();
                LOGGER.debug("Generated line: {}", line);
            }
        } catch (IOException e) {
            throw new InternalCompilerException(e, "Error while writing to {}.", witnessFileName);
        }
    }
}
