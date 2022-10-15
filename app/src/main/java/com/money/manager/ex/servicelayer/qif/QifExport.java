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
package com.money.manager.ex.servicelayer.qif;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.core.file.TextFileExport;
import com.money.manager.ex.utils.MmxDate;

import timber.log.Timber;

/**
 * Handles export of transactions from AllDataListFragment into QIF format.
 * References:
 * http://en.wikipedia.org/wiki/Quicken_Interchange_Format
 */
public class QifExport
        extends TextFileExport {

    public QifExport(Context context) {
        super(context);

        mContext = context;
    }

    /**
     * Export the transactions into qif format and offer file for sharing.
     */
    public void export(AllDataAdapter adapter) {
        // just e errors here
        try {
            this.export_internal(adapter);
        } catch (Exception e) {
            Timber.e(e, ".qif export");
        }
    }

    // Private

    private void export_internal(AllDataAdapter adapter)
            throws Exception {
        String fileName = generateFileName();

        // get data into qif structure
        IQifGenerator generator = getQifGenerator();
        String content = generator.createFromAdapter(adapter);
        String title = getContext().getString(R.string.qif_export);

        boolean success = this.export(fileName, content, title);
    }

//    private void dumpContentIntoFile(String content, File file) {
//        // files created this way are located in private files, not cache!
//        try {
//            FileOutputStream stream = this.context.openFileOutput(
//                    file.getName(), Context.MODE_PRIVATE);
//            // use Context.MODE_PRIVATE for private-only files. Context.MODE_APPEND
//
//            stream.write(content.getBytes());
//            stream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private boolean dumpContentIntoFile(String content, File file) {
//        boolean result;
//
//        try {
//            result = file.createNewFile();
//            if(!result) {
//                throw new Exception("Error creating file!");
//            }
//
//            FileWriter writer = new FileWriter(file);
//            writer.write(content);
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            result = false;
//        }
//
//        return result;
//    }

    private String generateFileName() {
        // use just the date for now?
        String format = "yyyy-MM-dd_HHmmss";
        String result = new MmxDate().toString(format);

        // append file extension.
        result += ".qif";

        return result;
    }

    /**
     * factory method for Qif generator.
     * @return implementation of Qif generator interface.
     */
    private IQifGenerator getQifGenerator() {
        return new QifGenerator(this.mContext);
    }
}
