package zkstrata.compiler;

import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.Statement;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;

public class Compiler {
    private Compiler() {
        throw new IllegalStateException("Utility class");
    }

    public static void run(Arguments args) {
        ParseTreeVisitor grammarParser = new ParseTreeVisitor();
        AbstractSyntaxTree ast = grammarParser.parse(args.getSource(), args.getStatement());
        ASTVisitor astVisitor = new ASTVisitor(ast, args.getWitnessData(), args.getInstanceData(), args.getSchemas());
        Statement statement = astVisitor.visitStatement();


        new SemanticAnalyzer().process(statement);
        new CodeGenerator(args.getName()).run(statement.getGadgets(), args.hasWitnessData());
    }
}
