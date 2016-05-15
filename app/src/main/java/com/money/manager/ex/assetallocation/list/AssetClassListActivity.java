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

package com.money.manager.ex.assetallocation.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.datalayer.AssetClassRepository;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class AssetClassListActivity
    extends BaseFragmentActivity {

    public static int LOADER_ASSET_CLASSES = 1;
    public static String EXTRA_ASSET_CLASS_ID = "AssetClassId";

    private AssetClassListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_list);

        mAdapter = new AssetClassListAdapter(null);
        initRecyclerView(mAdapter);

        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = initLoader();

        // start loader
        Loader loader = getSupportLoaderManager().initLoader(LOADER_ASSET_CLASSES, null, loaderCallbacks);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe
    public void onEvent(ListItemClickedEvent event) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ASSET_CLASS_ID, event.id);

        setResult(Activity.RESULT_OK, data);
        finish();
    }

    // Private

    private LoaderManager.LoaderCallbacks<Cursor> initLoader() {
        LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                // handle id if there are multiple loaders
                Context context = AssetClassListActivity.this;
                AssetClassRepository repo = new AssetClassRepository(context);
                // todo filter out the asset class that we are selecting the parent for.
                return new MmexCursorLoader(context, repo.getUri(), repo.getAllColumns(),
                        "",
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//                if (mAdapter == null) {
//                    mAdapter = new AssetClassListAdapter(data);
//                    initRecyclerView(mAdapter);
//                }

                mAdapter.changeCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.changeCursor(null);
            }
        };

        return callbacks;
    }

    private void initRecyclerView(AssetClassListAdapter adapter) {
        RecyclerView recycler = (RecyclerView) findViewById(R.id.assetClassListRecyclerView);
        if (recycler == null) return;

        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        // divider between items
        //recycler.addItemDecoration();
        //recycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

    }

    // todo load only groups and empty asset classes (not linked to any stocks)

}
