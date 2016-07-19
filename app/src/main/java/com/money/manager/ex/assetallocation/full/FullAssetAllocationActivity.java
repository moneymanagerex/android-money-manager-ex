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

package com.money.manager.ex.assetallocation.full;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetAllocationActivity;
import com.money.manager.ex.assetallocation.AssetAllocationOverviewActivity;
import com.money.manager.ex.assetallocation.AssetClassEditActivity;
import com.money.manager.ex.assetallocation.events.AssetAllocationItemLongPressedEvent;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;

public class FullAssetAllocationActivity
    extends AppCompatActivity {

    private FormatUtilities formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Theme
        Core core = new Core(this);
        this.setTheme(core.getThemeId());

        setContentView(R.layout.activity_full_asset_allocation);

        // Toolbar
        setUpToolbar();

        // Floating action button.
//        setUpFloatingButton();

        // List

        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass assetAllocation = service.loadAssetAllocation();

        List<AssetClassViewModel> model = createViewModel(assetAllocation);

        Money threshold = new AppSettings(this).getInvestmentSettings().getAssetAllocationDifferenceThreshold();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        FullAssetAllocationAdapter adapter = new FullAssetAllocationAdapter(model, threshold);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showTotal(assetAllocation);
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

    @Override
    public void onResume() {
        super.onResume();

        // todo reload asset allocation?
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_asset_allocation, menu);

        // customize icons

        // Currencies
        MenuItem currenciesMenu = menu.findItem(R.id.menu_currencies);
        if (currenciesMenu != null) {
            FontIconDrawable icon = FontIconDrawable.inflate(this, R.xml.ic_euro);
            icon.setTextColor(UIHelper.getColor(this, R.attr.toolbarItemColor));
            currenciesMenu.setIcon(icon);
        }

        // Overview
//        MenuItem overview = menu.findItem(R.id.menu_asset_allocation_overview);
//        FontIconDrawable icon = FontIconDrawable.inflate(this, R.xml.ic_report_page);
//        icon.setTextColor(UIHelper.getColor(this, R.attr.toolbarItemColor));
//        overview.setIcon(icon);

        // New Asset Allocation view
        MenuItem newForm = menu.findItem(R.id.menu_new_asset_allocation);
        FontIconDrawable icon = FontIconDrawable.inflate(this, R.xml.ic_pie_chart);
        icon.setTextColor(UIHelper.getColor(this, R.attr.toolbarItemColor));
        newForm.setIcon(icon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_currencies:
                // open the Currencies activity.
                intent = new Intent(this, CurrencyListActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
                break;

//            case R.id.menu_asset_allocation_overview:
//                // show the overview
//                intent = new Intent(this, AssetAllocationOverviewActivity.class);
//                startActivity(intent);
//                break;

            case R.id.menu_new_asset_allocation:
                intent = new Intent(this, AssetAllocationActivity.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /*
        Events
     */

    @Subscribe
    public void onEvent(AssetAllocationItemLongPressedEvent event) {
        // show context menu.
        // todo openContextMenu();
        if (BuildConfig.DEBUG) Log.d("test", "show the context menu here");
    }

    /*
     * Private
     */

    private List<AssetClassViewModel> createViewModel(AssetClass assetAllocation) {
        if (assetAllocation == null) {
            // get asset allocation
            AssetAllocationService service = new AssetAllocationService(this);
            assetAllocation = service.loadAssetAllocation();
        }

        // linearize for display
        List<AssetClassViewModel> modelList = new ArrayList<>();
        for (AssetClass child : assetAllocation.getChildren()) {
            addModelToList(child, modelList, 0);
        }

        return modelList;
    }

    private void addModelToList(AssetClass assetClass, List<AssetClassViewModel> modelList, int level) {
        // add the asset class first.
        AssetClassViewModel model = new AssetClassViewModel(assetClass, level);
        modelList.add(model);

        List<AssetClass> children = assetClass.getChildren();
        if (children.size() == 0) return;

        // then add the children.
        for (AssetClass child : children) {
            addModelToList(child, modelList, level + 1);
        }
    }

    private FormatUtilities getFormatter() {
        if (this.formatter == null) {
            formatter = new FormatUtilities(this);
        }
        return this.formatter;
    }

    private void setUpFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new asset allocation.
                Intent intent = new Intent(FullAssetAllocationActivity.this, AssetClassEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
//                intent.putExtra(AssetClassEditActivity.KEY_PARENT_ID, this.getAssetClassId());
                startActivity(intent);
            }
        });

        ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        if (scrollView != null) {
            fab.attachToScrollView(scrollView);
        }

        fab.setVisibility(View.VISIBLE);
    }

    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

//        actionBar.setSubtitle(R.string.asset_allocation);
        actionBar.setTitle(R.string.asset_allocation);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Title.
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.asset_allocation));
    }

    private void showTotal(AssetClass assetAllocation) {
        RobotoTextView totalView = (RobotoTextView) findViewById(R.id.totalAmountTextView);
        if (totalView == null) return;

        totalView.setText(getFormatter().getValueFormattedInBaseCurrency(assetAllocation.getValue()));
    }
}
