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

package com.money.manager.ex.utils;

import android.app.ProgressDialog;
import android.util.Log;

import com.money.manager.ex.core.ExceptionHandler;

/**
 * Common dialog utility functions.
 */
public class DialogUtils {
    public static void closeProgressDialog(ProgressDialog progressDialog) {
        try {
            progressDialog.hide();
            progressDialog.dismiss();
        } catch (Exception ex) {
            //ExceptionHandler handler = new ExceptionHandler(getapp)
//            Log.e("Dialog Utils", )
            ex.printStackTrace();
        }
    }

}
