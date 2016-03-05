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
package com.money.manager.ex.database;

import android.database.Cursor;

import com.money.manager.ex.domainmodel.Payee;

public class TablePayee
		extends Dataset {

    public TablePayee() {
        super("payee_v1", DatasetType.TABLE, "payee");
    }

	// Fields
	private int payeeId;
	private String payeeName;
	private int categId;
	private int subCategId;

    @Override
	public String[] getAllColumns() {
		return new String[] { "PAYEEID AS _id", Payee.PAYEEID, Payee.PAYEENAME, Payee.CATEGID,
				Payee.SUBCATEGID };
	}

	@Override
	public void setValueFromCursor(Cursor c) {
		if (c == null) return;

		this.setPayeeId(c.getInt(c.getColumnIndex(Payee.PAYEEID)));
		this.setPayeeName(c.getString(c.getColumnIndex(Payee.PAYEENAME)));
		this.setCategId(c.getInt(c.getColumnIndex(Payee.CATEGID)));
		this.setSubCategId(c.getInt(c.getColumnIndex(Payee.SUBCATEGID)));
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
