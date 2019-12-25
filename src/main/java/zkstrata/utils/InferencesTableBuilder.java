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
        appendHeader();

        for (Inference inference : inferences)
            appendRow(inference);

        GridTable gridTable = this.simpleTable.toGrid();

        return Util.asString(addBorder(gridTable)).trim();
    }

    private void appendHeader() {
        this.simpleTable = this.simpleTable.nextRow()
                .nextCell(pad("id"))
                .nextCell(pad("conclusion"))
                .nextCell(pad("assumptions"))
                .nextCell(pad("derived from"));
    }

    private void appendRow(Inference inference) {
        this.simpleTable = this.simpleTable.nextRow();

        addIdentifierCell(inference.hashCode());
        appendGadgetCell(inference.getConclusion());
        appendAssumptionsCell(inference.getAssumptions());
        appendDerivedFromCell(inference.getDerivedFrom());
    }

    private GridTable addBorder(GridTable gridTable) {
        return createBorder().apply(gridTable);
    }

    private Border createBorder() {
        return Border.of(Border.Chars.of('+', '-', '|'));
    }

    private void addIdentifierCell(int identifier) {
        this.simpleTable = this.simpleTable.nextCell();
        appendIdentifierLine(identifier);
    }

    private void appendIdentifierLine(int identifier) {
        this.simpleTable.addLine(pad(Integer.toHexString(identifier)));
    }

    private void appendGadgetCell(Gadget gadget) {
        this.simpleTable = this.simpleTable.nextCell();

        appendGadgetLines(gadget);
    }

    private void appendGadgetLines(Gadget gadget) {
        for (String line : gadget.getVerboseInformation().split(System.lineSeparator()))
            this.simpleTable = this.simpleTable.addLine(pad(line));
    }

    private void appendAssumptionsCell(Set<Gadget> assumptions) {
        this.simpleTable = this.simpleTable.nextCell();

        for (Gadget assumption : assumptions)
            appendGadgetLines(assumption);
    }

    private void appendDerivedFromCell(Set<Inference> derivedFrom) {
        this.simpleTable = this.simpleTable.nextCell();

        for (Inference axiom : derivedFrom)
            appendIdentifierLine(axiom.hashCode());
    }

    private String pad(String string) {
        return String.format(" %s ", string);
    }
}
