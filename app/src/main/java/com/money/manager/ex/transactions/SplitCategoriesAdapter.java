package com.money.manager.ex.transactions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.database.ISplitTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the recycler view version.
 */
public class SplitCategoriesAdapter
    extends RecyclerView.Adapter<SplitItemViewHolder> {

    public List<ISplitTransaction> splitTransactions;

    public SplitCategoriesAdapter() {
        splitTransactions = new ArrayList<>();
    }

    public SplitCategoriesAdapter(List<ISplitTransaction> splitTransactions) {
        this.splitTransactions = splitTransactions;
    }

    /**
     * Inflate a layout from XML and return the holder.
     */
    @Override
    public SplitItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_splittransaction, parent, false);

        SplitItemViewHolder viewHolder = new SplitItemViewHolder(view);
        return viewHolder;
    }

    /**
     * Populate data into the item through holder.
     */
    @Override
    public void onBindViewHolder(SplitItemViewHolder holder, int position) {
        ISplitTransaction split = this.splitTransactions.get(position);

        holder.txtSelectCategory.setText(split.getCategoryId().toString());
        holder.txtAmount.setText(split.getAmount().toString());

        Log.d("test", "here we are");
    }

    @Override
    public int getItemCount() {
        return splitTransactions.size();
    }
}
