package zkstrata.compiler;

import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.ASTVisitorImpl;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.optimizer.Optimizer;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.Statement;

import java.util.List;
import java.util.Map;

public class Compiler {
    public static void run(Arguments args) {
        run(
                args.getName(),
                args.getStatement(),
                args.getWitnessData(),
                args.getInstanceData(),
                args.getSchemas()
        );
    }

    public static void run(
            String name,
            String statement,
            Map<String, ValueAccessor> witnessData,
            Map<String, ValueAccessor> instanceData,
            Map<String, Schema> schemas
    ) {
        ParseTreeVisitor grammarParser = new ParseTreeVisitor();
        Statement ast = grammarParser.parse(statement);
        List<Gadget> gadgets = new ASTVisitorImpl(schemas, witnessData, instanceData).visitStatement(ast);

        gadgets = Optimizer.run(gadgets);

        new CodeGenerator(name).run(gadgets, !witnessData.isEmpty());
    }
}
