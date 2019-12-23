package zkstrata.api.representations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.api.TargetRepresentationFileWriter;
import zkstrata.codegen.representations.BulletproofsGadgets;
import zkstrata.exceptions.InternalCompilerException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class BulletproofsGadgetsFileWriter implements TargetRepresentationFileWriter<BulletproofsGadgets> {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private static final String GADGETS_FILE_EXT = ".gadgets";
    private static final String INSTANCE_FILE_EXT = ".inst";
    private static final String WITNESS_FILE_EXT = ".wtns";

    private BulletproofsGadgets bulletproofsGadgets;

    @Override
    public void write(BulletproofsGadgets bulletproofsGadgets) {
        this.bulletproofsGadgets = bulletproofsGadgets;

        writeGadgets();
        writeInstances();
        writeWitnesses();
    }

    private void writeGadgets() {
        String gadgetsFileName = bulletproofsGadgets.getName() + GADGETS_FILE_EXT;
        try (BufferedWriter writer = new BufferedWriter(getWriter(gadgetsFileName))) {
            LOGGER.debug("Writing gadgets to {}", gadgetsFileName);

            for (String line : bulletproofsGadgets.getGadgets()) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new InternalCompilerException("Error while writing gadgets to {}.", gadgetsFileName);
        }
    }

    private void writeInstances() {
        String instanceFileName = bulletproofsGadgets.getName() + INSTANCE_FILE_EXT;
        try (BufferedWriter writer = new BufferedWriter(getWriter(instanceFileName))) {
            LOGGER.debug("Writing instance data to {}", instanceFileName);

            for (String line : bulletproofsGadgets.getInstances()) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new InternalCompilerException(e, "Error while writing instance data to {}.", instanceFileName);
        }
    }

    private void writeWitnesses() {
        String witnessFileName = bulletproofsGadgets.getName() + WITNESS_FILE_EXT;
        try (BufferedWriter writer = new BufferedWriter(getWriter(witnessFileName))) {
            LOGGER.debug("Writing witness data to {}", witnessFileName);

            for (String line : bulletproofsGadgets.getWitnesses()) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new InternalCompilerException(e, "Error while writing witness data to {}.", witnessFileName);
        }
    }

    protected Writer getWriter(String fileName) throws IOException {
        return new FileWriter(fileName);
    }
}
