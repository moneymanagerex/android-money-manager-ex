/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.money.manager.ex.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.preferences.PreferencesConstant;

public abstract class BaseFragmentActivity extends ActionBarActivity {
    private boolean mDialogMode = false;
    private boolean mDisplayHomeAsUpEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstance) {
        // set theme
        Core core = new Core(getApplicationContext());

        String locale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(PreferencesConstant.PREF_LOCALE), "");
        core.changeLocaleApp(locale);

        try {
            this.setTheme(core.getThemeApplication());
        } catch (Exception e) {
            Log.e(BaseFragmentActivity.class.getSimpleName(), e.getMessage());
        }
        super.onCreate(savedInstance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isDialogMode()) {
            Core core = new Core(getApplicationContext());
            if (core.isTablet() || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getMenuInflater().inflate(R.menu.menu_button_cancel_done, menu);
            } else {
                createActionBar();
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isDisplayHomeAsUpEnabled()) {
                    finish();
                    return true;
                }
            case R.id.menu_cancel:
                if (isDialogMode()) {
                    onActionCancelClick();
                    return true;
                }
            case R.id.menu_done:
                if (isDialogMode()) {
                    onActionDoneClick();
                    return true;
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // set elevation actionbar 0
        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);
    }

    @Deprecated
    public void createActionBar() {
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        View actionBarButtons = inflater.inflate(R.layout.actionbar_button_cancel_done, new LinearLayout(this), false);
        View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
        cancelActionView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onActionCancelClick();
            }
        });
        View doneActionView = actionBarButtons.findViewById(R.id.action_done);
        doneActionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionDoneClick();
            }
        });
        getSupportActionBar().setCustomView(actionBarButtons);
    }

    public void setToolbarStandardAction(Toolbar toolbar) {
        if (toolbar != null) {
            View cancelActionView = toolbar.findViewById(R.id.action_cancel);
            if (cancelActionView != null)
                cancelActionView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onActionCancelClick();
                }
            });
            View doneActionView = toolbar.findViewById(R.id.action_done);
            if (doneActionView != null)
                doneActionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onActionDoneClick();
                }
            });
        }
    }

    public void forceRotateScreenActivity() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public boolean onActionCancelClick() {
        return true;
    }

    public boolean onActionDoneClick() {
        return true;
    }

    public boolean isDialogMode() {
        return mDialogMode;
    }

    @Deprecated
    public void setDialogMode(boolean mDialogMode) {
        this.mDialogMode = mDialogMode;
    }

    public boolean isDisplayHomeAsUpEnabled() {
        return mDisplayHomeAsUpEnabled;
    }

    public void setDisplayHomeAsUpEnabled(boolean mDisplayHomeAsUpEnabled) {
        this.mDisplayHomeAsUpEnabled = mDisplayHomeAsUpEnabled;
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
    }
}
