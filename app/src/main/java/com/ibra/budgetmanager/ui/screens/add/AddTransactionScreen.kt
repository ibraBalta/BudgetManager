package com.ibra.budgetmanager.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ibra.budgetmanager.R
import com.ibra.budgetmanager.model.Transaction
import com.ibra.budgetmanager.model.TransactionType
import com.ibra.budgetmanager.ui.theme.colorDark4
import com.ibra.budgetmanager.ui.theme.colorDark5
import com.ibra.budgetmanager.ui.theme.colorLight4
import com.ibra.budgetmanager.ui.theme.colorLight5
import com.ibra.budgetmanager.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean,
    onSaveTransaction: (Transaction) -> Unit
) {
    var titre by rememberSaveable { mutableStateOf("") }
    var montantStr by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(TransactionType.DEPENSE) }
    var isMensuel by rememberSaveable { mutableStateOf(false) }
    var jourDuMoisStr by rememberSaveable { mutableStateOf("1") }

    val incomeColor = if (isDarkTheme) colorDark4 else colorLight4
    val expenseColor = if (isDarkTheme) colorDark5 else colorLight5

    val categoriesDepenses = listOf(
        stringResource(R.string.cat_needs),
        stringResource(R.string.cat_wants),
        stringResource(R.string.cat_savings)
    )
    val categoriesRevenus = listOf(
        stringResource(R.string.cat_salary),
        stringResource(R.string.cat_occasional_income)
    )

    val currentCategories = if (selectedType == TransactionType.DEPENSE) categoriesDepenses else categoriesRevenus
    var selectedCategory by rememberSaveable(selectedType) { mutableStateOf(currentCategories.first()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val exempleIntitule = if (selectedType == TransactionType.DEPENSE) {
        stringResource(R.string.title_hint_expense)
    } else {
        stringResource(R.string.title_hint_income)
    }

    val jourChoisi = jourDuMoisStr.toIntOrNull() ?: 1
    val dateAffichee = if (isMensuel) {
        val prochaineEcheance = DateUtils.getProchaineDate(jourChoisi)
        stringResource(R.string.next_date_prefix, DateUtils.formatAffichage(prochaineEcheance))
    } else {
        DateUtils.formatAffichage(LocalDate.now())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_transaction),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sélecteur Dépense ou Revenu
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == TransactionType.DEPENSE,
                    onClick = { selectedType = TransactionType.DEPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = expenseColor.copy(alpha = 0.22f),
                        activeContentColor = MaterialTheme.colorScheme.onSurface,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.expense_singular),
                        fontWeight = FontWeight.Bold
                    )
                }

                SegmentedButton(
                    selected = selectedType == TransactionType.REVENU,
                    onClick = { selectedType = TransactionType.REVENU },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = incomeColor.copy(alpha = 0.22f),
                        activeContentColor = MaterialTheme.colorScheme.onSurface,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.income_singular),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Montant
            OutlinedTextField(
                value = montantStr,
                onValueChange = { montantStr = it.replace(',', '.') },
                label = { Text(stringResource(R.string.amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Nom / Titre avec label adapté
            OutlinedTextField(
                value = titre,
                onValueChange = { titre = it },
                label = { Text(exempleIntitule) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Sélecteur de catégorie
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    currentCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. Option Récurrence Mensuelle
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.monthly_operation),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.repeats_every_month),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = isMensuel,
                            onCheckedChange = { isMensuel = it }
                        )
                    }

                    if (isMensuel) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = jourDuMoisStr,
                            onValueChange = { input ->
                                if (input.isEmpty() || (input.toIntOrNull() != null && input.toInt() in 1..31)) {
                                    jourDuMoisStr = input
                                }
                            },
                            label = { Text(stringResource(R.string.day_of_month_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 6. Bouton Enregistrer
            Button(
                onClick = {
                    val montant = montantStr.toDoubleOrNull() ?: 0.0
                    if (titre.isNotBlank() && montant > 0) {
                        val nouvelleTransaction = Transaction(
                            titre = titre.trim(),
                            montant = montant,
                            categorie = selectedCategory,
                            date = dateAffichee,
                            type = selectedType,
                            isMensuel = isMensuel,
                            jourDuMois = if (isMensuel) jourChoisi else null
                        )
                        onSaveTransaction(nouvelleTransaction)
                    }
                },
                enabled = titre.isNotBlank() && (montantStr.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_transaction),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}