package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.utils.RawFileUtils;
import com.money.manager.ex.viewmodels.AccountTransaction;

/**
 * Account Transaction repository.
 *
 * Created by Alen Siljak on 02/09/2015.
 */
public class AccountTransactionRepository
    extends RepositoryBase {

    public AccountTransactionRepository(Context context) {
        super(context, RawFileUtils.getRawAsString(context, R.raw.query_alldata), DatasetType.QUERY, "queryalldata");
    }

    public int add(AccountTransaction entity) {
        return insert(entity.contentValues);
    }

    public Cursor query(String selection, String sort) {
        return openCursor(null, selection, null, sort);
    }

}
