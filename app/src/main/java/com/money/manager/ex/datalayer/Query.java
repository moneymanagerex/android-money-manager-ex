/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.domainmodel.EntityBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Query object for easier querying through repositories.
 */
public class Query {
//    public Query(T repository) {
//        this.repository = repository;
//    }

//    private T repository;
    public String[] projection = null;
    public String selection = null;
    public String[] selectionArgs = null;
    public String sort = null;

    public Query select(String[] projection) {
        // add selection
        this.projection = projection;
        return this;
    }

    public Query where(String selection) {
        this.selection = selection;
        return this;
    }

//    public Query where(String selection, String[] args) {
//        // append condition
//        this.selection = selection;
//        this.selectionArgs = args;
//
//        return this;
//    }

    public Query where(String selection, String... args) {
        this.selection = selection;
        this.selectionArgs = args;
        return this;
    }

    public Query orderBy(String sort) {
        // sort
        this.sort = sort;
        return this;
    }

    public List query() {
//        this.repository.query()

        // todo assemble the query

        // todo execute the query.

        List list = new ArrayList<>();
        return list;
    }
}
