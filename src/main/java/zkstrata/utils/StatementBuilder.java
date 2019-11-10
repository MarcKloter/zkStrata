package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class StatementBuilder {
    private static final String PREFIX = "PROOF FOR ";
    private static final String INFIX = " THAT ";
    private static final String AND = " AND ";
    private static final String WITNESS = "";
    private static final String INSTANCE = "INSTANCE ";

    private List<String> subjects = new ArrayList<>();
    private List<String> predicates = new ArrayList<>();

    public static String stringLiteral(String string) {
        return String.format("'%s'", string);
    }

    public static String integerLiteral(BigInteger integer) {
        return String.format("%s", integer.toString());
    }

    public StatementBuilder subject(String schema, String alias, boolean isWitness) {
        subjects.add(String.format("%s%s AS %s", isWitness ? WITNESS : INSTANCE, schema, alias));

        return this;
    }

    public StatementBuilder equality(String leftHand, String rightHand) {
        predicates.add(String.format("%s IS EQUAL TO %s", leftHand, rightHand));

        return this;
    }

    public StatementBuilder boundsCheck(String value, BigInteger min, BigInteger max) {
        if (min == null && max == null)
            return this;
        else if (min == null)
            predicates.add(String.format("%s IS LESS THAN %d", value, max));
        else if (max == null)
            predicates.add(String.format("%s IS GREATER THAN %s", value, min));
        else
            predicates.add(String.format("%s IS LESS THAN %s AND GREATER THAN %s", value, max, min));

        return this;
    }

    public StatementBuilder mimcHash(String preimage, String image) {
        predicates.add(String.format("%s IS PREIMAGE OF %s", preimage, image));

        return this;
    }

    public String build() {
        TextStringBuilder builder = new TextStringBuilder();
        builder.append(PREFIX);
        builder.append(String.join(AND, subjects));
        builder.append(INFIX);
        builder.append(String.join(AND, predicates));
        return builder.build();
    }
}
