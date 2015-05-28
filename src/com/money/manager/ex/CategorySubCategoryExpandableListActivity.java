/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.adapter.CategoryExpandableListAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseExpandableListFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.CategorySubCategoryExpandableLoaderListFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class CategorySubCategoryExpandableListActivity
        extends BaseFragmentActivity {

    public static final String INTENT_RESULT_CATEGID = "CategorySubCategory:CategId";
    public static final String INTENT_RESULT_CATEGNAME = "CategorySubCategory:CategName";
    public static final String INTENT_RESULT_SUBCATEGID = "CategorySubCategory:SubCategId";
    public static final String INTENT_RESULT_SUBCATEGNAME = "CategorySubCategory:SubCategName";
    @SuppressWarnings("unused")

    public static final String FRAGMENTTAG = CategorySubCategoryExpandableListActivity.class.getSimpleName() + "_Fragment";
    private static final String LOGCAT = CategorySubCategoryExpandableListActivity.class.getSimpleName();

    CategorySubCategoryExpandableLoaderListFragment listFragment = new CategorySubCategoryExpandableLoaderListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_toolbar_activity);

        // enable home button into actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // get intent
        Intent intent = getIntent();

        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
            listFragment.mAction = intent.getAction();
        }

        // management fragment
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // set result and terminate activity
            CategorySubCategoryExpandableLoaderListFragment fragment =
                    (CategorySubCategoryExpandableLoaderListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.setResultAndFinish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

}
