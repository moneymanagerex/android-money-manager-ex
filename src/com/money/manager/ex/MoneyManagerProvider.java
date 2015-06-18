/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableAssets;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableBudgetTable;
import com.money.manager.ex.database.TableBudgetYear;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.businessobjects.StockHistoryRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MoneyManagerProvider is the extension of the base class of Android
 * ContentProvider. Its purpose is to implement the read access and modify the
 * application data
 *
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.1.0
 */
public class MoneyManagerProvider
        extends ContentProvider {
    // tag LOGCAT
    private static final String LOGCAT = MoneyManagerProvider.class.getSimpleName();
    // object definition for the call to check the content
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // object map for the definition of the objects referenced in the URI
    private static Map<Integer, Object> mapContent = new HashMap<>();
    // authority of application
    private static String mAuthority;

    public MoneyManagerProvider() {
        super();

    }

    public static String getAuthority() {
        return mAuthority;
    }

    public static void setAuthority(String mAuthority) {
        MoneyManagerProvider.mAuthority = mAuthority;
    }

    @Override
    public boolean onCreate() {
        // create authority
        setAuthority(getContext().getApplicationContext().getPackageName() + ".provider");
        // create object provider
        List<Dataset> objMoneyManager = Arrays.asList(new TableAccountList(),
                new TableAssets(),
                new TableBillsDeposits(),
                new TableBudgetTable(),
                new TableBudgetSplitTransactions(),
                new TableBudgetYear(),
                new TableCategory(),
                new TableCheckingAccount(),
                new TableCurrencyFormats(),
                new TableInfoTable(),
                new TablePayee(),
                new TableSplitTransactions(),
                new TableStock(),
                new StockHistoryRepository(),
                new TableSubCategory(),
                new QueryAccountBills(getContext()),
                new QueryCategorySubCategory(getContext()),
                new QueryAllData(getContext()),
                new QueryBillDeposits(getContext()),
                new QueryReportIncomeVsExpenses(getContext()),
                new ViewMobileData(),
                new SQLDataSet());

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
    public Uri insert(Uri uri, ContentValues values) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Insert Uri: " + uri);

        // find object from uri
        Object ret = getObjectFromUri(uri);
        // database reference
        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(getContext().getApplicationContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        long id = 0;
        String parse;
        // check instance type object
        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
                    String log = "INSERT INTO " + dataset.getSource();
                    // compose log verbose
                    if (values != null) {
                        log += " VALUES ( " + values.toString() + ")";
                    }
                    // open transaction
                    ////database.beginTransaction();
                    //if (BuildConfig.DEBUG) Log.d(LOGCAT, "database begin transaction");
                    try {
                        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);

                        id = database.insert(dataset.getSource(), null, values);
                        // committed
                        ////if (BuildConfig.DEBUG) Log.d(LOGCAT, "database set transaction successful");
                        ////database.setTransactionSuccessful();
                    } catch (SQLiteException sqlLiteExc) {
                        Log.e(LOGCAT, "SQLiteException: " + sqlLiteExc.getMessage());
                    } catch (Exception exc) {
                        Log.e(LOGCAT, exc.getMessage());
                    }
                    parse = dataset.getBasepath() + "/" + id;
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not istance of dataset");
        }
        // notify the data inserted
        getContext().getContentResolver().notifyChange(uri, null);
        // notify dropbox data changed
        DropboxHelper.notifyDataChanged();
        // close connection to the database
        //databaseHelper.close();
        // return Uri with primary key inserted
        return Uri.parse(parse);
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Update Uri: " + uri);

        // find object from uri
        Object ret = getObjectFromUri(uri);
        // Instance of database
        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(getContext().getApplicationContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int rowsUpdate = 0;
        // check ret what type of class
        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
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
                    if (BuildConfig.DEBUG) Log.d(LOGCAT, "database begin transaction");

                    // update
                    try {
                        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);

                        rowsUpdate = database.update(dataset.getSource(), values, whereClause, whereArgs);
                        // committed
                        ////if (BuildConfig.DEBUG) Log.d(LOGCAT, "database set transaction successful");
                        //database.setTransactionSuccessful();
                    } catch (SQLiteException sqlLiteExc) {
                        Log.e(LOGCAT, "SQLiteException: " + sqlLiteExc.getMessage());
                    } catch (Exception exc) {
                        Log.e(LOGCAT, exc.getMessage());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type of dataset not supported for update");
            }
        } else {
            throw new IllegalArgumentException("Object ret of mapContent is not istance of dataset");
        }
        // notify update
        getContext().getContentResolver().notifyChange(uri, null);
        // notify dropbox data changed
        DropboxHelper.notifyDataChanged();
        // close connection to the database
        //databaseHelper.close();
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
            Log.e(LOGCAT, "Delete not permitted because not define where clause");
            return 0;
        }
        // take a database reference
        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(getContext().getApplicationContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsDelete = 0;
        // check type of dataset instance.
        if (Dataset.class.isInstance(ret)) {
            Dataset dataset = ((Dataset) ret);
            switch (dataset.getType()) {
                case TABLE:
                    String log = "DELETE FROM " + dataset.getSource();
                    // compose log verbose
                    if (!TextUtils.isEmpty(selection)) {
                        log += " WHERE " + selection;
                    }
                    if (selectionArgs != null) {
                        log += "; ARGS=" + Arrays.asList(selectionArgs).toString();
                    }
                    // open transaction
                    //database.beginTransaction();
                    if (BuildConfig.DEBUG) Log.d(LOGCAT, "database begin transaction");
                    try {
                        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);

                        rowsDelete = database.delete(dataset.getSource(), selection, selectionArgs);
                        // committed
                        //if (BuildConfig.DEBUG) Log.d(LOGCAT, "database set transaction successful");
                        //database.setTransactionSuccessful();
                    } catch (SQLiteException sqlLiteExc) {
                        Log.e(LOGCAT, "SQLiteException: " + sqlLiteExc.getMessage());
                    } catch (Exception exc) {
                        Log.e(LOGCAT, exc.getMessage());
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
        // close connection to the database
        //databaseHelper.close();
        // return rows delete
        return rowsDelete;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context context = null;

        try {
            context = getContext();
            return query_internal(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "Error fetching data");
        }
        return null;
    }

    private Cursor query_internal(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Query URI: " + uri);

        // find object from uri
        Object sourceObject = getObjectFromUri(uri);
        // take a database reference
        MoneyManagerOpenHelper databaseHelper = MoneyManagerOpenHelper.getInstance(getContext().getApplicationContext());

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
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

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
            if (!TextUtils.isEmpty(selection)) {
                log += " WHERE " + selection;
            }
            if (!TextUtils.isEmpty(sortOrder)) {
                log += " ORDER BY " + sortOrder;
            }
            if (selectionArgs != null) {
                log += "; ARGS=" + Arrays.asList(selectionArgs).toString();
            }
        }
        // log
        if (BuildConfig.DEBUG) Log.d(LOGCAT, log);
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
        // match dell'uri
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
}
