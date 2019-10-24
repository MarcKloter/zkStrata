package zkstrata.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.compiler.cli.CommandLineInterface;

public class Starter {
    private static final Logger LOGGER = LogManager.getLogger(Starter.class);

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
