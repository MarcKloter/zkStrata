package zkstrata.domain.data.types.custom;

import zkstrata.domain.data.types.Constant;
import zkstrata.domain.data.types.Literal;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateConstants {
    private DateConstants() {
        throw new IllegalStateException("Utility class");
    }

    @Constant ("UNIX_TIME_NOW")
    public static Literal getCurrentUnixTimestamp() {
        return new Literal(BigInteger.valueOf(Instant.now().getEpochSecond()));
    }

    @Constant ("TODAY_DAY_OF_MONTH")
    public static Literal getCurrentDay() {
        LocalDate localDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Literal(BigInteger.valueOf(localDate.getDayOfMonth()));
    }

    @Constant ("TODAY_MONTH")
    public static Literal getCurrentMonth() {
        LocalDate localDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Literal(BigInteger.valueOf(localDate.getMonthValue()));
    }

    @Constant ("TODAY_YEAR")
    public static Literal getCurrentYear() {
        LocalDate localDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Literal(BigInteger.valueOf(localDate.getYear()));
    }
}
