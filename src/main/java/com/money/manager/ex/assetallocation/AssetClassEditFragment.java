package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.domainmodel.AssetClass;

import info.javaperformance.money.Money;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssetClassEditFragment
    extends Fragment
    implements IInputAmountDialogListener {

    public static final int INPUT_ALLOCATION = 1;

    public AssetClassEditFragment() {
    }

    public AssetClass assetClass;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.assetClass == null) {
            this.assetClass = AssetClass.create();
        }

        View view = getView();
        initializeNameEdit(view);
        initializeAllocationPicker(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_class_edit, container, false);

        return view;
    }

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

    private void initializeAllocationPicker(View view) {
        TextView textView = (TextView) view.findViewById(R.id.allocationEdit);
        if (textView == null) return;

        textView.setText(assetClass.getAllocation().toString());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAmountDialog dialog = InputAmountDialog.getInstance(INPUT_ALLOCATION,
                    assetClass.getAllocation(),
                    null);
                dialog.roundToCurrencyDecimals = false;
                dialog.setTargetFragment(AssetClassEditFragment.this, INPUT_ALLOCATION);
                dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        });
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Money amount) {
        switch (id) {
            case INPUT_ALLOCATION:
                assetClass.setAllocation(amount.toDouble());
                updateAllocation();
                break;
        }
    }

    private void updateAllocation() {
        TextView textView = (TextView) getView().findViewById(R.id.allocationEdit);
        if (textView != null) {
            Money allocation = assetClass.getAllocation();
            //FormatUtilities.formatAmountTextView();
            textView.setText(allocation.toString());
            textView.setTag(allocation.toString());
        }
    }
}
