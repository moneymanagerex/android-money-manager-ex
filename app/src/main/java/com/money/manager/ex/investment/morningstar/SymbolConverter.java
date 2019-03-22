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

package com.money.manager.ex.investment.morningstar;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Converts Yahoo to Morningstar symbols.
 */
public class SymbolConverter {

    public SymbolConverter() {
        initializeMap();
    }

    BiMap<String, String> mMap;

    public String convert(String yahooSymbol) {
        // The list can be stored elsewhere, editable, and loaded when conversion is needed.

        String[] parts = yahooSymbol.split("\\.");

        // e US exchanges
        if (parts.length <= 1) {
            // U.S. Do not use an exchange prefix for now.
            return yahooSymbol;
        }

        String symbol = parts[0];
        String yahooExchange = parts[1].toUpperCase();

        String morningstarExchange = mMap.get(yahooExchange);

        String result = morningstarExchange + ":" + symbol;
        return result;
    }

    public String getYahooSymbol(String morningstarSymbol) {
        String[] parts = morningstarSymbol.split("\\:");

        if (parts.length <= 1) {
            return morningstarSymbol;
        }

        String symbol = parts[1];
        String morningstarExchange = parts[0];

        String yahooExchange = mMap.inverse().get(morningstarExchange);

        return symbol + "." + yahooExchange;
    }

    private void initializeMap() {
        mMap = HashBiMap.create();

        mMap.put("AS", "XAMS");
        mMap.put("AX", "XASX");
        mMap.put("DE", "XETR");
        mMap.put("L", "XLON");
        mMap.put("PA", "XPAR");
        // mMap.put("VI", "");
    }
}
