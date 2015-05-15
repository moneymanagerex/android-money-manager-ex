/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.database;

import android.database.Cursor;

public class TablePayee
		extends Dataset {

    public TablePayee() {
        super("payee_v1", DatasetType.TABLE, "payee");
    }

	// definizione dei nomi dei campi
	public static final String PAYEEID = "PAYEEID";
	public static final String PAYEENAME = "PAYEENAME";
	public static final String CATEGID = "CATEGID";
	public static final String SUBCATEGID = "SUBCATEGID";
	// definizione dei campi
	private int payeeId;
	private String payeeName;
	private int categId;
	private int subCategId;

    @Override
	public String[] getAllColumns() {
		return new String[] { "PAYEEID AS _id", PAYEEID, PAYEENAME, CATEGID, SUBCATEGID };
	}

	@Override
	public void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// imposto i vari valori
		this.setPayeeId(c.getInt(c.getColumnIndex(PAYEEID)));
		this.setPayeeName(c.getString(c.getColumnIndex(PAYEENAME)));
		this.setCategId(c.getInt(c.getColumnIndex(CATEGID)));
		this.setSubCategId(c.getInt(c.getColumnIndex(SUBCATEGID)));
	}
	/**
	 * @return the payeeId
	 */
	public int getPayeeId() {
		return payeeId;
	}
	/**
	 * @param payeeId the payeeId to set
	 */
	public void setPayeeId(int payeeId) {
		this.payeeId = payeeId;
	}
	/**
	 * @return the payeeName
	 */
	public String getPayeeName() {
		return payeeName;
	}
	/**
	 * @param payeeName the payeeName to set
	 */
	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}
	/**
	 * @return the categId
	 */
	public int getCategId() {
		return categId;
	}
	/**
	 * @param categId the categId to set
	 */
	public void setCategId(int categId) {
		this.categId = categId;
	}
	/**
	 * @return the subCategId
	 */
	public int getSubCategId() {
		return subCategId;
	}
	/**
	 * @param subCategId the subCategId to set
	 */
	public void setSubCategId(int subCategId) {
		this.subCategId = subCategId;
	}
}
