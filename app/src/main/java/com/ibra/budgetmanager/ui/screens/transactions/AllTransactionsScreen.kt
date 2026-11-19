package com.ibra.budgetmanager.ui.screens.transactions

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ibra.budgetmanager.R
import com.ibra.budgetmanager.model.Transaction
import com.ibra.budgetmanager.model.TransactionType
import com.ibra.budgetmanager.ui.BudgetPeriod
import com.ibra.budgetmanager.ui.screens.home.TransactionItem
import com.ibra.budgetmanager.ui.theme.colorDark4
import com.ibra.budgetmanager.ui.theme.colorDark5
import com.ibra.budgetmanager.ui.theme.colorLight4
import com.ibra.budgetmanager.ui.theme.colorLight5
import com.ibra.budgetmanager.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

data class CategorySlice(
    val category: String,
    val amount: Double,
    val color: Color
)

private enum class StatsFilterMode {
    MONTH, YEAR, CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    transactions: List<Transaction>,
    isDarkTheme: Boolean,
    selectedPeriod: BudgetPeriod,
    onPeriodSelected: (BudgetPeriod) -> Unit,
    onBackClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val aujourdhui = LocalDate.now()
    val moisActuel = YearMonth.from(aujourdhui)

    var filterMode by rememberSaveable {
        mutableStateOf(
            if (selectedPeriod == BudgetPeriod.YEAR) StatsFilterMode.YEAR else StatsFilterMode.MONTH
        )
    }

    // Dates personnalisées (par défaut : du 1er du mois jusqu'à aujourd'hui)
    var customStartEpochDay by rememberSaveable { mutableLongStateOf(aujourdhui.withDayOfMonth(1).toEpochDay()) }
    var customEndEpochDay by rememberSaveable { mutableLongStateOf(aujourdhui.toEpochDay()) }

    val customStartDate = LocalDate.ofEpochDay(customStartEpochDay)
    val customEndDate = LocalDate.ofEpochDay(customEndEpochDay)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Définition des bornes exactes [dateDebut..dateFin] selon le mode choisi
    val (dateDebut, dateFin) = when (filterMode) {
        StatsFilterMode.MONTH -> aujourdhui.withDayOfMonth(1) to aujourdhui.withDayOfMonth(aujourdhui.lengthOfMonth())
        StatsFilterMode.YEAR -> LocalDate.of(aujourdhui.year, 1, 1) to aujourdhui
        StatsFilterMode.CUSTOM -> customStartDate to customEndDate
    }

