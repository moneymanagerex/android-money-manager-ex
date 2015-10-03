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
 */
package com.money.manager.ex.database;

import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.domainmodel.Currency;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
/**
 * Dataset for currencies. Being replaced by Currency entity.
 */
public class TableCurrencyFormats
		extends Dataset {

	// class member
	private int currencyId;
	private String currencyName;
	private String pfxSymbol;
	private String sfxSymbol;
	private String decimalPoint;
	private String groupSeparator;
	private String unitName;
	private String centName;
	private double scale;
	private double baseConvRate;
	private String currencySymbol;

	public TableCurrencyFormats() {
		super("currencyformats_v1", DatasetType.TABLE, "currencyformats");
	}

	/**
	 * @return the all columns of the tables
	 */
	@Override
	public String[] getAllColumns() {
		return new String[] {
			"CURRENCYID AS _id", Currency.CURRENCYID, Currency.CURRENCYNAME,
			Currency.PFX_SYMBOL, Currency.SFX_SYMBOL, Currency.DECIMAL_POINT,
			Currency.GROUP_SEPARATOR, Currency.UNIT_NAME, Currency.CENT_NAME,
			Currency.SCALE, Currency.BASECONVRATE, Currency.CURRENCY_SYMBOL
		};
	}
	/**
	 * @return the currencyId
	 */
	public int getCurrencyId() {
		return currencyId;
	}
	/**
	 * @param currencyId the currencyId to set
	 */
	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}
	/**
	 * @return the currencyName
	 */
	public String getCurrencyName() {
		return currencyName;
	}
	/**
	 * @param currencyName the currencyName to set
	 */
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	/**
	 * @return the pfxSymbol
	 */
	public String getPfxSymbol() {
		return pfxSymbol;
	}
	/**
	 * @param pfxSymbol the pfxSymbol to set
	 */
	public void setPfxSymbol(String pfxSymbol) {
		this.pfxSymbol = pfxSymbol;
	}
	/**
	 * @return the sfxSymbol
	 */
	public String getSfxSymbol() {
		return sfxSymbol;
	}
	/**
	 * @param sfxSymbol the sfxSymbol to set
	 */
	public void setSfxSymbol(String sfxSymbol) {
		this.sfxSymbol = sfxSymbol;
	}
	/**
	 * @return the decimalPoint
	 */
	public String getDecimalPoint() {
		return decimalPoint;
	}
	/**
	 * @param decimalPoint the decimalPoint to set
	 */
	public void setDecimalPoint(String decimalPoint) {
		this.decimalPoint = decimalPoint;
	}
	/**
	 * @return the groupSeparator
	 */
	public String getGroupSeparator() {
		return groupSeparator;
	}
	/**
	 * @param groupSeparator the groupSeparator to set
	 */
	public void setGroupSeparator(String groupSeparator) {
		this.groupSeparator = groupSeparator;
	}
	/**
	 * @return the unitName
	 */
	public String getUnitName() {
		return unitName;
	}
	/**
	 * @param unitName the unitName to set
	 */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	/**
	 * @return the centName
	 */
	public String getCentName() {
		return centName;
	}
	/**
	 * @param centName the centName to set
	 */
	public void setCentName(String centName) {
		this.centName = centName;
	}
	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}
	/**
	 * @param scale the scale to set
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}
	/**
	 * @return the baseConvRate
	 */
	public double getBaseConvRate() {
		return baseConvRate;
	}
	/**
	 * @param baseConvRate the baseConvRate to set
	 */
	public void setBaseConvRate(double baseConvRate) {
		this.baseConvRate = baseConvRate;
	}
	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	/**
	 * @param currencySymbol the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	@Override
	public void setValueFromCursor(Cursor c) {
		if (c == null) { return; }
		// check number of columns
		if (!(c.getColumnCount() == this.getAllColumns().length)) { return; }
		// set value of instance
		this.setCurrencyId(c.getInt(c.getColumnIndex(Currency.CURRENCYID)));
		this.setCurrencyName(c.getString(c.getColumnIndex(Currency.CURRENCYNAME)));
		this.setPfxSymbol(c.getString(c.getColumnIndex(Currency.PFX_SYMBOL)));
		this.setSfxSymbol(c.getString(c.getColumnIndex(Currency.SFX_SYMBOL)));
		this.setDecimalPoint(c.getString(c.getColumnIndex(Currency.DECIMAL_POINT)));
		this.setGroupSeparator(c.getString(c.getColumnIndex(Currency.GROUP_SEPARATOR)));
		this.setUnitName(c.getString(c.getColumnIndex(Currency.UNIT_NAME)));
		this.setCentName(c.getString(c.getColumnIndex(Currency.CENT_NAME)));
		this.setScale(c.getDouble(c.getColumnIndex(Currency.SCALE)));
		this.setBaseConvRate(c.getDouble(c.getColumnIndex(Currency.BASECONVRATE)));
//		String test = c.getString(c.getColumnIndex(BASECONVRATE));
		this.setCurrencySymbol(c.getString(c.getColumnIndex(Currency.CURRENCY_SYMBOL)));
	}
}
