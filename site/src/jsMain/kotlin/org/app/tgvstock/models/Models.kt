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
    val quantityCrates: Int,
    val supplier: String = "Brasseries / Fournisseur",
    val notes: String = ""
)

data class Sale(
    val id: String,
    val date: String,
    val productId: String,
    val productName: String,
    val quantityCrates: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val destination: String = "Dépôt", // Dépôt, Bar, Client Gros
    val customerName: String = "Comptoir",
    val paymentMethod: String = "Espèces", // Espèces, Mobile Money, À Crédit, Virement
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
    var quantityCrates: Int,
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
    val stockInitial: Int,
    val entrees: Int,
    val totalStock: Int,
    val sortiesBar: Int,
    val ventesQty: Int,
    val totalSorties: Int,
    val avariesBottles: Int,
    val avariesCratesEquiv: Double,
    val resteTheorique: Double,
    var restePhysique: Double,
    val ecart: Double,
    val unitPrice: Double,
    val montantVentes: Double
)

data class FinancialReconciliation(
    val totalVentes: Double,
    val totalVersement: Double,
    val totalAvarisValeur: Double,
    val totalDepenses: Double,
    val totalDettes: Double,
    val difference: Double // totalVersement + totalDepenses + totalDettes - totalVentes
)
