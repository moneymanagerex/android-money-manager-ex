package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.utils.RawFileUtils;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

/**
 * Account Transaction repository.
 */
public class QueryAllDataRepository
    extends RepositoryBase {

    public QueryAllDataRepository(Context context) {
        super(context, RawFileUtils.getRawAsString(context, R.raw.query_alldata), DatasetType.QUERY, "queryalldata");
    }

    public int add(AccountTransactionDisplay entity) {
        return insert(entity.contentValues);
    }

    public Cursor query(String selection, String sort) {
        return openCursor(null, selection, null, sort);
    }

}
