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
package com.money.manager.ex.investment.yql;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * YQL query generator
 */
public class YqlQueryGenerator {
    //    private String source = "yahoo.finance.quote";
    public final String source = "yahoo.finance.quotes";

    public String getQueryFor(List<String> symbols) {
        // http://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
        List<String> fields = Arrays.asList("symbol", "LastTradePriceOnly", "LastTradeDate", "Currency");

        String query = getQueryFor(this.source, fields, symbols);

        return query;
    }

    public String getQueryFor(String source, List<String> fields, List<String> symbols) {
        // append quotes to all the symbols
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            symbol = "\"" + symbol + "\"";
            symbols.set(i, symbol);
        }

        String query = "select ";
        query += TextUtils.join(",", fields);
        query += " from ";
        query += source;    // table
        query += " where symbol in (";
        query += TextUtils.join(",", symbols);
        query += ")";

        return query;
    }

}
