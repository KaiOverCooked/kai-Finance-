package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.KaiBottomBar
import com.example.ui.components.NavItem
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.InvestmentsScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.RecurringScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.viewmodel.AccountsViewModel
import com.example.ui.viewmodel.AiAdvisorViewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.DebtsViewModel
import com.example.ui.viewmodel.GoalsViewModel
import com.example.ui.viewmodel.InvestmentsViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.RecurringViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TransactionsViewModel

@Composable
fun KaiNavGraph(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isUnlocked by mainViewModel.isUnlocked.collectAsState()
    val isPinEnabled by mainViewModel.isPinEnabled.collectAsState()
    val currencySymbol by mainViewModel.currencySymbol.collectAsState()

    if (isPinEnabled && !isUnlocked) {
        PinLockScreen(
            onUnlock = { pin -> mainViewModel.unlockWithPin(pin) }
        )
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Dashboard.route

        Scaffold(
            bottomBar = {
                KaiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(NavItem.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            },
            modifier = Modifier.testTag("kai_main_scaffold")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavItem.Dashboard.route
                ) {
                    composable(NavItem.Dashboard.route) {
                        val dashboardVm: DashboardViewModel = viewModel()
                        val state by dashboardVm.uiState.collectAsState()
                        DashboardScreen(
                            state = state,
                            currencySymbol = currencySymbol,
                            onNavigateToAddTransaction = { navController.navigate(NavItem.Transactions.route) },
                            onNavigateToAiAdvisor = { navController.navigate(NavItem.AiAdvisor.route) },
                            onNavigateToTransactions = { navController.navigate(NavItem.Transactions.route) },
                            onNavigateToAccounts = { navController.navigate(NavItem.Accounts.route) },
                            onNavigateToDebts = { navController.navigate(NavItem.Debts.route) },
                            onNavigateToRecurring = { navController.navigate(NavItem.Recurring.route) },
                            onNavigateToInvestments = { navController.navigate(NavItem.Investments.route) },
                            onNavigateToBudget = { navController.navigate(NavItem.Budget.route) },
                            onNavigateToGoals = { navController.navigate(NavItem.Goals.route) }
                        )
                    }

                    composable(NavItem.Accounts.route) {
                        val accVm: AccountsViewModel = viewModel()
                        val accounts by accVm.accounts.collectAsState()

                        AccountsScreen(
                            accounts = accounts,
                            currencySymbol = currencySymbol,
                            onCreateAccount = { name, type, bal, num, inst ->
                                accVm.createAccount(name, type, bal, num, inst)
                            },
                            onTransfer = { from, to, amt, note ->
                                accVm.transferBetweenAccounts(from, to, amt, note) {}
                            },
                            onDeleteAccount = { id -> accVm.deleteAccount(id) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavItem.Debts.route) {
                        val debtsVm: DebtsViewModel = viewModel()
                        val debts by debtsVm.debts.collectAsState()

                        DebtsScreen(
                            debts = debts,
                            currencySymbol = currencySymbol,
                            getPayments = { id -> debtsVm.getPayments(id) },
                            onCreateDebt = { person, amt, type, due, notes ->
                                debtsVm.createDebt(person, amt, type, due, notes)
                            },
                            onRecordPayment = { id, amt, note ->
                                debtsVm.recordPayment(id, amt, note)
                            },
                            onDeleteDebt = { id -> debtsVm.deleteDebt(id) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavItem.Recurring.route) {
                        val recVm: RecurringViewModel = viewModel()
                        val recList by recVm.recurringList.collectAsState()
                        val accList by recVm.accounts.collectAsState()

                        RecurringScreen(
                            recurringList = recList,
                            accounts = accList,
                            currencySymbol = currencySymbol,
                            onCreateRecurring = { title, amt, type, cat, recCat, freq, due, auto, note, accId ->
                                recVm.createRecurring(title, amt, type, cat, recCat, freq, due, auto, note, accId)
                            },
                            onExecuteNow = { id -> recVm.executeNow(id) {} },
                            onDeleteRecurring = { id -> recVm.deleteRecurring(id) },
                            onTriggerPendingCheck = { recVm.checkAndProcessPendingRecurring() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavItem.Investments.route) {
                        val invVm: InvestmentsViewModel = viewModel()
                        val invList by invVm.investments.collectAsState()

                        InvestmentsScreen(
                            investments = invList,
                            currencySymbol = currencySymbol,
                            onCreateInvestment = { name, sym, qty, buy, curr, type, notes ->
                                invVm.createInvestment(name, sym, qty, buy, curr, type, notes)
                            },
                            onUpdatePrice = { id, price -> invVm.updatePrice(id, price) },
                            onAddDividend = { id, div -> invVm.addDividend(id, div) },
                            onDeleteInvestment = { id -> invVm.deleteInvestment(id) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavItem.Transactions.route) {
                        val txVm: TransactionsViewModel = viewModel()
                        val filteredList by txVm.filteredTransactions.collectAsState()
                        val query by txVm.searchQuery.collectAsState()
                        val filter by txVm.selectedTypeFilter.collectAsState()

                        TransactionsScreen(
                            transactions = filteredList,
                            searchQuery = query,
                            onSearchQueryChange = { txVm.searchQuery.value = it },
                            selectedFilter = filter,
                            onFilterChange = { txVm.selectedTypeFilter.value = it },
                            currencySymbol = currencySymbol,
                            onAddTransaction = { title, amt, type, category, note, imageUri ->
                                txVm.addTransaction(title, amt, type, category, note, imageUri)
                            },
                            onDeleteTransaction = { tx -> txVm.deleteTransaction(tx) }
                        )
                    }

                    composable(NavItem.Analytics.route) {
                        val analyticsVm: AnalyticsViewModel = viewModel()
                        val state by analyticsVm.uiState.collectAsState()
                        val period by analyticsVm.selectedPeriod.collectAsState()

                        AnalyticsScreen(
                            state = state,
                            selectedPeriod = period,
                            onPeriodChange = { analyticsVm.selectedPeriod.value = it },
                            currencySymbol = currencySymbol
                        )
                    }

                    composable(NavItem.Budget.route) {
                        val budgetVm: BudgetViewModel = viewModel()
                        val budgets by budgetVm.budgets.collectAsState()

                        BudgetScreen(
                            budgets = budgets,
                            currencySymbol = currencySymbol,
                            onCreateBudget = { category, limit, period -> budgetVm.createOrUpdateBudget(category, limit, period) },
                            onDeleteBudget = { id -> budgetVm.deleteBudget(id) }
                        )
                    }

                    composable(NavItem.Goals.route) {
                        val goalsVm: GoalsViewModel = viewModel()
                        val goals by goalsVm.goals.collectAsState()

                        GoalsScreen(
                            goals = goals,
                            currencySymbol = currencySymbol,
                            onCreateGoal = { name, target, current, date, category ->
                                goalsVm.createGoal(name, target, current, date, category)
                            },
                            onContribute = { goal, amt -> goalsVm.contributeToGoal(goal, amt) },
                            onDeleteGoal = { id -> goalsVm.deleteGoal(id) }
                        )
                    }

                    composable(NavItem.AiAdvisor.route) {
                        val aiVm: AiAdvisorViewModel = viewModel()
                        val state by aiVm.uiState.collectAsState()

                        AiAdvisorScreen(
                            state = state,
                            onRunAudit = { aiVm.runHighReasoningAudit() },
                            onAskQuestion = { q -> aiVm.askQuestion(q) },
                            onScanReceipt = { bitmap -> aiVm.scanReceiptImage(bitmap) },
                            onConfirmScannedReceipt = { result -> aiVm.saveScannedReceiptAsTransaction(result) },
                            onDismissReceipt = { aiVm.clearScannedResult() }
                        )
                    }

                    composable(NavItem.Settings.route) {
                        val settingsVm: SettingsViewModel = viewModel()
                        val themeMode by settingsVm.themeMode.collectAsState()
                        val pinEnabled by settingsVm.isPinEnabled.collectAsState()
                        val curr by settingsVm.currencySymbol.collectAsState()

                        SettingsScreen(
                            currentThemeMode = themeMode,
                            onThemeChange = { settingsVm.setThemeMode(it) },
                            isPinEnabled = pinEnabled,
                            onSetPin = { pin, enabled -> settingsVm.setPin(pin, enabled) },
                            currencySymbol = curr,
                            onCurrencyChange = { settingsVm.setCurrency(it) },
                            onExportCsv = { settingsVm.exportCsv() },
                            onImportCsv = { csv -> settingsVm.importCsv(csv) },
                            onExportFullBackup = { settingsVm.exportFullBackup() },
                            onRestoreFullBackup = { json -> settingsVm.restoreFullBackup(json) }
                        )
                    }
                }
            }
        }
    }
}
