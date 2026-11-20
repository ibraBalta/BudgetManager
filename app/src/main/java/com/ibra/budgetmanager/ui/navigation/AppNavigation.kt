package com.ibra.budgetmanager.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibra.budgetmanager.R
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

        val bottomBarContent: @Composable () -> Unit = {
            BudgetBottomBar(
                currentScreen = currentScreen,
                onSelectScreen = { target -> currentScreen = target }
            )
        }

        when (currentScreen) {
            "home" -> {
                HomeScreen(
                    transactions = viewModel.transactions,
                    isDarkTheme = isDarkTheme,
                    selectedPeriod = viewModel.selectedPeriod,
                    onPeriodSelected = { newPeriod ->
                        viewModel.updatePeriod(newPeriod)
                    },
                    onAddTransactionClick = {
                        previousScreen = "home"
                        currentScreen = "add"
                    },
                    onTransactionClick = { transaction ->
                        editingTransactionId = transaction.id
                        previousScreen = "home"
                        currentScreen = "edit"
                    },
                    onSettingsClick = {
                        previousScreen = "home"
                        currentScreen = "settings"
                    },
                    bottomBar = bottomBarContent
                )
            }
            "stats" -> {
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
                    onTransactionClick = { transaction ->
                        editingTransactionId = transaction.id
                        previousScreen = "stats"
                        currentScreen = "edit"
                    },
                    onSettingsClick = {
                        previousScreen = "stats"
                        currentScreen = "settings"
                    },
                    bottomBar = bottomBarContent
                )
            }
            "add" -> {
                BackHandler {
                    currentScreen = previousScreen
                }

                AddTransactionScreen(
                    isDarkTheme = isDarkTheme,
                    onBackClick = { currentScreen = previousScreen },
                    onSaveTransaction = { nouvelleTransaction ->
                        viewModel.ajouterTransaction(nouvelleTransaction)
                        currentScreen = previousScreen
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
                    currentScreen = previousScreen
                }

                SettingsScreen(
                    selectedTheme = viewModel.themeMode,
                    onThemeSelected = { newMode ->
                        viewModel.updateThemeMode(newMode)
                    },
                    onBackClick = { currentScreen = previousScreen }
                )
            }
        }
    }
}

@Composable
private fun BudgetBottomBar(
    currentScreen: String,
    onSelectScreen: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        val isHomeSelected = currentScreen == "home"
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = { onSelectScreen("home") },
            icon = {
                Icon(
                    imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.nav_home)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_home),
                    fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        )

        val isStatsSelected = currentScreen == "stats"
        NavigationBarItem(
            selected = isStatsSelected,
            onClick = { onSelectScreen("stats") },
            icon = {
                Icon(
                    imageVector = if (isStatsSelected) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                    contentDescription = stringResource(R.string.nav_stats)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_stats),
                    fontWeight = if (isStatsSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}