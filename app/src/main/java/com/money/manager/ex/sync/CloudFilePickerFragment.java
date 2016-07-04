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

package com.money.manager.ex.sync;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.R;
import com.money.manager.ex.common.events.ListItemClickedEvent;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.sync.events.RemoteFolderContentsRetrievedEvent;
import com.money.manager.ex.view.recycler.DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CloudFilePickerFragment
    extends Fragment {

    public CloudFilePickerFragment() {
        // Required empty public constructor
    }

    private SyncManager syncManager;
    private RecyclerView mRecyclerView;
    private CloudDataAdapter mAdapter;
    private MaterialDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cloud_file_picker, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Separator
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        // get data
        getFolderContents("/");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(final RemoteFolderContentsRetrievedEvent event) {
        event.items = sortRemoteItems(event.items);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // hide the progress dialog
                progressDialog.hide();

                // fill the list
                mAdapter = new CloudDataAdapter(getActivity(), event.items);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @Subscribe
    public void onEvent(ListItemClickedEvent event) {
        // get item
        CloudMetaData item = mAdapter.mData.get(event.id);
        if (item.getFolder()) {
            // open folder
            String folder = item.getPath();
            getFolderContents(folder);
        } else {
            // check if the file is a valid database?
            if (!isValidDatabase(item)) {
                // show notification
                Core.alertDialog(getActivity(), R.string.invalid_database);
            }

            // select file.
            Intent data = new Intent();
            data.putExtra(SyncPreferenceFragment.EXTRA_REMOTE_FILE, item.getPath());

            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }

    private void getFolderContents(String folder) {
        // show progress bar
        progressDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.loading)
                .content(R.string.please_wait)
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();

        getSyncManager().getRemoteFolderContentsAsync(folder);
    }

    private boolean isValidDatabase(CloudMetaData item) {
        return item.getName().endsWith(".mmb");
    }

    private SyncManager getSyncManager() {
        if (syncManager == null) {
            syncManager = new SyncManager(getActivity());
        }
        return syncManager;
    }

    private List<CloudMetaData> sortRemoteItems(List<CloudMetaData> items) {
        // sort the retrieved items: folders first, order by name.
        Comparator<CloudMetaData> nameComparator = new Comparator<CloudMetaData>() {
            @Override
            public int compare(CloudMetaData lhs, CloudMetaData rhs) {
                // order by name
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        };
        Collections.sort(items, nameComparator);
        Comparator<CloudMetaData> folderComparator = new Comparator<CloudMetaData>() {
            @Override
            public int compare(CloudMetaData lhs, CloudMetaData rhs) {
                // folders before files
                if (lhs.getFolder() && !rhs.getFolder()) return -1;
                //if (lhs.getFolder() && rhs.getFolder()) return 0;
                if (!lhs.getFolder() && rhs.getFolder()) return 1;

                return 0;
            }
        };
        Collections.sort(items, folderComparator);

        return items;
    }
}
