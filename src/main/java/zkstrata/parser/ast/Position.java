package zkstrata.parser.ast;

public class Position {
    private int line;
    private int charPositionInLine;

    public Position(int line, int charPositionInLine) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }
}
