/*
* Copyright 2014 Mikhail Vorontsov
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package info.javaperformance.money;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Converter from String/double/float/integer types into Money instances.
 * Requires the precision to be specified for double to Money conversion. Precision is usually based
 * on the smallest tick in your exchange data.
 */
public class MoneyFactory {
    static final int MAX_LONG_LENGTH = Long.toString( Long.MAX_VALUE ).length();

    public static final int MAX_ALLOWED_PRECISION = 15;
    //needed for overflow checking during conversion
    private static final long MAX_LONG_DIVIDED_BY_10 = Long.MAX_VALUE / 10;

    /** Non-negative powers of 10 */
    static final long[] MULTIPLIERS = new long[ MoneyFactory.MAX_ALLOWED_PRECISION + 1 ];
    /** Non-positive powers of 10 */
    static final double[] MULTIPLIERS_NEG = new double[ MoneyFactory.MAX_ALLOWED_PRECISION + 1 ];
    static
    {
        long val = 1;
        for ( int i = 0; i <= MoneyFactory.MAX_ALLOWED_PRECISION; ++i )
        {
            MULTIPLIERS[ i ] = val;
            MULTIPLIERS_NEG[ i ] = 1.0 / val;
            val *= 10;
        }
    }

    static void checkPrecision(int precision) {
        if ( precision < 0 || precision > MAX_ALLOWED_PRECISION )
            throw new IllegalArgumentException( "Precision must be between 0 and " + MAX_ALLOWED_PRECISION );
    }

    /**
     * Convert from currency units and their precision into Money object.
     * @param units Currency units (cents, for example)
     * @param precision Number of digits after decimal point in your smallest possible currency unit. Should be between
     *                  0 and <code>MAX_ALLOWED_PRECISION</code> (inclusive).
     * @return Money object
     * @throws java.lang.IllegalArgumentException In case of invalid precision
     */
    public static Money fromUnits( final long units, final int precision )
    {
        checkPrecision( precision );
        return new MoneyLong( units, precision ).normalize();
    }

    /**
     * Same as <code>fromString</code>, but characters are extracted from the given part of char array
     * @param chars Char array
     * @param offset Start position
     * @param length number of characters to process
     * @return Money object
     */
    public static Money fromCharArray( final char[] chars, final int offset, final int length )
    {
        return fromCharSequence( new CharArraySeq( chars, offset, length ) );
    }

    private static class CharArraySeq implements CharSequence
    {
        private final char[] chars;
        private final int offset;
        private final int length;

        private CharArraySeq(char[] chars, int offset, int length) {
            this.chars = chars;
            this.offset = offset;
            this.length = length;
        }

        public int length() {
            return length;
        }

        public char charAt( final int index ) {
            return chars[ offset + index ];
        }

        public CharSequence subSequence(int start, int end) {
            //not needed for this implemenration
            return null;
        }
    }

    /**
     * Same as <code>fromString</code>, but characters are extracted from the given part of byte array.
     * We expect the data to be ASCII-encoded in the given part of the array.
     * @param bytes Byte array
     * @param offset Start position
     * @param length number of characters to process
     * @return Money object
     */
    public static Money fromByteArray( final byte[] bytes, final int offset, final int length )
    {
        return fromCharSequence( new ByteArraySeq( bytes, offset, length));
    }

    private static class ByteArraySeq implements CharSequence
    {
        private final byte[] bytes;
        private final int offset;
        private final int length;

        private ByteArraySeq(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        public int length() {
            return length;
        }

        public char charAt(int index) {
            return (char) bytes[offset + index];
        }

        public CharSequence subSequence(int start, int end) {
            return null; //not needed for this implementation
        }
    }

    /**
     * Same as <code>fromString</code>, but characters are extracted from the <code>CharSequence</code>
     * @param seq <code>CharSequence</code> object, may be more convenient in some situations.
     * @return Money object
     */
    public static Money fromCharSequence( final CharSequence seq )
    {
        final Money fast = parseFast( seq );
        if ( fast != null )
            return fast;
        return fromString0( seq.toString() ); //slow path, convert to String
    }

    /**
     * Convert a String monetary value into a Money object. We support a first format - dot-separated decimal part without
     * the scientific notation support ( valid example - 355.56 ).
     * @param value Value to parse
     * @return Money
     * @throws java.lang.IllegalArgumentException In case of any conversion errors
     */
    public static Money fromString( final String value )
    {
        //fast first pass parser first
        final Money fast = parseFast( value );
        if ( fast != null )
            return fast;

        return fromString0(value);
    }

    private static Money fromString0( final String value )
    {
        final int dotPos = value.indexOf('.');
        final int precision = dotPos == -1 ? 0 : value.length() - dotPos - 1;
        if ( precision > MAX_ALLOWED_PRECISION ) //too high precision
            return new MoneyBigDecimal( value );
        if ( dotPos != -1 && value.indexOf( '.', dotPos + 1 ) != -1 )
            throw new IllegalArgumentException( "Unparseable String value has more than 1 decimal point: " + value );
        try
        {
            final long units = Long.parseLong( value.replace(".", "") );
            return new MoneyLong( units, precision ); //actual precision, not the maximal one
        }
        catch ( NumberFormatException ex )
        {
            try
            {
                return new MoneyBigDecimal( value );
            }
            catch ( NumberFormatException ex2 )
            {
                throw new IllegalArgumentException( "Unparseable value provided: " + value, ex2 );
            }
        }
    }

