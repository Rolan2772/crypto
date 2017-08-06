package com.crypto.trade.poloniex.services.utils;

import eu.verdelhan.ta4j.Decimal;

import java.math.BigDecimal;

public class CalculationsUtils {

    public static final int CRYPTO_SCALE = 8;
    public static final int CRYPTO_ROUNDING_MODE = BigDecimal.ROUND_DOWN;
    // @TODO: Both BUY/SELL can have 0.25% fee
    public static final BigDecimal FEE_PERCENT = new BigDecimal("0.0025");
    public static final BigDecimal AFTER_FEE_PERCENT = BigDecimal.ONE.subtract(FEE_PERCENT);
    // Value shouldn't be less than maximum fee buy 0,0025 + sell 0,0025
    public static final BigDecimal MIN_PROFIT_PERCENT = new BigDecimal("1.01");

    private CalculationsUtils() {
    }

    public static BigDecimal setCryptoScale(BigDecimal value) {
        return value.setScale(CalculationsUtils.CRYPTO_SCALE, CRYPTO_ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal value, BigDecimal divisor) {
        return value.divide(divisor, CRYPTO_SCALE, CRYPTO_ROUNDING_MODE);
    }

    public static BigDecimal toBigDecimal(Decimal decimal) {
        return new BigDecimal(decimal.toString());
    }

    public static Decimal toDecimal(BigDecimal bigDecimal) {
        return Decimal.valueOf(bigDecimal.toString());
    }
}
