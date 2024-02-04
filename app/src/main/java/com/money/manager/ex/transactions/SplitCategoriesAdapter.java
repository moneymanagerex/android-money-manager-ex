/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.transactions;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.transactions.events.AmountEntryRequestedEvent;
import com.money.manager.ex.transactions.events.CategoryRequestedEvent;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the recycler view version of Split Categories view.
 */
public class SplitCategoriesAdapter
        extends RecyclerView.Adapter<SplitItemViewHolder>
        implements SplitItemTouchAdapter {

    public List<ISplitTransaction> splitTransactions;
    public int currencyId;
    /**
     * Transaction type for the main transaction that contains the splits.
     */
    public TransactionTypes transactionType;
    private Context mContext;

    public SplitCategoriesAdapter() {
        splitTransactions = new ArrayList<>();
    }

    /**
     * Inflate a layout from XML and return the holder.
     */
    @Override
    public SplitItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        mContext = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.item_splittransaction, parent, false);

        final SplitItemViewHolder viewHolder = new SplitItemViewHolder(view);

        initAmountControl(viewHolder);
        initCategorySelector(viewHolder);
        initNotesControls(viewHolder);
        initTransactionTypeButton(viewHolder);

        return viewHolder;
    }

    /**
     * Populate data into the item through holder.
     */
    @Override
    public void onBindViewHolder(final SplitItemViewHolder holder, final int position) {
        final ISplitTransaction split = splitTransactions.get(position);

        bindCategory(mContext, holder, split);
        bindTransactionTypeButton(split, holder);
        bindAmount(split, holder);
        bindNotes(split, holder);
    }

    @Override
    public int getItemCount() {
        return splitTransactions.size();
    }

    public Context getContext() {
        return mContext;
    }

    private void bindAmount(final ISplitTransaction splitTransaction, final SplitItemViewHolder holder) {
        Money displayAmount = splitTransaction.getAmount();
        if (null != displayAmount && !(displayAmount.isZero())) {
            // Change the sign to positive.
            if (0 > displayAmount.toDouble()) {
                displayAmount = displayAmount.negate();
            }
        }

        final FormatUtilities formatter = new FormatUtilities(mContext);
        formatter.formatAmountTextView(holder.txtAmount, displayAmount, currencyId);
    }

    private void bindCategory(final Context context, final SplitItemViewHolder holder, final ISplitTransaction split) {
        final CategoryService service = new CategoryService(context);

        final String buttonText = service.getCategorySubcategoryName(split.getCategoryId());
        holder.txtSelectCategory.setText(buttonText);
    }

    private void bindNotes(final ISplitTransaction splitTransaction, final SplitItemViewHolder holder) {
        final String notes = splitTransaction.getNotes();
        holder.txtNotesSplit.setText(notes);
    }


    private void bindTransactionTypeButton(final ISplitTransaction split, final SplitItemViewHolder viewHolder) {
        final int green;
        final int red;
        // 15
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            green = mContext.getColor(R.color.material_green_700);
            red = mContext.getColor(R.color.material_red_700);
        } else {
            green = mContext.getResources().getColor(R.color.material_green_700);
            red = mContext.getResources().getColor(R.color.material_red_700);
        }

        if (TransactionTypes.Withdrawal == split.getTransactionType(transactionType)) {
            // withdrawal
            viewHolder.transactionTypeButton.setText(R.string.ic_diff_removed);
            viewHolder.transactionTypeButton.setTextColor(red);
        } else {
            // deposit
            viewHolder.transactionTypeButton.setText(R.string.ic_diff_added);
            viewHolder.transactionTypeButton.setTextColor(green);
        }
    }

    private void initAmountControl(final SplitItemViewHolder viewHolder) {
        if (viewHolder.txtAmount.hasOnClickListeners()) return;

        viewHolder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Object tag = v.getTag();
                final Money amount;
                if (null == tag) {
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
            public void onClick(final View v) {
                // request category selection
                EventBus.getDefault().post(new CategoryRequestedEvent(holder.getAdapterPosition()));
            }
        });
    }

    private void initTransactionTypeButton(final SplitItemViewHolder viewHolder) {
        viewHolder.transactionTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final int position = viewHolder.getAdapterPosition();
                // change transaction type.
                final ISplitTransaction split = splitTransactions.get(position);
                final TransactionTypes newTransactionType;
                if (TransactionTypes.Withdrawal == split.getTransactionType(transactionType)) {
                    newTransactionType = TransactionTypes.Deposit;
                } else {
                    newTransactionType = TransactionTypes.Withdrawal;
                }
                split.setTransactionType(newTransactionType, transactionType);
                notifyItemChanged(position);
            }
        });
    }

    public void initNotesControls(final SplitItemViewHolder viewHolder) {
        viewHolder.txtNotesSplit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                // todo: empty method?
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                // todo: empty method?
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                final int position = viewHolder.getAdapterPosition();
                // change transaction type.
                final ISplitTransaction split = splitTransactions.get(position);
                split.setNotes(editable.toString());
//                notifyItemChanged(position);
            }
        });
    }

    /**
     * Swipe support
     */

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
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
    public void onItemDismiss(final int position) {
        // add the removed item to the collection
        final ISplitTransaction tx = splitTransactions.get(position);
        EventBus.getDefault().post(new SplitItemRemovedEvent(tx));

        splitTransactions.remove(position);
        notifyItemRemoved(position);
    }
}