    /**
     * Fast first pass parser, should correctly process most of positive/negative values fitting into <code>long</code>
     * @param str Floating point number to parse
     * @return Money object or null (if can't parse)
     * @throws java.lang.IllegalArgumentException If a value has more than one decimal digit
     */
    private static Money parseFast( final CharSequence str )
    {
        if ( str.length() >= MAX_LONG_LENGTH )
            return null;
        long res = 0;
        int start = 0;
        long sign = 1;
        int precision = 0;
        if ( str.charAt( 0 ) == '-' )
        {
            sign = -1;
            start = 1;
        }
        else if ( str.charAt( 0 ) == '+' )
        {
            sign = 1;
            start = 1;
        }
        for ( int i = start; i < str.length(); ++i )
        {
            final char c = str.charAt( i );
            if ( c == '.' )
            {
                if ( precision > 0 )
                    throw new IllegalArgumentException( "Unparseable String value has more than 1 decimal point: " + str );
                precision = str.length() - i - 1;
            }
            else if ( c >= '0' && c <= '9' )
                res = res * 10 + ( c - '0' );
            else //unsupported char, handle in the caller
                return null;
        }
        if ( precision >= 0 && precision <= MAX_ALLOWED_PRECISION )
            return new MoneyLong( res * sign, precision ).normalize();
        else
            return new MoneyBigDecimal( str.toString() );
    }

    /**
     * <p>
     *     Convert a double monetary value into a Money object. You will end up with the most efficient Money type
     *     if you have no more than <code>MAX_ALLOWED_PRECISION</code> decimal digits in your value.
     * </p>
     * <p>
     *     This method will attempt to look by ulp in both directions during conversions to cater for already slightly
     *     incorrect values - results of <code>double</code> operations outside this library.
     * </p>
     * @param value Double monetary value
     * @return Money object
     */
    public static Money fromDouble( final double value )
    {
        return fromDouble( value, MAX_ALLOWED_PRECISION );
    }
    /**
     * <p>
     *     Convert a double monetary value into a Money object. You will end up with the most efficient Money type
     *     if you have no more than <code>precision</code> decimal digits in your value.
     * </p>
     * <p>
     *     This method will attempt to look by ulp in both directions during conversions to cater for already slightly
     *     incorrect values - results of <code>double</code> operations outside this library.
     * </p>
     * <p>
     *     Do not try to set too high precision for this method - it may prevent you from correcting a slightly
     *     incorrect value (off by ulp) into a correct one. As a result, you will end up with BigDecimal-based
     *     implementation, which requires more memory and which is much slower to calculate.
     * </p>
     * @param value Double monetary value
     * @param precision Number of digits after decimal point in your smallest possible currency unit.
     *                  Should be between 0 and <code>MAX_ALLOWED_PRECISION</code> (inclusive).
     *                  This parameter is a hint only for more efficient conversion. It does not truncate the result.
     * @return Money object with a value as close as possible to a provided value (first parameter)
     */
    public static Money fromDouble( final double value, final int precision )
    {
        checkPrecision( precision );
        //attempt direct
        final Money direct = fromDoubleNoFallback( value, precision );
        if ( direct != null )
            return direct;

        return new MoneyBigDecimal( value );
    }

    static MoneyLong fromDoubleNoFallback( final double value, final int precision )
    {
        //attempt direct
        final MoneyLong direct = fromDouble0( value, precision );
        if ( direct != null )
            return direct;
        //ulp down
        final MoneyLong down = fromDouble0( Math.nextAfter( value, -Double.MAX_VALUE ), precision );
        if ( down != null )
            return down;
        //ulp up
        final MoneyLong up = fromDouble0( Math.nextAfter( value, Double.MAX_VALUE ), precision );
        if ( up != null )
            return up;
        return null;
    }

    private static MoneyLong fromDouble0( final double value, final int precision )
    {
        final double multiplied = value * MULTIPLIERS[ precision ];
        final long converted = (long) multiplied;
        if ( multiplied == converted ) //here is an implicit conversion from long to double
            return new MoneyLong( converted, precision ).normalize();
        return null;
    }

    /**
     * <p>
     *     Convert a given BigDecimal value into money. Conversion is similar to <code>toDouble</code>,
     *     though this method does not attempt to make any corrections: it assumes that BigDecimal is a result
     *     of exact calculations.
     * </p>
     * <p>
     *     This method will try to use the most efficient representation if possible.
     * </p>
     * @param value BigDecimal value to convert
     * @return Money object
     */
    public static Money fromBigDecimal( final BigDecimal value )
    {
        final BigDecimal cleaned = value.stripTrailingZeros();

        //try to convert to double using a fixed precision = 3, which will cover most of currencies
        //it is required to get rid of rounding issues
        final double dbl = value.doubleValue();
        final Money res = fromDoubleNoFallback( dbl, 3 );
        if ( res != null )
            return res;

        final int scale = cleaned.scale();
        if ( scale > MAX_ALLOWED_PRECISION || scale < -MAX_ALLOWED_PRECISION )
            return new MoneyBigDecimal( cleaned );
        //we may not fit into the Long, but we should try
        //this value may be truncated!
        final BigInteger unscaledBigInt = cleaned.unscaledValue();
        final long unscaledUnits = unscaledBigInt.longValue();
        //check that it was not
        if ( !BigInteger.valueOf(unscaledUnits).equals( unscaledBigInt ) )
            return new MoneyBigDecimal( cleaned );
        //scale could be negative here - we must multiply in that case
        if ( scale >= 0 )
            return new MoneyLong( unscaledUnits, scale );
        //multiply by 10 and each time check that sign did not change
        //scale is negative
        long units = unscaledUnits;
        for ( int i = 0; i < -scale; ++i )
        {
            units *= 10;
            if ( units >= MAX_LONG_DIVIDED_BY_10 )
                return new MoneyBigDecimal( value );
        }
        return new MoneyLong( units, 0 );

    }

}
