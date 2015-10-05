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
package com.money.manager.ex.servicelayer;

import android.content.Context;

import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.domainmodel.AssetClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Functions for the Asset Allocation
 */
public class AssetAllocationService {

    public AssetAllocationService(Context context) {
        this.repository = new AssetClassRepository(context);
    }

    private AssetClassRepository repository;

    /**
     * Move the asset class up in the sort order.
     * Increase sort order for this item. Finds the next in order and decrease it's sort order.
     */
    public void moveClassUp(int id) {
        // todo: this is incomplete. Need to set the default value on creation and handle
        // deletions. Also pay attention if the order will be ascending or descending and adjust.

        List<AssetClass> bulk = new ArrayList();

        AssetClass up = repository.load(id);
        Integer currentPosition = up.getSortOrder();
        if (currentPosition == null) currentPosition = 0;
        int upPosition = currentPosition + 1;

        up.setSortOrder(upPosition);
        bulk.add(up);

        WhereStatementGenerator where = new WhereStatementGenerator();
        String filter = where.getStatement(AssetClass.SORTORDER, "=", upPosition);
        AssetClass down = repository.first(filter);
        if (down != null) {
            down.setSortOrder(currentPosition);
            bulk.add(down);
        }

        // save in transaction
        repository.bulkUpdate(bulk);
    }

    public List<AssetClass> loadAssetAllocation() {
        List<AssetClass> result = new ArrayList<>();

        // https://communities.bmc.com/docs/DOC-9902
        // http://mikehillyer.com/articles/managing-hierarchical-data-in-mysql/
        // http://docs.mongodb.org/manual/tutorial/model-tree-structures/

        // fetch all root-level elements
//        this.repository.


        // todo

        return result;
    }
}
