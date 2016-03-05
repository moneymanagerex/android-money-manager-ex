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
package com.money.manager.ex.dropbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.RESTUtility;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Dropbox file browser fragment.
 * Created by Alen Siljak on 14/09/2015.
 */
public class DropboxBrowserFragment
        extends BaseListFragment {

    private final String LOGCAT = this.getClass().getSimpleName();

    DropboxHelper mHelper;
    DropboxEntryAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // take a instance
        mHelper = DropboxHelper.getInstance(getActivity());
        mAdapter = new DropboxEntryAdapter(getActivity(), R.layout.simple_list_item_multiple_choice_2);
        mAdapter.setListFragment(this);
        // set adapter
        setListAdapter(mAdapter);

        setEmptyText(getString(R.string.dropbox_empty_folder));

        registerForContextMenu(getListView());

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListShown(false);

        //check item
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView)view.findViewById(android.R.id.text1);
                checkedTextView.toggle();
                getListView().setItemChecked(position, checkedTextView.isChecked());
                ((DropboxEntryAdapter)getListAdapter()).setNotifyOnChange(true);
            }
        });

        //set option menu
        setHasOptionsMenu(true);

        //take a entries
        if (mAdapter.getCount() <= 0)
            refreshEntries();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) setResultAndFinish();
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dropbox_browser_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResultAndFinish();
                return true;
            case R.id.menu_refresh:
                refreshEntries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    public void setResultAndFinish() {
        Intent result = new Intent();
        if (getListView().getCheckedItemPosition() == ListView.INVALID_POSITION) {
            result.putExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH, (String)null);
        } else {
            // set result and exit
            int position = getListView().getCheckedItemPosition();
            int itemCount = mAdapter.getCount();
            // sometimes the list here has size 0 and the position is out of bounds.
            if(position > itemCount) {
                Log.e(LOGCAT, "Position is larger than the number if items in the list!");
                return;
            }

            result.putExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH, mAdapter.getItem(position).path);
        }
        getActivity().setResult(Activity.RESULT_OK, result);
        // exit
        getActivity().finish();
    }

    private void refreshEntries() {
        mHelper.getEntries(new DropboxHelper.OnGetEntries() {

            public void onStarting() {
                setListShown(false);
                mAdapter.clear();
                mAdapter.setNotifyOnChange(true);
            }

            public void onFinished(List<DropboxAPI.Entry> result) {
                if (isVisible()) {
                    if (result != null) {
                        for(int i = 0; i < result.size(); i ++) {
                            if (result.get(i).path.toLowerCase().endsWith(".mmb")) {
                                mAdapter.add(result.get(i));
                                //check if file is same pass from intent
                                if (getListView().getCheckedItemPosition() == ListView.INVALID_POSITION &&
                                        result.get(i).path.equals(DropboxBrowserActivity.dropboxFile)) {
                                    getListView().setItemChecked(mAdapter.getCount() - 1, true);
                                }
                            }
                        }
                    }
                    mAdapter.setNotifyOnChange(true);
                    setListShown(true);
                }
            }
        });
    }
}
