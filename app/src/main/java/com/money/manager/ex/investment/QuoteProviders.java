/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.investment;

import com.money.manager.ex.Constants;

/**
 * List of quote providers for stock prices and currency rates.
 */
public enum QuoteProviders {
    YahooYql,
    YahooCsv,
    Morningstar;

    public static String[] names() {
        QuoteProviders[] providers = QuoteProviders.values();
        int count = providers.length;
        String[] result = new String[count];

        for (int i = 0; i < count; i++) {
            result[i] = providers[i].name();
        }
        return result;
    }

    public static int indexOf(QuoteProviders value) {
        QuoteProviders[] providers = QuoteProviders.values();
        int count = providers.length;

        for (int i = 0; i < count; i++) {
            if (providers[i] == value) return i;
        }
        return Constants.NOT_SET;
    }
}
