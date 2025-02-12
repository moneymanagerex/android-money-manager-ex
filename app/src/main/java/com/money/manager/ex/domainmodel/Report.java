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

package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

/**
 * Report model.
 */
public class Report extends EntityBase {

    public static final String REPORTID = "REPORTID";
    public static final String REPORTNAME = "REPORTNAME";
    public static final String GROUPNAME = "GROUPNAME";
    public static final String ACTIVE = "ACTIVE";
    public static final String SQLCONTENT = "SQLCONTENT";
    public static final String LUACONTENT = "LUACONTENT";
    public static final String TEMPLATECONTENT = "TEMPLATECONTENT";
    public static final String DESCRIPTION = "DESCRIPTION";

    public Report() {
        super();
        setLong(Report.ACTIVE, 1L);  // Default to active
    }

    public Report(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return REPORTID;  // This returns the column name
    }

    public String getReportName() {
        return getString(Report.REPORTNAME);
    }

    public void setReportName(String value) {
        setString(Report.REPORTNAME, value);
    }

    public String getGroupName() {
        return getString(Report.GROUPNAME);
    }

    public void setGroupName(String value) {
        setString(Report.GROUPNAME, value);
    }

    public Boolean getActive() {
        return getLong(ACTIVE) == null || getLong(ACTIVE) != 0L;
    }

    public void setActive(Boolean value) {
        setLong(ACTIVE, value ? 1L : 0L);
    }

    public String getSqlContent() {
        return getString(Report.SQLCONTENT);
    }

    public void setSqlContent(String value) {
        setString(Report.SQLCONTENT, value);
    }

    public String getLuaContent() {
        return getString(Report.LUACONTENT);
    }

    public void setLuaContent(String value) {
        setString(Report.LUACONTENT, value);
    }

    public String getTemplateContent() {
        return getString(Report.TEMPLATECONTENT);
    }

    public void setTemplateContent(String value) {
        setString(Report.TEMPLATECONTENT, value);
    }

    public String getDescription() {
        return getString(Report.DESCRIPTION);
    }

    public void setDescription(String value) {
        setString(Report.DESCRIPTION, value);
    }
}
