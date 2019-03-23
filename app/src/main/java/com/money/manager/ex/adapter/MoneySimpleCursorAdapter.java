/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.adapter;

import android.content.Context;
import android.database.Cursor;

import android.text.TextUtils;
import android.widget.TextView;

import com.money.manager.ex.core.Core;
import com.money.manager.ex.view.RobotoView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import timber.log.Timber;

/**
 */
public class MoneySimpleCursorAdapter
        extends SimpleCursorAdapter {

    //private static final String LOGCAT = MoneySimpleCursorAdapter.class.getSimpleName();
    private String mHighlight;
    private Core mCore;
    private Context mContext;

    public MoneySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        mCore = new Core(context);
        mContext = context;
    }

    @Override
    public void setViewText(TextView v, String text) {
        if (v != null) {
            try {
                v.setTypeface(RobotoView.obtainTypeface(mContext, RobotoView.getUserFont()));
            } catch (Exception e) {
                Timber.e(e, "getting roboto typeface");
            }
            super.setViewText(v, text);
            // check if highlight text
            if (!TextUtils.isEmpty(getHighlightFilter())) {
                v.setText(mCore.highlight(getHighlightFilter(), v.getText().toString()));
            }
        }
    }

    public String getHighlightFilter() {
        return mHighlight;
    }

    public void setHighlightFilter(String mHighlight) {
        this.mHighlight = mHighlight;
    }
}
