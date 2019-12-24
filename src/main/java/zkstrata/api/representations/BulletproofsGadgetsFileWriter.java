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
import java.util.List;

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
        LOGGER.debug("Writing gadgets to {}", gadgetsFileName);
        writeLinesToFile(gadgetsFileName, bulletproofsGadgets.getGadgets());
    }

    private void writeInstances() {
        String instanceFileName = bulletproofsGadgets.getName() + INSTANCE_FILE_EXT;
        LOGGER.debug("Writing instance data to {}", instanceFileName);
        writeLinesToFile(instanceFileName, bulletproofsGadgets.getInstances());
    }

    private void writeWitnesses() {
        String witnessFileName = bulletproofsGadgets.getName() + WITNESS_FILE_EXT;
        LOGGER.debug("Writing witness data to {}", witnessFileName);
        writeLinesToFile(witnessFileName, bulletproofsGadgets.getWitnesses());
    }

    private void writeLinesToFile(String filename, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(getWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new InternalCompilerException(e, "Error while writing data to {}.", filename);
        }
    }

    protected Writer getWriter(String filename) throws IOException {
        return new FileWriter(filename);
    }
}
