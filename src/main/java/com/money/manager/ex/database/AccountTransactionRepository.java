package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

/**
 * Account Transaction repository.
 * Created by Alen Siljak on 02/09/2015.
 */
public class AccountTransactionRepository {

    public AccountTransactionRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    private Context context;

    public Cursor query(String selection, String sort) {
        QueryAllData allData = new QueryAllData(this.context);

        Cursor c = this.context.getContentResolver().query(allData.getUri(),
                null,
                selection,
                null,
                sort);
        return c;
    }
}
