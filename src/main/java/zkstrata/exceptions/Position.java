package zkstrata.exceptions;

import java.io.Serializable;

public abstract class Position implements Serializable {
    private String statement;
    private int line;
    private int charPositionInLine;

    public Position(String statement, int line, int charPositionInLine) {
        this.statement = statement;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public String getStatement() {
        return statement;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return charPositionInLine;
    }

    public static class Relative extends Position {
        public Relative(String target, int line, int charPositionInLine) {
            super(target, line, charPositionInLine);
        }
    }

    public static class Absolute extends Position {
        private String source;

        public Absolute(String source, String statement, Position position) {
            super(statement, position.getLine(), position.getPosition());
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + getLine();
            hash = hash * 31 + getPosition();
            return hash;
        }
    }
}
