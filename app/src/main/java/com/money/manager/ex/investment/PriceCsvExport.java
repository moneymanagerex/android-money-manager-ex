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
package com.money.manager.ex.investment;

import android.content.Context;
import android.database.Cursor;
import android.widget.ListAdapter;

import com.money.manager.ex.R;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.core.file.TextFileExport;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Export of security prices to CSV file.
 */
public class PriceCsvExport
    extends TextFileExport {

    public PriceCsvExport(Context context) {
        super(context);

        mContext = context;
    }

    private Context mContext;

    /**
     * Gets the data from adapter and packs it into the CSV format.
     *
     * The price date is set to today until the price history is used.
     * @param adapter Adapter containing the data records (in the visible list, for example).
     * @param filePrefix Prefix for the exported file name (generally account name).
     */
    public boolean exportPrices(ListAdapter adapter, String filePrefix)
            throws IOException {

        boolean result = false;
        String content = this.getContent(adapter);
        String filename = generateFileName(filePrefix);
        String title = getContext().getString(R.string.export_data_to_csv);

        try {
            result = this.export(filename, content, title);
        } catch (IOException ex) {
            Timber.e(ex, "exporting prices");
        }

        return result;
    }

    private String getContent(ListAdapter adapter) {
        final String lineSeparator = System.getProperty("line.separator");

        StringBuilder builder = new StringBuilder();
        char separator = ',';
        StockHistoryRepository historyRepository = new StockHistoryRepository(mContext);

        int itemCount = adapter.getCount();
        Stock stock = new Stock();

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) adapter.getItem(i);
            stock.loadFromCursor(cursor);

            // use the latest price date here.
            StockHistory latestPrice = historyRepository.getLatestPriceFor(stock.getSymbol());
            if (latestPrice == null) continue;

            Date date = latestPrice.getDate();
            if (date == null) {
                date = new MmxDate().toDate();
            }
            // format date
            String csvDate = getDateInCsvFormat(date);


            // code
            builder.append(stock.getSymbol());
            builder.append(separator);
            // price
            builder.append(stock.getCurrentPrice());
            builder.append(separator);
            // date
            builder.append(csvDate);
            builder.append(lineSeparator);
        }

        return builder.toString();
    }

    public String getDateInCsvFormat(Date date) {
        // todo: make this configurable.
        String csvFormat = "dd/MM/yyyy";
//        SimpleDateFormat sdf = new SimpleDateFormat(csvFormat, Locale.US);
//        String result = sdf.format(date);

        String result = new MmxDateTimeUtils().getDateStringFrom(date, csvFormat);

        // append quotes
        result = "\"" + result + "\"";

        return result;
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
