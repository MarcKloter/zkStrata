package zkstrata.codegen.representations;

import zkstrata.codegen.TargetRepresentation;

import java.util.List;

public class BulletproofsGadgets implements TargetRepresentation {
    private String name;
    private List<String> gadgets;
    private List<String> instances;
    private List<String> witnesses;

    public BulletproofsGadgets(String name, List<String> gadgets, List<String> instances, List<String> witnesses) {
        this.name = name;
        this.gadgets = gadgets;
        this.instances = instances;
        this.witnesses = witnesses;
    }

    public String getName() {
        return name;
    }

    public List<String> getGadgets() {
        return gadgets;
    }

    public List<String> getInstances() {
        return instances;
    }

    public List<String> getWitnesses() {
        return witnesses;
    }
}
