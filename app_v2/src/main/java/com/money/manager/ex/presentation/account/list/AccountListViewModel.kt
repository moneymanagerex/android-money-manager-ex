package com.money.manager.ex.presentation.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.money.manager.ex.domain.usecase.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountListUiState>(AccountListUiState.Loading)
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccountsUseCase()
                .catch { e ->
                    _uiState.value = AccountListUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { accounts ->
                    _uiState.value = AccountListUiState.Success(accounts)
                }
        }
    }
}
