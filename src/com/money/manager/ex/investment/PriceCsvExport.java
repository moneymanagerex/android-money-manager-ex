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
import android.util.Log;
import android.widget.ListAdapter;

import com.money.manager.ex.businessobjects.StockRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Export of security prices to CSV file.
 */
public class PriceCsvExport {
    public PriceCsvExport(Context context) {
        mContext = context;
    }

    private final String LOGCAT = this.getClass().getSimpleName();

    private Context mContext;

    /**
     * Gets the data from adapter and packs it into the CSV format.
     *
     * The price date is set to today until the price history is used.
     * @param adapter Adapter containing the data records (in the visible list, for example)
     * @param filePrefix Prefix for the exported file name.
     */
    public void exportPrices(ListAdapter adapter, String filePrefix)
            throws IOException {
        int itemCount = adapter.getCount();
        CSVWriter writer = new CSVWriter(new FileWriter(getTempFile(filePrefix)));

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) adapter.getItem(i);

            String symbol = cursor.getString(cursor.getColumnIndex(StockRepository.SYMBOL));
            String date = cursor.getString(cursor.getColumnIndex(StockRepository.PURCHASEDATE));
            String price = cursor.getString(cursor.getColumnIndex(StockRepository.CURRENTPRICE));

            Log.d(LOGCAT, symbol);
        }

    }

    private File getTempFile(String prefix) {

    }

}
