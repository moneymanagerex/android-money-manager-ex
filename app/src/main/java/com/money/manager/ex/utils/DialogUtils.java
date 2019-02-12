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

package com.money.manager.ex.utils;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.ProgressBar;

import timber.log.Timber;

/**
 * Common binaryDialog utility functions.
 */
public class DialogUtils {
    public static void closeProgressDialog(ProgressDialog progressDialog) {
        try {
            progressDialog.hide();
            progressDialog.dismiss();
        } catch (Exception ex) {
            Timber.e("error closing a binaryDialog");
        }
    }

    public static void closeProgressBar(ProgressBar progressBar) {
        try {
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            Timber.e("error closing progress bar");
        }
    }
}
