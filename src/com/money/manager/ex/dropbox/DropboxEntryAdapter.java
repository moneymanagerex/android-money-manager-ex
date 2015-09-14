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

import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Adapter for the file list on Dropbox.
 * Created by Alen Siljak on 14/09/2015.
 */
public class DropboxEntryAdapter extends ArrayAdapter<DropboxAPI.Entry> {
    private int mLayoutId;
    private LayoutInflater mInflater;

    public DropboxEntryAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<DropboxAPI.Entry>());
    }

    public DropboxEntryAdapter(Context context, int resource, List<DropboxAPI.Entry> objects) {
        super(context, resource, objects);
        mLayoutId = resource;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            DropboxAPI.Entry entry = getItem(position);
            if (entry != null) {
                convertView = mInflater.inflate(mLayoutId, parent, false);
                CheckedTextView text1 = (CheckedTextView)convertView.findViewById(android.R.id.text1);
                TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
                text1.setText(entry.path);
                text2.setText(getContext().getString(R.string.last_modified) + ": " +
                        new SimpleDateFormat().format(RESTUtility.parseDate(entry.modified)));
            }
        }
        //checked item
        CheckedTextView text1 = (CheckedTextView)convertView.findViewById(android.R.id.text1);
        text1.setChecked(getListFragment().getListView().getCheckedItemPosition() == position);

        return convertView;
    }

    private BaseListFragment listFragment;

    public void setListFragment(BaseListFragment object) {
        this.listFragment = object;
    }

    public BaseListFragment getListFragment() {
        return this.listFragment;
    }
}
