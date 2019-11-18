package zkstrata.parser.ast;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public class Subject {
    private Schema schema;
    private Alias alias;
    private boolean witness;

    public Subject(Schema schema, Alias alias, boolean witness) {
        this.schema = schema;
        this.alias = alias;
        this.witness = witness;
    }

    public Schema getSchema() {
        return schema;
    }

    public Alias getAlias() {
        return alias;
    }

    public boolean isWitness() {
        return witness;
    }

    public static class Schema extends AbstractTraceable {
        private String name;

        public Schema(String name, Position position) {
            super(position);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Alias extends AbstractTraceable {
        private String name;

        public Alias(String name, Position position) {
            super(position);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
