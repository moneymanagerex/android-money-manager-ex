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

package com.money.manager.ex.dropbox;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.money.manager.ex.R;
import com.money.manager.ex.dropbox.DropboxHelper.OnGetEntries;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DropboxBrowserActivity
        extends BaseFragmentActivity {

    public static class DropboxBrowserFragment
            extends BaseListFragment {

        private final String LOGCAT = this.getClass().getSimpleName();

        //Define EntryAdapter
        private class EntryAdapter extends ArrayAdapter<Entry> {
            private int mLayoutId;
            private LayoutInflater mInflater;

            public EntryAdapter(Context context, int resource) {
                this(context, resource, new ArrayList<Entry>());
            }

            public EntryAdapter(Context context, int resource, List<Entry> objects) {
                super(context, resource, objects);
                mLayoutId = resource;
                mInflater = LayoutInflater.from(context);
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    Entry entry = getItem(position);
                    if (entry != null) {
                        convertView = mInflater.inflate(mLayoutId, parent, false);
                        CheckedTextView text1 = (CheckedTextView)convertView.findViewById(android.R.id.text1);
                        TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
                        text1.setText(entry.path);
                        text2.setText(getString(R.string.last_modified) + ": " + new SimpleDateFormat().format(RESTUtility.parseDate(entry.modified)));
                    }
                }
                //checked item
                CheckedTextView text1 = (CheckedTextView)convertView.findViewById(android.R.id.text1);
                text1.setChecked(getListView().getCheckedItemPosition() == position);

                return convertView;
            }

        }

        DropboxHelper mHelper;
        EntryAdapter mAdapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // take a instance
            mHelper = DropboxHelper.getInstance(getActivity());
            mAdapter = new EntryAdapter(getActivity(), R.layout.simple_list_item_multiple_choice_2);
            // set adapter
            setListAdapter(mAdapter);

            setEmptyText(getString(R.string.dropbox_empty_folder));

            registerForContextMenu(getListView());

            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            setListShown(false);

            //check item
            getListView().setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckedTextView checkedTextView = (CheckedTextView)view.findViewById(android.R.id.text1);
                    checkedTextView.toggle();
                    getListView().setItemChecked(position, checkedTextView.isChecked());
                    ((EntryAdapter)getListAdapter()).setNotifyOnChange(true);
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

        public void setResultAndFinish() {
            Intent result = new Intent();
            if (getListView().getCheckedItemPosition() == ListView.INVALID_POSITION) {
                result.putExtra(INTENT_DROBPOXFILE_PATH, (String)null);
            } else {
                // set result and exit
                int position = getListView().getCheckedItemPosition();
                int itemCount = mAdapter.getCount();
                // sometimes the list here has size 0 and the position is out of bounds.
                if(position > itemCount) {
                    Log.e(LOGCAT, "Position is larger than the number if items in the list!");
                    return;
                }

                result.putExtra(INTENT_DROBPOXFILE_PATH, mAdapter.getItem(position).path);
            }
            getActivity().setResult(RESULT_OK, result);
            // exit
            getActivity().finish();
        }

        private void refreshEntries() {
            mHelper.getEntries(new OnGetEntries() {

                public void onStarting() {
                    setListShown(false);
                    mAdapter.clear();
                    mAdapter.setNotifyOnChange(true);
                }

                public void onFinished(List<Entry> result) {
                    if (isVisible()) {
                        if (result != null) {
                            for(int i = 0; i < result.size(); i ++) {
                                if (result.get(i).path.toLowerCase().endsWith(".mmb")) {
                                    mAdapter.add(result.get(i));
                                    //check if file is same pass from intent
                                    if (getListView().getCheckedItemPosition() == ListView.INVALID_POSITION && result.get(i).path.equals(mDropboxFile)) {
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

        @Override
        public String getSubTitle() {
            return null;
        }
    }

    public static final String INTENT_DROBPOXFILE_PATH = "DropboxBrowserActivity:DropboxFile";
    private static String mDropboxFile = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fm = getSupportFragmentManager();
        // intent
        if (getIntent() != null && getIntent().getExtras().containsKey(INTENT_DROBPOXFILE_PATH)) {
            mDropboxFile = getIntent().getExtras().getString(INTENT_DROBPOXFILE_PATH);
        }
        // attach fragment to activity
        if (fm.findFragmentById(R.id.content) == null) {
            if (fm.findFragmentByTag(DropboxBrowserFragment.class.getSimpleName()) == null) {
                DropboxBrowserFragment fragment = new DropboxBrowserFragment();
                fm.beginTransaction().add(R.id.content, fragment, DropboxBrowserFragment.class.getSimpleName()).commit();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DropboxBrowserFragment fragment = (DropboxBrowserFragment)getSupportFragmentManager().findFragmentByTag(DropboxBrowserFragment.class.getSimpleName());
            if (fragment != null) {
                return fragment.onKeyUp(keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}