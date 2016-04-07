/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.settings.AppSettings;

import net.sqlcipher.database.SQLiteDatabase;

public abstract class BaseFragmentActivity
    extends AppCompatActivity {

    private boolean mDisplayHomeAsUpEnabled = false;
    private Toolbar mToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // check if Toolbar define into layout
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        setTheme();

        AppSettings settings = new AppSettings(this);
        String locale = settings.getGeneralSettings().getApplicationLanguage();
        Core.setAppLocale(this, locale);

        // Initialize database encryption.
        SQLiteDatabase.loadLibs(this);

        super.onCreate(savedInstance);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // This is used to handle the <- Home arrow button in the toolbar (i.e. settings screens).
        
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDisplayHomeAsUpEnabled) {
                    finish();
                    return true;
                }
//            case R.id.menu_cancel:
//                if (isDialogMode()) {
//                    onActionCancelClick();
//                    return true;
//                }
//            case R.id.menu_done:
//                if (isDialogMode()) {
//                    onActionDoneClick();
//                    return true;
//                }
//
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

    public void setToolbarStandardAction(Toolbar toolbar) {
        setToolbarStandardAction(toolbar, R.id.action_cancel, R.id.action_done);
    }

    public void setToolbarStandardAction(Toolbar toolbar, int actionCancel, int actionDone) {
        if (toolbar != null) {
            View cancelActionView = toolbar.findViewById(actionCancel);
            if (cancelActionView != null)
                cancelActionView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onActionCancelClick();
                    }
                });
            View doneActionView = toolbar.findViewById(actionDone);
            if (doneActionView != null)
                doneActionView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onActionDoneClick();
                    }
                });
        }
    }

    public boolean onActionCancelClick() {
        return true;
    }

    public boolean onActionDoneClick() {
        return true;
    }

    public void setDisplayHomeAsUpEnabled(boolean mDisplayHomeAsUpEnabled) {
        this.mDisplayHomeAsUpEnabled = mDisplayHomeAsUpEnabled;
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
    }

    protected Toolbar getToolbar() {
        return mToolbar;
    }

    protected void setTheme() {
        try {
            Core core = new Core(this);
            this.setTheme(core.getThemeId());
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(this, this);
            handler.handle(e, "setting theme");
        }
    }
}
