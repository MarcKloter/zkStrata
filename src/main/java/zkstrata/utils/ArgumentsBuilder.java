package zkstrata.utils;

import zkstrata.codegen.CodeGenerator;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeGenerator;
import zkstrata.compiler.Arguments;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static zkstrata.compiler.Arguments.*;

public class ArgumentsBuilder {
    private static final String STATEMENTS_PATH = "src/test/resources/statements/";
    private static final String DATA_PATH = "src/test/resources/data/";
    private static final String SCHEMAS_PATH = "src/test/resources/schemas/";
    private static final String ZKSTRATA_EXT = ".zkstrata";
    private static final String JSON_EXT = ".json";
    private static final String SCHEMA_EXT = ".schema.json";

    private String statementsPath;
    private String dataPath;
    private String schemaPath;
    private CodeGenerator codeGenerator;
    private Statement statement;
    private List<Arguments.Statement> premises = new ArrayList<>();
    private Map<String, ValueAccessor> witnessData = new HashMap<>();
    private Map<String, ValueAccessor> instanceData = new HashMap<>();
    private Map<String, Schema> schemas = new HashMap<>();


    public ArgumentsBuilder(String statementsPath, String dataPath, String schemaPath, Class clazz) {
        this.statementsPath = statementsPath;
        this.dataPath = dataPath;
        this.schemaPath = schemaPath;
        this.codeGenerator = new BulletproofsGadgetsCodeGenerator(clazz.getSimpleName());
    }

    public ArgumentsBuilder(Class clazz) {
        this.statementsPath = STATEMENTS_PATH;
        this.dataPath = DATA_PATH;
        this.schemaPath = SCHEMAS_PATH;
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
        String witnessFile = this.dataPath + filename + JSON_EXT;
        this.witnessData.put(alias, new JsonAccessor(witnessFile));
        return this;
    }

    public ArgumentsBuilder withInstance(String alias, String filename) {
        String instanceFile = this.dataPath + filename + JSON_EXT;
        this.instanceData.put(alias, new JsonAccessor(instanceFile));
        return this;
    }

    public ArgumentsBuilder withSchema(String identifier, String filename) {
        String schemaFile = this.schemaPath + filename + SCHEMA_EXT;
        this.schemas.put(identifier, new JsonSchema(schemaFile, identifier));
        return this;
    }

    public Arguments build() {
        return new Arguments(codeGenerator, statement, premises, new SubjectData(witnessData, instanceData, schemas));
    }

    private String getStatements(String name) {
        String filename = this.statementsPath + name + ZKSTRATA_EXT;

        try {
            return Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
