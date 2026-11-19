package com.ibra.budgetmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ibra.budgetmanager.model.Transaction
import com.ibra.budgetmanager.model.TransactionType
import com.ibra.budgetmanager.util.DateUtils
import java.time.LocalDate

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class BudgetPeriod {
    MONTH, YEAR
}

class BudgetViewModel : ViewModel() {

    var themeMode by mutableStateOf(AppThemeMode.SYSTEM)
        private set

    fun updateThemeMode(newMode: AppThemeMode) {
        themeMode = newMode
    }

    var selectedPeriod by mutableStateOf(BudgetPeriod.MONTH)
        private set

    fun updatePeriod(newPeriod: BudgetPeriod) {
        selectedPeriod = newPeriod
    }


    val transactions = mutableStateListOf(
        Transaction(
            id = 1L,
            titre = "Salaire",
            montant = 2200.0,
            categorie = "Salaire",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(1))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.REVENU,
            isMensuel = true,
            jourDuMois = 1
        ),
        Transaction(
            id = 2L,
            titre = "Loyer",
            montant = 650.0,
            categorie = "Besoins",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(5))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 5
        ),
        Transaction(
            id = 3L,
            titre = "Courses",
            montant = 64.50,
            categorie = "Besoins",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.DEPENSE,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 4L,
            titre = "Prime",
            montant = 300.0,
            categorie = "Revenu occasionnel",
            date = DateUtils.formatAffichage(LocalDate.now().minusMonths(1)),
            dateCreation = LocalDate.now().minusMonths(1),
            type = TransactionType.REVENU,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 5L,
            titre = "Électricité",
            montant = 85.0,
            categorie = "Besoins",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(10))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 10
        ),
        Transaction(
            id = 6L,
            titre = "Internet",
            montant = 39.99,
            categorie = "Besoins",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(12))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 12
        ),
        Transaction(
            id = 7L,
            titre = "Livret A",
            montant = 250.0,
            categorie = "Épargne",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(15))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 15
        ),
        Transaction(
            id = 8L,
            titre = "Restaurant",
            montant = 42.0,
            categorie = "Plaisirs",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.DEPENSE,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 9L,
            titre = "Essence",
            montant = 58.30,
            categorie = "Besoins",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.DEPENSE,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 10L,
            titre = "Cinéma",
            montant = 24.50,
            categorie = "Plaisirs",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.DEPENSE,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 11L,
            titre = "Sport",
            montant = 29.90,
            categorie = "Plaisirs",
            date = "Prochain : ${DateUtils.formatAffichage(DateUtils.getProchaineDate(20))}",
            dateCreation = LocalDate.now().withDayOfMonth(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 20
        ),
        Transaction(
            id = 12L,
            titre = "Vinted",
            montant = 45.0,
            categorie = "Revenu occasionnel",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.REVENU,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 13L,
            titre = "Boulangerie",
            montant = 11.20,
            categorie = "Plaisirs",
            date = DateUtils.formatAffichage(LocalDate.now()),
            dateCreation = LocalDate.now(),
            type = TransactionType.DEPENSE,
            isMensuel = false,
            jourDuMois = null
        ),
        Transaction(
            id = 14L,
            titre = "Netflix",
            montant = 13.49,
            categorie = "Plaisirs",
            date = "Arrêté le ${DateUtils.formatAffichage(LocalDate.now().minusMonths(1))}",
            dateCreation = LocalDate.now().minusMonths(3),
            dateFin = LocalDate.now().minusMonths(1),
            type = TransactionType.DEPENSE,
            isMensuel = true,
            jourDuMois = 18
        )
    )


    fun ajouterTransaction(nouvelleTransaction: Transaction) {
        val nextId = (transactions.maxOfOrNull { it.id } ?: 0L) + 1L
        transactions.add(0, nouvelleTransaction.copy(id = nextId))
    }

    fun modifierTransaction(transactionModifiee: Transaction) {
        val index = transactions.indexOfFirst { it.id == transactionModifiee.id }
        if (index != -1) {
            transactions[index] = transactionModifiee
        }
    }

    fun supprimerTransaction(transactionId: Long) {
        transactions.removeAll { it.id == transactionId }
    }
}