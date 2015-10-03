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

    private AssetClassRepository repository;

    public AssetAllocationService(Context context) {
        this.repository = new AssetClassRepository(context);
    }

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
        AssetClass down = repository.query(filter);
        if (down != null) {
            down.setSortOrder(currentPosition);
            bulk.add(down);
        }

        // save in transaction
        repository.bulkUpdate(bulk);
    }
}
