package zkstrata.compiler;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;

import java.util.List;
import java.util.Map;

public class Arguments {
    private String name;
    private Statement statement;
    private List<Statement> premises;
    private Map<String, ValueAccessor> witnessData;
    private Map<String, ValueAccessor> instanceData;
    private Map<String, Schema> schemas;

    public Arguments(
            String name,
            Statement statement,
            List<Statement> premises,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData,
            Map<String, Schema> schemas
    ) {
        this.name = name;
        this.statement = statement;
        this.premises = premises;
        this.witnessData = witnessData;
        this.instanceData = instanceData;
        this.schemas = schemas;
    }

    public Arguments(String source, String statement, Arguments arguments) {
        this(
                arguments.getName(),
                new Statement(source, statement),
                arguments.getPremises(),
                arguments.getWitnessData(),
                arguments.getInstanceData(),
                arguments.getSchemas()
        );
    }

    public String getName() {
        return name;
    }

    public Statement getStatement() {
        return statement;
    }

    public List<Statement> getPremises() {
        return premises;
    }

    public Map<String, ValueAccessor> getWitnessData() {
        return witnessData;
    }

    public Map<String, ValueAccessor> getInstanceData() {
        return instanceData;
    }

    public Map<String, Schema> getSchemas() {
        return schemas;
    }

    public boolean hasWitnessData() {
        return !witnessData.isEmpty();
    }

    public static class Statement {
        private String source;
        private String value;

        public Statement(String source, String value) {
            this.source = source;
            this.value = value;
        }

        public String getSource() {
            return source;
        }

        public String getValue() {
            return value;
        }
    }
}
