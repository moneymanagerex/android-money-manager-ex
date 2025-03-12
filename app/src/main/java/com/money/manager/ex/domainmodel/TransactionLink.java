/*
 * Copyright (C) 2025-2025 The Android Money Manager Ex Project Team
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

public class TransactionLink extends EntityBase {

    public static final String TRANSLINKID = "TRANSLINKID";
    public static final String CHECKINGACCOUNTID = "CHECKINGACCOUNTID";
    public static final String LINKTYPE = "LINKTYPE";
    public static final String LINKRECORDID = "LINKRECORDID";

    public TransactionLink() {
        super();
    }

    public TransactionLink(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return TRANSLINKID;
    }

    public Long getCheckingAccountId() {
        return getLong(CHECKINGACCOUNTID);
    }

    public void setCheckingAccountId(Long value) {
        setLong(CHECKINGACCOUNTID, value);
    }

    public String getLinkType() {
        return getString(LINKTYPE);
    }

    public void setLinkType(String value) {
        setString(LINKTYPE, value);
    }

    public Long getLinkRecordId() {
        return getLong(LINKRECORDID);
    }

    public void setLinkRecordId(Long value) {
        setLong(LINKRECORDID, value);
    }
}
