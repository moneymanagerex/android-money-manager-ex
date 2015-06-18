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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ListAdapter;

import com.money.manager.ex.Constants;
import com.money.manager.ex.businessobjects.StockHistory;
import com.money.manager.ex.businessobjects.StockHistoryRepository;
import com.money.manager.ex.businessobjects.StockRepository;
import com.money.manager.ex.core.file.TextFileExport;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Export of security prices to CSV file.
 */
public class PriceCsvExport
        extends TextFileExport {
    public PriceCsvExport(Context context) {
        super(context);

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
    public boolean exportPrices(ListAdapter adapter, String filePrefix)
            throws IOException {
        boolean result = false;
        String content = this.getContent(adapter);
        String filename = generateFileName(filePrefix);

        try {
            result = this.export(filename, content);
        } catch (IOException ioex) {
            Log.e(LOGCAT, "Error exporting prices: " + ioex.getMessage());
        }

        return result;
    }

    private String getTodayAsString() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        Date now = new Date();
        String result = sdf.format(now);
        return result;
    }

    private String getContent(ListAdapter adapter) {
        StringBuilder builder = new StringBuilder();
        char separator = ',';
        StockHistoryRepository historyRepository = new StockHistoryRepository(mContext.getApplicationContext());

        int itemCount = adapter.getCount();

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) adapter.getItem(i);

            // symbol.
            String symbol = cursor.getString(cursor.getColumnIndex(StockRepository.SYMBOL));
            // use the latest price date here.
            String date;
            ContentValues latestPrice = historyRepository.getLatestPriceFor(symbol);
            if (latestPrice == null) continue;

            if(latestPrice.containsKey(StockHistory.DATE)) {
                date = (String) latestPrice.get(StockHistory.DATE);
            } else {
                date = getTodayAsString();
            }
            // format date
            String csvDate = getDateInCsvFormat(date);
            // price.
            String price = cursor.getString(cursor.getColumnIndex(StockRepository.CURRENTPRICE));

            // code
            builder.append(symbol);
            builder.append(separator);
            // price
            builder.append(price);
            builder.append(separator);
            // date
            builder.append(csvDate);
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /**
     * Convert between different date formats.
     * @param listDate The string of date as stored in the database.
     * @return The string of date the way it is to be stored in the CSV file.
     */
    private String getDateInCsvFormat(String listDate) {
        SimpleDateFormat listFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        Date priceDate;
        try {
            priceDate = listFormat.parse(listDate);
        } catch (ParseException pex) {
            Log.e(LOGCAT, "Error converting list date: " + pex.getMessage());
            return "error";
        }

        // now convert this date into the CSV format date.
        String result = getDateInCsvFormat(priceDate);
        return result;
    }

    public String getDateInCsvFormat(Date date) {
        // todo: make this configurable.
        String csvFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(csvFormat, Locale.US);
        String result = sdf.format(date);

        // append quotes
        result = "\"" + result + "\"";

        return  result;
    }

    private String generateFileName(String filePrefix) {
        StringBuilder fileName = new StringBuilder(filePrefix);
        fileName.append('_');

        // get the date string.
        Date today = new Date();
        String format = "yyyy-MM-dd_HHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        fileName.append(sdf.format(today));

        // append file extension.
        fileName.append(".csv");

        return fileName.toString();
    }

}
