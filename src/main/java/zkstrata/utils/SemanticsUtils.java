package zkstrata.utils;

import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;

public class SemanticsUtils {
    public static boolean testMaxBitSize(BigInteger bigInteger, int maxNumberOfBits) {
        return bigInteger.bitCount() > maxNumberOfBits;
    }
}
