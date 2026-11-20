package com.ibra.budgetmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ibra.budgetmanager.R
import com.ibra.budgetmanager.model.Transaction
import com.ibra.budgetmanager.model.TransactionType
import com.ibra.budgetmanager.ui.BudgetPeriod
import com.ibra.budgetmanager.ui.theme.colorDark4
import com.ibra.budgetmanager.ui.theme.colorDark5
import com.ibra.budgetmanager.ui.theme.colorLight4
import com.ibra.budgetmanager.ui.theme.colorLight5
import com.ibra.budgetmanager.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    transactions: List<Transaction>,
    isDarkTheme: Boolean,
    selectedPeriod: BudgetPeriod,
    onPeriodSelected: (BudgetPeriod) -> Unit,
    onAddTransactionClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val aujourdhui = LocalDate.now()
    val moisActuel = YearMonth.from(aujourdhui)
    val debutDuMois = aujourdhui.withDayOfMonth(1)
    val debutDeLAnnee = LocalDate.of(aujourdhui.year, 1, 1)

    // 1. Filtrer les transactions visibles selon la période choisie
    val transactionsFiltrees = transactions.filter { tx ->
        if (selectedPeriod == BudgetPeriod.MONTH) {
            if (tx.isMensuel) {
                val activeDepuis = !YearMonth.from(tx.dateCreation).isAfter(moisActuel)
                val pasEncoreStoppee = tx.dateFin == null || !YearMonth.from(tx.dateFin).isBefore(moisActuel)
                activeDepuis && pasEncoreStoppee
            } else {
                !tx.dateCreation.isBefore(debutDuMois) &&
                        tx.dateCreation.month == aujourdhui.month &&
                        tx.dateCreation.year == aujourdhui.year
            }
        } else {
            if (tx.isMensuel) {
                val creeeAvantFinAnnee = tx.dateCreation.year <= aujourdhui.year
                val activeCetteAnnee = tx.dateFin == null || tx.dateFin.year >= aujourdhui.year
                creeeAvantFinAnnee && activeCetteAnnee
            } else {
                !tx.dateCreation.isBefore(debutDeLAnnee) &&
                        !tx.dateCreation.isAfter(aujourdhui) &&
                        tx.dateCreation.year == aujourdhui.year
            }
        }
    }

    // 2. Calculer le montant effectif de chaque transaction sur la période
    fun montantPourPeriode(tx: Transaction): Double {
        if (!tx.isMensuel || selectedPeriod == BudgetPeriod.MONTH) {
            return tx.montant
        }

        val moisDebut = if (tx.dateCreation.year < aujourdhui.year) 1 else tx.dateCreation.monthValue
        val moisFin = if (tx.dateFin != null && tx.dateFin.year == aujourdhui.year) {
            minOf(aujourdhui.monthValue, tx.dateFin.monthValue)
        } else {
            aujourdhui.monthValue
        }
        val nbMois = (moisFin - moisDebut + 1).coerceAtLeast(1)
        return tx.montant * nbMois
    }

    val totalRevenus = transactionsFiltrees
        .filter { it.type == TransactionType.REVENU }
        .sumOf { montantPourPeriode(it) }

    val totalDepenses = transactionsFiltrees
        .filter { it.type == TransactionType.DEPENSE }
        .sumOf { montantPourPeriode(it) }

    val soldeTotal = totalRevenus - totalDepenses

    // Maximum 5 opérations affichées sur l'accueil
    val dernieresTransactions = transactionsFiltrees.take(5)

    val incomeColor = if (isDarkTheme) colorDark4 else colorLight4
    val expenseColor = if (isDarkTheme) colorDark5 else colorLight5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_transaction)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Sélecteur Ce mois / Cette année
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = selectedPeriod == BudgetPeriod.MONTH,
                    onClick = { onPeriodSelected(BudgetPeriod.MONTH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.filter_month),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SegmentedButton(
                    selected = selectedPeriod == BudgetPeriod.YEAR,
                    onClick = { onPeriodSelected(BudgetPeriod.YEAR) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.filter_year),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Carte récapitulative
            BalanceSummaryCard(
                solde = soldeTotal,
                revenus = totalRevenus,
                depenses = totalDepenses,
                incomeColor = incomeColor,
                expenseColor = expenseColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dernieresTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(dernieresTransactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            montantAffiche = montantPourPeriode(transaction),
                            incomeColor = incomeColor,
                            expenseColor = expenseColor,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BalanceSummaryCard(
    solde: Double,
    revenus: Double,
    depenses: Double,
    incomeColor: Color,
    expenseColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.remaining_balance),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = String.format("%.2f", solde),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (solde >= 0) incomeColor else expenseColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = stringResource(R.string.income_plural),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = String.format("+ %.2f", revenus),
                        fontWeight = FontWeight.Bold,
                        color = incomeColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.expenses_plural),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = String.format("- %.2f", depenses),
                        fontWeight = FontWeight.Bold,
                        color = expenseColor
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TransactionItem(
    transaction: Transaction,
    montantAffiche: Double,
    incomeColor: Color,
    expenseColor: Color,
    onClick: () -> Unit
) {
    val isStopped = transaction.isMensuel && transaction.dateFin != null

    val dateAffichee = when {
        isStopped -> stringResource(
            R.string.stopped_on_prefix,
            DateUtils.formatAffichage(transaction.dateFin)
        )
        transaction.isMensuel && transaction.jourDuMois != null -> stringResource(
            R.string.next_date_prefix,
            DateUtils.formatAffichage(DateUtils.getProchaineDate(transaction.jourDuMois))
        )
        else -> transaction.date
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.titre,
                        fontWeight = FontWeight.Medium
                    )
                    if (transaction.isMensuel) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = onClick,
                            label = {
                                Text(
                                    text = stringResource(
                                        if (isStopped) R.string.stopped_badge else R.string.monthly_badge
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${transaction.categorie} • $dateAffichee",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            val isIncome = transaction.type == TransactionType.REVENU
            Text(
                text = String.format(if (isIncome) "+ %.2f" else "- %.2f", montantAffiche),
                fontWeight = FontWeight.Bold,
                color = if (isIncome) incomeColor else expenseColor
            )
        }
    }
}