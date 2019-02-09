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

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Surface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public class ActivityUtils {

    /**
     * Returns current device orientation.
     * @param activity Activity from which to get the current orientation information.
     * @return Code indicating the current device orientation.
     */
    public static int forceCurrentOrientation(FragmentActivity activity) {
        int prevOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        if (activity != null) {
            prevOrientation = activity.getRequestedOrientation(); // update current position

            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_0 ||
                    activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_90) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            } else if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }
        }
        return prevOrientation;
    }

    /**
     * Sets the device orientation for the activity.
     * @param activity Activity to which to apply the orientation.
     * @param orientation Code for orientation.
     */
    public static void restoreOrientation(AppCompatActivity activity, int orientation) {
        if (activity != null) {
            activity.setRequestedOrientation(orientation);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}
