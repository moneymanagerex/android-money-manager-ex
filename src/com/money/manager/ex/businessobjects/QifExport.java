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
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Handles export of transactions from AllDataFragment into QIF format.
 */
public class QifExport {
    public  QifExport(Context context) {
        this.context = context;
    }

    private Context context;

    public void export() {
        // get data into qif structure

        // store into file?

        // initiate share
        Uri contentUri = generateContentUri();

        Log.d(this.getClass().getName(), contentUri.toString());
    }

    private Uri generateContentUri() {
        //File imagePath = new File(this.context.getFilesDir(), "export");
        //File file = new File(this.context.getExternalFilesDir(), "export");
        File filePath = new File(this.context.getCacheDir(), "export");
        // todo: generate file name
        File newFile = new File(filePath, "mmex.qif");
        Uri contentUri = FileProvider.getUriForFile(this.context,
                "com.money.manager.ex.fileprovider", newFile);

        return contentUri;
    }
}
