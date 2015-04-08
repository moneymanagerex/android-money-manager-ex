package com.money.manager.ex.database;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Used to fetch various data entities.
 * If there is a need, this should be split into separate repositories for different entities.
 * Created by alen.siljak on 07/04/2015.
 */
public class DataRepository {

    public DataRepository(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    private ContentResolver mContentResolver;

    /**
     * Load split transactions.
     * @param transId
     * @return
     */
    public ArrayList<TableBudgetSplitTransactions> loadSplitTransactionFor(int transId) {
        ArrayList<TableBudgetSplitTransactions> listSplitTrans = null;

        TableBudgetSplitTransactions split = new TableBudgetSplitTransactions();
        // getContentResolver()
        Cursor curSplit = mContentResolver.query(split.getUri(), null,
                TableBudgetSplitTransactions.TRANSID + "=" + Integer.toString(transId), null,
                TableBudgetSplitTransactions.SPLITTRANSID);
        if (curSplit != null && curSplit.moveToFirst()) {
            listSplitTrans = new ArrayList<>();
            while (!curSplit.isAfterLast()) {
                TableBudgetSplitTransactions obj = new TableBudgetSplitTransactions();
                obj.setValueFromCursor(curSplit);
                listSplitTrans.add(obj);
                curSplit.moveToNext();
            }
        }

        return listSplitTrans;
    }

}
