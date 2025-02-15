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
import android.text.Editable;
// not used
// import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.transactions.events.AmountEntryRequestedEvent;
import com.money.manager.ex.transactions.events.CategoryRequestedEvent;
import com.money.manager.ex.transactions.events.SplitItemRemovedEvent;
import com.money.manager.ex.utils.TagLinkUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the recycler view version of Split Categories view.
 */
public class SplitCategoriesAdapter
    extends RecyclerView.Adapter<SplitItemViewHolder>
    implements SplitItemTouchAdapter {

    public SplitCategoriesAdapter() {
        splitTransactions = new ArrayList<>();
    }

    public List<ISplitTransaction> splitTransactions;
    public long currencyId;
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
        initCategorySelector(viewHolder);
        initNotesControls(viewHolder);
        initTransactionTypeButton(viewHolder);
// not here        initTagsControls(viewHolder);

        return viewHolder;
    }

    /**
     * Populate data into the item through holder.
     */
    @Override
    public void onBindViewHolder(SplitItemViewHolder holder, int position) {
        ISplitTransaction split = this.splitTransactions.get(position);

        bindCategory(getContext(), holder, split);
        bindTransactionTypeButton(split, holder);
        bindAmount(split, holder);
        bindNotes(split, holder);
        initTagsControls(split, holder);

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
        CategoryService service = new CategoryService(context);

        String buttonText = service.getCategorySubcategoryName(split.getCategoryId());
        holder.txtSelectCategory.setText(buttonText);
    }

    private void bindNotes(ISplitTransaction splitTransaction, SplitItemViewHolder holder) {
        String notes = splitTransaction.getNotes();
        holder.txtNotesSplit.setText(notes);
    }


    private void bindTransactionTypeButton(ISplitTransaction split, SplitItemViewHolder viewHolder) {
        // 15
        int green = getContext().getColor(R.color.material_green_700);
        int red = getContext().getColor(R.color.material_red_700);

        if (split.getTransactionType(transactionType) == TransactionTypes.Withdrawal) {
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

    private void initTransactionTypeButton(final SplitItemViewHolder viewHolder) {
        viewHolder.transactionTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                // change transaction type.
                ISplitTransaction split = splitTransactions.get(position);
                TransactionTypes newTransactionType;
                if (split.getTransactionType(transactionType) == TransactionTypes.Withdrawal) {
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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // todo: empty method?
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // todo: empty method?
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int position = viewHolder.getAdapterPosition();
                // change transaction type.
                ISplitTransaction split = splitTransactions.get(position);
                split.setNotes(editable.toString());
//                notifyItemChanged(position);
            }
        });
    }

    public void initTagsControls(ISplitTransaction splitTransaction, SplitItemViewHolder viewHolder) {
        (new TagLinkUtils(getContext())).initTagControls(
                viewHolder.txtTagsList,
                splitTransaction.getTagLinks(),
                splitTransaction.getId(),
                splitTransaction.getTransactionModel(),
                tagLink -> {
                    splitTransaction.setTagLinks(tagLink);
                }
        );
    }

        /**
         * Swipe support
         */

    @Override
    public void onItemMove(long fromPosition, long toPosition) {
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
        // add the removed item to the collection
        ISplitTransaction tx = splitTransactions.get(position);
        EventBus.getDefault().post(new SplitItemRemovedEvent(tx));

        this.splitTransactions.remove(position);
        notifyItemRemoved(position);
    }
}
