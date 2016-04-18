package com.money.manager.ex.transactions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.database.ISplitTransaction;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the recycler view version.
 */
public class SplitCategoriesAdapter
    extends RecyclerView.Adapter<SplitItemViewHolder> {

    public SplitCategoriesAdapter(int currencyId) {
        splitTransactions = new ArrayList<>();
        this.currencyId = currencyId;
    }

//    public SplitCategoriesAdapter(List<ISplitTransaction> splitTransactions) {
//        this.splitTransactions = splitTransactions;
//    }

    public List<ISplitTransaction> splitTransactions;
    public int currencyId;

    private Context mContext;

    /**
     * Inflate a layout from XML and return the holder.
     */
    @Override
    public SplitItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(mContext);

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

        bindAmount(split, holder);
        initAmountControl(holder, position);
    }

    @Override
    public int getItemCount() {
        return splitTransactions.size();
    }

    public Context getContext() {
        return mContext;
    }

    private void bindAmount(ISplitTransaction splitTransaction, SplitItemViewHolder holder) {
        // Amount

        Money splitTransactionAmount = splitTransaction.getAmount();
        if (splitTransactionAmount != null && !(splitTransactionAmount.isZero())) {
            // Change the sign to positive.
            if(splitTransactionAmount.toDouble() < 0) {
                splitTransactionAmount = splitTransactionAmount.negate();
            }
        }

        FormatUtilities formatter = new FormatUtilities(getContext());
        formatter.formatAmountTextView(holder.txtAmount, splitTransactionAmount, currencyId);
    }

    private void initAmountControl(final SplitItemViewHolder viewHolder, final int position) {
        if (viewHolder.txtAmount.hasOnClickListeners()) return;

        viewHolder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Object tag = v.getTag();
//                Money amount;
//                if (tag == null) {
//                    amount = MoneyFactory.fromString("0");
//                } else {
//                    amount = MoneyFactory.fromString(tag.toString());
//                }
//
//                AmountInputDialog dialog = AmountInputDialog.getInstance(
//                        position, amount, currencyId);
//                dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
            }
        });

    }
}
