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
package com.money.manager.ex;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

import com.money.manager.ex.budget.BudgetQuery;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.datalayer.SplitRecurringCategoriesRepository;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.sync.SyncManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

/**
 * MmxContentProvider is the extension of the base class of Android
 * ContentProvider. Its purpose is to implement the read access and modify the
 * application data
 */
public class MmxContentProvider
        extends ContentProvider {

    // object definition for the call to check the content
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // object map for the definition of the objects referenced in the URI
    private static final SparseArrayCompat<Object> mapContent = new SparseArrayCompat<>();
    private static String mAuthority;
    @Inject
    Lazy<MmxOpenHelper> openHelper;

    public MmxContentProvider() {
    }

    public static String getAuthority() {
        return mAuthority;
    }

    public static void setAuthority(final String mAuthority) {
        MmxContentProvider.mAuthority = mAuthority;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        if (null == context) return false;

        mAuthority = context.getApplicationContext().getPackageName() + ".provider";

        final List<Dataset> objMoneyManager = Arrays.asList(
                new AccountRepository(context),
                new AccountTransactionRepository(context),
                new BudgetEntryRepository(context),
                new BudgetRepository(context),
                new CategoryRepository(context),
                new CurrencyRepository(context),
//            new InfoRepositorySql(context),
                new PayeeRepository(context),
                new RecurringTransactionRepository(context),
                new SplitCategoriesRepository(context),
                new SplitRecurringCategoriesRepository(context),
                new StockRepository(context),
                new StockHistoryRepository(context),
                new QueryAccountBills(context),
                new QueryCategorySubCategory(context),
                new QueryAllData(context),
                new QueryBillDeposits(context),
                new QueryReportIncomeVsExpenses(context),
                new BudgetQuery(context),
                new ViewMobileData(context),
                new SQLDataSet()
        );

        // Cycle all data sets for the composition of UriMatcher
        for (int i = 0; i < objMoneyManager.size(); i++) {
            // add URI
            sUriMatcher.addURI(mAuthority, objMoneyManager.get(i).getBasepath(), i);
            // put map in the object being added in UriMatcher
            mapContent.put(i, objMoneyManager.get(i));
        }
        return false;
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        try {
            return query_internal(uri, projection, selection, selectionArgs, sortOrder);
        } catch (final Exception e) {
            Timber.e(e, "content provider.query %s", uri);
        }
        return null;
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        Timber.d("Insert Uri: %s", uri);

        // find object from uri
        final Object ret = getObjectFromUri(uri);
        long id = Constants.NOT_SET;
        final String parse;

        if (ret instanceof Dataset) {
            final Dataset dataset = ((Dataset) ret);
            if (DatasetType.TABLE == Objects.requireNonNull(dataset.getType())) {
                logTableInsert(dataset, values);

                //database.beginTransaction();
                try {
                    initializeDependencies();

                    id = openHelper.get().getWritableDatabase()
                            .insertOrThrow(dataset.getSource(), null, values);
                    //database.setTransactionSuccessful();
                } catch (final Exception e) {
                    Timber.e(e, "inserting: %s", "insert");
                }
                parse = dataset.getBasepath() + "/" + id;
            } else {
                throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not instance of dataset");
        }

        if (0 < id) {
            notifyChange(uri);
        }

        // return Uri with the primary key of the inserted record.
        return Uri.parse(parse);
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String whereClause, final String[] whereArgs) {
        Timber.d("Update Uri: %s", uri);

        final Object ret = getObjectFromUri(uri);

        initializeDependencies();

        final SQLiteDatabase database = openHelper.get().getWritableDatabase();

        int rowsUpdate = 0;

        if (ret instanceof Dataset) {
            final Dataset dataset = ((Dataset) ret);
            if (DatasetType.TABLE == Objects.requireNonNull(dataset.getType())) {
                logUpdate(dataset, values, whereClause, whereArgs);

                try {
                    rowsUpdate = database.update(dataset.getSource(), values, whereClause, whereArgs);
                } catch (final Exception ex) {
                    Timber.e(ex, "updating: %s", "update");
                }
            } else {
                throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not instance of dataset");
        }

        if (0 < rowsUpdate) {
            notifyChange(uri);
        }

        // return rows modified
        return rowsUpdate;
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        Timber.d("Delete URI: %s", uri);

        // find object from uri
        final Object ret = getObjectFromUri(uri);
        // safety control of having the where if not clean the table
        if (TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("Delete not permitted because not define where clause");
        }
        // take a database reference
        int rowsDelete = 0;

        if (ret instanceof Dataset) {
            final Dataset dataset = ((Dataset) ret);
            if (DatasetType.TABLE == Objects.requireNonNull(dataset.getType())) {
                logDelete(dataset, selection, selectionArgs);
                try {
                    initializeDependencies();

                    rowsDelete = openHelper.get().getWritableDatabase()
                            .delete(dataset.getSource(), selection, selectionArgs);

                    /*
                     committed
                    if (BuildConfig.DEBUG) Log.d(LOGCAT, "database set transaction successful");
                    database.setTransactionSuccessful();
                    */
                } catch (final Exception e) {
                    Timber.e(e, "insert");
                }
            } else {
                throw new IllegalArgumentException("Type of dataset not supported for delete");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not instance of dataset");
        }

        if (0 < rowsDelete) notifyChange(uri);

        return rowsDelete;
    }

    /**
     * Prepare statement SQL from data set object
     *
     * @param query      SQL query
     * @param projection ?
     * @param selection  ?
     * @param sortOrder  field name for sort order
     * @return statement
     */
    public String prepareQuery(String query, final String[] projection, final String selection, final String sortOrder) {
        String selectList;
        final String from;
        String where = "";
        String sort = "";

        // todo: use builder?
//        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
//        SQLiteQueryBuilder.buildQueryString(false, )

        // compose select list
        if (null == projection) {
            selectList = "SELECT *";
        } else {
            selectList = "SELECT ";

            for (int i = 0; i < projection.length; i++) {
                if (0 < i) {
                    selectList += ", ";
                }
                selectList += projection[i];
            }
        }
        // FROM
        from = "FROM (" + query + ") T";
        // WHERE
        if (!TextUtils.isEmpty(selection)) {
//            if (!selection.contains("WHERE")) {
            if (!selection.startsWith("WHERE")) {
                where += "WHERE";
            }
            where += " " + selection;
        }
        // compose sort
        if (!TextUtils.isEmpty(sortOrder)) {
            if (!sortOrder.contains("ORDER BY")) {
                sort += "ORDER BY ";
            }
            sort += " " + sortOrder;
        }
        // compose statement to return
        query = selectList + " " + from;
        // check where or sort not empty
        if (!TextUtils.isEmpty(where)) {
            query += " " + where;
        }
        if (!TextUtils.isEmpty(sort)) {
            query += " " + sort;
        }

        return query;
    }

    public Object getObjectFromUri(final Uri uri) {
        final int uriMatch = sUriMatcher.match(uri);
//        Timber.d("Uri Match Result: %s", Integer.toString(uriMatch));

        // find key into hash map
        final Object objectRet = mapContent.get(uriMatch);
        if (null == objectRet) {
            throw new IllegalArgumentException("Unknown URI for Update: " + uri);
        }

        return objectRet;
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        return null;
    }

    public void resetDatabase() {
        if (null != openHelper) {
            openHelper.get().close();
        }

        openHelper = null;
        initializeDependencies();
    }

    // Private

    private void initializeDependencies() {
        if (null != openHelper) return;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    private void logTableInsert(final Dataset dataset, final ContentValues values) {
        String log = "INSERT INTO " + dataset.getSource();
        if (null != values) {
            log += " VALUES ( " + values + ")";
        }
        Timber.d(log);
    }

    private Cursor query_internal(final Uri uri, final String[] projection, final String selection,
                                  final String[] selectionArgs, final String sortOrder) {
        Timber.v("Querying URI: %s", uri);
        Timber.v("Querying projection: %s", projection);
        Timber.v("Querying selection: %s", selection);
        Timber.v("Querying selectionArgs: %s", selectionArgs);

        // find object from uri
        final Object sourceObject = getObjectFromUri(uri);

        initializeDependencies();

        final SQLiteDatabase database = openHelper.get().getReadableDatabase();
        if (null == database) {
            Timber.e("Database could not be opened");
            return null;
        }

        final Cursor cursor;

        // check type of instance data set
        if (sourceObject instanceof Dataset) {
            final Dataset dataset = ((Dataset) sourceObject);

//            logQuery(dataset, projection, selection, selectionArgs, sortOrder);

            switch (dataset.getType()) {
                case QUERY:
                    final String query = prepareQuery(dataset.getSource(), projection, selection, sortOrder);
                    cursor = database.rawQuery(query, selectionArgs);
                    break;
                case SQL:
                    cursor = database.rawQuery(selection, selectionArgs);
                    break;
                case TABLE:
                case VIEW:
                    final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                    queryBuilder.setTables(dataset.getSource());
                    cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not defined");
            }
        } else {
            throw new IllegalArgumentException("Object sourceObject of mapContent is not instance of dataset");
        }

        // notify listeners waiting for the data is ready
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);

        if (!cursor.isClosed()) {
            Timber.v("Rows returned: %d", cursor.getCount());
        }

        return cursor;
    }

    private void logQuery(final Dataset dataset, final String[] projection, final String selection,
                          final String[] selectionArgs, final String sortOrder) {
        // compose log verbose instruction
        String log;
        // compose log
        if (DatasetType.SQL == dataset.getType()) {
            log = selection;
        } else {
            if (null != projection) {
                log = "SELECT " + Arrays.asList(projection);
            } else {
                log = "SELECT *";
            }
            log += " FROM " + dataset.getSource();
            if (!TextUtils.isEmpty(selection)) {
                log += " WHERE " + selection;
            }
            if (!TextUtils.isEmpty(sortOrder)) {
                log += " ORDER BY " + sortOrder;
            }
            if (null != selectionArgs) {
                log += "; ARGS=" + Arrays.asList(selectionArgs);
            }
        }
        // log
        Timber.d(log);
    }

    private void logUpdate(final Dataset dataset, final ContentValues values, final String whereClause, final String[] whereArgs) {
        String log = "UPDATE " + dataset.getSource();
        // compose log verbose
        if (null != values) {
            log += " SET " + values;
        }
        if (!TextUtils.isEmpty(whereClause)) {
            log += " WHERE " + whereClause;
        }
        if (null != whereArgs) {
            log += "; ARGS=" + Arrays.asList(whereArgs);
        }

        // open transaction

        Timber.d(log);
    }

    private void logDelete(final Dataset dataset, final String selection, final String[] selectionArgs) {
        String log = "DELETE FROM " + dataset.getSource();
        // compose log verbose
        if (!TextUtils.isEmpty(selection)) {
            log += " WHERE " + selection;
        }
        if (null != selectionArgs) {
            log += "; ARGS=" + Arrays.asList(selectionArgs);
        }
        // open transaction
        Timber.d(log);
    }

    private void notifyChange(final Uri uri) {
        if (null == getContext()) return;

        // notify update. todo Do this also after changes via sqlite.
        getContext().getContentResolver().notifyChange(uri, null);
        // notify the sync that database has changed.
        new SyncManager(getContext()).dataChanged();
    }
}
