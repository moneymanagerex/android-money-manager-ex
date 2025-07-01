package com.money.manager.ex.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.money.manager.ex.budget.BudgetPeriodEnum;
import com.money.manager.ex.budget.models.BudgetModel;
import com.money.manager.ex.domainmodel.Budget; // La tua classe Budget (testata)
import com.money.manager.ex.domainmodel.BudgetEntry; // La tua classe BudgetEntry (posizione)

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class BudgetModelTest {

    private Budget testHeadMonthly;
    private Budget testHeadYearly;
    private List<BudgetEntry> testEntriesMonthly;
    private List<BudgetEntry> testEntriesYearly;

    // Questo metodo viene eseguito prima di ogni singolo test
    @Before
    public void setUp() {
        // Inizializza una testata di budget mensile
        testHeadMonthly = new Budget();
        testHeadMonthly.setName("2025-06"); // Nome che indica un budget mensile
        // Nota: i metodi getYear() e getMonth() dipendono da questo formato di nome

        // Inizializza una testata di budget annuale
        testHeadYearly = new Budget();
        testHeadYearly.setName("2024"); // Nome che indica un budget annuale

        // Inizializza alcune voci di budget (BudgetEntry) per il test mensile
        testEntriesMonthly = new ArrayList<>();
        // ID della categoria 101, BudgetHeadId (non usato qui), "Cibo", importo mensile, importo annuale
        // Ho aggiunto i costruttori per BudgetEntry e Budget con ID per facilitare i test,
        // che potrebbero non essere nel tuo codice, ma sono utili per la creazione di dati di test.
        // Se non hai questi costruttori, dovrai usare i setter.
        BudgetEntry entryFoodMonthly = new BudgetEntry();
        entryFoodMonthly.setCategoryId(101L);
        entryFoodMonthly.setAmount(100.0); // Questo è l'AMOUNT base, i metodi getMonthly/YearlyAmount lo calcolano
        // Per i test, assicurati che BudgetPeriodEnum e i relativi calcoli di Monthly/YearlyAmount funzionino come previsto.
        // Assumiamo che getMonthlyAmount() e getYearlyAmount() restituiscano l'importo corretto per il periodo.
        // Ad esempio, se l'AMOUNT è l'importo mensile e getMonthlyAmount() restituisce AMOUNT e getYearlyAmount() restituisce AMOUNT * 12:
        entryFoodMonthly.setPeriod(BudgetPeriodEnum.MONTHLY.toString()); // Assicurati che "Monthly" sia un valore valido per BudgetPeriodEnum
        // Oppure, se gli importi mensili/annuali sono separati:
        // entryFoodMonthly.setMonthlyAmount(100.0);
        // entryFoodMonthly.setYearlyAmount(1200.0);

        BudgetEntry entryRentMonthly = new BudgetEntry();
        entryRentMonthly.setCategoryId(102L);
        entryRentMonthly.setAmount(500.0);
        entryRentMonthly.setPeriod(BudgetPeriodEnum.MONTHLY.toString());

        testEntriesMonthly.add(entryFoodMonthly);
        testEntriesMonthly.add(entryRentMonthly);

        // Inizializza alcune voci di budget (BudgetEntry) per il test annuale
        testEntriesYearly = new ArrayList<>();
        BudgetEntry entryTravelYearly = new BudgetEntry();
        entryTravelYearly.setCategoryId(201L);
        entryTravelYearly.setAmount(2400.0); // Budget annuale per viaggi
        entryTravelYearly.setPeriod(BudgetPeriodEnum.MONTHLY.toString()); // Assicurati che "Yearly" sia un valore valido

        testEntriesYearly.add(entryTravelYearly);
    }

    @Test
    public void testBudgetModelCreationWithHeadAndItems() {
        BudgetModel model = new BudgetModel(testHeadMonthly, testEntriesMonthly);

        assertNotNull(model.getHead());
        assertEquals(testHeadMonthly.getName(), model.getHead().getName());
        assertTrue(model.isMonthlyBudget());
        assertNotNull(model.getItems());
        assertEquals(2, model.getItems().size()); // Verifica che entrambi gli item siano stati aggiunti

        // Verifica che gli item siano accessibili tramite la loro chiave
        assertNotNull(model.getItem(101L));
        assertEquals(100.0, model.getItem(101L).getAmount(), 0.001); // Assumendo amount è il valore base
    }

    @Test
    public void testGetBudgetAmountForCategory_MonthlyBudget() {
        BudgetModel model = new BudgetModel(testHeadMonthly, testEntriesMonthly);
        assertTrue(model.isMonthlyBudget());

        // Test per una categoria esistente
        Double foodAmount = model.getBudgetAmountForCategory(101L);
        assertNotNull(foodAmount);
        // Questo dipende da come getMonthlyAmount() è implementato in BudgetEntry.
        // Se entry.setAmount(100.0) e getMonthlyAmount() restituisce amount, allora 100.0
        assertEquals(100.0, foodAmount, 0.001);

        Double rentAmount = model.getBudgetAmountForCategory(102L);
        assertNotNull(rentAmount);
        assertEquals(500.0, rentAmount, 0.001);

        // Test per una categoria non esistente
        Double nonExistentAmount = model.getBudgetAmountForCategory(999L);
        assertNull(nonExistentAmount);
    }

    @Test
    public void testGetBudgetAmountForCategory_YearlyBudget() {
        BudgetModel model = new BudgetModel(testHeadYearly, testEntriesYearly);

        // Test per una categoria esistente in un budget annuale
        Double travelAmount = model.getBudgetAmountForCategory(201L);
        assertNotNull(travelAmount);
        // Amount is 2400 with period montyly, so total need to be 2400*12 = 28800
        assertEquals(28800.0, travelAmount, 0.001);

        // Test per una categoria non esistente
        Double nonExistentAmount = model.getBudgetAmountForCategory(999L);
        assertNull(nonExistentAmount);
    }

    @Test
    public void testIsMonthlyBudget() {
        BudgetModel monthlyModel = new BudgetModel(testHeadMonthly, Collections.emptyList());
        assertTrue(monthlyModel.isMonthlyBudget());

        BudgetModel yearlyModel = new BudgetModel(testHeadYearly, Collections.emptyList());
        assertFalse(yearlyModel.isMonthlyBudget());
    }

    @Test
    public void testGetItemsAsList() {
        BudgetModel model = new BudgetModel(testHeadMonthly, testEntriesMonthly);
        List<BudgetEntry> itemsList = model.getItemsAsList();
        assertNotNull(itemsList);
        assertEquals(2, itemsList.size());
        // Verifica che gli item corretti siano nella lista
        assertTrue(itemsList.stream().anyMatch(e -> e.getCategoryId() == 101L));
        assertTrue(itemsList.stream().anyMatch(e -> e.getCategoryId() == 102L));
    }

    @Test
    public void testAddItem() {
        BudgetModel model = new BudgetModel(testHeadMonthly, new ArrayList<>()); // Inizia con lista vuota

        BudgetEntry newEntry = new BudgetEntry();
        newEntry.setCategoryId(300L);
        newEntry.setAmount(75.0);
        newEntry.setPeriod("Monthly");

        model.addItem(newEntry);

        assertEquals(1, model.getItems().size());
        assertNotNull(model.getItem(300L));
        assertEquals(75.0, model.getItem(300L).getAmount(), 0.001);
    }
}