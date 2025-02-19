/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.domainmodel.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Report repository
 */
public class ReportRepository extends RepositoryBase<Report> {

    private static final String TABLE_NAME = "report_v1";
    private static final String ID_COLUMN = Report.REPORTID;
    private static final String NAME_COLUMN = Report.REPORTNAME;

    public ReportRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "report", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected Report createEntity() {
        return new Report();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the REPORTID column as _id for SQLite database
                Report.REPORTID,
                Report.REPORTNAME,
                Report.GROUPNAME,
                Report.ACTIVE,
                Report.SQLCONTENT,
                Report.LUACONTENT,
                Report.TEMPLATECONTENT,
                Report.DESCRIPTION
        };
    }

    // custom func
    public List<Report> loadByGroupName(String groupName) {
        return query(new Select().where(Report.GROUPNAME + " = ?", groupName));
    }

    public Map<String, List<Report>> loadGroupedByName() {
        List<Report> reports = loadAll();

        Map<String, List<Report>> reportMap = new HashMap<>();
        for (Report report : reports) {
            String groupName = report.getGroupName();
            reportMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(report);
        }

        return reportMap;
    }

    public ReportResult runReport(Report report) {
        Cursor cursor = null;
        try {
            // Execute the query on the content resolver
            cursor = getContext().getContentResolver().query(
                    new SQLDataSet().getUri(),
                    null,  // null to get all columns
                    report.getSqlContent(),  // The raw SQL query
                    null,  // No selection arguments
                    null   // No sort order
            );

            if (cursor == null) {
                Timber.e("Cursor is null. Query failed.");
                return new ReportResult(null, null);
            }

            // Get the column names
            int columnCount = cursor.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = cursor.getColumnName(i);
            }

            // Collect the rows from the cursor
            ArrayList<Map<String, String>> queryResult = new ArrayList<>();
            while (cursor.moveToNext()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = columnNames[i];
                    String columnValue = cursor.getString(i);  // You can handle nulls as needed
                    row.put(columnName, columnValue != null ? columnValue : "");
                }
                queryResult.add(row);
            }

            return new ReportResult(columnNames, queryResult);
        } catch (Exception e) {
            Timber.e(e, "Error executing query: %s", e.getMessage());
            return new ReportResult(null, null);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ReportResult class to hold column names and query result
    public static class ReportResult {
        private final String[] columnNames;
        private final ArrayList<Map<String, String>> queryResult;

        public ReportResult(String[] columnNames, ArrayList<Map<String, String>> queryResult) {
            this.columnNames = columnNames;
            this.queryResult = queryResult;
        }

        public String[] getColumnNames() {
            return columnNames;
        }

        public ArrayList<Map<String, String>> getQueryResult() {
            return queryResult;
        }
    }
}
