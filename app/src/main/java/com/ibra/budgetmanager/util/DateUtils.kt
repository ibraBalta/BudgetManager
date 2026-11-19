package com.ibra.budgetmanager.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateUtils {

    /**
     * Calcule la date exacte d'un prélèvement mensuel pour un mois donné.
     * Si le jour demandé dépasse le dernier jour du mois (ex: 31 en février),
     * il est automatiquement ramené au dernier jour valide (ex: 28 février).
     */
    fun getEcheanceMensuelle(jourSouhaite: Int, mois: Int, annee: Int): LocalDate {
        val yearMonth = YearMonth.of(annee, mois)
        val dernierJourDuMois = yearMonth.lengthOfMonth()
        val jourEffectif = jourSouhaite.coerceIn(1, dernierJourDuMois)
        return yearMonth.atDay(jourEffectif)
    }

    /**
     * Calcule la date de la prochaine échéance à partir d'aujourd'hui.
     */
    fun getProchaineDate(jourSouhaite: Int): LocalDate {
        val aujourdhui = LocalDate.now()
        val dateCeMois = getEcheanceMensuelle(jourSouhaite, aujourdhui.monthValue, aujourdhui.year)

        // Si le jour est déjà passé ce mois-ci, on prend le mois prochain
        return if (aujourdhui.isAfter(dateCeMois)) {
            val moisSuivant = aujourdhui.plusMonths(1)
            getEcheanceMensuelle(jourSouhaite, moisSuivant.monthValue, moisSuivant.year)
        } else {
            dateCeMois
        }
    }

    /**
     * Formate une date en texte lisible (ex: "28 févr. 2026")
     */
    fun formatAffichage(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(formatter)
    }
}