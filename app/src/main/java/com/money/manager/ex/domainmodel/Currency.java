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
package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;

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

    public static Currency fromCursor(Cursor c) {
        Currency currency = new Currency();
        currency.loadFromCursor(c);
        return currency;
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all Double values.
        DatabaseUtils.cursorDoubleToCursorValues(c, SCALE, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, BASECONVRATE, this.contentValues);
    }

    public Double getBaseConversionRate() {
        return getDouble(BASECONVRATE);
    }

    public String getCentName() {
        return getString(CENT_NAME);
    }

    public void setCentName(String value) {
        setString(CENT_NAME, value);
    }

    /**
     * @return Currency symbol in ISO format.
     */
    public String getCode() {
        return getString(CURRENCY_SYMBOL);
    }

    public int getCurrencyId() {
        return getInt(CURRENCYID);
    }

    public void setCurrencyid(int value) {
        setInt(CURRENCYID, value);
    }

    public String getDecimalSeparator() {
        return getString(DECIMAL_POINT);
    }

    public void setDecimalPoint(String value) {
        setString(DECIMAL_POINT, value);
    }

    public String getGroupSeparator() {
        return getString(GROUP_SEPARATOR);
    }

    public String getName() {
        return getString(CURRENCYNAME);
    }

    public void setName(String value) {
        setString(CURRENCYNAME, value);
    }

    public void setCode(String value) {
        setString(CURRENCY_SYMBOL, value);
    }

    public String getPfxSymbol() {
        return getString(PFX_SYMBOL);
    }

    public void setPfxSymbol(String value) {
        setString(PFX_SYMBOL, value);
    }

    public void setGroupSeparator(String value) {
        setString(GROUP_SEPARATOR, value);
    }

    public Integer getScale() {
        return getInt(SCALE);
    }

    public void setScale(int value) {
        setInt(SCALE, value);
    }

    public String getSfxSymbol() {
        return getString(SFX_SYMBOL);
    }

    public void setSfxSymbol(String value) {
        setString(SFX_SYMBOL, value);
    }

    public void setConversionRate(Double value) {
        setDouble(BASECONVRATE, value);
    }

    public void setUnitName(String value) {
        setString(UNIT_NAME, value);
    }
}
