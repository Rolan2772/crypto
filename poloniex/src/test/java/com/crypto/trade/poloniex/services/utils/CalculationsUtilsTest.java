package com.crypto.trade.poloniex.services.utils;

import eu.verdelhan.ta4j.Decimal;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CalculationsUtilsTest {

    @Test
    public void cryptoScale() {
        assertEquals(new BigDecimal("0.00043654"), CalculationsUtils.setCryptoScale(BigDecimal.valueOf(0.000436546)));
        assertEquals(new BigDecimal("0.99999999"), CalculationsUtils.setCryptoScale(BigDecimal.valueOf(0.99999999)));
    }

    @Test
    public void divide() {
        assertEquals(new BigDecimal("0.33333333"), CalculationsUtils.divide(BigDecimal.ONE, BigDecimal.valueOf(3)));
        assertEquals(new BigDecimal("2.00000000"), CalculationsUtils.divide(BigDecimal.valueOf(4), BigDecimal.valueOf(2)));
    }

    @Test
    public void toBigDecimal() {
        assertEquals(new BigDecimal("0.3333"), CalculationsUtils.toBigDecimal(Decimal.valueOf("0.3333")));
    }

    @Test
    public void wrongDecimalConstruction() {
        assertNotEquals(BigDecimal.valueOf(0.5555), CalculationsUtils.toBigDecimal(Decimal.valueOf(0.5555)));
    }

    public void toDecimal() {
        assertEquals(Decimal.valueOf(0.3333), CalculationsUtils.toDecimal(BigDecimal.valueOf(0.3333)));
        assertEquals(Decimal.valueOf("0.3333"), CalculationsUtils.toDecimal(new BigDecimal("0.3333")));

    }
}
