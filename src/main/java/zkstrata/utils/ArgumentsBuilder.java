package zkstrata.utils;

import org.apache.commons.io.IOUtils;
import zkstrata.codegen.CodeGenerator;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeGenerator;
import zkstrata.compiler.Arguments;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static zkstrata.compiler.Arguments.*;

public class ArgumentsBuilder {
    private static final String STATEMENTS_PATH = "statements/";
    private static final String DATA_PATH = "src/test/resources/data/";
    private static final String SCHEMAS_PATH = "src/test/resources/schemas/";
    private static final String ZKSTRATA_EXT = ".zkstrata";
    private static final String JSON_EXT = ".json";
    private static final String SCHEMA_EXT = ".schema.json";

    private CodeGenerator codeGenerator;
    private Statement statement;
    private List<Arguments.Statement> premises = new ArrayList<>();
    private Map<String, ValueAccessor> witnessData = new HashMap<>();
    private Map<String, ValueAccessor> instanceData = new HashMap<>();
    private Map<String, Schema> schemas = new HashMap<>();

    public ArgumentsBuilder(Class clazz) {
        this.codeGenerator = new BulletproofsGadgetsCodeGenerator(clazz.getSimpleName());
    }

    public ArgumentsBuilder withStatement(String filename) {
        this.statement = new Statement(filename, getStatements(filename));
        return this;
    }

    public ArgumentsBuilder withPremise(String filename) {
        this.premises.add(new Statement(filename, getStatements(filename)));
        return this;
    }

    public ArgumentsBuilder withWitness(String alias, String filename) {
        String witnessFile = DATA_PATH + filename + JSON_EXT;
        this.witnessData.put(alias, new JsonAccessor(witnessFile));
        return this;
    }

    public ArgumentsBuilder withInstance(String alias, String filename) {
        String instanceFile = DATA_PATH + filename + JSON_EXT;
        this.instanceData.put(alias, new JsonAccessor(instanceFile));
        return this;
    }

    public ArgumentsBuilder withSchema(String identifier, String filename) {
        String schemaFile = SCHEMAS_PATH + filename + SCHEMA_EXT;
        this.schemas.put(identifier, new JsonSchema(schemaFile, identifier));
        return this;
    }

    public Arguments build() {
        return new Arguments(codeGenerator, statement, premises, new SubjectData(witnessData, instanceData, schemas));
    }

    private String getStatements(String name) {
        String filename = STATEMENTS_PATH + name + ZKSTRATA_EXT;
        InputStream inputStream = ArgumentsBuilder.class.getClassLoader().getResourceAsStream(filename);

        try {
            if (inputStream == null)
                throw new IOException(String.format("Unable to load statement %s.", filename));

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
