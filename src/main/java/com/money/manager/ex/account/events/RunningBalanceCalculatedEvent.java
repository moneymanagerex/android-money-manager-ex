package com.money.manager.ex.account.events;

import java.util.HashMap;

import info.javaperformance.money.Money;

/**
 * Fired when a running balance has been calculated for account.
 */
public class RunningBalanceCalculatedEvent {

    public RunningBalanceCalculatedEvent(HashMap<Integer, Money> balances) {
        this.balances = balances;
    }

    public HashMap<Integer, Money> balances;
}
