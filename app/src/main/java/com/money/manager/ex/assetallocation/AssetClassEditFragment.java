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
package com.money.manager.ex.assetallocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.list.AssetClassListActivity;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Fragment for editing an asset class.
 */
public class AssetClassEditFragment
    extends Fragment {

    public static final int INPUT_ALLOCATION = 1;
    public static final int INPUT_SORT_ORDER = 2;
    public static final int CONTEXT_MENU_DELETE = 1;
    public static final int REQUEST_ASSET_CLASS_PARENT = 1;

    public AssetClassEditFragment() {
    }

    public AssetClass assetClass;

    private AssetClassEditViewHolder viewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_class_edit, container, false);

        this.viewHolder = new AssetClassEditViewHolder(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.assetClass == null) {
            this.assetClass = AssetClass.create("");
        }

        View view = getView();
        initializeParentEdit(view);
        initializeNameEdit(view);
        initializeAllocationPicker();
        initializeSortOrderInput(view);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        // request code

        int id = data.getIntExtra(AssetClassListActivity.EXTRA_ASSET_CLASS_ID, Constants.NOT_SET);
        if (id == Constants.NOT_SET) return;

        // set the parent for the current item
        this.assetClass.setParentId(id);

        // refresh the view.
        displayParent();
    }

    // Context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        boolean result = false;

        switch (item.getItemId()) {
//            case CONTEXT_MENU_DELETE:
//                // Delete
//                ListView listView = getListView();
//                if (listView == null) return false;
//
//                Cursor cursor = (Cursor) listView.getItemAtPosition(info.position);
//                Stock stock = Stock.fromCursor(cursor);
//                String stockSymbol = stock.getSymbol();
//
//                AssetClassStockRepository repo = new AssetClassStockRepository(getActivity());
//                int assetClassId = this.assetClass.getId();
//                boolean deleted = repo.delete(assetClassId, stockSymbol);
//                if (!deleted) {
//                    ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
//                    handler.showMessage(getString(R.string.error));
//                }
//
//                result = true;
//                break;
        }
        return result;
    }

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        int id = Integer.parseInt(event.requestId);
        switch (id) {
            case INPUT_ALLOCATION:
                assetClass.setAllocation(event.amount);
                displayAllocation();
                break;

            case INPUT_SORT_ORDER:
                int value = Integer.valueOf(event.amount.truncate(0).toString());
                assetClass.setSortOrder(value);
                displaySortOrder();
                break;
        }
    }

    // Private

    private void initializeNameEdit(View view) {
        final EditText edit = (EditText) view.findViewById(R.id.nameEdit);
        if (edit == null) return;

        edit.setText(assetClass.getName());

        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // edit.getText().toString()
                String newValue = s.toString();
                assetClass.setName(newValue);
            }
        });
    }

    private void initializeAllocationPicker() {
        TextView view = viewHolder.allocationTextView;
        if (view == null) return;

        view.setText(assetClass.getAllocation().toString());

        TextDrawable1 drawable = new TextDrawable1("%", 32.0f);
        view.setCompoundDrawables(drawable, null, null, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(INPUT_ALLOCATION,
                    assetClass.getAllocation());
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        });
    }

    private void initializeParentEdit(View view) {
        TextView edit = (TextView) view.findViewById(R.id.parentAssetClass);
        if (edit == null) return;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show asset allocation selector.
                // send the allocation id to exclude from the selection list.
                Intent intent = new Intent(getContext(), AssetClassListActivity.class);
                intent.putExtra(AssetClassListActivity.EXTRA_ASSET_CLASS_ID, assetClass.getId());
                startActivityForResult(intent, REQUEST_ASSET_CLASS_PARENT);
            }
        };

        // allow changing parent only on existing items
        if (getActivity().getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            edit.setOnClickListener(onClickListener);
        }

        displayParent();
    }

    private void initializeSortOrderInput(View view) {
        TextView textView = (TextView) view.findViewById(R.id.sortOrderEdit);
        if (textView == null) return;

        textView.setText(assetClass.getSortOrder().toString());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Money number = MoneyFactory.fromString(Integer.toString(assetClass.getSortOrder()));

                AmountInputDialog dialog = AmountInputDialog.getInstance(INPUT_SORT_ORDER,
                    number, Constants.NOT_SET, false);
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        });
    }

    private void displayAllocation() {
        View view = getView();
        if (view == null) return;

        if (viewHolder.allocationTextView != null) {
            Money allocation = assetClass.getAllocation();
            viewHolder.allocationTextView.setText(allocation.toString());
            viewHolder.allocationTextView.setTag(allocation.toString());
        }
    }

    private void displayParent() {
        String name;

        if (assetClass.getParentId() == null) {
            name = getString(R.string.none);
        } else {
            AssetAllocationService service = new AssetAllocationService(getActivity());
            name = service.loadName(assetClass.getParentId());
        }

        viewHolder.parentAssetClass.setText(name);
    }

    private void displaySortOrder() {
        View view = getView();
        if (view == null) return;

        TextView textView = (TextView) view.findViewById(R.id.sortOrderEdit);
        if (textView != null) {
            Integer sortOrder = assetClass.getSortOrder();

            textView.setText(sortOrder.toString());
            textView.setTag(sortOrder.toString());
        }
    }
}
