/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.content.Intent;

import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;

/**
 * Various helper methods for Intent creation and starting of activities.
 */
public class IntentUtils {

    public IntentUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public Intent getIntentForSearch(SearchParameters parameters) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, parameters);
        intent.setAction(Intent.ACTION_INSERT);
        //getContext().startActivity(intent);
        return intent;
    }

}
