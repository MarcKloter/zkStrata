package zkstrata.utils;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.parser.ast.types.StringLiteral;

import java.math.BigInteger;
import java.util.List;

import static zkstrata.parser.ast.Subject.*;

public class TestHelper {
    private static final String SELECTOR_PREFIX = "selector";
    private static final String SCHEMA_PREFIX = "schema";
    private static final String ALIAS_PREFIX = "alias";
    private static final String SOURCE = "";
    private static final String STATEMENT = "";
    private static final String TARGET = "";
    private static final int LINE_NUMBER = 1;
    private static final int CHAR_POSITION = 0;
    private static final Position.Relative REL_POSITION = new Position.Relative(TARGET, LINE_NUMBER, CHAR_POSITION);
    private static final Position.Absolute ABS_POSITION = new Position.Absolute(SOURCE, STATEMENT, REL_POSITION);

    private TestHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static WitnessVariable createWitnessVariable(Class<?> type, int id) {
        Reference reference = createReference(type, Integer.toString(id));
        return new WitnessVariable(reference, reference, ABS_POSITION);
    }

    public static InstanceVariable createInstanceVariable(Literal literal) {
        return new InstanceVariable(literal, null, ABS_POSITION);
    }

    private static Reference createReference(Class<?> type, String suffix) {
        return new Reference(type, createAlias(suffix), new Selector(List.of(createSelector(suffix))));
    }

    public static IntegerLiteral createIntegerLiteral(int value) {
        return new IntegerLiteral(Integer.toString(value), ABS_POSITION);
    }

    public static IntegerLiteral createIntegerLiteral(BigInteger value) {
        return new IntegerLiteral(value.toString(10), ABS_POSITION);
    }

    public static StringLiteral createStringLiteral(String value) {
        return new StringLiteral(value, ABS_POSITION);
    }

    public static Identifier createIdentifier(String aliasSuffix, String selectorSuffix) {
        return new Identifier(createAlias(aliasSuffix), List.of(createSelector(selectorSuffix)), ABS_POSITION);
    }

    public static Subject createSubject(boolean isWitness, String suffix) {
        return new Subject(new Schema(createSchema(suffix), ABS_POSITION), new Alias(createAlias(suffix), ABS_POSITION), isWitness);
    }

    private static String createAlias(String suffix) {
        return String.format("%s%s", ALIAS_PREFIX, suffix);
    }

    private static String createSelector(String suffix) {
        return String.format("%s%s", SELECTOR_PREFIX, suffix);
    }

    private static String createSchema(String suffix) {
        return String.format("%s%s", SCHEMA_PREFIX, suffix);
    }

    public static Position.Absolute getAbsPosition() {
        return ABS_POSITION;
    }
}
