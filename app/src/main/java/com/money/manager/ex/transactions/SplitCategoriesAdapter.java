package com.money.manager.ex.transactions;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.money.manager.ex.R;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.transactions.events.AmountEntryRequestedEvent;
import com.money.manager.ex.transactions.events.CategoryRequestedEvent;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the recycler view version.
 */
public class SplitCategoriesAdapter
    extends RecyclerView.Adapter<SplitItemViewHolder>
    implements SplitItemTouchAdapter {

    public SplitCategoriesAdapter() {
        splitTransactions = new ArrayList<>();
    }

    public List<ISplitTransaction> splitTransactions;
    public int currencyId;
    /**
     * Transaction type for the main transaction that contains the splits.
     */
    public TransactionTypes transactionType;

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

        initAmountControl(viewHolder);
        initTransactionType(mContext, viewHolder);
        initCategorySelector(viewHolder);
        //todo initTransactionTypeButton
        
        return viewHolder;
    }

    /**
     * Populate data into the item through holder.
     */
    @Override
    public void onBindViewHolder(SplitItemViewHolder holder, int position) {
        ISplitTransaction split = this.splitTransactions.get(position);

        bindCategory(getContext(), holder, split);
        bindTransactionType(holder, split);
        bindAmount(split, holder);
    }

    @Override
    public int getItemCount() {
        return splitTransactions.size();
    }

    public Context getContext() {
        return mContext;
    }

    private void bindAmount(ISplitTransaction splitTransaction, SplitItemViewHolder holder) {
        Money displayAmount = splitTransaction.getAmount();
        if (displayAmount != null && !(displayAmount.isZero())) {
            // Change the sign to positive.
            if(displayAmount.toDouble() < 0) {
                displayAmount = displayAmount.negate();
            }
        }

        FormatUtilities formatter = new FormatUtilities(getContext());
        formatter.formatAmountTextView(holder.txtAmount, displayAmount, currencyId);
    }

    private void bindCategory(Context context, SplitItemViewHolder holder, ISplitTransaction split) {
        Core core = new Core(context.getApplicationContext());

        String buttonText = core.getCategSubName(split.getCategoryId(), split.getSubcategoryId());
        holder.txtSelectCategory.setText(buttonText);
    }

    private void bindTransactionType(SplitItemViewHolder viewHolder, ISplitTransaction split) {
        // find the split transaction type.
        int transactionTypeSelection = split.getTransactionType(transactionType).getCode();
        viewHolder.spinTransCode.setSelection(transactionTypeSelection);
    }

    private void initAmountControl(final SplitItemViewHolder viewHolder) {
        if (viewHolder.txtAmount.hasOnClickListeners()) return;

        viewHolder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Object tag = v.getTag();
                Money amount;
                if (tag == null) {
                    amount = MoneyFactory.fromString("0");
                } else {
                    amount = MoneyFactory.fromString(tag.toString());
                }

                // Request the amount entry.
                EventBus.getDefault().post(new AmountEntryRequestedEvent(viewHolder.getAdapterPosition(), amount));
            }
        });
    }

    private void initCategorySelector(final SplitItemViewHolder holder) {
        holder.txtSelectCategory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // request category selection
                EventBus.getDefault().post(new CategoryRequestedEvent(holder.getAdapterPosition()));
            }
        });
    }

    private void initTransactionType(Context context, final SplitItemViewHolder viewHolder) {
        String[] transCodeItems = context.getResources().getStringArray(R.array.split_transcode_items);

        ArrayAdapter<String> adapterTrans = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, transCodeItems);
        adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        viewHolder.spinTransCode.setAdapter(adapterTrans);

        viewHolder.spinTransCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TransactionTypes selectedType = TransactionTypes.values()[position];
                ISplitTransaction split = splitTransactions.get(viewHolder.getAdapterPosition());

                if (selectedType != split.getTransactionType(transactionType)) {
                    split.setAmount(split.getAmount().negate());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initTransactionTypeButton() {

    }

    /**
     * Swipe support
     */

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
//        if (fromPosition < toPosition) {
//            for (int i = fromPosition; i < toPosition; i++) {
//                Collections.swap(mItems, i, i + 1);
//            }
//        } else {
//            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(mItems, i, i - 1);
//            }
//        }
//        notifyItemMoved(fromPosition, toPosition);
//        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        this.splitTransactions.remove(position);
        notifyItemRemoved(position);
    }
}