    // Dialogues de sélection de dates
    if (showStartPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            customStartEpochDay = picked.toEpochDay()
                            if (picked.isAfter(customEndDate)) {
                                customEndEpochDay = picked.toEpochDay()
                            }
                        }
                        showStartPicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = customEndDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            customEndEpochDay = picked.toEpochDay()
                            if (picked.isBefore(customStartDate)) {
                                customStartEpochDay = picked.toEpochDay()
                            }
                        }
                        showEndPicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // 1. Filtrer les transactions sur l'intervalle [dateDebut..dateFin]
    val ymDebut = YearMonth.from(dateDebut)
    val ymFin = YearMonth.from(dateFin)

    val transactionsFiltrees = transactions.filter { tx ->
        if (tx.isMensuel) {
            val activeDepuis = !YearMonth.from(tx.dateCreation).isAfter(ymFin)
            val pasEncoreStoppee = tx.dateFin == null || !YearMonth.from(tx.dateFin).isBefore(ymDebut)
            activeDepuis && pasEncoreStoppee
        } else {
            !tx.dateCreation.isBefore(dateDebut) && !tx.dateCreation.isAfter(dateFin)
        }
    }

    // 2. Montant effectif sur l'intervalle choisi
    fun montantPourPeriode(tx: Transaction): Double {
        if (!tx.isMensuel || filterMode == StatsFilterMode.MONTH) {
            return tx.montant
        }

        val debutEffectif = maxOf(YearMonth.from(tx.dateCreation), ymDebut)
        val finLimite = if (tx.dateFin != null) minOf(YearMonth.from(tx.dateFin), ymFin) else ymFin
        val finEffective = minOf(finLimite, moisActuel)

        val nbMois = ((finEffective.year - debutEffectif.year) * 12 + (finEffective.monthValue - debutEffectif.monthValue) + 1)
            .coerceAtLeast(1)
        return tx.montant * nbMois
    }

    // 3. Date de tri cohérente avec la période sélectionnée
    fun dateDeTri(tx: Transaction): LocalDate {
        return if (tx.isMensuel && tx.jourDuMois != null) {
            val moisRef = minOf(ymFin, moisActuel)
            val moisCible = if (tx.dateFin != null && YearMonth.from(tx.dateFin).isBefore(moisRef)) {
                YearMonth.from(tx.dateFin)
            } else {
                moisRef
            }
            DateUtils.getEcheanceMensuelle(tx.jourDuMois, moisCible.monthValue, moisCible.year)
        } else {
            tx.dateCreation
        }
    }

    // 4. Préparation des 2 camemberts
    val expensePalette = listOf(
        Color(0xFFE57373), // Corail doux
        Color(0xFFFFB74D), // Ambre doux
        Color(0xFF64B5F6), // Bleu pastel
        Color(0xFFBA68C8)  // Lavande
    )
    val incomePalette = listOf(
        Color(0xFF81C784), // Vert sauge
        Color(0xFF4DB6AC), // Turquoise doux
        Color(0xFFA1887F)  // Taupe doux
    )

    val depensesParCategorie = transactionsFiltrees
        .filter { it.type == TransactionType.DEPENSE }
        .groupBy { it.categorie }
        .entries
        .map { (cat, list) -> cat to list.sumOf { montantPourPeriode(it) } }
        .sortedByDescending { it.second }
        .mapIndexed { index, (cat, total) ->
            CategorySlice(
                category = cat,
                amount = total,
                color = expensePalette[index % expensePalette.size]
            )
        }

    val revenusParCategorie = transactionsFiltrees
        .filter { it.type == TransactionType.REVENU }
        .groupBy { it.categorie }
        .entries
        .map { (cat, list) -> cat to list.sumOf { montantPourPeriode(it) } }
        .sortedByDescending { it.second }
        .mapIndexed { index, (cat, total) ->
            CategorySlice(
                category = cat,
                amount = total,
                color = incomePalette[index % incomePalette.size]
            )
        }

    // 5. Tri décroissant par date et regroupement par jour
    val transactionsTrieesParDate = transactionsFiltrees
        .sortedWith(compareByDescending<Transaction> { dateDeTri(it) }.thenByDescending { it.id })
        .groupBy { dateDeTri(it) }

    val incomeColor = if (isDarkTheme) colorDark4 else colorLight4
    val expenseColor = if (isDarkTheme) colorDark5 else colorLight5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.all_transactions),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Sélecteur Ce mois / Cette année / Période
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = filterMode == StatsFilterMode.MONTH,
                    onClick = {
                        filterMode = StatsFilterMode.MONTH
                        onPeriodSelected(BudgetPeriod.MONTH)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
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
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SegmentedButton(
                    selected = filterMode == StatsFilterMode.YEAR,
                    onClick = {
                        filterMode = StatsFilterMode.YEAR
                        onPeriodSelected(BudgetPeriod.YEAR)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
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
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SegmentedButton(
                    selected = filterMode == StatsFilterMode.CUSTOM,
                    onClick = { filterMode = StatsFilterMode.CUSTOM },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.filter_custom),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Sélecteurs de dates personnalisées (visibles uniquement en mode "Période")
            if (filterMode == StatsFilterMode.CUSTOM) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = CardDefaults.shape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                R.string.date_from_prefix,
                                DateUtils.formatAffichage(customStartDate)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showEndPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = CardDefaults.shape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                R.string.date_to_prefix,
                                DateUtils.formatAffichage(customEndDate)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // SECTION STATISTIQUES : 2 CAMEMBERTS CÔTE À CÔTE
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryPieCard(
                            title = stringResource(R.string.expenses_plural),
                            slices = depensesParCategorie,
                            accentColor = expenseColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        CategoryPieCard(
                            title = stringResource(R.string.income_plural),
                            slices = revenusParCategorie,
                            accentColor = incomeColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // LISTE DES OPÉRATIONS REGROUPÉES ET TRIÉES PAR DATE
                if (transactionsTrieesParDate.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_transactions),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    transactionsTrieesParDate.forEach { (dateGroupe, listeDuJour) ->
                        item(key = "header_$dateGroupe") {
                            DateSectionHeader(date = dateGroupe, aujourdhui = aujourdhui)
                        }

                        items(listeDuJour, key = { it.id }) { transaction ->
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
}

@SuppressLint("DefaultLocale")
@Composable
fun CategoryPieCard(
    title: String,
    slices: List<CategorySlice>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.amount }
    val emptyTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    if (total <= 0.0) {
                        drawArc(
                            color = emptyTrackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                    } else {
                        var startAngle = -90f
                        val gapAngle = if (slices.size > 1) 3f else 0f

                        slices.forEach { slice ->
                            val sweepAngle = ((slice.amount / total) * 360.0).toFloat()
                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle + (gapAngle / 2f),
                                    sweepAngle = (sweepAngle - gapAngle).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }
                }

                Text(
                    text = String.format("%.0f", total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (slices.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_data_chart),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEach { slice ->
                        val pourcentage = if (total > 0) ((slice.amount / total) * 100).toInt() else 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$pourcentage%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSectionHeader(
    date: LocalDate,
    aujourdhui: LocalDate
) {
    val label = when (date) {
        aujourdhui -> stringResource(R.string.today)
        aujourdhui.minusDays(1) -> stringResource(R.string.yesterday)
        else -> DateUtils.formatAffichage(date)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }
}