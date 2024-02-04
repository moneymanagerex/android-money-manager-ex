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
import com.money.manager.ex.core.file.TextFileExport;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.StockHistory;
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

    private final Context mContext;

    public PriceCsvExport(final Context context) {
        super(context);

        mContext = context;
    }

    /**
     * Gets the data from adapter and packs it into the CSV format.
     * <p>
     * The price date is set to today until the price history is used.
     *
     * @param adapter    Adapter containing the data records (in the visible list, for example).
     * @param filePrefix Prefix for the exported file name (generally account name).
     */
    public boolean exportPrices(final ListAdapter adapter, final String filePrefix)
            throws IOException {

        boolean result = false;
        final String content = getContent(adapter);
        final String filename = generateFileName(filePrefix);
        final String title = getContext().getString(R.string.export_data_to_csv);

        try {
            result = export(filename, content, title);
        } catch (final IOException ex) {
            Timber.e(ex, "exporting prices");
        }

        return result;
    }

    private String getContent(final ListAdapter adapter) {
        final String lineSeparator = System.getProperty("line.separator");

        final StringBuilder builder = new StringBuilder();
        final char separator = ',';
        final StockHistoryRepository historyRepository = new StockHistoryRepository(mContext);

        final int itemCount = adapter.getCount();
        final Stock stock = new Stock();

        for (int i = 0; i < itemCount; i++) {
            final Cursor cursor = (Cursor) adapter.getItem(i);
            stock.loadFromCursor(cursor);

            // use the latest price date here.
            final StockHistory latestPrice = historyRepository.getLatestPriceFor(stock.getSymbol());
            if (null == latestPrice) continue;

            Date date = latestPrice.getDate();
            if (null == date) {
                date = new MmxDate().toDate();
            }
            // format date
            final String csvDate = getDateInCsvFormat(date);


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

    public String getDateInCsvFormat(final Date date) {
        // todo: make this configurable.
        final String csvFormat = "dd/MM/yyyy";
//        SimpleDateFormat sdf = new SimpleDateFormat(csvFormat, Locale.US);
//        String result = sdf.format(date);

        String result = new MmxDateTimeUtils().getDateStringFrom(date, csvFormat);

        // append quotes
        result = "\"" + result + "\"";

        return result;
    }

    private String generateFileName(final String filePrefix) {

        final Date today = new Date();
        final String format = "yyyy-MM-dd_HHmmss";
        final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

        final String fileName = filePrefix + '_' +

                // get the date string.
                //        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                sdf.format(today) +

                // append file extension.
                ".csv";

        return fileName;
    }

}
