package org.app.tgvstock.models

data class Product(
    val id: String,
    var name: String,
    var unitPrice: Double,
    var bottlesPerCrate: Int = 12,
    var category: String = "Bières",
    var stockInitial: Int = 0,
    var consignationPrice: Double = 3000.0
)

/**
 * All beers, alcopops, and Guinness may arrive in half-crates and may also be sold in half-crates.
 * Canettes (cans / cartons) do not support crates or half-crates.
 */
fun Product.supportsHalfCrate(): Boolean {
    val cat = category.trim().lowercase()
    val n = name.trim().lowercase()
    if (cat.contains("canette") || n.contains("canette") || n.contains("cannette")) return false
    return cat in setOf("bières", "bieres", "alcopops", "guinness") || n.contains("guinness")
}

data class Packaging(
    val id: String,
    var name: String,
    var crateType: String,
    var emptyCrates: Int = 0,
    var fullCrates: Int = 0,
    var depositValuePerCrate: Double = 3000.0,
    var lastUpdated: String = ""
) {
    val totalCrates: Int get() = emptyCrates + fullCrates
    val totalDepositValue: Double get() = totalCrates * depositValuePerCrate
}

data class StockEntry(
    val id: String,
    val date: String,
    val productId: String,
    val productName: String,
    val quantityCrates: Double,      // supports half-crates (0.5)
    val extras: Int = 0,             // extra bottles per crate (e.g. +2, +3 extra bottles per crate)
    val totalExtras: Int = 0,        // total extra bottles in this delivery (calculated or custom)
    val motif: String = "",          // reason / motif for the delivery discrepancy / bonus
    val supplier: String = "Brasseries / Fournisseur",
    val notes: String = ""
) {
    /** Total extra bottles received in this delivery */
    val calculatedTotalExtraBottles: Int
        get() = if (totalExtras != 0) totalExtras else (extras * quantityCrates).toInt()

    /** Effective crates received */
    val effectiveCrates: Double get() = quantityCrates
}

data class Sale(
    val id: String,
    val date: String,
    val productId: String,
    val productName: String,
    val quantityCrates: Double,      // supports half-crates (0.5)
    val unitPrice: Double,
    val totalAmount: Double,
    val destination: String = "Dépôt", // Dépôt, Bar, Client Gros
    val customerName: String = "Comptoir",
    val paymentMethod: String = "Espèces", // Espèces, Mobile Money, À Crédit, Virement, Transfert Interne
    val isDebt: Boolean = false
)

data class SpoiledProduct(
    val id: String,
    val date: String,
    val productId: String,
    val productName: String,
    val quantityBottles: Int,
    val comment: String,
    val estimatedLoss: Double = 0.0
)

data class Debt(
    val id: String,
    val date: String,
    var debtorName: String,
    var productId: String,
    var productName: String,
    var quantityCrates: Double,      // supports half-crates (0.5)
    var unitPrice: Double,
    var totalAmount: Double,
    var amountPaid: Double = 0.0,
    var remainingAmount: Double = totalAmount,
    var status: String = "Non Payé", // Non Payé, Partiel, Réglé
    var phone: String = "",
    var notes: String = ""
)

data class Expenditure(
    val id: String,
    val date: String,
    val motive: String,
    val category: String = "Manutention", // Manutention, Transport, Factures, Personnel, Divers
    val amount: Double,
    val beneficiary: String = ""
)

data class CashVersement(
    val id: String,
    val date: String,
    val sellerName: String = "Vendeur ZAMBOY",
    val amount: Double,
    val notes: String = ""
)

data class InventoryRow(
    val product: Product,
    val stockInitial: Double,
    val entrees: Double,             // sum of effectiveCrates
    val entreesBonusBottles: Int = 0,// extra bonus bottles received in arrivals (e.g. +2/+3 btl/crate)
    val totalStock: Double,
    val sortiesBar: Double,          // bar transfers (not counted in sold revenue or total sold)
    val ventesQty: Double,           // depot / wholesale sales (total sold)
    val totalSorties: Double,        // total physical stock movement (sortiesBar + ventesQty)
    val avariesBottles: Int,
    val avariesCratesEquiv: Double,
    val resteTheorique: Double,
    var restePhysique: Double,
    val ecart: Double,
    val unitPrice: Double,
    val montantVentes: Double,       // revenue from depot/wholesale only (excludes bar transfers)
    val montantTransfertBar: Double  // value of bar transfers (informational)
)

data class FinancialReconciliation(
    val totalVentes: Double,
    val totalVersement: Double,
    val totalAvarisValeur: Double,
    val totalDepenses: Double,
    val totalDettes: Double,
    val difference: Double // totalVersement + totalDepenses + totalDettes - totalVentes
)

/**
 * Represents a batch operation on a specific packaging type.
 * movementType:
 *   - "RECEPTION_PLEIN"      : Réception casiers pleins (livraison brasserie)
 *   - "RETOUR_VIDE_CLIENT"   : Retour casiers vides depuis un client
 *   - "RETOUR_BRASSERIE"     : Envoi casiers vides à la brasserie
 *   - "VENTE_CASIER_VIDE"    : Vente de casiers vides à un tiers
 *   - "AJUSTEMENT_VIDE"      : Ajustement manuel casiers vides
 *   - "AJUSTEMENT_PLEIN"     : Ajustement manuel casiers pleins
 *   - "CONVERSION"           : Passage plein → vide (casiers débouchés / sortis)
 */
data class PackagingMovement(
    val id: String,
    val date: String,
    val packagingId: String,
    val packagingName: String,
    val movementType: String,
    val quantityEmpty: Int = 0,  // delta on emptyCrates (can be negative)
    val quantityFull: Int = 0,   // delta on fullCrates (can be negative)
    val notes: String = "",
    val contactName: String = "" // client, fournisseur...
)
