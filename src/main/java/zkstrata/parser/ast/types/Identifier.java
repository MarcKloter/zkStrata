package zkstrata.parser.ast.types;

import zkstrata.parser.ast.Position;

import java.util.List;

public class Identifier extends Value {
    private String subject;
    private List<String> selectors;

    public Identifier(String subject, List<String> selectors, Position position) {
        super(position);
        this.subject = subject;
        this.selectors = selectors;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getSelectors() {
        return selectors;
    }
}
