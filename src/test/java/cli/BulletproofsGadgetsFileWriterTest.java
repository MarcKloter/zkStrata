package cli;

import org.junit.jupiter.api.Test;
import zkstrata.api.representations.BulletproofsGadgetsFileWriter;
import zkstrata.codegen.representations.BulletproofsGadgets;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BulletproofsGadgetsFileWriterTest {
    @Test
    void Write_Should_Succeed() {
        List<String> gadgets = List.of("gadget1", "gadget2");
        List<String> instances = List.of("instance1", "instance2", "instance3");
        List<String> witnesses = List.of("witness1");

        BulletproofsGadgets bulletproofsGadgets = new BulletproofsGadgets("name", gadgets, instances, witnesses);
        MockBulletproofsGadgetFileWriter fileWriter = new MockBulletproofsGadgetFileWriter();
        fileWriter.write(bulletproofsGadgets);

        assertEquals(gadgets.stream().collect(joining(lineSeparator())), fileWriter.getWriters().get(0).toString().trim());
        assertEquals(instances.stream().collect(joining(lineSeparator())), fileWriter.getWriters().get(1).toString().trim());
        assertEquals(witnesses.stream().collect(joining(lineSeparator())), fileWriter.getWriters().get(2).toString().trim());
    }

    public class MockBulletproofsGadgetFileWriter extends BulletproofsGadgetsFileWriter {
        List<StringWriter> writers = new ArrayList<>();

        public List<StringWriter> getWriters() {
            return writers;
        }

        @Override
        protected Writer getWriter(String fileName) {
            StringWriter stringWriter = new StringWriter();
            this.writers.add(stringWriter);
            return stringWriter;
        }
    }
}
