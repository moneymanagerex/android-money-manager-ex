/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.assetallocation.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.assetallocation.editor.AssetAllocationEditorActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.view.RobotoTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import info.javaperformance.money.Money;


public class AssetAllocationOverviewActivity
    extends MmxBaseFragmentActivity {

    private FormatUtilities formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_asset_allocation_overview);

        // Toolbar
        setUpToolbar();

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.AssetAllocationOverview.name()));
    }

    @Override
    public void onResume() {
        super.onResume();

        displayAssetAllocation();
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_asset_allocation_editor, menu);

        UIHelper ui = new UIHelper(this);

        // Currencies
        MenuItem currenciesMenu = menu.findItem(R.id.menu_currencies);
        if (currenciesMenu != null) {
            IconicsDrawable icon = ui.getIcon(GoogleMaterial.Icon.gmd_euro_symbol);
            currenciesMenu.setIcon(icon);
        }

        MenuHelper helper = new MenuHelper(this, menu);

        // Edit Asset Allocation.
        helper.add(MenuHelper.edit, R.string.edit, GoogleMaterial.Icon.gmd_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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

            case MenuHelper.edit:
                intent = new Intent(this, AssetAllocationEditorActivity.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        // This happens.
        if (assetAllocation == null) {
            return null;
        }

        // linearize for display
        List<AssetClassViewModel> modelList = new ArrayList<>();
        for (AssetClass child : assetAllocation.getChildren()) {
            addModelToList(child, modelList, 0);
        }

        // add the totals at the end
        AssetClassViewModel totalModel = new AssetClassViewModel(assetAllocation, 0);
        totalModel.assetClass.setType(ItemType.Footer);
        modelList.add(totalModel);

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

    private void displayAssetAllocation() {
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass assetAllocation = service.loadAssetAllocation();

        List<AssetClassViewModel> model = createViewModel(assetAllocation);

        Money threshold = new AppSettings(this).getInvestmentSettings().getAssetAllocationDifferenceThreshold();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FullAssetAllocationAdapter adapter = new FullAssetAllocationAdapter(model, threshold, getFormatter());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showTotal(assetAllocation);
    }

    private FormatUtilities getFormatter() {
        if (this.formatter == null) {
            formatter = new FormatUtilities(this);
        }
        return this.formatter;
    }

    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

//        actionBar.setSubtitle(R.string.asset_allocation);
//        actionBar.setTitle(R.string.asset_allocation);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Title.
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.asset_allocation));
    }

    private void showTotal(AssetClass assetAllocation) {
        if (assetAllocation == null) return;

        RobotoTextView totalView = (RobotoTextView) findViewById(R.id.totalAmountTextView);
        if (totalView == null) return;

        totalView.setText(getFormatter().getValueFormattedInBaseCurrency(assetAllocation.getValue()));
    }
}
