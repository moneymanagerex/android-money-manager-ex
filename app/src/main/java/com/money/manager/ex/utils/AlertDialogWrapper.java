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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Replacing material dialogs AlertDialogWrapper so that the changes to the dialog code is minimal
 */

public class AlertDialogWrapper {
    private final AlertDialog.Builder builder;

    public AlertDialogWrapper(Context context) {
        builder = new AlertDialog.Builder(context);
    }

    public AlertDialogWrapper setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }

    public AlertDialogWrapper setTitle(int resId) {
        builder.setTitle(resId);
        return this;
    }

    public AlertDialogWrapper setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }

    public AlertDialogWrapper setIcon(Drawable icon) {
        builder.setIcon(icon);
        return this;
    }

    public AlertDialogWrapper setMessage(int resId) {
        builder.setMessage(resId);
        return this;
    }

    public AlertDialogWrapper setNegativeButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.setNegativeButton(captionResId, callback);
        return this;
    }

    public AlertDialogWrapper setNeutralButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.setNeutralButton(captionResId, callback);
        return this;
    }

    public AlertDialogWrapper setPositiveButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.setPositiveButton(captionResId, callback);
        return this;
    }

    public AlertDialogWrapper setView(View view) {
        builder.setView(view);
        return this;
    }

    public AlertDialog create() {
        return builder.create();
    }
}

