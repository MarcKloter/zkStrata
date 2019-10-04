package zkstrata.compiler;

import zkstrata.compiler.cli.CommandLineInterface;
import zkstrata.exceptions.CompileException;

public class Starter {
    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface();
        Arguments arguments = cli.parse(args);
        try {
            Compiler.run(arguments);
        } catch (CompileException e) {
            System.err.println(e.getMessage());
        }
    }
}
