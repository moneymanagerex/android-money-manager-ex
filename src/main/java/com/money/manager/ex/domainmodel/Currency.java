/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 *
 */
package com.money.manager.ex.domainmodel;

import com.money.manager.ex.database.TableCurrencyFormats;

/**
 * Currency entity
 */
public class Currency
    extends EntityBase {

    public static final String CURRENCYID = "CURRENCYID";
    public static final String CURRENCYNAME = "CURRENCYNAME";
    public static final String PFX_SYMBOL = "PFX_SYMBOL";
    public static final String SFX_SYMBOL = "SFX_SYMBOL";
    public static final String DECIMAL_POINT = "DECIMAL_POINT";
    public static final String GROUP_SEPARATOR = "GROUP_SEPARATOR";
    public static final String UNIT_NAME = "UNIT_NAME";
    public static final String CENT_NAME = "CENT_NAME";
    public static final String SCALE = "SCALE";
    public static final String BASECONVRATE = "BASECONVRATE";
    public static final String CURRENCY_SYMBOL = "CURRENCY_SYMBOL";

    public int getCurrencyId() {
        return getInt(CURRENCYID);
    }

    public void setName(String value) {
        setString(CURRENCYNAME, value);
    }

    public void setCode(String value) {
        setString(CURRENCY_SYMBOL, value);
    }

    public void setPfxSymbol(String value) {
        setString(PFX_SYMBOL, value);
    }

    public void setDecimalPoint(String value) {
        setString(DECIMAL_POINT, value);
    }

    public void setGroupSeparator(String value) {
        setString(GROUP_SEPARATOR, value);
    }

    public void setScale(double value) {
        setDouble(SCALE, value);
    }

    public void setConversionRate(Double value) {
        setDouble(BASECONVRATE, value);
    }
}
