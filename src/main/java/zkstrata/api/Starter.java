package zkstrata.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.api.cli.CommandLineInterface;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;

public class Starter {
    private static final Logger LOGGER = LogManager.getRootLogger();

    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface();
        Arguments arguments = cli.parse(args);
        try {
            Compiler.run(arguments);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
