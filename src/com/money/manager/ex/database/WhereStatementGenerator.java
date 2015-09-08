package com.money.manager.ex.database;

import android.database.DatabaseUtils;

import java.util.ArrayList;

/**
 * A new database query helper. Uses direct statements, not arguments.
 *
 * Created by Alen Siljak on 08/09/2015.
 */
public class WhereStatementGenerator {

    private ArrayList<String> statements;

    public void addStatement(String field, String operator, Object argument) {
        this.statements.add(getStatement(field, operator, argument));
    }

    public String getWhere() {
        String where = "";
        for (String statement : this.statements) {
            where = DatabaseUtils.concatenateWhere(where, statement);
        }
        return where;
    }

    private String getStatement(String field, String operator, Object argument) {
        StringBuilder sb = new StringBuilder();

        sb.append(field);
        sb.append(operator);
        DatabaseUtils.appendValueToSql(sb, argument);

        return sb.toString();
    }
}
