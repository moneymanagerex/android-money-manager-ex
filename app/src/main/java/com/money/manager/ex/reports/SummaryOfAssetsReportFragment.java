/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.reports;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.TableAssets;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class SummaryOfAssetsReportFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_of_accounts_report, container, false);

        tableLayout = view.findViewById(R.id.summaryAccountsTable);
        emptyView = view.findViewById(R.id.summaryAccountsEmpty);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadReportAsync();
    }

    private void loadReportAsync() {
        new Thread(() -> {
            try {
                List<AssetRow> rows = buildModel();
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> renderModel(rows));
            } catch (Exception e) {
                Timber.e(e, "loading summary of assets report");
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    emptyView.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private List<AssetRow> buildModel() {
        List<AssetRow> rows = new ArrayList<>();
        Cursor cursor = requireContext().getContentResolver().query(new SQLDataSet().getUri(), null,
                "SELECT ASSETNAME, ASSETTYPE, VALUE FROM assets_v1 ORDER BY ASSETNAME", null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(TableAssets.ASSETNAME));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(TableAssets.ASSETTYPE));
                    double value = cursor.getDouble(cursor.getColumnIndexOrThrow(TableAssets.VALUE));
                    rows.add(new AssetRow(name, type, value));
                }
            } finally {
                cursor.close();
            }
        }
        return rows;
    }

    private void renderModel(List<AssetRow> rows) {
        tableLayout.removeAllViews();

        if (rows.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);

        tableLayout.addView(createHeaderRow());

        CurrencyService currencyService = new CurrencyService(requireContext());
        double grandTotal = 0;

        for (AssetRow row : rows) {
            TableRow tableRow = new TableRow(requireContext());
            tableRow.addView(createCell(row.name, true));
            tableRow.addView(createCell(row.type, true));
            tableRow.addView(createCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(row.value)), false));
            tableLayout.addView(tableRow);
            grandTotal += row.value;
        }

        // Grand Total row
        TableRow totalRow = new TableRow(requireContext());
        totalRow.addView(createHeaderCell(getString(R.string.total), true));
        totalRow.addView(createCell("", true));
        totalRow.addView(createHeaderCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(grandTotal)), false));
        tableLayout.addView(totalRow);
    }

    private TableRow createHeaderRow() {
        TableRow row = new TableRow(requireContext());
        row.addView(createHeaderCell(getString(R.string.asset_name), true));
        row.addView(createHeaderCell(getString(R.string.asset_type), true));
        row.addView(createHeaderCell(getString(R.string.asset_value), false));
        return row;
    }

    private TextView createHeaderCell(String text, boolean alignStart) {
        TextView cell = createCell(text, alignStart);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        return cell;
    }

    private TextView createCell(String text, boolean alignStart) {
        TextView cell = new TextView(requireContext());
        cell.setText(text);
        cell.setPadding(16, 8, 16, 8);
        cell.setGravity(alignStart ? Gravity.START : Gravity.END);
        return cell;
    }

    private static class AssetRow {
        final String name;
        final String type;
        final double value;

        AssetRow(String name, String type, double value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
    }
}
