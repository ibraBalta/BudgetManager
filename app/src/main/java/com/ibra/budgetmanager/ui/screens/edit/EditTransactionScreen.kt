package com.ibra.budgetmanager.ui.screens.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ibra.budgetmanager.R
import com.ibra.budgetmanager.model.Transaction
import com.ibra.budgetmanager.model.TransactionType
import com.ibra.budgetmanager.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onBackClick: () -> Unit,
    onUpdateTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var titre by rememberSaveable { mutableStateOf(transaction.titre) }
    var montantStr by rememberSaveable { mutableStateOf(transaction.montant.toString()) }
    var jourDuMoisStr by rememberSaveable { mutableStateOf((transaction.jourDuMois ?: 1).toString()) }
    var isStopped by rememberSaveable { mutableStateOf(transaction.dateFin != null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val categoriesDepenses = listOf(
        stringResource(R.string.cat_needs),
        stringResource(R.string.cat_wants),
        stringResource(R.string.cat_savings)
    )
    val categoriesRevenus = listOf(
        stringResource(R.string.cat_salary),
        stringResource(R.string.cat_occasional_income)
    )

    val currentCategories = if (transaction.type == TransactionType.DEPENSE) {
        categoriesDepenses
    } else {
        categoriesRevenus
    }

    var selectedCategory by rememberSaveable {
        mutableStateOf(
            if (transaction.categorie in currentCategories) transaction.categorie else currentCategories.first()
        )
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val exempleIntitule = if (transaction.type == TransactionType.DEPENSE) {
        stringResource(R.string.title_hint_expense)
    } else {
        stringResource(R.string.title_hint_income)
    }

    val jourChoisi = jourDuMoisStr.toIntOrNull() ?: 1
    val dateAffichee = when {
        transaction.isMensuel && isStopped -> {
            val dateArret = transaction.dateFin ?: LocalDate.now()
            stringResource(R.string.stopped_on_prefix, DateUtils.formatAffichage(dateArret))
        }
        transaction.isMensuel -> {
            val prochaineEcheance = DateUtils.getProchaineDate(jourChoisi)
            stringResource(R.string.next_date_prefix, DateUtils.formatAffichage(prochaineEcheance))
        }
        else -> transaction.date
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteTransaction(transaction.id)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirm_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_transaction),
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
            // 1. Montant
            OutlinedTextField(
                value = montantStr,
                onValueChange = { montantStr = it.replace(',', '.') },
                label = { Text(stringResource(R.string.amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Nom / Titre
            OutlinedTextField(
                value = titre,
                onValueChange = { titre = it },
                label = { Text(exempleIntitule) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Sélecteur de catégorie
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

            // 4. Bloc affiché uniquement si l'opération est mensuelle
            if (transaction.isMensuel) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            enabled = !isStopped,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { isStopped = !isStopped },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isStopped) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        ) {
                            Text(
                                text = if (isStopped) {
                                    stringResource(R.string.resume_monthly_button)
                                } else {
                                    stringResource(R.string.stop_monthly_button)
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Bouton Enregistrer les modifications
            Button(
                onClick = {
                    val montant = montantStr.toDoubleOrNull() ?: 0.0
                    if (titre.isNotBlank() && montant > 0) {
                        val nouvelleDateFin = when {
                            !transaction.isMensuel -> null
                            isStopped -> transaction.dateFin ?: LocalDate.now()
                            else -> null
                        }

                        val transactionModifiee = transaction.copy(
                            titre = titre.trim(),
                            montant = montant,
                            categorie = selectedCategory,
                            date = dateAffichee,
                            dateFin = nouvelleDateFin,
                            jourDuMois = if (transaction.isMensuel) jourChoisi else null
                        )
                        onUpdateTransaction(transactionModifiee)
                    }
                },
                enabled = titre.isNotBlank() && (montantStr.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_changes),
                    fontWeight = FontWeight.Bold
                )
            }

            // 6. Bouton Supprimer
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.delete_transaction),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}