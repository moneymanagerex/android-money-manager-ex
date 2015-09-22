package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

/**
 * Recurring transaction repository.
 * Created by Alen Siljak on 13/08/2015.
 */
public class RecurringTransactionRepository {

    public RecurringTransactionRepository(Context context) {
        this.context = context;
    }

    public Context context;

    public TableBillsDeposits load(int id) {
        TableBillsDeposits tx = new TableBillsDeposits();

        Cursor cursor = this.context.getContentResolver().query(
                tx.getUri(),
                tx.getAllColumns(),
                TableBillsDeposits.BDID + "=?",
                new String[] { Integer.toString(id) },
                null);
        if (cursor == null) return null;
        if (!cursor.moveToFirst()) return null;

        tx.setValueFromCursor(cursor);

        cursor.close();

        return tx;
    }
}
