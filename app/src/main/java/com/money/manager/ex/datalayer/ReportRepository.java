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

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
