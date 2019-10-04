package zkstrata.domain.visitor;

import zkstrata.domain.gadgets.Gadget;
import zkstrata.parser.ast.Statement;

import java.util.List;

public interface ASTVisitor {
    List<Gadget> visitStatement(Statement statement);
}
