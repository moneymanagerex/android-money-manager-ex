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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.moneymanagerex.android.testhelpers;

import android.app.Activity;

import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

/**
 * Additionally simplify and standardize certain calls to assist when setting up and running
 * the unit tests.
 *
 * Created by Alen Siljak on 25/09/2015.
 */
public class UnitTestHelper {
    public static <T extends Activity> ActivityController<T> getController(Class<T> activityClass) {
        return Robolectric.buildActivity(activityClass);
    }

    public static <T extends Activity> T getActivity(ActivityController<T> controller) {
        return controller.create().visible().start().get();
    }

//    public static <T extends Activity> T create(Class<T> activityClass) {
//        // standard set of calls until the activity is displayed.
//        return Robolectric.buildActivity(activityClass)
//                .create().visible().start().get();
//
//        // suggested:
//        //                .attach().create().visible().start().resume().get();
//
//        // available methods:
//        // .create().start().resume().visible() - .pause().stop().destroy()
//
//    }
}
