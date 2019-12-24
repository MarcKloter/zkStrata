package zkstrata.utils;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import zkstrata.analysis.Inference;
import zkstrata.domain.gadgets.Gadget;

import java.util.Set;

public class InferencesTableBuilder {
    private SimpleTable simpleTable = new SimpleTable();

    public String buildTable(Set<Inference> inferences) {
        createHeader();

        for (Inference inference : inferences)
            createRow(inference);

        GridTable gridTable = this.simpleTable.toGrid();

        return Util.asString(addBorder(gridTable)).trim();
    }

    private void createHeader() {
        this.simpleTable = this.simpleTable.nextRow()
                .nextCell(pad("id"))
                .nextCell(pad("conclusion"))
                .nextCell(pad("assumptions"))
                .nextCell(pad("derived from"));
    }

    private void createRow(Inference inference) {
        this.simpleTable = this.simpleTable.nextRow();

        addIdentifierCell(inference.hashCode());
        addGadgetCell(inference.getConclusion());
        addAssumptionsCell(inference.getAssumptions());
        addDerivedFromCell(inference.getDerivedFrom());
    }

    private GridTable addBorder(GridTable gridTable) {
        return createBorder().apply(gridTable);
    }

    private Border createBorder() {
        return Border.of(Border.Chars.of('+', '-', '|'));
    }

    private void addIdentifierCell(int identifier) {
        this.simpleTable = this.simpleTable.nextCell();
        addIdentifierLine(identifier);
    }

    private void addIdentifierLine(int identifier) {
        this.simpleTable.addLine(pad(Integer.toHexString(identifier)));
    }

    private void addGadgetCell(Gadget gadget) {
        this.simpleTable = this.simpleTable.nextCell();

        addGadgetLines(gadget);
    }

    private void addGadgetLines(Gadget gadget) {
        for (String line : gadget.toDebugString().split(System.lineSeparator()))
            this.simpleTable = this.simpleTable.addLine(pad(line));
    }

    private void addAssumptionsCell(Set<Gadget> assumptions) {
        this.simpleTable = this.simpleTable.nextCell();

        for (Gadget assumption : assumptions)
            addGadgetLines(assumption);
    }

    private void addDerivedFromCell(Set<Inference> derivedFrom) {
        this.simpleTable = this.simpleTable.nextCell();

        for (Inference axiom : derivedFrom)
            addIdentifierLine(axiom.hashCode());
    }

    private String pad(String string) {
        return String.format(" %s ", string);
    }
}
