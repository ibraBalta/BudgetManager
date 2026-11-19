package com.ibra.budgetmanager.model

import java.time.LocalDate

enum class TransactionType {
    DEPENSE,
    REVENU
}

data class Transaction(
    val id: Long = 0,
    val titre: String,
    val montant: Double,
    val categorie: String,
    val date: String,
    val dateCreation: LocalDate = LocalDate.now(),
    val dateFin: LocalDate? = null,
    val type: TransactionType,
    val isMensuel: Boolean = false,
    val jourDuMois: Int? = null
)