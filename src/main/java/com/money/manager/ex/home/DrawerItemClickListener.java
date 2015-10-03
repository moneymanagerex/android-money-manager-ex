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
package com.money.manager.ex.home;

import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Click listener for the linear drawer.
 * Created by Alen Siljak on 21/07/2015.
 */
public class DrawerItemClickListener
        implements ListView.OnItemClickListener {

    public DrawerItemClickListener(IDrawerItemClickListenerCallbacks host, LinearLayout drawerLayout,
                                   ListView drawerList, DrawerLayout drawer) {
        mHost = host;
        mDrawer = drawer;
        mDrawerLayout = drawerLayout;
        mDrawerList = drawerList;
    }

    IDrawerItemClickListenerCallbacks mHost;
    private LinearLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerLayout mDrawer;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mDrawer == null) return;
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        // You should reset item counter
        mDrawer.closeDrawer(mDrawerLayout);
        // check item selected
        final DrawerMenuItem item = ((DrawerMenuItemAdapter) mDrawerList.getAdapter()).getItem(position);
        if (item != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // execute operation
                    mHost.onDrawerMenuAndOptionMenuSelected(item);
                }
            }, 250);
        }
    }
}
