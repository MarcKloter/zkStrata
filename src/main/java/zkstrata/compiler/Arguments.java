package zkstrata.compiler;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;

import java.util.Map;

public class Arguments {
    private String name;
    private String source;
    private String statement;
    private Map<String, ValueAccessor> witnessData;
    private Map<String, ValueAccessor> instanceData;
    private Map<String, Schema> schemas;

    public Arguments(
            String name,
            String source,
            String statement,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData,
            Map<String, Schema> schemas
    ) {
        this.name = name;
        this.source = source;
        this.statement = statement;
        this.witnessData = witnessData;
        this.instanceData = instanceData;
        this.schemas = schemas;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public String getStatement() {
        return statement;
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
}
