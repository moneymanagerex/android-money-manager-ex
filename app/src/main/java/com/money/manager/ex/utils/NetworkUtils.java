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
package com.money.manager.ex.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Network utility functions
 */
public class NetworkUtils {

    public static boolean isOnline(Context context) {
        return new NetworkUtils(context).isOnline();
    }

    public NetworkUtils(Context context) {
        mContext = context;
    }

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    /**
     * Check if device has connection
     *
     * @return true if is online otherwise false
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
        // isConnectedOrConnecting
    }

    public boolean isOnWiFi() {
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // check connManager.getAllNetworks()

        // deprecated as of API 23.
//        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo == null) return false;  // no network
        if (!networkInfo.isConnected()) return false;

        return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;

    }
}
