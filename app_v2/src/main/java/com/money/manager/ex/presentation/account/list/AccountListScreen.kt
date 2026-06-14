package com.money.manager.ex.presentation.account.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.money.manager.ex.presentation.theme.MmexTheme
import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.model.AccountType
    import com.money.manager.ex.presentation.components.AccountCard

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AccountListScreenContent(uiState = uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreenContent(
    uiState: AccountListUiState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Soft Mint from Theme
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "I Miei Account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AccountListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is AccountListUiState.Error -> Text(
                    text = state.message, 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is AccountListUiState.Success -> {
                    if (state.accounts.isEmpty()) {
                        Text(
                            "Nessun account trovato",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AccountCarousel(accounts = state.accounts)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCarousel(accounts: List<Account>) {
    val pagerState = rememberPagerState(pageCount = { accounts.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            AccountCard(account = accounts[page])
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Indicatori (opzionale)
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(accounts.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.outlineVariant
                
                Surface(
                    modifier = Modifier.padding(2.dp).size(8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = color
                ) {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountListScreenPreview() {
    val mockAccounts = listOf(
        Account(
            id = 1,
            name = "Cassa",
            type = AccountType.CASH,
            status = "Open",
            isFavorite = true,
            currencySymbol = "€",
            currencyPrefix = null
        ),
        Account(
            id = 2,
            name = "Conto Corrente",
            type = AccountType.CHECKING,
            status = "Open",
            isFavorite = false,
            currencySymbol = "$",
            currencyPrefix = null
        )
    )
    MmexTheme {
        AccountListScreenContent(
            uiState = AccountListUiState.Success(accounts = mockAccounts)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountListScreenLoadingPreview() {
    MmexTheme {
        AccountListScreenContent(
            uiState = AccountListUiState.Loading
        )
    }
}
