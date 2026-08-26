/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
package org.moneymanagerex.android.tests;

import com.money.manager.ex.core.FormatUtilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import info.javaperformance.money.MoneyFactory;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that number formatting is driven purely by the supplied currency separators
 * and does not leak the system/default-locale separators.
 *
 * Regression test for issue #2764.
 *
 * {@link FormatUtilities#formatNumber} is a pure function with no instance/DB dependencies,
 * so the test calls it statically and needs no Robolectric/Context setup.
 */
public class FormatUtilitiesTest {

    private Locale originalLocale;

    @Before
    public void setUp() {
        originalLocale = Locale.getDefault();
    }

    @After
    public void tearDown() {
        Locale.setDefault(originalLocale);
    }

    /**
     * Explicit separators must be honoured regardless of the active default locale.
     */
    @Test
    public void formatNumberUsesSuppliedSeparators() {
        String result = FormatUtilities.formatNumber(
            MoneyFactory.fromString("1234.56"), 2, ",", ".", null, null);

        assertEquals("1.234,56", result);
    }

    /**
     * The output must be identical across very different default locales, proving the
     * formatter no longer leaks the system locale's separators.
     */
    @Test
    public void formatNumberIsIndependentOfDefaultLocale() {
        Locale.setDefault(Locale.US);
        String inUs = FormatUtilities.formatNumber(
            MoneyFactory.fromString("1234.56"), 2, ",", ".", null, null);

        Locale.setDefault(Locale.GERMANY);
        String inGermany = FormatUtilities.formatNumber(
            MoneyFactory.fromString("1234.56"), 2, ",", ".", null, null);

        Locale.setDefault(new Locale("ar", "EG"));
        String inEgypt = FormatUtilities.formatNumber(
            MoneyFactory.fromString("1234.56"), 2, ",", ".", null, null);

        assertEquals(inUs, inGermany);
        assertEquals(inUs, inEgypt);
        assertEquals("1.234,56", inUs);
    }

    /**
     * With empty separators the locale-root defaults (dot decimal, comma group) are used,
     * not whatever the system default locale happens to provide.
     */
    @Test
    public void formatNumberWithEmptySeparatorsUsesLocaleRootDefaults() {
        Locale.setDefault(Locale.GERMANY);

        String result = FormatUtilities.formatNumber(
            MoneyFactory.fromString("1234.5"), 2, "", "", null, null);

        assertEquals("1,234.50", result);
    }
}
