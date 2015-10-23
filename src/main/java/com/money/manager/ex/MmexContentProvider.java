/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.budget.BudgetQuery;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.BudgetTable;
import com.money.manager.ex.database.BudgetYear;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.datalayer.StockHistoryRepository;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MmexContentProvider is the extension of the base class of Android
 * ContentProvider. Its purpose is to implement the read access and modify the
 * application data
 */
public class MmexContentProvider
    extends ContentProvider {

    private static final String LOGCAT = MmexContentProvider.class.getSimpleName();
    // object definition for the call to check the content
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // object map for the definition of the objects referenced in the URI
    private static Map<Integer, Object> mapContent = new HashMap<>();
    private static String mAuthority;

    public MmexContentProvider() {
        super();

    }

    public static String getAuthority() {
        return mAuthority;
    }

    public static void setAuthority(String mAuthority) {
        MmexContentProvider.mAuthority = mAuthority;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();

        setAuthority(getContext().getApplicationContext().getPackageName() + ".provider");

        List<Dataset> objMoneyManager = Arrays.asList(
            new AccountRepository(context),
            new AssetClassRepository(context),
            new AssetClassStockRepository(context),
//                new TableAssets(),
                new TableBillsDeposits(),
                new BudgetTable(),
                new TableBudgetSplitTransactions(),
                new BudgetYear(),
            new CategoryRepository(context),
            new AccountTransactionRepository(context),
                new TableCurrencyFormats(),
                new TableInfoTable(),
                new TablePayee(),
                new TableSplitTransactions(),
            new StockRepository(context),
            new StockHistoryRepository(context),
            new SubcategoryRepository(context),
                new QueryAccountBills(getContext()),
                new QueryCategorySubCategory(getContext()),
                new QueryAllData(getContext()),
                new QueryBillDeposits(getContext()),
                new QueryReportIncomeVsExpenses(getContext()),
                new BudgetQuery(getContext()),
                new ViewMobileData(getContext()),
                new SQLDataSet()
        );

        // Cycle all data sets for the composition of UriMatcher
        for (int i = 0; i < objMoneyManager.size(); i++) {
            // add URI
            sUriMatcher.addURI(getAuthority(), objMoneyManager.get(i).getBasepath(), i);
            // put map in the object being added in UriMatcher
            mapContent.put(i, objMoneyManager.get(i));
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return query_internal(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Context context = getContext();
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "content provider.query " + uri);
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Insert Uri: " + uri);

        // find object from uri
        Object ret = getObjectFromUri(uri);
        // database reference
        MmexOpenHelper databaseHelper = MmexOpenHelper.getInstance(getContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        long id = Constants.NOT_SET;
        String parse;
        // check instance type object
        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
                    logTableInsert(dataset, values);

                    //database.beginTransaction();
                    try {
                        id = database.insertOrThrow(dataset.getSource(), null, values);
                        //database.setTransactionSuccessful();
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                        handler.handle(e, "inserting: " + e.getMessage());
                    }
                    parse = dataset.getBasepath() + "/" + id;
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not istance of dataset");
        }

        if (id > 0) {
            // notify the data inserted
            getContext().getContentResolver().notifyChange(uri, null);

            // notify dropbox of the data changes
            DropboxHelper.notifyDataChanged();
        }

        // return Uri with the primary key of the inserted record.
        return Uri.parse(parse);
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Update Uri: " + uri);

        Object ret = getObjectFromUri(uri);

        MmexOpenHelper databaseHelper = MmexOpenHelper.getInstance(getContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int rowsUpdate = 0;

        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
                    logUpdate(dataset, values, whereClause, whereArgs);

                    try {
                        rowsUpdate = database.update(dataset.getSource(), values, whereClause, whereArgs);
                    } catch (Exception ex) {
                        ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                        handler.handle(ex, "updating: " + ex.getMessage());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not istance of dataset");
        }

        if (rowsUpdate > 0) {
            // notify update
            getContext().getContentResolver().notifyChange(uri, null);
            // notify dropbox data changed
            DropboxHelper.notifyDataChanged();
        }

        // return rows modified
        return rowsUpdate;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Delete URI: " + uri);

        // find object from uri
        Object ret = getObjectFromUri(uri);
        // safety control of having the where if not clean the table
        if (TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("Delete not permitted because not define where clause");
//            Log.e(LOGCAT, "Delete not permitted because not define where clause");
//            return 0;
        }
        // take a database reference
        MmexOpenHelper databaseHelper = MmexOpenHelper.getInstance(getContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsDelete = 0;

        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
                    logDelete(dataset, selection, selectionArgs);
                    try {
                        rowsDelete = database.delete(dataset.getSource(), selection, selectionArgs);

                        // committed
                        //if (BuildConfig.DEBUG) Log.d(LOGCAT, "database set transaction successful");
                        //database.setTransactionSuccessful();
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(getContext(), this);
                        handler.handle(e, "insert");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not supported for delete");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not istance of dataset");
        }
        // delete notify
        getContext().getContentResolver().notifyChange(uri, null);
        // notify dropbox data changed
        DropboxHelper.notifyDataChanged();

        return rowsDelete;
    }

    /**
     * Prepare statement SQL from data set object
     *
     * @param query SQL query
     * @param projection ?
     * @param selection ?
     * @param sortOrder field name for sort order
     * @return statement
     */
    public String prepareQuery(String query, String[] projection, String selection, String sortOrder) {
        String selectList, from, where = "", sort = "";

        // todo: use builder?
//        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
//        SQLiteQueryBuilder.buildQueryString(false, )

        // compose select list
        if (projection == null) {
            selectList = "SELECT *";
        } else {
            selectList = "SELECT ";

            for (int i = 0; i < projection.length; i++) {
                if (i > 0) {
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

    public Object getObjectFromUri(Uri uri) {
        int uriMatch = sUriMatcher.match(uri);
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Uri Match Result: " + Integer.toString(uriMatch));

        // find key into hash map
        Object objectRet = mapContent.get(uriMatch);
        if (objectRet == null) {
            throw new IllegalArgumentException("Unknown URI for Update: " + uri);
        }

        return objectRet;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private void logTableInsert(Dataset dataset, ContentValues values) {
        String log = "INSERT INTO " + dataset.getSource();
        if (values != null) {
            log += " VALUES ( " + values.toString() + ")";
        }
        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);
    }

    private Cursor query_internal(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Query URI: " + uri);

        // find object from uri
        Object sourceObject = getObjectFromUri(uri);
        // take a database reference
        Context context = getContext();
        MmexOpenHelper databaseHelper = MmexOpenHelper.getInstance(context);

        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        if (database == null) {
            Log.e(LOGCAT, "Database could not be opened");
            return null;
        }

        Cursor cursor;

        // check type of instance data set
        if (Dataset.class.isInstance(sourceObject)) {
            Dataset dataset = ((Dataset) sourceObject);

            logQuery(dataset, projection, selection, selectionArgs, sortOrder);

            switch (dataset.getType()) {
                case QUERY:
                    String query = prepareQuery(dataset.getSource(), projection, selection, sortOrder);
                    cursor = database.rawQuery(query, selectionArgs);
                    break;
                case SQL:
                    cursor = database.rawQuery(selection, selectionArgs);
                    break;
                case TABLE:
                case VIEW:
                    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
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
        cursor.setNotificationUri(context.getContentResolver(), uri);

        if (BuildConfig.DEBUG && !cursor.isClosed()) {
            Log.d(LOGCAT, "Rows returned: " + cursor.getCount());
        }

        return cursor;
    }

    private void logQuery(Dataset dataset, String[] projection, String selection,
                          String[] selectionArgs, String sortOrder) {
        // compose log verbose instruction
        String log;
        // compose log
        if (dataset.getType() == DatasetType.SQL) {
            log = selection;
        } else {
            if (projection != null) {
                log = "SELECT " + Arrays.asList(projection).toString();
            } else {
                log = "SELECT *";
            }
            log += " FROM " + dataset.getSource();
            if (StringUtils.isNotEmpty(selection)) {
                log += " WHERE " + selection;
            }
            if (StringUtils.isNotEmpty(sortOrder)) {
                log += " ORDER BY " + sortOrder;
            }
            if (selectionArgs != null) {
                log += "; ARGS=" + Arrays.asList(selectionArgs).toString();
            }
        }
        // log
        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);
    }

    private void logUpdate(Dataset dataset, ContentValues values, String whereClause, String[] whereArgs) {
        String log = "UPDATE " + dataset.getSource();
        // compose log verbose
        if (values != null) {
            log += " SET " + values.toString();
        }
        if (!TextUtils.isEmpty(whereClause)) {
            log += " WHERE " + whereClause;
        }
        if (whereArgs != null) {
            log += "; ARGS=" + Arrays.asList(whereArgs).toString();
        }

        // open transaction
        //database.beginTransaction();
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "database begin transaction");

        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);
    }

    private void logDelete(Dataset dataset, String selection, String[] selectionArgs) {
        String log = "DELETE FROM " + dataset.getSource();
        // compose log verbose
        if (StringUtils.isNotEmpty(selection)) {
            log += " WHERE " + selection;
        }
        if (selectionArgs != null) {
            log += "; ARGS=" + Arrays.asList(selectionArgs).toString();
        }
        // open transaction
        //database.beginTransaction();
//                    if (BuildConfig.DEBUG) Log.d(LOGCAT, "database begin transaction");
        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);
    }
}
