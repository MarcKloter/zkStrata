package zkstrata.api.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static zkstrata.compiler.Arguments.*;

public class CommandLineInterface {
    private Options options;
    private PrintWriter printWriter;

    public CommandLineInterface(PrintWriter printWriter) {
        this.printWriter = printWriter;
        this.options = new OptionBuilder().withLongOpts().withFlags().build();
    }

    /**
     * Parses the supplied arguments using Apache Commons CLI.
     *
     * @param args command line arguments
     * @return Parsed arguments for the zkStrata compiler to use
     * @throws SpecialOptionException if a special flag matched, exiting the program early
     */
    public Arguments parse(String[] args) {
        try {
            checkSpecialFlags(args);
            return parseArguments(args);
        } catch (ParseException e) {
            printHelp(printWriter);
            throw new SpecialOptionException();
        } finally {
            printWriter.flush();
        }
    }

    /**
     * Checks whether special flags were used, independent of whether the {@code args} are valid.
     *
     * @param args command line arguments
     * @throws ParseException         if the arguments do not contain anything recognizable
     * @throws SpecialOptionException if a special flag matched, exiting the program early
     */
    private void checkSpecialFlags(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options flagOptions = new OptionBuilder().withFlags().build();
        CommandLine cmd = parser.parse(flagOptions, args, true);
        checkFlags(cmd);
    }

    /**
     * Parses the given {@code args} according to the {@link CommandLineInterface#options}.
     *
     * @param args command line arguments
     * @return {@link Arguments} representation of the parsed {@code args}
     * @throws ParseException         if the provided {@code args} are malformed
     * @throws SpecialOptionException if a special flag matched, exiting the program early
     */
    private Arguments parseArguments(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        checkFlags(cmd);

        String file = getStatementFile(cmd);
        String name = getStatementName(file);
        Statement statement = new Statement(file, getStatement(file));
        List<Statement> premises = getPremises(cmd);
        setVerbosity(cmd);

        SubjectData subjectData = new SubjectData(getWitnessData(cmd), getInstanceData(cmd), getSchemas(cmd));

        return new Arguments(name, statement, premises, subjectData);
    }

    /**
     * Checks whether special option flags were used.
     *
     * @param cmd {@link CommandLine} object that represents a list of arguments
     * @throws SpecialOptionException if a special flag matched, exiting the program early
     */
    private void checkFlags(CommandLine cmd) {
        if (cmd.hasOption("help")) {
            printHelp(printWriter);
            throw new SpecialOptionException();
        }

        if (cmd.hasOption("version")) {
            printVersion(printWriter);
            throw new SpecialOptionException();
        }
    }

    private void printHelp(PrintWriter printWriter) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null); // print options in the order they were added
        String name = getImplementationTitle();
        String header = "Compiles the given zkStrata statement into an intermediate representation of gadgets.";
        formatter.printHelp(printWriter, formatter.getWidth(), name, header, options, formatter.getLeftPadding(),
                formatter.getDescPadding(), null, true);
    }

    private void printVersion(PrintWriter printWriter) {
        String name = getImplementationTitle();
        String version = getImplementationVersion();
        printWriter.println(String.format("%s %s", name, version));
    }

    private String getImplementationTitle() {
        return Optional.ofNullable(Compiler.class.getPackage().getImplementationTitle()).orElse("zkstratac");
    }

    private String getImplementationVersion() {
        return Optional.ofNullable(Compiler.class.getPackage().getImplementationVersion()).orElse("0.0");
    }

    /**
     * Checks whether the verbose option is set.
     *
     * @param cmd {@link CommandLine} object that represents a list of arguments
     */
    private void setVerbosity(CommandLine cmd) {
        if (cmd.hasOption("verbose"))
            Configurator.setRootLevel(Level.DEBUG);
    }

    private String getStatementFile(CommandLine cmd) {
        return cmd.getOptionValue("statement");
    }

    private String getStatementName(String file) {
        return FilenameUtils.getBaseName(file);
    }

    private String getStatement(String file) {
        try {
            return Files.readString(Path.of(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to read file: %s", file));
        }
    }

    private HashMap<String, Schema> getSchemas(CommandLine cmd) {
        HashMap<String, Schema> schemas = new HashMap<>();
        if (cmd.hasOption("schemas")) {
            for (String schema : cmd.getOptionValues("schemas")) {
                String[] parts = schema.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed schema provided as argument: %s", schema);
                    throw new IllegalArgumentException(msg);
                }
                schemas.put(parts[0], new JsonSchema(parts[1], parts[0]));
            }
        }
        return schemas;
    }

    private HashMap<String, ValueAccessor> getWitnessData(CommandLine cmd) {
        HashMap<String, ValueAccessor> witnessData = new HashMap<>();
        if (cmd.hasOption("witness-data")) {
            for (String witness : cmd.getOptionValues("witness-data")) {
                String[] parts = witness.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed witness data provided as argument: %s", witness);
                    throw new IllegalArgumentException(msg);
                }
                witnessData.put(parts[0], new JsonAccessor(parts[1]));
            }
        }
        return witnessData;
    }

    private HashMap<String, ValueAccessor> getInstanceData(CommandLine cmd) {
        HashMap<String, ValueAccessor> instanceData = new HashMap<>();
        if (cmd.hasOption("instance-data")) {
            for (String instance : cmd.getOptionValues("instance-data")) {
                String[] parts = instance.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed instance data provided as argument: %s", instance);
                    throw new IllegalArgumentException(msg);
                }
                instanceData.put(parts[0], new JsonAccessor(parts[1]));
            }
        }
        return instanceData;
    }

    private List<Statement> getPremises(CommandLine cmd) {
        List<Statement> premises = new ArrayList<>();
        if (cmd.hasOption("premises"))
            for (String file : cmd.getOptionValues("premises"))
                premises.add(new Statement(file, getStatement(file)));

        return premises;
    }
}
