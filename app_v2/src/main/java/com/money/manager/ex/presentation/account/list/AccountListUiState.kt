package com.money.manager.ex.presentation.account.list

import com.money.manager.ex.domain.model.Account

sealed interface AccountListUiState {
    data object Loading : AccountListUiState
    data class Success(val accounts: List<Account>) : AccountListUiState
    data class Error(val message: String) : AccountListUiState
}
