package com.money.manager.ex.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.money.manager.ex.datalayer.StockRepository;

// ViewModelFactory.java
public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final StockRepository repository;

    public ViewModelFactory(Application application, StockRepository repository) {
        this.application = application;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StockViewModel.class)) {
            return (T) new StockViewModel(application, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}