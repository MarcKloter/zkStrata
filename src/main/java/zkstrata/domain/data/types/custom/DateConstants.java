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

    @Constant ("CURRENT_DATE")
    public static Literal getCurrentUnixTimestamp() {
        long timestamp = now().atStartOfDay(systemDefault()).toInstant().getEpochSecond();
        return new Literal(BigInteger.valueOf(timestamp ));
    }

    @Constant ("CURRENT_DAY_OF_MONTH")
    public static Literal getCurrentDay() {
        return new Literal(BigInteger.valueOf(now(systemDefault()).getDayOfMonth()));
    }

    @Constant ("CURRENT_MONTH")
    public static Literal getCurrentMonth() {
        return new Literal(BigInteger.valueOf(now(systemDefault()).getMonthValue()));
    }

    @Constant ("CURRENT_YEAR")
    public static Literal getCurrentYear() {
        return new Literal(BigInteger.valueOf(now(systemDefault()).getYear()));
    }
}
