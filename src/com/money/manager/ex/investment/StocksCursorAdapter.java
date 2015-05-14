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
package com.money.manager.ex.investment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 *
 */
public class StocksCursorAdapter extends CursorAdapter {

    public StocksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, -1);

    }

    private SQLiteDatabase mDatabase;
    private HashMap<Integer, Integer> mHeadersAccountIndex;
    private SparseBooleanArray mCheckedPosition;
    private int mAccountId = -1;

    private boolean mShowAccountName = false;
    private boolean mShowBalanceAmount = false;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    public void clearPositionChecked() {
        mCheckedPosition.clear();
    }

    public void setDatabase(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public void resetAccountHeaderIndexes() {
        mHeadersAccountIndex.clear();
    }

    public void setAccountId(int mAccountId) {
        this.mAccountId = mAccountId;
    }

    public void setPositionChecked(int position, boolean checked) {
        mCheckedPosition.put(position, checked);
    }

    public void setShowAccountName(boolean showAccountName) {
        this.mShowAccountName = showAccountName;
    }

    public void setShowBalanceAmount(boolean mShowBalanceAmount) {
        this.mShowBalanceAmount = mShowBalanceAmount;
    }

}
