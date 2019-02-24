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

/**
 * <p>
 *  An abstraction for a money value using as efficient as possible format to keep and process your data.
 *  All methods allocating a new Money object will try to output the most efficient prepresentation
 *  (for example, if precision=1, then 0.05+0.05 (each of them does not fit into precision) will be equal to
 *  0.1 (units=1, precision=1).
 * </p>
 * <p>
 *  You should use one of <code>to*</code> methods to convert it into a more commonly used formats after the
 *  calculation is over.
 * </p>
 */
public interface Money extends Comparable<Money> {

    /**
     * Convert to the original currency - divide <code>units</code> by <code>10^precision</code>.
     * @return <code>units / (10^precision)</code>
     */
    public double toDouble();

    /**
     * Convert into a String in a plain notation with a decimal dot.
     * @return a String in a plain notation with a decimal dot.
     */
    public String toString();

    /**
     * Convert this value into a BigDecimal. This method is also used for arithmetic calculations when necessary.
     * @return This object as BigDecimal
     */
    public BigDecimal toBigDecimal();

    /**
     * Add another Money object to this one.
     * @param other Other Money object
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money add( final Money other );

    /**
     * Return this value with an opposite sign.
     * @return A new object with the same value with a different sign
     */
    public Money negate();

    /**
     * Subtract another Money object from this one.
     * @param other Other money object
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money subtract( final Money other );

    /**
     * Multiply the current object by the <code>long</code> value.
     * @param multiplier Multiplier
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money multiply( final long multiplier );

    /**
     * Multiply the current object by the <code>double</code> value.
     * @param multiplier Multiplier
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money multiply( final double multiplier );

    /**
     * Divide the current object by the given <code>long</code> divider.
     * @param divider Divider
     * @param precision Maximal precision to keep. We will round the next digit.
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money divide( final long divider, final int precision );

    /**
     * Divide the current object by the given <code>long</code> divider.
     * @param divider Divider
     * @param precision Maximal precision to keep. We will round the next digit.
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money divide( final double divider, final int precision );

    /**
     * Truncate the current value leaving no more than {@code maximalPrecision} signs after decimal point.
     * The number will be rounded towards closest digit (0-4 -{@literal >} 0; 5-9 -> 1)
     * @param maximalPrecision Required precision
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money truncate( final int maximalPrecision );

    /**
     * Compares another Money object to this one. Should be used like {@link BigDecimal#compareTo(Object)}. May or may
     * not share the same specification, read return. However this method will be up to specification as defined in
     * {@link Comparable}
     * <p>
     * Two {@code Money} objects that are equal in value but have a different scale (like 2.0 and 2.00)
     * are considered equal by this method.  This method is provided in preference to individual methods for each of
     * the six boolean comparison operators ({@literal <}, ==, {@literal >}, {@literal >=}, !=, {@literal <=}).  The
     * suggested idiom for performing these comparisons is: {@code (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
     * {@code op} is one of the six comparison operators.
     * @param other Other money object
     * @return As of this version, -1, 0, or 1 as this {@code Money} is numerically less than, equal to, or greater
     * than {@code other}. This specification may possibly change to -x or x with x representing the difference in
     * precision. I.E If the number is negative than it will still be numerically less than {@code other}.
     */
    public int compareTo(final Money other);

    public boolean isZero();
}
