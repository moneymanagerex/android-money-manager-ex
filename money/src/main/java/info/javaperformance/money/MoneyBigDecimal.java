/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.javaperformance.money;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Safe but slow Money implementation. Uses BigDecimal as a storage.
 */
class MoneyBigDecimal extends AbstractMoney {
    private final BigDecimal m_value;

    public MoneyBigDecimal( final BigDecimal value ) {
        m_value = value;
    }

    public MoneyBigDecimal( final double value )
    {
        m_value = new BigDecimal( value, MathContext.DECIMAL64 ).stripTrailingZeros(); //decimal64 to match double
    }

    public MoneyBigDecimal( final String value )
    {
        //important - do not use DECIMAL64 context here - you will lose precision for huge values.
        //at the same time using it is required for BigDecimal(double) constructor - it matches "double" range.
        m_value = new BigDecimal( value );
    }

    public double toDouble() {
        return m_value.doubleValue();
    }

    /**
     * Convert this value into a BigDecimal. This method is also used for arithmetic calculations when necessary.
     *
     * @return This object as BigDecimal
     */
    public BigDecimal toBigDecimal() {
        return m_value;
    }

    /**
     * Return this value with an opposite sign.
     *
     * @return A new object with the same value with a different sign
     */
    public Money negate() {
        return new MoneyBigDecimal( m_value.negate() );
    }

    /**
     * Convert into a String in a plain notation with a decimal dot.
     * @return a String in a plain notation with a decimal dot.
     */
    @Override
    public String toString() {
        return m_value.toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoneyBigDecimal that = (MoneyBigDecimal) o;

        return m_value.equals(that.m_value);
    }

    @Override
    public int hashCode() {
        return m_value.hashCode();
    }

    protected Money add( final MoneyLong other )
    {
        return other.add( this ); //implemented in MoneyLong
    }

    @Override
    protected int compareTo(MoneyLong other) {
        return -(other.compareTo(this)); // flips the response with unary
    }

    /**
     * Multiply the current object by the <code>long</code> value.
     *
     * @param multiplier Multiplier
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money multiply( final long multiplier ) {
        final BigDecimal res = m_value.multiply(BigDecimal.valueOf(multiplier));
        return MoneyFactory.fromBigDecimal( res );
    }

    /**
     * Multiply the current object by the <code>double</code> value.
     *
     * @param multiplier Multiplier
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money multiply(double multiplier) {
        return MoneyFactory.fromBigDecimal(
                m_value.multiply(new BigDecimal(multiplier, MathContext.DECIMAL64), MathContext.DECIMAL64) );
    }

    /**
     * Divide the current object by the given <code>long</code> divider.
     *
     * @param divider   Divider
     * @param precision Maximal precision to keep. We will round the next digit.
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money divide( final long divider, final int precision )
    {
        final BigDecimal res = m_value.divide( BigDecimal.valueOf( divider ), MathContext.DECIMAL64 ).stripTrailingZeros();
        return truncate( res, precision );
    }

    /**
     * Divide the current object by the given <code>long</code> divider.
     *
     * @param divider   Divider
     * @param precision Maximal precision to keep. We will round the next digit.
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money divide( final double divider, final int precision )
    {
        final BigDecimal res = m_value.divide( BigDecimal.valueOf( divider ), MathContext.DECIMAL64 ).stripTrailingZeros();
        return truncate( res, precision );
    }

    /**
     * Truncate the current value leaving no more than {@code maximalPrecision} signs after decimal point.
     * The number will be rounded towards closest digit (0-4 -> 0; 5-9 -> 1)
     *
     * @param maximalPrecision Required precision
     * @return A new Money object normalized to the efficient representation if possible
     */
    private static Money truncate( final BigDecimal val, final int maximalPrecision ) {
        MoneyFactory.checkPrecision( maximalPrecision );

        final BigDecimal res = val.setScale( maximalPrecision, BigDecimal.ROUND_HALF_UP );
        return MoneyFactory.fromBigDecimal( res );
    }

    /**
     * Truncate the current value leaving no more than {@code maximalPrecision} signs after decimal point.
     * The number will be rounded towards closest digit (0-4 -> 0; 5-9 -> 1)
     *
     * @param maximalPrecision Required precision
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money truncate( final int maximalPrecision ) {
        if ( m_value.scale() <= maximalPrecision )
            return this;
        return truncate(m_value, maximalPrecision);
    }
}
