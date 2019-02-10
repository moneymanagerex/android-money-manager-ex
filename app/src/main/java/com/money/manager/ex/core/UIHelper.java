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

package com.money.manager.ex.core;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import dagger.Lazy;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Various methods that assist with the UI Android requirements.
 */
public class UIHelper {

    /**
     * Extracts the path to the selected database file.
     * @param data Intent
     * @return Path to the selected file.
     */
    public static String getSelectedFile(Intent data) {
        if (data == null) return null;

        String filePath = data.getData().getPath();

        // check if the db file is valid
        if (!MmxDatabaseUtils.isValidDbFile(filePath)) return null;

        return filePath;
    }

    /*
        Instance
     */

    public UIHelper(Context context) {
        this.context = context;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject Lazy<AppSettings> appSettingsLazy;
    private Context context;

    public Context getContext() {
        return this.context;
    }

    public Observable<Boolean> binaryDialog(final int title, final int message) {
        return binaryDialog(title, message, android.R.string.ok, android.R.string.cancel);
    }

    public Observable<Boolean> binaryDialog(final int title, final int message,
                                            final int positiveTextId, final int negativeTextId) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title(title)
                        .content(message)
                        .positiveText(positiveTextId)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            }
                        })
                        .negativeText(negativeTextId)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                subscriber.onNext(false);
                                subscriber.onCompleted();
                            }
                        })
                        .build();

                // cleaning up
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        dialog.dismiss();
                    }
                }));

                // show the dialog
                dialog.show();
            }
        });
    }

    public int getColor(int colorId) {
        return ContextCompat.getColor(getContext(), colorId);
    }

    public int getDimenInDp(int dimenId) {
        int sizeInDp = (int) (getContext().getResources().getDimension(dimenId)
            / getContext().getResources().getDisplayMetrics().density);
        return sizeInDp;
    }

    /**
     * Creates an icon with default settings. The default color is the toolbar item color.
     * @param icon Icon to instantiate.
     * @return Drawable (Iconics drawable).
     */
    public IconicsDrawable getIcon(IIcon icon) {
        return new IconicsDrawable(getContext())
                .icon(icon)
                .color(getToolbarItemColor())
                .sizeDp(this.getToolbarIconSize());
    }

    public int getPrimaryTextColor() {
        return isUsingDarkTheme()
            ? ContextCompat.getColor(getContext(), android.R.color.primary_text_dark)
            : ContextCompat.getColor(getContext(), android.R.color.primary_text_light);
    }

    public int getSecondaryTextColor() {
        return isUsingDarkTheme()
                ? ContextCompat.getColor(getContext(), android.R.color.secondary_text_dark)
                : ContextCompat.getColor(getContext(), android.R.color.secondary_text_light);
    }

    public int getToolbarItemColor() {
        return ContextCompat.getColor(getContext(), R.color.material_white);
    }

    public int getToolbarIconSize() {
        return getDimenInDp(R.dimen.mmx_icon_size);
    }

    /**
     * Return application theme choice from user
     * @return application theme id
     */
    public int getThemeId() {
        try {
            String darkTheme = Constants.THEME_DARK;
            String currentTheme = appSettingsLazy.get().getGeneralSettings().getTheme();

            if (currentTheme.endsWith(darkTheme)) {
                // Dark theme
                return R.style.Theme_Money_Manager_Dark;
            } else {
                // Light theme
                return R.style.Theme_Money_Manager_Light;
            }
        } catch (Exception e) {
            Timber.e(e, "getting theme setting");

            return R.style.Theme_Money_Manager_Light;
        }
    }

    public boolean isUsingDarkTheme() {
        return getThemeId() == R.style.Theme_Money_Manager_Dark;
    }

    /**
     * Resolves the attribute into a resource id.
     * For example attr/color resolves into color.red, which is used to get the Color object.
     * @param attr id attribute
     * @return resource id for the given attribute.
     */
    public int resolveAttribute(int attr) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, tv, true))
            return tv.resourceId;
        else
            return Constants.NOT_SET;
    }

    public void showToast(int messageId) {
        showToast(messageId, Toast.LENGTH_SHORT);
    }

    public void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public void showToast(final int message, final int length) {
        Context context = getContext();
        if (!(context instanceof AppCompatActivity)) return;

        final AppCompatActivity parent = (AppCompatActivity) context;

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, length).show();
            }
        });
    }

    public void showToast(final String message, final int length) {
        Context context = getContext();
        if (!(context instanceof AppCompatActivity)) return;

        final AppCompatActivity parent = (AppCompatActivity) context;

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, length).show();
            }
        });
    }
}
