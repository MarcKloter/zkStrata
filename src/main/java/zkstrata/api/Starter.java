package zkstrata.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.api.cli.CommandLineInterface;
import zkstrata.api.cli.SpecialOptionException;
import zkstrata.api.representations.BulletproofsGadgetsFileWriter;
import zkstrata.codegen.representations.BulletproofsGadgets;
import zkstrata.compiler.Compiler;

import java.io.PrintWriter;

public class Starter {
    private static final Logger LOGGER = LogManager.getRootLogger();

    public static void main(String[] args) {
        try {
            CommandLineInterface cli = new CommandLineInterface(new PrintWriter(System.out));
            new BulletproofsGadgetsFileWriter().write((BulletproofsGadgets) new Compiler(cli.parse(args)).compile());
        } catch (SpecialOptionException e) {
            System.exit(1);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
