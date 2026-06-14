package com.money.manager.ex.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.money.manager.ex.domain.model.*
import com.money.manager.ex.presentation.dashboard.components.*
import com.money.manager.ex.presentation.theme.MmexTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val lightGreen = Color(0xFFE8F5E9)
    val darkGreen = Color(0xFF00897B)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                DashboardDrawerContent(
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Profile Image (Click to open drawer)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { scope.launch { drawerState.open() } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Text(
                                    text = "Account Details",
                                    fontWeight = FontWeight.Bold,
                                    color = darkGreen,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.togglePeriodMenu() }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Settings",
                                    tint = if (uiState.isPeriodMenuVisible) darkGreen else Color.Gray
                                )
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = lightGreen
                        )
                    )

                    // Horizontal Period Selector Menu
                    AnimatedVisibility(
                        visible = uiState.isPeriodMenuVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        PeriodSelector(
                            selectedPeriod = uiState.selectedPeriodType,
                            onPeriodSelected = { viewModel.onPeriodTypeSelected(it) },
                            modifier = Modifier.background(lightGreen)
                        )
                    }
                }
            }
        ) { paddingValues ->
            DashboardContent(
                uiState = uiState,
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color.White),
                onAccountSelected = { viewModel.onAccountSelected(it) }
            )
        }
    }
}

@Composable
fun DashboardDrawerContent(
    onClose: () -> Unit
) {
    val darkGreen = Color(0xFF00897B)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Image Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    // Presence indicator
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(darkGreen)
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Alex Thorne",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Selected Database Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF1F8F7),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = darkGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "local_main.db",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF455A64)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        
        // Menu Items
        DrawerMenuItem(
            icon = Icons.Default.AddBox,
            label = "Crea database",
            onClick = { /* TODO */ }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DrawerMenuItem(
            icon = Icons.Default.FolderOpen,
            label = "Apri database",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val darkGreen = Color(0xFF00897B)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF455A64)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF455A64),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PeriodType.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Surface(
                color = if (isSelected) Color(0xFF00695C) else Color(0xFFF1F8F7),
                contentColor = if (isSelected) Color.White else Color(0xFF455A64),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.clickable { onPeriodSelected(period) }
            ) {
                Text(
                    text = period.label,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    onAccountSelected: (Int?) -> Unit = {}
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Accounts Carousel
            if (uiState.accounts.isNotEmpty()) {
                AccountCarousel(
                    accounts = uiState.accounts,
                    forecastSummary = uiState.currentForecastSummary,
                    onAccountSelected = onAccountSelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary (Income/Expenses)
            SummaryRow(
                currentActual = uiState.currentActualSummary,
                currentForecast = uiState.currentForecastSummary,
                previousActual = uiState.previousActualSummary,
                previousForecast = null // In futuro caricheremo anche questo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activity
            RecentActivityList(
                transactions = uiState.recentActivity,
                onViewAllClick = { /* TODO */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val currentActual = PeriodSummary(
        values = FinancialValues(income = 2000.0, expense = 1500.0),
        periodModel = PeriodModel.ACTUAL,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val currentForecast = PeriodSummary(
        values = FinancialValues(income = 1500.0, expense = 300.0),
        periodModel = PeriodModel.FORECAST,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val previousActual = PeriodSummary(
        values = FinancialValues(income = 2800.0, expense = 1600.0),
        periodModel = PeriodModel.ACTUAL,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now().minusMonths(1),
        endDate = LocalDate.now().minusMonths(1)
    )

    val mockState = DashboardUiState(
        accounts = listOf(
            Account(1, "Bank", AccountType.CHECKING, "Open", true, "€", null, 12000.0, 11500.0),
            Account(2, "Cash", AccountType.CASH, "Open", false, "€", null, 500.0, 500.0)
        ),
        currentActualSummary = currentActual,
        currentForecastSummary = currentForecast,
        previousActualSummary = previousActual,
        recentActivity = listOf(
            Transaction(
                id = 1,
                accountId = 1,
                payeeId = 1,
                transCode = TransactionCode.WITHDRAWAL,
                transAmount = -10.0,
                transDate = LocalDate.now(),
                payee = "Payee 1",
                notes = "Ice Cream",
                category = "Cat 1"
            ),
            Transaction(
                id = 2,
                accountId = 1,
                payeeId = 2,
                transCode = TransactionCode.DEPOSIT,
                transAmount = 100.0,
                transDate = LocalDate.now(),
                payee = "Payee 2",
                category = "Cat 2"
            )
        ),
        isPeriodMenuVisible = false
    )
    MmexTheme {
        DashboardContent(uiState = mockState)
    }
}
