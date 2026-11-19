package com.ibra.budgetmanager.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibra.budgetmanager.ui.AppThemeMode
import com.ibra.budgetmanager.ui.BudgetViewModel
import com.ibra.budgetmanager.ui.screens.add.AddTransactionScreen
import com.ibra.budgetmanager.ui.screens.edit.EditTransactionScreen
import com.ibra.budgetmanager.ui.screens.home.HomeScreen
import com.ibra.budgetmanager.ui.screens.settings.SettingsScreen
import com.ibra.budgetmanager.ui.screens.transactions.AllTransactionsScreen
import com.ibra.budgetmanager.ui.theme.BudgetManagerTheme

@Composable
fun AppNavigation(
    viewModel: BudgetViewModel = viewModel()
) {
    val systemInDark = isSystemInDarkTheme()
    val isDarkTheme = when (viewModel.themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    BudgetManagerTheme(darkTheme = isDarkTheme, dynamicColor = false) {
        var currentScreen by rememberSaveable { mutableStateOf("home") }
        var previousScreen by rememberSaveable { mutableStateOf("home") }
        var editingTransactionId by rememberSaveable { mutableStateOf<Long?>(null) }

        when (currentScreen) {
            "home" -> {
                HomeScreen(
                    transactions = viewModel.transactions,
                    isDarkTheme = isDarkTheme,
                    selectedPeriod = viewModel.selectedPeriod,
                    onPeriodSelected = { newPeriod ->
                        viewModel.updatePeriod(newPeriod)
                    },
                    onAddTransactionClick = { currentScreen = "add" },
                    onTransactionClick = { transaction ->
                        editingTransactionId = transaction.id
                        previousScreen = "home"
                        currentScreen = "edit"
                    },
                    onShowMoreClick = { currentScreen = "all_transactions" },
                    onSettingsClick = { currentScreen = "settings" }
                )
            }
            "all_transactions" -> {
                BackHandler {
                    currentScreen = "home"
                }

                AllTransactionsScreen(
                    transactions = viewModel.transactions,
                    isDarkTheme = isDarkTheme,
                    selectedPeriod = viewModel.selectedPeriod,
                    onPeriodSelected = { newPeriod ->
                        viewModel.updatePeriod(newPeriod)
                    },
                    onBackClick = { currentScreen = "home" },
                    onTransactionClick = { transaction ->
                        editingTransactionId = transaction.id
                        previousScreen = "all_transactions"
                        currentScreen = "edit"
                    }
                )
            }
            "add" -> {
                BackHandler {
                    currentScreen = "home"
                }

                AddTransactionScreen(
                    isDarkTheme = isDarkTheme,
                    onBackClick = { currentScreen = "home" },
                    onSaveTransaction = { nouvelleTransaction ->
                        viewModel.ajouterTransaction(nouvelleTransaction)
                        currentScreen = "home"
                    }
                )
            }
            "edit" -> {
                val transactionToEdit = viewModel.transactions.find { it.id == editingTransactionId }

                BackHandler {
                    editingTransactionId = null
                    currentScreen = previousScreen
                }

                if (transactionToEdit != null) {
                    EditTransactionScreen(
                        transaction = transactionToEdit,
                        onBackClick = {
                            editingTransactionId = null
                            currentScreen = previousScreen
                        },
                        onUpdateTransaction = { updated ->
                            viewModel.modifierTransaction(updated)
                            editingTransactionId = null
                            currentScreen = previousScreen
                        },
                        onDeleteTransaction = { idToDelete ->
                            viewModel.supprimerTransaction(idToDelete)
                            editingTransactionId = null
                            currentScreen = previousScreen
                        }
                    )
                } else {
                    currentScreen = previousScreen
                }
            }
            "settings" -> {
                BackHandler {
                    currentScreen = "home"
                }

                SettingsScreen(
                    selectedTheme = viewModel.themeMode,
                    onThemeSelected = { newMode ->
                        viewModel.updateThemeMode(newMode)
                    },
                    onBackClick = { currentScreen = "home" }
                )
            }
        }
    }
}