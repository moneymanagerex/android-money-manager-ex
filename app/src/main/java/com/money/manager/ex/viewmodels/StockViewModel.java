package com.money.manager.ex.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Stock;

import java.util.List;
import java.util.concurrent.Executors;

// StockViewModel.java
public class StockViewModel extends AndroidViewModel {
    private final StockRepository stockRepository;
    private final MutableLiveData<List<Stock>> stocks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public StockViewModel(@NonNull Application application, StockRepository repository) {
        super(application);
        this.stockRepository = repository;
    }

    public LiveData<List<Stock>> getStocks() { return stocks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadStocks(long accountId) {
        isLoading.postValue(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Stock> result = stockRepository.loadByAccount(accountId);
            stocks.postValue(result);
            isLoading.postValue(false);
        });
    }
}