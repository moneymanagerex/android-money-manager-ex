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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import androidx.annotation.NonNull;

/**
 * Replacing material dialogs AlertDialogWrapper so that the changes to the dialog code is minimal
 */

public class AlertDialogWrapper {
    public AlertDialogWrapper(Context context) {
        builder = new MaterialDialog.Builder(context);
    }

    private MaterialDialog.Builder builder;

    public AlertDialogWrapper setCancelable(boolean cancelable) {
        builder.cancelable(cancelable);
        return this;
    }

    public AlertDialogWrapper setTitle(int resId) {
        builder.title(resId);
        return this;
    }

    public AlertDialogWrapper setTitle(CharSequence title) {
        builder.title(title);
        return this;
    }

    public AlertDialogWrapper setIcon(Drawable icon) {
        builder.icon(icon);
        return this;
    }

    public AlertDialogWrapper setMessage(int resId) {
        builder.content(resId);
        return this;
    }

    public AlertDialogWrapper setNegativeButton(int captionResId, MaterialDialog.SingleButtonCallback callback) {
        builder.negativeText(captionResId);
        builder.onNegative(callback);
        return this;
    }

    public AlertDialogWrapper setNegativeButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.negativeText(captionResId);
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                callback.onClick(dialog, which.ordinal());
            }
        });
        return this;
    }

    public AlertDialogWrapper setNeutralButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.neutralText(captionResId);
        builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                callback.onClick(dialog, which.ordinal());
            }
        });
        return this;
    }

    public AlertDialogWrapper setPositiveButton(int captionResId) {
        builder.positiveText(captionResId);
        return this;
    }

    public AlertDialogWrapper setPositiveButton(int captionResId, MaterialDialog.SingleButtonCallback callback) {
        builder.positiveText(captionResId);
        builder.onPositive(callback);
        return this;
    }

    public AlertDialogWrapper setPositiveButton(int captionResId, final DialogInterface.OnClickListener callback) {
        builder.positiveText(captionResId);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                callback.onClick(dialog, which.ordinal());
            }
        });
        return this;
    }

    public AlertDialogWrapper onPositive(MaterialDialog.SingleButtonCallback callback) {
        builder.onPositive(callback);
        return this;
    }

    public MaterialDialog create() {
        return builder.build();
    }

    public AlertDialogWrapper setView(View view) {
        builder.customView(view, true);
        return this;
    }
}
