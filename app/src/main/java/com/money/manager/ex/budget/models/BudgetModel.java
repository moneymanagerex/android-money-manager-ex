package com.money.manager.ex.budget.models;

import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BudgetModel {

    // FullBudget.java
    private Budget head;
    private HashMap<String, BudgetEntry> items; // La lista delle posizioni associate a questa testata

    public BudgetModel() {
        this.head = new Budget();
        this.items = new HashMap<>();
    }

    public BudgetModel(String name) {
        this();
        this.head.setName(name);
    }

    public BudgetModel(Budget head, List<BudgetEntry> items) {
        this.head = head;
        this.items = new HashMap<>();
        if (items != null) {
            for (BudgetEntry entry : items) {
                this.items.put(entry.getKey(), entry);
            }
        }
    }

    // Getter per accedere alla testata del budget
    public Budget getHead() {
        return head;
    }

    public void setHead(Budget head) {
        this.head = head;
    }

    // return item as map
    public HashMap<String, BudgetEntry> getItems() {
        return items;
    }

    // return item as list
    public List<BudgetEntry> getItemsAsList() {
        return new ArrayList<>(items.values());
    }

    public BudgetEntry getItem(long categId) {
        return items.get(BudgetEntry.getKeyForCategories(categId));
    }

    public void addItem(BudgetEntry item) {
        items.put(item.getKey(), item);
    }

    public boolean isMonthlyBudget() {
        return head.isMonthlyBudget();
    }

    public Double getBudgetAmountForCategory(long categId) {
        BudgetEntry entry = getItem(categId);
        if (entry == null) return null;
        if (head.isMonthlyBudget()) {
            return entry.getMonthlyAmount();
        } else {
            return entry.getYearlyAmount();
        }
    }

    public MmxDate getDateFrom(BudgetSettings budgetSettings) {
        if (isMonthlyBudget()) {
            return new MmxDate(head.getYear(), head.getMonth(), 1);
        } else {
            return  budgetSettings.getBudgetDateFromForYear(head.getYear());
        }
    }

    public MmxDate getDateTo(BudgetSettings budgetSettings) {
        MmxDate newDate = getDateFrom(budgetSettings);
        if (isMonthlyBudget()) {
            newDate.addMonth(1).minusDays(1);
        } else {
            newDate.addYear(1).minusDays(1);
        }
        return newDate;
    }

    @Override
    public String toString() {
        return "BudgetMode{" +
                "head=" + head +
                ", items=" + items +
                '}';
    }
}
