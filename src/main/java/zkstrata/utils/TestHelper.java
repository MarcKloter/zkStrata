package zkstrata.utils;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;

import java.util.List;

public class TestHelper {
    private static final String SUBJECT_PREFIX = "alias";
    private static final String SELECTOR_PREFIX = "selector";
    private static final String SOURCE = "";
    private static final String STATEMENT = "";
    private static final String TARGET = "";
    private static final int LINE_NUMBER = 1;
    private static final int CHAR_POSITION = 0;
    private static final Position.Relative REL_POSITION = new Position.Relative(TARGET, LINE_NUMBER, CHAR_POSITION);
    public static final Position.Absolute ABS_POSITION = new Position.Absolute(SOURCE, STATEMENT, REL_POSITION);
    private static long idCounter = 0;

    private TestHelper() {
        throw new IllegalStateException("Utility class");
    }

    private static synchronized String createID() {
        return String.valueOf(idCounter++);
    }

    public static WitnessVariable createWitnessVariable(Class<?> type) {
        Reference reference = createReference(type, createID());
        return new WitnessVariable(reference, reference, ABS_POSITION);
    }

    public static InstanceVariable createInstanceVariable(Literal literal) {
        return new InstanceVariable(literal, null, ABS_POSITION);
    }

    private static Reference createReference(Class<?> type, String id) {
        return new Reference(type, SUBJECT_PREFIX + id, new Selector(List.of(SELECTOR_PREFIX + id)));
    }
}
