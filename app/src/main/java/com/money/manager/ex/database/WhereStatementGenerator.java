/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.database;

import android.database.DatabaseUtils;
import android.text.TextUtils;

import java.util.ArrayList;

import info.javaperformance.money.Money;

/**
 * A new database query helper. Uses direct statements, not arguments.
 *
 * Created by Alen Siljak on 08/09/2015.
 */
public class WhereStatementGenerator {

    public WhereStatementGenerator() {
        this.statements = new ArrayList<>();
    }

    private ArrayList<String> statements;

    public void addStatement(String statement) {
        this.statements.add(statement);
    }

    public void addStatement(String field, String operator, Integer argument) {
        this.statements.add(getStatement(field, operator, argument));
    }

    public void addStatement(String field, String operator, Money argument) {
        this.statements.add(getStatement(field, operator, argument));
    }

    public void addStatement(String field, String operator, Object argument) {
        this.statements.add(getStatement(field, operator, argument));
    }

    public void clear() {
        this.statements.clear();
    }

    public String getWhere() {
        String where = "";
        for (String statement : this.statements) {
            where = DatabaseUtils.concatenateWhere(where, statement);
        }
        return where;
    }

    public String getStatement(String field, String operator, Integer argument) {
        StringBuilder sb = new StringBuilder();

        sb.append(field);
        sb.append(operator);
        sb.append(argument);

        return sb.toString();
    }

    public String getStatement(String field, String operator, Money argument) {
        StringBuilder sb = new StringBuilder();

        sb.append(field);
        sb.append(operator);
        sb.append(argument);

        return sb.toString();
    }

    public String getStatement(String field, String operator, Object argument) {
        StringBuilder sb = new StringBuilder();

        sb.append(field);
        sb.append(" ");
        sb.append(operator);
        sb.append(" ");
        if (operator.equalsIgnoreCase("in")) {
            sb.append(argument);
        } else {
            DatabaseUtils.appendValueToSql(sb, argument);
        }

        return sb.toString();
    }

    public String concatenateOr(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }

        return "( (" + a + ") OR (" + b + ") )";
    }
}
