package com.money.manager.ex.servicelayer;
// mmm need to be com.money.manager.ex

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong; // Utile per quando non ti importa l'ID esatto
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
// You might also need others depending on what you use:
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.anyLong; // For the argument matcher

import android.content.Context;

import com.money.manager.ex.budget.BudgetPeriodEnum;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.budget.models.BudgetModel;
import com.money.manager.ex.servicelayer.BudgetService;

@RunWith(RobolectricTestRunner.class)
public class BudgetServiceTest {

    // 1. Dichiara i mock per le dipendenze
    @Mock
    private BudgetRepository mockBudgetRepository;
    @Mock
    private BudgetEntryRepository mockBudgetEntryRepository;
    @Mock
    private CategoryRepository mockCategoryRepository;

    // 2. L'istanza del servizio che vogliamo testare
    private BudgetService budgetService;

    private Context context;


    // 3. Il metodo @Before viene eseguito prima di ogni test
    @Before
    public void setUp() {
        this.context = UnitTestHelper.getContext();

        // Inizializza i mock dichiarati con @Mock
        MockitoAnnotations.openMocks(this);
        // Inizializza il tuo BudgetService con i mock
        budgetService = new BudgetService(context,
                mockBudgetRepository,
                mockBudgetEntryRepository,
                mockCategoryRepository
        );
    }

    // 4. Scrivi i tuoi metodi di test
    @Test
    public void testGetFullBudgetById_Success() {
        // Prepara i dati di test
        long testBudgetHeadId = 1L;
        Budget mockHead = new Budget();
        mockHead.setId(testBudgetHeadId);
        mockHead.setName("2025");

        List<BudgetEntry> mockItems = new ArrayList<>();

        BudgetEntry mockItem1 = new BudgetEntry();
        mockItem1.setBudgetYearId(testBudgetHeadId);
        mockItem1.setCategoryId(101L);
        mockItem1.setAmount(300.0);
        mockItem1.setPeriod(BudgetPeriodEnum.MONTHLY.getDisplayName());
        mockItems.add(mockItem1);

        mockItem1 = new BudgetEntry();
        mockItem1.setBudgetYearId(testBudgetHeadId);
        mockItem1.setCategoryId(102L);
        mockItem1.setAmount(300.0);
        mockItem1.setPeriod(BudgetPeriodEnum.MONTHLY.getDisplayName());
        mockItems.add(mockItem1);

        // period is montly, value il 300. budget amount = 300x12

        // Definisci il comportamento dei mock
        // Quando viene chiamato mockBudgetRepository.getBudgetHeadById con testBudgetHeadId, restituisci mockHead
        when(mockBudgetRepository.load(testBudgetHeadId)).thenReturn(mockHead);
        // Quando viene chiamato mockBudgetEntryRepository.getItemsByBudgetHeadId, restituisci mockItems
        when(mockBudgetEntryRepository.loadForBudgetId(testBudgetHeadId)).thenReturn(mockItems);

        // Chiama il metodo che vuoi testare
        BudgetModel result = budgetService.loadFullBudget(testBudgetHeadId);

        // Verifica i risultati
        assertNotNull(result);
        assertEquals(mockHead, result.getHead());
        assertEquals(2, result.getItems().size());
        assertEquals(300.0, result.getItem(101L).getAmount(), 0.001); // Usa delta per double

        assertEquals((300.0 * 12), result.getBudgetAmountForCategory(101L), 0.001);

        // Verifica che i metodi dei repository siano stati chiamati come previsto
        verify(mockBudgetRepository).load(testBudgetHeadId);
        verify(mockBudgetEntryRepository).loadForBudgetId(testBudgetHeadId);
    }

    @Test
    public void testGetFullBudgetById_BudgetHeadNotFound() {
        long testBudgetHeadId = 99L;

        // Definisci il comportamento: restituisci null per la testata
        when(mockBudgetRepository.load(testBudgetHeadId)).thenReturn(null);

        // Chiama il metodo
        BudgetModel result = budgetService.loadFullBudget(testBudgetHeadId);

        // Verifica il risultato
        assertNull(result);

        // Verifica le chiamate ai mock
        verify(mockBudgetRepository).load(testBudgetHeadId);
        // Il metodo getItemsByBudgetHeadId non dovrebbe essere chiamato se la testata non è trovata
        verify(mockBudgetEntryRepository, never()).loadForBudgetId(anyLong());
    }

    @Test
    public void testReloadItemsFromCategories_AddsNewItems() {
    }
}