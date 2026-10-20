package org.app.tgvstock.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.app.tgvstock.models.*
import kotlin.js.Date
import kotlin.math.roundToInt

object StockRepository {
    private const val KEY_PRODUCTS = "tgv_stock_products_v2"
    private const val KEY_PACKAGING = "tgv_stock_packaging_v2"
    private const val KEY_PKG_MOVEMENTS = "tgv_stock_pkg_movements_v2"
    private const val KEY_ENTRIES = "tgv_stock_entries_v2"
    private const val KEY_SALES = "tgv_stock_sales_v2"
    private const val KEY_SPOILED = "tgv_stock_spoiled_v2"
    private const val KEY_DEBTS = "tgv_stock_debts_v2"
    private const val KEY_EXPENSES = "tgv_stock_expenses_v2"
    private const val KEY_VERSEMENTS = "tgv_stock_versements_v2"
    private const val KEY_PHYSICAL_COUNTS = "tgv_stock_physical_counts_v2"

    var products by mutableStateOf(listOf<Product>())
        private set

    var packaging by mutableStateOf(listOf<Packaging>())
        private set

    var packagingMovements by mutableStateOf(listOf<PackagingMovement>())
        private set

    var entries by mutableStateOf(listOf<StockEntry>())
        private set

    var sales by mutableStateOf(listOf<Sale>())
        private set

    var spoiled by mutableStateOf(listOf<SpoiledProduct>())
        private set

    var debts by mutableStateOf(listOf<Debt>())
        private set

    var expenses by mutableStateOf(listOf<Expenditure>())
        private set

    var versements by mutableStateOf(listOf<CashVersement>())
        private set

