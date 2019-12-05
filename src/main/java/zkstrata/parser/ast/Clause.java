package zkstrata.parser.ast;

import zkstrata.utils.StatementBuilder;

public interface Clause {
    void addTo(StatementBuilder statementBuilder);
}
