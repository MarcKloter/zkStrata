package zkstrata.utils;

import java.math.BigInteger;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final BigInteger ED25519_PRIME_ORDER = new BigInteger("1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed", 16);
    public static final BigInteger ED25519_MAX_VALUE = ED25519_PRIME_ORDER.subtract(BigInteger.ONE);
    public static final BigInteger UNSIGNED_64BIT_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);
    public static final BigInteger UNSIGNED_65BIT_MIN = new BigInteger("010000000000000000", 16);

    public static final int EQUALITY_COST_ESTIMATE = 1;
    public static final int INEQUALITY_COST_ESTIMATE = 5;
    public static final int MIMC_HASH_COST_ESTIMATE = 1946;
    public static final int LESS_THAN_COST_ESTIMATE = 763;
    public static final int BOUNDS_CHECK_COST_ESTIMATE = 259;
}
