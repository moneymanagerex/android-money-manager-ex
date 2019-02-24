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
 * Some shared methods are kept here
 */
abstract class AbstractMoney implements Money {
    /**
     * Add another Money object to this one.
     *
     * @param other Other Money object
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money add( final Money other ) {
        if ( other instanceof MoneyLong )
            return add( ( MoneyLong ) other );
        else
            return add( ( MoneyBigDecimal ) other );
    }

    protected abstract Money add( final MoneyLong other );

    protected Money add( final MoneyBigDecimal other )
    {
        final BigDecimal res = toBigDecimal().add( other.toBigDecimal(), MathContext.DECIMAL128 );
        return MoneyFactory.fromBigDecimal( res );
    }

    public boolean isZero() {
        return this.toDouble() == 0;
    }

    /**
     * Subtract another Money object from this one.
     *
     * @param other Other money object
     * @return A new Money object normalized to the efficient representation if possible
     */
    public Money subtract( final Money other )
    {
        return add( other.negate() );
    }

    /**
     * Compares another Money object to this one. Should be used like {@link BigDecimal#compareTo(Object)}. May or may
     * not share the same specification, read return. However this method is up to specification as defined in
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
    public int compareTo(final Money other) {
        if (other instanceof MoneyLong) return compareTo((MoneyLong) other);
        else return compareTo((MoneyBigDecimal) other);
    }

    protected abstract int compareTo(final MoneyLong other);

    public int compareTo(final MoneyBigDecimal other) {
        return toBigDecimal().compareTo(other.toBigDecimal());
    }
}