    var physicalCounts by mutableStateOf(mapOf<String, Double>())
        private set

    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        loadAll()
        isInitialized = true
    }

    fun todayString(): String {
        val d = Date()
        val year = d.getFullYear()
        val month = (d.getMonth() + 1).toString().padStart(2, '0')
        val day = d.getDate().toString().padStart(2, '0')
        return "$year-$month-$day"
    }

    private fun generateId(prefix: String): String {
        val timestamp = Date.now().toLong().toString(36)
        val rand = (1000..9999).random()
        return "$prefix-$timestamp-$rand"
    }

    // --- Persistence Helpers ---
    private fun loadAll() {
        products = loadProducts()
        packaging = loadPackaging()
        packagingMovements = loadPackagingMovements()
        entries = loadEntries()
        sales = loadSales()
        spoiled = loadSpoiled()
        debts = loadDebts()
        expenses = loadExpenses()
        versements = loadVersements()
        physicalCounts = loadPhysicalCounts()

    }

    private fun loadProducts(): List<Product> {
        val json = window.localStorage.getItem(KEY_PRODUCTS) ?: return InitialData.defaultProducts
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                Product(
                    id = d.id as String,
                    name = d.name as String,
                    unitPrice = (d.unitPrice as Number).toDouble(),
                    bottlesPerCrate = (d.bottlesPerCrate as? Number)?.toInt() ?: 12,
                    category = (d.category as? String) ?: "Bières",
                    stockInitial = (d.stockInitial as? Number)?.toInt() ?: 0,
                    consignationPrice = (d.consignationPrice as? Number)?.toDouble() ?: 3000.0
                )
            }.ifEmpty { InitialData.defaultProducts }
        } catch (_: Throwable) {
            InitialData.defaultProducts
        }
    }

    fun saveProducts() {
        val dynamicList = products.map { p ->
            val obj = js("{}")
            obj.id = p.id
            obj.name = p.name
            obj.unitPrice = p.unitPrice
            obj.bottlesPerCrate = p.bottlesPerCrate
            obj.category = p.category
            obj.stockInitial = p.stockInitial
            obj.consignationPrice = p.consignationPrice
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_PRODUCTS, JSON.stringify(dynamicList))
    }

    private fun loadPackaging(): List<Packaging> {
        val json = window.localStorage.getItem(KEY_PACKAGING) ?: return InitialData.defaultPackaging
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                Packaging(
                    id = d.id as String,
                    name = d.name as String,
                    crateType = (d.crateType as? String) ?: "Casier",
                    emptyCrates = (d.emptyCrates as? Number)?.toInt() ?: 0,
                    fullCrates = (d.fullCrates as? Number)?.toInt() ?: 0,
                    depositValuePerCrate = (d.depositValuePerCrate as? Number)?.toDouble() ?: 3000.0,
                    lastUpdated = (d.lastUpdated as? String) ?: todayString()
                )
            }.ifEmpty { InitialData.defaultPackaging }
        } catch (_: Throwable) {
            InitialData.defaultPackaging
        }
    }

    fun savePackaging() {
        val dynamicList = packaging.map { p ->
            val obj = js("{}")
            obj.id = p.id
            obj.name = p.name
            obj.crateType = p.crateType
            obj.emptyCrates = p.emptyCrates
            obj.fullCrates = p.fullCrates
            obj.depositValuePerCrate = p.depositValuePerCrate
            obj.lastUpdated = p.lastUpdated
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_PACKAGING, JSON.stringify(dynamicList))
    }

    private fun loadEntries(): List<StockEntry> {
        val json = window.localStorage.getItem(KEY_ENTRIES) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                StockEntry(
                    id = d.id as String,
                    date = d.date as String,
                    productId = d.productId as String,
                    productName = d.productName as String,
                    quantityCrates = (d.quantityCrates as Number).toDouble(),
                    extras = (d.extras as? Number)?.toInt() ?: 0,
                    totalExtras = (d.totalExtras as? Number)?.toInt() ?: 0,
                    motif = (d.motif as? String) ?: "",
                    supplier = (d.supplier as? String) ?: "Fournisseur",
                    notes = (d.notes as? String) ?: ""
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveEntries() {
        val dynamicList = entries.map { e ->
            val obj = js("{}")
            obj.id = e.id
            obj.date = e.date
            obj.productId = e.productId
            obj.productName = e.productName
            obj.quantityCrates = e.quantityCrates
            obj.extras = e.extras
            obj.totalExtras = e.totalExtras
            obj.motif = e.motif
            obj.supplier = e.supplier
            obj.notes = e.notes
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_ENTRIES, JSON.stringify(dynamicList))
    }

    private fun loadSales(): List<Sale> {
        val json = window.localStorage.getItem(KEY_SALES) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                Sale(
                    id = d.id as String,
                    date = d.date as String,
                    productId = d.productId as String,
                    productName = d.productName as String,
                    quantityCrates = (d.quantityCrates as Number).toDouble(),
                    unitPrice = (d.unitPrice as Number).toDouble(),
                    totalAmount = (d.totalAmount as Number).toDouble(),
                    destination = (d.destination as? String) ?: "Dépôt",
                    customerName = (d.customerName as? String) ?: "Comptoir",
                    paymentMethod = (d.paymentMethod as? String) ?: "Espèces",
                    isDebt = (d.isDebt as? Boolean) ?: false
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveSales() {
        val dynamicList = sales.map { s ->
            val obj = js("{}")
            obj.id = s.id
            obj.date = s.date
            obj.productId = s.productId
            obj.productName = s.productName
            obj.quantityCrates = s.quantityCrates
            obj.unitPrice = s.unitPrice
            obj.totalAmount = s.totalAmount
            obj.destination = s.destination
            obj.customerName = s.customerName
            obj.paymentMethod = s.paymentMethod
            obj.isDebt = s.isDebt
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_SALES, JSON.stringify(dynamicList))
    }

    private fun loadSpoiled(): List<SpoiledProduct> {
        val json = window.localStorage.getItem(KEY_SPOILED) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                SpoiledProduct(
                    id = d.id as String,
                    date = d.date as String,
                    productId = d.productId as String,
                    productName = d.productName as String,
                    quantityBottles = (d.quantityBottles as Number).toInt(),
                    comment = (d.comment as? String) ?: "",
                    estimatedLoss = (d.estimatedLoss as? Number)?.toDouble() ?: 0.0
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveSpoiled() {
        val dynamicList = spoiled.map { sp ->
            val obj = js("{}")
            obj.id = sp.id
            obj.date = sp.date
            obj.productId = sp.productId
            obj.productName = sp.productName
            obj.quantityBottles = sp.quantityBottles
            obj.comment = sp.comment
            obj.estimatedLoss = sp.estimatedLoss
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_SPOILED, JSON.stringify(dynamicList))
    }

    private fun loadDebts(): List<Debt> {
        val json = window.localStorage.getItem(KEY_DEBTS) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                Debt(
                    id = d.id as String,
                    date = d.date as String,
                    debtorName = d.debtorName as String,
                    productId = d.productId as String,
                    productName = d.productName as String,
                    quantityCrates = (d.quantityCrates as Number).toDouble(),
                    unitPrice = (d.unitPrice as Number).toDouble(),
                    totalAmount = (d.totalAmount as Number).toDouble(),
                    amountPaid = (d.amountPaid as? Number)?.toDouble() ?: 0.0,
                    remainingAmount = (d.remainingAmount as? Number)?.toDouble() ?: ((d.totalAmount as Number).toDouble() - ((d.amountPaid as? Number)?.toDouble() ?: 0.0)),
                    status = (d.status as? String) ?: "Non Payé",
                    phone = (d.phone as? String) ?: "",
                    notes = (d.notes as? String) ?: ""
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveDebts() {
        val dynamicList = debts.map { d ->
            val obj = js("{}")
            obj.id = d.id
            obj.date = d.date
            obj.debtorName = d.debtorName
            obj.productId = d.productId
            obj.productName = d.productName
            obj.quantityCrates = d.quantityCrates
            obj.unitPrice = d.unitPrice
            obj.totalAmount = d.totalAmount
            obj.amountPaid = d.amountPaid
            obj.remainingAmount = d.remainingAmount
            obj.status = d.status
            obj.phone = d.phone
            obj.notes = d.notes
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_DEBTS, JSON.stringify(dynamicList))
    }

    private fun loadExpenses(): List<Expenditure> {
        val json = window.localStorage.getItem(KEY_EXPENSES) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                Expenditure(
                    id = d.id as String,
                    date = d.date as String,
                    motive = d.motive as String,
                    category = (d.category as? String) ?: "Divers",
                    amount = (d.amount as Number).toDouble(),
                    beneficiary = (d.beneficiary as? String) ?: ""
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveExpenses() {
        val dynamicList = expenses.map { ex ->
            val obj = js("{}")
            obj.id = ex.id
            obj.date = ex.date
            obj.motive = ex.motive
            obj.category = ex.category
            obj.amount = ex.amount
            obj.beneficiary = ex.beneficiary
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_EXPENSES, JSON.stringify(dynamicList))
    }

    private fun loadVersements(): List<CashVersement> {
        val json = window.localStorage.getItem(KEY_VERSEMENTS) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                CashVersement(
                    id = d.id as String,
                    date = d.date as String,
                    sellerName = (d.sellerName as? String) ?: "Vendeur ZAMBOY",
                    amount = (d.amount as Number).toDouble(),
                    notes = (d.notes as? String) ?: ""
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveVersements() {
        val dynamicList = versements.map { v ->
            val obj = js("{}")
            obj.id = v.id
            obj.date = v.date
            obj.sellerName = v.sellerName
            obj.amount = v.amount
            obj.notes = v.notes
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_VERSEMENTS, JSON.stringify(dynamicList))
    }

    private fun loadPhysicalCounts(): Map<String, Double> {
        val json = window.localStorage.getItem(KEY_PHYSICAL_COUNTS) ?: return emptyMap()
        return try {
            val parsed = JSON.parse<dynamic>(json)
            val map = mutableMapOf<String, Double>()
            val keys = js("Object.keys(parsed)") as Array<String>
            for (k in keys) {
                map[k] = (parsed[k] as Number).toDouble()
            }
            map
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    private fun savePhysicalCounts() {
        val obj = js("{}")
        for ((k, v) in physicalCounts) {
            obj[k] = v
        }
        window.localStorage.setItem(KEY_PHYSICAL_COUNTS, JSON.stringify(obj))
    }

    // --- CRUD: Products ---
    fun addProduct(name: String, unitPrice: Double, bottlesPerCrate: Int, category: String, stockInitial: Int, consignationPrice: Double): Product {
        val p = Product(
            id = generateId("prd"),
            name = name.trim(),
            unitPrice = unitPrice,
            bottlesPerCrate = bottlesPerCrate,
            category = category.trim(),
            stockInitial = stockInitial,
            consignationPrice = consignationPrice
        )
        products = products + p
        saveProducts()
        return p
    }

    fun updateProduct(product: Product) {
        products = products.map { if (it.id == product.id) product else it }
        saveProducts()
    }

    fun deleteProduct(id: String) {
        products = products.filterNot { it.id == id }
        saveProducts()
    }

    // --- CRUD: Packaging ---
    fun addPackaging(name: String, crateType: String, emptyCrates: Int, fullCrates: Int, depositValuePerCrate: Double): Packaging {
        val pkg = Packaging(
            id = generateId("pkg"),
            name = name.trim(),
            crateType = crateType.trim(),
            emptyCrates = emptyCrates,
            fullCrates = fullCrates,
            depositValuePerCrate = depositValuePerCrate,
            lastUpdated = todayString()
        )
        packaging = packaging + pkg
        savePackaging()
        return pkg
    }

    fun updatePackaging(pkg: Packaging) {
        packaging = packaging.map { if (it.id == pkg.id) pkg.copy(lastUpdated = todayString()) else it }
        savePackaging()
    }

    fun deletePackaging(id: String) {
        packaging = packaging.filterNot { it.id == id }
        packagingMovements = packagingMovements.filterNot { it.packagingId == id }
        savePackaging()
        savePackagingMovements()
    }

    /**
     * Records a packaging movement and applies its deltas to the target packaging.
     * All quantities in the movement are deltas (positive = gain, negative = loss).
     */
    fun recordPackagingMovement(
        packagingId: String,
        movementType: String,
        quantityEmpty: Int,
        quantityFull: Int,
        notes: String = "",
        contactName: String = ""
    ): PackagingMovement? {
        val pkg = packaging.find { it.id == packagingId } ?: return null
        val movement = PackagingMovement(
            id = generateId("pmv"),
            date = todayString(),
            packagingId = packagingId,
            packagingName = pkg.name,
            movementType = movementType,
            quantityEmpty = quantityEmpty,
            quantityFull = quantityFull,
            notes = notes.trim(),
            contactName = contactName.trim()
        )
        packagingMovements = listOf(movement) + packagingMovements // newest first
        savePackagingMovements()

        // Apply deltas to the packaging
        val updated = pkg.copy(
            emptyCrates = (pkg.emptyCrates + quantityEmpty).coerceAtLeast(0),
            fullCrates = (pkg.fullCrates + quantityFull).coerceAtLeast(0),
            lastUpdated = todayString()
        )
        packaging = packaging.map { if (it.id == packagingId) updated else it }
        savePackaging()
        return movement
    }

    private fun loadPackagingMovements(): List<PackagingMovement> {
        val json = window.localStorage.getItem(KEY_PKG_MOVEMENTS) ?: return emptyList()
        return try {
            val parsed = JSON.parse<Array<dynamic>>(json)
            parsed.map { d ->
                PackagingMovement(
                    id = d.id as String,
                    date = (d.date as? String) ?: todayString(),
                    packagingId = d.packagingId as String,
                    packagingName = (d.packagingName as? String) ?: "",
                    movementType = (d.movementType as? String) ?: "AJUSTEMENT_VIDE",
                    quantityEmpty = (d.quantityEmpty as? Number)?.toInt() ?: 0,
                    quantityFull = (d.quantityFull as? Number)?.toInt() ?: 0,
                    notes = (d.notes as? String) ?: "",
                    contactName = (d.contactName as? String) ?: ""
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun savePackagingMovements() {
        val arr = packagingMovements.map { m ->
            val obj = js("{}")
            obj.id = m.id
            obj.date = m.date
            obj.packagingId = m.packagingId
            obj.packagingName = m.packagingName
            obj.movementType = m.movementType
            obj.quantityEmpty = m.quantityEmpty
            obj.quantityFull = m.quantityFull
            obj.notes = m.notes
            obj.contactName = m.contactName
            obj
        }.toTypedArray()
        window.localStorage.setItem(KEY_PKG_MOVEMENTS, JSON.stringify(arr))
    }

    // --- Stock Entries ---
    fun addEntry(
        productId: String,
        quantityCrates: Double,
        extras: Int = 0,
        totalExtras: Int = 0,
        motif: String = "",
        supplier: String,
        date: String = todayString(),
        notes: String = ""
    ): StockEntry {
        val prod = products.find { it.id == productId } ?: error("Produit non trouvé")
        val entry = StockEntry(
            id = generateId("ent"),
            date = date,
            productId = productId,
            productName = prod.name,
            quantityCrates = quantityCrates,
            extras = extras,
            totalExtras = totalExtras,
            motif = motif.trim(),
            supplier = supplier.trim(),
            notes = notes.trim()
        )
        entries = listOf(entry) + entries
        saveEntries()
        return entry
    }

    fun deleteEntry(id: String) {
        entries = entries.filterNot { it.id == id }
        saveEntries()
    }

    // --- Sales ---
    fun recordSale(
        productId: String,
        quantityCrates: Double,
        unitPrice: Double? = null,
        destination: String = "Dépôt",
        customerName: String = "Comptoir",
        paymentMethod: String = "Espèces",
        date: String = todayString()
    ): Sale {
        val prod = products.find { it.id == productId } ?: error("Produit non trouvé")
        val finalPrice = unitPrice ?: prod.unitPrice
        val total = quantityCrates * finalPrice
        val isCredit = paymentMethod == "À Crédit"

        val sale = Sale(
            id = generateId("sal"),
            date = date,
            productId = productId,
            productName = prod.name,
            quantityCrates = quantityCrates,
            unitPrice = finalPrice,
            totalAmount = total,
            destination = destination,
            customerName = customerName.trim(),
            paymentMethod = paymentMethod,
            isDebt = isCredit
        )
        sales = listOf(sale) + sales
        saveSales()

        // If credit sale, automatically create a debt entry!
        if (isCredit) {
            addDebt(
                debtorName = customerName,
                productId = productId,
                quantityCrates = quantityCrates,
                unitPrice = finalPrice,
                date = date,
                notes = "Généré automatiquement par vente à crédit"
            )
        }

        return sale
    }

    fun deleteSale(id: String) {
        sales = sales.filterNot { it.id == id }
        saveSales()
    }

    // --- Spoiled Products ---
    fun recordSpoiled(
        productId: String,
        quantityBottles: Int,
        comment: String,
        date: String = todayString()
    ): SpoiledProduct {
        val prod = products.find { it.id == productId } ?: error("Produit non trouvé")
        val bpc = if (prod.bottlesPerCrate > 0) prod.bottlesPerCrate else 12
        val estimatedLoss = (prod.unitPrice / bpc) * quantityBottles

        val sp = SpoiledProduct(
            id = generateId("sp"),
            date = date,
            productId = productId,
            productName = prod.name,
            quantityBottles = quantityBottles,
            comment = comment.trim(),
            estimatedLoss = estimatedLoss
        )
        spoiled = listOf(sp) + spoiled
        saveSpoiled()
        return sp
    }

    fun deleteSpoiled(id: String) {
        spoiled = spoiled.filterNot { it.id == id }
        saveSpoiled()
    }

    // --- Debts ---
    fun addDebt(
        debtorName: String,
        productId: String,
        quantityCrates: Double,
        unitPrice: Double? = null,
        amountPaid: Double = 0.0,
        phone: String = "",
        date: String = todayString(),
        notes: String = ""
    ): Debt {
        val prod = products.find { it.id == productId } ?: error("Produit non trouvé")
        val pu = unitPrice ?: prod.unitPrice
        val total = quantityCrates * pu
        val remaining = (total - amountPaid).coerceAtLeast(0.0)
        val status = when {
            remaining <= 0.0 -> "Réglé"
            amountPaid > 0.0 -> "Partiel"
            else -> "Non Payé"
        }

        val debt = Debt(
            id = generateId("deb"),
            date = date,
            debtorName = debtorName.trim(),
            productId = productId,
            productName = prod.name,
            quantityCrates = quantityCrates,
            unitPrice = pu,
            totalAmount = total,
            amountPaid = amountPaid,
            remainingAmount = remaining,
            status = status,
            phone = phone.trim(),
            notes = notes.trim()
        )
        debts = listOf(debt) + debts
        saveDebts()
        return debt
    }

    fun payDebt(debtId: String, paymentAmount: Double) {
        debts = debts.map { d ->
            if (d.id == debtId) {
                val newPaid = (d.amountPaid + paymentAmount).coerceAtMost(d.totalAmount)
                val newRemaining = (d.totalAmount - newPaid).coerceAtLeast(0.0)
                val newStatus = if (newRemaining <= 0.0) "Réglé" else "Partiel"
                d.copy(amountPaid = newPaid, remainingAmount = newRemaining, status = newStatus)
            } else d
        }
        saveDebts()
    }

    fun deleteDebt(id: String) {
        debts = debts.filterNot { it.id == id }
        saveDebts()
    }

    // --- Expenditures ---
    fun addExpense(motive: String, category: String, amount: Double, beneficiary: String = "", date: String = todayString()): Expenditure {
        val ex = Expenditure(
            id = generateId("exp"),
            date = date,
            motive = motive.trim(),
            category = category.trim(),
            amount = amount,
            beneficiary = beneficiary.trim()
        )
        expenses = listOf(ex) + expenses
        saveExpenses()
        return ex
    }

    fun deleteExpense(id: String) {
        expenses = expenses.filterNot { it.id == id }
        saveExpenses()
    }

    // --- Versements ---
    fun addVersement(amount: Double, sellerName: String = "Vendeur ZAMBOY", notes: String = "", date: String = todayString()): CashVersement {
        val v = CashVersement(
            id = generateId("ver"),
            date = date,
            sellerName = sellerName.trim(),
            amount = amount,
            notes = notes.trim()
        )
        versements = listOf(v) + versements
        saveVersements()
        return v
    }

    fun deleteVersement(id: String) {
        versements = versements.filterNot { it.id == id }
        saveVersements()
    }

    // --- Physical Audit Counts ---
    fun updatePhysicalCount(productId: String, count: Double) {
        val updated = physicalCounts.toMutableMap()
        updated[productId] = count
        physicalCounts = updated
        savePhysicalCounts()
    }

    // --- Stock Calculations & Inventory Engine ---
    fun getInventoryRows(filterDate: String? = null): List<InventoryRow> {
        val targetSales = if (filterDate != null) sales.filter { it.date == filterDate } else sales
        val targetEntries = if (filterDate != null) entries.filter { it.date == filterDate } else entries
        val targetSpoiled = if (filterDate != null) spoiled.filter { it.date == filterDate } else spoiled

        return products.map { p ->
            val pEntriesList = targetEntries.filter { it.productId == p.id }
            val pEntries = pEntriesList.sumOf { it.effectiveCrates }
            val pBonusBottles = pEntriesList.sumOf { it.calculatedTotalExtraBottles }
            val totalStock = p.stockInitial + pEntries

            val pSales = targetSales.filter { it.productId == p.id }
            // Bar transfers: recorded for stock tracking between sale and stock movement, but NOT included in sold revenue
            val barSales = pSales.filter { it.destination == "Bar" }.sumOf { it.quantityCrates }
            // Depot / wholesale sales: what was actually sold to customers
            val depotSales = pSales.filter { it.destination != "Bar" }.sumOf { it.quantityCrates }
            val totalSorties = barSales + depotSales

            val pSpoiled = targetSpoiled.filter { it.productId == p.id }.sumOf { it.quantityBottles }
            val bpc = if (p.bottlesPerCrate > 0) p.bottlesPerCrate else 12
            val spoiledCratesEquiv = (pSpoiled.toDouble() / bpc * 100.0).roundToInt() / 100.0

            val resteTheorique = ((totalStock - totalSorties - spoiledCratesEquiv) * 100.0).roundToInt() / 100.0
            val physicalCount = physicalCounts[p.id] ?: resteTheorique
            val ecart = ((physicalCount - resteTheorique) * 100.0).roundToInt() / 100.0

            // Revenue excludes bar transfers (they are internal movements, not sales)
            val montantVentes = pSales.filter { it.destination != "Bar" }.sumOf { it.totalAmount }
            val montantTransfertBar = pSales.filter { it.destination == "Bar" }.sumOf { it.totalAmount }

            InventoryRow(
                product = p,
                stockInitial = p.stockInitial.toDouble(),
                entrees = pEntries,
                entreesBonusBottles = pBonusBottles,
                totalStock = totalStock,
                sortiesBar = barSales,
                ventesQty = depotSales,
                totalSorties = totalSorties,
                avariesBottles = pSpoiled,
                avariesCratesEquiv = spoiledCratesEquiv,
                resteTheorique = resteTheorique,
                restePhysique = physicalCount,
                ecart = ecart,
                unitPrice = p.unitPrice,
                montantVentes = montantVentes,
                montantTransfertBar = montantTransfertBar
            )
        }
    }

    fun getFinancialReconciliation(filterDate: String? = null): FinancialReconciliation {
        val targetSales = if (filterDate != null) sales.filter { it.date == filterDate } else sales
        val targetVersements = if (filterDate != null) versements.filter { it.date == filterDate } else versements
        val targetSpoiled = if (filterDate != null) spoiled.filter { it.date == filterDate } else spoiled
        val targetExpenses = if (filterDate != null) expenses.filter { it.date == filterDate } else expenses
        val targetDebts = if (filterDate != null) debts.filter { it.date == filterDate } else debts

        // Bar transfers are internal movements — exclude from cash reconciliation
        val totalVentes = targetSales.filter { it.destination != "Bar" }.sumOf { it.totalAmount }
        val totalVersement = targetVersements.sumOf { it.amount }
        val totalAvarisValeur = targetSpoiled.sumOf { it.estimatedLoss }
        val totalDepenses = targetExpenses.sumOf { it.amount }
        val totalDettes = targetDebts.sumOf { it.remainingAmount }

        // Financial formula: Difference / Ecart de caisse:
        // Cash turned in + valid deductions (expenses, remaining customer debts, avaries losses) vs Total sales
        val difference = (totalVersement + totalDepenses + totalDettes) - totalVentes

        return FinancialReconciliation(
            totalVentes = totalVentes,
            totalVersement = totalVersement,
            totalAvarisValeur = totalAvarisValeur,
            totalDepenses = totalDepenses,
            totalDettes = totalDettes,
            difference = difference
        )
    }

    // --- Dashboard Metrics ---
    fun getTotalStockCrates(): Int {
        val rows = getInventoryRows()
        return rows.sumOf { it.resteTheorique.roundToInt() }
    }

    fun getTotalStockValue(): Double {
        val rows = getInventoryRows()
        return rows.sumOf { it.resteTheorique * it.unitPrice }
    }

    fun getTodaySalesAmount(): Double {
        val today = todayString()
        return sales.filter { it.date == today && it.destination != "Bar" }.sumOf { it.totalAmount }
    }

    fun getTodaySalesCrates(): Double {
        val today = todayString()
        return sales.filter { it.date == today && it.destination != "Bar" }.sumOf { it.quantityCrates }
    }

    fun getTodayBarTransfersAmount(): Double {
        val today = todayString()
        return sales.filter { it.date == today && it.destination == "Bar" }.sumOf { it.totalAmount }
    }

    fun getTodayBarTransfersCrates(): Double {
        val today = todayString()
        return sales.filter { it.date == today && it.destination == "Bar" }.sumOf { it.quantityCrates }
    }

    fun getTotalActiveDebtsAmount(): Double {
        return debts.filter { it.status != "Réglé" }.sumOf { it.remainingAmount }
    }

    fun getTodayExpensesAmount(): Double {
        val today = todayString()
        return expenses.filter { it.date == today }.sumOf { it.amount }
    }

    fun getLowStockProducts(threshold: Int = 10): List<InventoryRow> {
        return getInventoryRows().filter { it.resteTheorique <= threshold }
    }

    // --- Backup & Restore ---
    fun exportBackupJson(): String {
        val backup = js("{}")
        backup.version = "1.0"
        backup.exportDate = Date().toISOString()
        backup.products = window.localStorage.getItem(KEY_PRODUCTS)
        backup.packaging = window.localStorage.getItem(KEY_PACKAGING)
        backup.entries = window.localStorage.getItem(KEY_ENTRIES)
        backup.sales = window.localStorage.getItem(KEY_SALES)
        backup.spoiled = window.localStorage.getItem(KEY_SPOILED)
        backup.debts = window.localStorage.getItem(KEY_DEBTS)
        backup.expenses = window.localStorage.getItem(KEY_EXPENSES)
        backup.versements = window.localStorage.getItem(KEY_VERSEMENTS)
        backup.physicalCounts = window.localStorage.getItem(KEY_PHYSICAL_COUNTS)
        return JSON.stringify(backup)
    }

    fun resetToDefaults() {
        window.localStorage.removeItem(KEY_PRODUCTS)
        window.localStorage.removeItem(KEY_PACKAGING)
        window.localStorage.removeItem(KEY_ENTRIES)
        window.localStorage.removeItem(KEY_SALES)
        window.localStorage.removeItem(KEY_SPOILED)
        window.localStorage.removeItem(KEY_DEBTS)
        window.localStorage.removeItem(KEY_EXPENSES)
        window.localStorage.removeItem(KEY_VERSEMENTS)
        window.localStorage.removeItem(KEY_PHYSICAL_COUNTS)
        isInitialized = false
        init()
    }
}
