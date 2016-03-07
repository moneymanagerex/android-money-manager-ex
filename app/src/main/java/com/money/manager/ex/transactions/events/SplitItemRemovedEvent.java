package com.money.manager.ex.transactions.events;

import com.money.manager.ex.database.ISplitTransaction;

/**
 * Split item removed by the user.
 * Called from the individual split item fragment.
 */
public class SplitItemRemovedEvent {
    public SplitItemRemovedEvent(ISplitTransaction entity) {
        this.entity = entity;
    }

    public ISplitTransaction entity;
}
