package zkstrata.parser.ast;

import zkstrata.utils.StatementBuilder;

public interface Node {
    void addTo(StatementBuilder statementBuilder);
}
