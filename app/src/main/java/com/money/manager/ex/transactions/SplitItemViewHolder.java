package com.money.manager.ex.transactions;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.R;

/**
 * View Holder pattern for Split Category
 */
public class SplitItemViewHolder
    extends RecyclerView.ViewHolder {

    public TextView txtSelectCategory;
    public Spinner spinTransCode;
    public TextView txtAmount;

    public SplitItemViewHolder(View itemView) {
        super(itemView);

        txtSelectCategory = (TextView) itemView.findViewById(R.id.textViewCategory);
        spinTransCode = (Spinner) itemView.findViewById(R.id.spinnerTransCode);
        txtAmount = (TextView) itemView.findViewById(R.id.editTextTotAmount);
    }
}
