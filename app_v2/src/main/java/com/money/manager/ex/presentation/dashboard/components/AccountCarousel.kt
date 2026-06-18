package com.money.manager.ex.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.money.manager.ex.domain.model.*
import com.money.manager.ex.presentation.components.AccountCard
import com.money.manager.ex.presentation.theme.MmexTheme
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun AccountCarousel(
    accounts: List<Account>,
    modifier: Modifier = Modifier,
    forecastSummary: PeriodSummary? = null,
    onAccountSelected: (Int?) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { accounts.size })

    // Notifico il cambiamento di account quando la pagina cambia
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        if (accounts.isNotEmpty() && pagerState.currentPage < accounts.size) {
            onAccountSelected(accounts[pagerState.currentPage].id)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            AccountCard(
                account = accounts[page],
                forecastSummary = forecastSummary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicators
        Row(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(accounts.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AccountCarouselPreview() {
    val mockAccounts = listOf(
        Account(
            id = 1,
            name = "Bank Account",
            type = AccountType.CHECKING,
            status = "Open",
            isFavorite = true,
            currencySymbol = "€",
            currencyPrefix = null,
            balance = BigDecimal.valueOf(12500.50),
            ledgerBalance = BigDecimal.valueOf(12000.00)
        ),
        Account(
            id = 2,
            name = "Cash",
            type = AccountType.CASH,
            status = "Open",
            isFavorite = false,
            currencySymbol = "€",
            currencyPrefix = null,
            balance = BigDecimal.valueOf(450.00),
            ledgerBalance = BigDecimal.valueOf(450.00)
        )
    )
    val mockForecast = PeriodSummary(
        values = FinancialValues(income = BigDecimal.valueOf(200.0), expense = BigDecimal.valueOf(100.0)),
        model = PeriodModel.FORECAST,
        type = PeriodType.MONTH,
        shift = PeriodShift.CURRENT,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    MmexTheme {
        AccountCarousel(
            accounts = mockAccounts,
            forecastSummary = mockForecast,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
