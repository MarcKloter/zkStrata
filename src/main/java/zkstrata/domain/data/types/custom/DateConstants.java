package zkstrata.domain.data.types.custom;

import zkstrata.domain.data.types.Constant;
import zkstrata.domain.data.types.Literal;

import java.math.BigInteger;

import static java.time.LocalDate.now;
import static java.time.ZoneId.systemDefault;

public class DateConstants {
    private DateConstants() {
        throw new IllegalStateException("Utility class");
    }

    @Constant("CURRENT_DATE")
    public static Literal getCurrentUnixTimestamp() {
        return new Literal(new BigInteger(getISO8601BasicDate()));
    }

    /**
     * Returns the current date as basic format of ISO 8601 without ambiguity.
     * <p>
     * Example: February 6, 2020 is encoded as 20200206
     *
     * @return current date as basic format of ISO 8601 as String
     */
    private static String getISO8601BasicDate() {
        return String.format("%d%02d%02d", getYear(), getMonth(), getDayOfMonth());
    }

    @Constant("CURRENT_DAY_OF_MONTH")
    public static Literal getCurrentDay() {
        return new Literal(BigInteger.valueOf(getDayOfMonth()));
    }

    private static int getDayOfMonth() {
        return now(systemDefault()).getDayOfMonth();
    }

    @Constant("CURRENT_MONTH")
    public static Literal getCurrentMonth() {
        return new Literal(BigInteger.valueOf(getMonth()));
    }

    private static int getMonth() {
        return now(systemDefault()).getMonthValue();
    }

    @Constant("CURRENT_YEAR")
    public static Literal getCurrentYear() {
        return new Literal(BigInteger.valueOf(getYear()));
    }

    private static int getYear() {
        return now(systemDefault()).getYear();
    }
}
