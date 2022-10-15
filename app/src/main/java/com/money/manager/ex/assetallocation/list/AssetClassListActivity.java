/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.common.events.ListItemClickedEvent;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.view.recycler.DividerItemDecoration;

import org.greenrobot.eventbus.Subscribe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Activity for selecting an Asset Class (as a parent, etc.)
 */
public class AssetClassListActivity
    extends MmxBaseFragmentActivity {

    public static int LOADER_ASSET_CLASSES = 1;
    public static String EXTRA_ASSET_CLASS_ID = "AssetClassId";

    private AssetClassListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_list);

        mAdapter = new AssetClassListAdapter(null);
        initRecyclerView(mAdapter);

        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = initLoaderCallbacks();

        // get target asset class id, to exclude from the offered list
        Bundle loaderArgs = null;
        Intent intent = getIntent();
        if (intent != null) {
            loaderArgs = new Bundle();
            int assetClassId = intent.getIntExtra(EXTRA_ASSET_CLASS_ID, Constants.NOT_SET);
            loaderArgs.putInt(EXTRA_ASSET_CLASS_ID, assetClassId);
        }
        // start loader
        Loader loader = getSupportLoaderManager().initLoader(LOADER_ASSET_CLASSES, loaderArgs, loaderCallbacks);
    }

    @Subscribe
    public void onEvent(ListItemClickedEvent event) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ASSET_CLASS_ID, event.id);

        setResult(AppCompatActivity.RESULT_OK, data);
        finish();
    }

    // Private

    private LoaderManager.LoaderCallbacks<Cursor> initLoaderCallbacks() {
        LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                // e id if there are multiple loaders?

                Context context = AssetClassListActivity.this;
                AssetClassRepository repo = new AssetClassRepository(context);

                // filter out the asset class that we are selecting the parent for.
                WhereStatementGenerator where = new WhereStatementGenerator();
                int assetClassId = args.getInt(EXTRA_ASSET_CLASS_ID, Constants.NOT_SET);
                if (assetClassId != Constants.NOT_SET) {
                    where.addStatement(AssetClass.ID, "<>", assetClassId);
                }

                // todo: add option None, to be able to move the asset class to the root.
                // todo Do not offer any children of the selected asset class!
                // todo Load only groups and empty asset classes, not those linked to any stocks!

                Select query = new Select(repo.getAllColumns())
                    .where(where.getWhere());

                return new MmxCursorLoader(context, repo.getUri(), query);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
        RecyclerView recycler = (RecyclerView) findViewById(R.id.list);
        if (recycler == null) return;

        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        // divider between items
        //recycler.addItemDecoration();
        recycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }
}
