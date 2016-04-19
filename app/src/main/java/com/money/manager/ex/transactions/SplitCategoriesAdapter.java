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
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.transactions.events.AmountEntryRequestedEvent;
import com.money.manager.ex.transactions.events.CategoryRequestedEvent;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the recycler view version.
 */
public class SplitCategoriesAdapter
    extends RecyclerView.Adapter<SplitItemViewHolder> {

    public SplitCategoriesAdapter() {
        splitTransactions = new ArrayList<>();
//        this.currencyId = currencyId;
    }

//    public SplitCategoriesAdapter(List<ISplitTransaction> splitTransactions) {
//        this.splitTransactions = splitTransactions;
//    }

    public List<ISplitTransaction> splitTransactions;
    public int currencyId;
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
        initRemoveButton(viewHolder);

        return viewHolder;
    }

    /**
     * Populate data into the item through holder.
     */
    @Override
    public void onBindViewHolder(SplitItemViewHolder holder, int position) {
        ISplitTransaction split = this.splitTransactions.get(position);

        holder.position = position;

        bindCategory(getContext(), holder, split);
        bindTransactionType(holder, this.transactionType, split.getAmount());
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

    private void bindCategory(Context context, SplitItemViewHolder holder, ISplitTransaction split) {
        Core core = new Core(context.getApplicationContext());

        String buttonText = core.getCategSubName(split.getCategoryId(), split.getSubcategoryId());
        holder.txtSelectCategory.setText(buttonText);

    }

    private void bindTransactionType(SplitItemViewHolder viewHolder, TransactionTypes transactionType, Money amount) {
        // find the split transaction type.
        int transactionTypeSelection = getTransactionTypeCode(transactionType, amount);
        viewHolder.spinTransCode.setSelection(transactionTypeSelection);
    }

    private int getTransactionTypeCode(TransactionTypes transactionType, Money amount){
        // define the transaction type based on the amount and the parent type.

        int transactionTypeSelection;

//        SplitCategoriesActivity splitActivity = (SplitCategoriesActivity) getActivity();
        boolean parentIsWithdrawal = transactionType.equals(TransactionTypes.Withdrawal);
//        Money amount = mSplitTransaction.getAmount();
        if(parentIsWithdrawal){
            // parent is Withdrawal.
            transactionTypeSelection = amount.toDouble() >= 0
                    ? TransactionTypes.Withdrawal.getCode() // 0
                    : TransactionTypes.Deposit.getCode(); // 1;
        } else {
            // parent is Deposit.
            transactionTypeSelection = amount.toDouble() >= 0
                    ? TransactionTypes.Deposit.getCode() // 1
                    : TransactionTypes.Withdrawal.getCode(); // 0;
        }

        return transactionTypeSelection;
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
                EventBus.getDefault().post(new AmountEntryRequestedEvent(viewHolder.position, amount));
            }
        });

    }

    private void initCategorySelector(final SplitItemViewHolder holder) {
        holder.txtSelectCategory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // request category selection
                EventBus.getDefault().post(new CategoryRequestedEvent(holder.position));
            }
        });

    }

    private void initRemoveButton(final SplitItemViewHolder viewHolder) {
        viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ISplitTransaction split = splitTransactions.get(viewHolder.position);
                EventBus.getDefault().post(new SplitItemRemovedEvent(split));

                splitTransactions.remove(viewHolder.position);
                notifyItemRemoved(viewHolder.position);
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
                TransactionTypes splitTransactionType = TransactionTypes.values()[position];
                ISplitTransaction split = splitTransactions.get(viewHolder.position);

                if(!transactionType.equals(splitTransactionType)){
                    // parent transaction type is different. Invert the amount. What if the amount is already negative?
                    split.setAmount(split.getAmount().negate());
                } else {
                    split.setAmount(split.getAmount());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
