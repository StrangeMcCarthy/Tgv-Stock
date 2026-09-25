package org.app.tgvstock.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.ActionCardStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.toSitePalette

@InitRoute
fun initHomePage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Tableau de Bord"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun HomePage() {
    val router = rememberPageContext().router
    val palette = ColorMode.current.toSitePalette()

    // Modals state for quick action on dashboard
    var showSaleModal by remember { mutableStateOf(false) }
    var showEntryModal by remember { mutableStateOf(false) }
    var showSpoiledModal by remember { mutableStateOf(false) }
    var showDebtModal by remember { mutableStateOf(false) }
    var showExpenseModal by remember { mutableStateOf(false) }
    var showStockAuditModal by remember { mutableStateOf(false) }

    // Search query for stock table
    var stockSearchQuery by remember { mutableStateOf("") }

    val inventoryRows = StockRepository.getInventoryRows()
    val lowStockList = inventoryRows.filter { it.resteTheorique <= 10.0 }
    val totalCrates = inventoryRows.sumOf { it.resteTheorique.toInt() }
    val totalValue = StockRepository.getTotalStockValue()
    val todaySales = StockRepository.getTodaySalesAmount()
    val activeDebts = StockRepository.getTotalActiveDebtsAmount()
    val todayExpenses = StockRepository.getTodayExpensesAmount()

    val emptyPackagingCount = StockRepository.packaging.sumOf { it.emptyCrates }
    val fullPackagingCount = StockRepository.packaging.sumOf { it.fullCrates }

    Column(Modifier.fillMaxWidth().gap(2.cssRem)) {

        // Hero & Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.5.cssRem)) {
                    Span(HeadlineTextStyle.toModifier().toAttrs()) {
                        Text("Dépôt TGV Stock")
                    }
                    Box(
                        Modifier
                            .backgroundColor(palette.success.toRgb().copyf(alpha = 0.2f))
                            .color(palette.success)
                            .padding(leftRight = 0.6.cssRem, topBottom = 0.2.cssRem)
                            .borderRadius(20.px)
                            .fontSize(0.75.cssRem)
                            .fontWeight(FontWeight.Bold)
                    ) {
                        Text("● Opérationnel")
                    }
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Date: ${StockRepository.todayString()} — Suivi en temps réel des ventes, entrées, avaries, dettes et bilans financiers.")
                }
            }

            Spacer()

            Row(Modifier.gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                AppButton(
                    text = "Fiche d'Inventaire",
                    onClick = { router.tryRoutingTo("/inventory") },
                    icon = { FaClipboardList() },
                    backgroundColor = palette.brand.primary
                )
                AppButton(
                    text = "Rapports PDF",
                    onClick = { router.tryRoutingTo("/reports") },
                    icon = { FaFilePdf() },
                    backgroundColor = Color.rgb(0xDC2626),
                    textColor = Colors.White
                )
            }
        }

        // Top KPI Cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))") }
                .gap(1.2.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = "Valeur du Stock",
                value = formatFcfa(totalValue),
                subtitle = "$totalCrates casiers en stock théorique",
                icon = { FaWarehouse() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = "Ventes Aujourd'hui",
                value = formatFcfa(todaySales),
                subtitle = "${StockRepository.sales.filter { it.date == StockRepository.todayString() }.size} transactions enregistrées",
                icon = { FaCashRegister() },
                accentColor = palette.success
            )

            StatCard(
                title = "Dettes Clients Actives",
                value = formatFcfa(activeDebts),
                subtitle = "${StockRepository.debts.filter { it.status != "Réglé" }.size} créances en attente",
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            StatCard(
                title = "Dépenses du Jour",
                value = formatFcfa(todayExpenses),
                subtitle = "${StockRepository.expenses.filter { it.date == StockRepository.todayString() }.size} motifs de charges",
                icon = { FaReceipt() },
                accentColor = Color.rgb(0x8B5CF6)
            )

            StatCard(
                title = "Emballages (Casiers)",
                value = "$emptyPackagingCount Vides / $fullPackagingCount Pleins",
                subtitle = "Total: ${emptyPackagingCount + fullPackagingCount} casiers suivis",
                icon = { FaBoxesStacked() },
                accentColor = Color.rgb(0x06B6D4)
            )
        }

        // Action Buttons Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(1.1.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                        Text("Actions Rapides du Dépôt")
                    }
                    Spacer()
                    Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                        Text("Accès direct aux modules d'enregistrement")
                    }
                }

                Div(
                    Modifier
                        .fillMaxWidth()
                        .display(DisplayStyle.Grid)
                        .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(180px, 1fr))") }
                        .gap(1.cssRem)
                        .toAttrs()
                ) {
                    QuickActionTile(
                        title = "Nouvelle Vente",
                        desc = "Enregistrer une sortie",
                        icon = { FaCartPlus() },
                        color = palette.success,
                        onClick = { showSaleModal = true }
                    )

                    QuickActionTile(
                        title = "Nouvel Arrivage",
                        desc = "Entrée livraison camion",
                        icon = { FaTruckLoading() },
                        color = palette.brand.primary,
                        onClick = { showEntryModal = true }
                    )

                    QuickActionTile(
                        title = "Déclarer Avarie",
                        desc = "Casse & bouteilles gâtées",
                        icon = { FaWineGlass() },
                        color = palette.danger,
                        onClick = { showSpoiledModal = true }
                    )

                    QuickActionTile(
                        title = "Nouvelle Dette",
                        desc = "Casiers dus par client",
                        icon = { FaHandHoldingDollar() },
                        color = Color.rgb(0xEA580C),
                        onClick = { showDebtModal = true }
                    )

                    QuickActionTile(
                        title = "Nouvelle Dépense",
                        desc = "Charges, manutention, etc.",
                        icon = { FaReceipt() },
                        color = Color.rgb(0x9333EA),
                        onClick = { showExpenseModal = true }
                    )

                    QuickActionTile(
                        title = "Fiche d'Inventaire",
                        desc = "Bilan et écart de caisse",
                        icon = { FaClipboardCheck() },
                        color = Color.rgb(0x2563EB),
                        onClick = { router.tryRoutingTo("/inventory") }
                    )

                    QuickActionTile(
                        title = "État des Stocks",
                        desc = "Audit physique / Théorique",
                        icon = { FaBoxesStacked() },
                        color = Color.rgb(0x0D9488),
                        onClick = { showStockAuditModal = true }
                    )

                    QuickActionTile(
                        title = "Exporter PDF",
                        desc = "Journalier / Mensuel / Annuel",
                        icon = { FaFilePdf() },
                        color = Color.rgb(0xDC2626),
                        onClick = { router.tryRoutingTo("/reports") }
                    )
                }
            }
        }

        // Low stock alerts banner (if any)
        if (lowStockList.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.rgb(0xFEF2F2))
                    .border(1.px, LineStyle.Solid, Color.rgb(0xFECACA))
                    .borderRadius(12.px)
                    .padding(1.cssRem)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(2.2.cssRem)
                            .borderRadius(50.percent)
                            .backgroundColor(Color.rgb(0xEF4444))
                            .color(Colors.White)
                            .margin(right = 0.8.cssRem),
                        contentAlignment = Alignment.Center
                    ) {
                        FaTriangleExclamation()
                    }
                    Column {
                        Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).fontSize(0.95.cssRem).toAttrs()) {
                            Text("Alerte Stock Bas (${lowStockList.size} produits sous le seuil de 10 casiers)")
                        }
                        Span(Modifier.color(Color.rgb(0x7F1D1D)).fontSize(0.85.cssRem).toAttrs()) {
                            Text(lowStockList.take(6).joinToString(", ") { "${it.product.name} (${it.resteTheorique.toInt()} casiers)" } + if (lowStockList.size > 6) "..." else "")
                        }
                    }
                    Spacer()
                    AppButton(
                        text = "Réapprovisionner",
                        onClick = { showEntryModal = true },
                        icon = { FaTruckLoading() },
                        backgroundColor = Color.rgb(0xEF4444),
                        textColor = Colors.White
                    )
                }
            }
        }

        // Real-Time Stock Status Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.2.cssRem)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Span(Modifier.fontSize(1.2.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("État Instantané des Stocks")
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("Calcul automatique en direct : Initial + Entrées - Ventes - Avaries = Reste")
                        }
                    }

                    Spacer()

                    AppTextInput(
                        value = stockSearchQuery,
                        onValueChange = { stockSearchQuery = it },
                        placeholderText = "Rechercher un produit...",
                        type = "search",
                        modifier = Modifier.width(18.cssRem)
                    )
                }

                // Table
                Div(
                    Modifier
                        .fillMaxWidth()
                        .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                        .toAttrs()
                ) {
                    Table(
                        Modifier
                            .fillMaxWidth()
                            .borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse)
                            .fontSize(0.88.cssRem)
                            .toAttrs()
                    ) {
                        Thead {
                            Tr(
                                Modifier
                                    .backgroundColor(palette.nearBackground)
                                    .borderBottom(2.px, LineStyle.Solid, palette.cardBorder)
                                    .toAttrs()
                            ) {
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("PRODUIT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("CATÉGORIE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("STOCK INITIAL") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ENTRÉES") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("SORTIES") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("RESTE EN STOCK") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("P.U (FCFA)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("VALEUR EN STOCK") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTIONS") }
                            }
                        }
                        Tbody {
                            val filteredRows = inventoryRows.filter {
                                stockSearchQuery.isBlank() || it.product.name.contains(stockSearchQuery, ignoreCase = true) || it.product.category.contains(stockSearchQuery, ignoreCase = true)
                            }

                            if (filteredRows.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text("Aucun produit ne correspond à votre recherche.")
                                    }
                                }
                            } else {
                                filteredRows.forEach { row ->
                                    val isLow = row.resteTheorique <= 10.0
                                    Tr(
                                        Modifier
                                            .borderBottom(1.px, LineStyle.Solid, palette.cardBorder)
                                            .backgroundColor(if (isLow) Color.rgb(0xFEF2F2).toRgb().copyf(alpha = if (ColorMode.current.isDark) 0.15f else 0.5f) else Colors.Transparent)
                                            .toAttrs()
                                    ) {
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.4.cssRem)) {
                                                Text(row.product.name)
                                                if (isLow) {
                                                    Span(Modifier.fontSize(0.7.cssRem).color(palette.danger).fontWeight(FontWeight.Bold).toAttrs()) {
                                                        Text("(Bas)")
                                                    }
                                                }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(row.product.category) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${row.stockInitial} casiers") }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) { Text("+${row.entrees}") }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.danger).fontWeight(FontWeight.SemiBold).toAttrs()) { Text("-${row.totalSorties}") }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Box(
                                                Modifier
                                                    .backgroundColor(if (isLow) palette.danger.toRgb().copyf(alpha = 0.2f) else palette.success.toRgb().copyf(alpha = 0.2f))
                                                    .color(if (isLow) palette.danger else palette.success)
                                                    .borderRadius(12.px)
                                                    .padding(leftRight = 0.6.cssRem, topBottom = 0.2.cssRem)
                                                    .fontWeight(FontWeight.Bold)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text("${row.resteTheorique} casiers")
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                            Text(formatFcfa(row.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                            Text(formatFcfa(row.resteTheorique * row.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            AppButton(
                                                text = "Vendre",
                                                onClick = { showSaleModal = true },
                                                modifier = Modifier.fontSize(0.75.cssRem).padding(leftRight = 0.6.cssRem, topBottom = 0.25.cssRem)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    QuickSaleModal(isOpen = showSaleModal, onClose = { showSaleModal = false })
    QuickEntryModal(isOpen = showEntryModal, onClose = { showEntryModal = false })
    QuickSpoiledModal(isOpen = showSpoiledModal, onClose = { showSpoiledModal = false })
    QuickDebtModal(isOpen = showDebtModal, onClose = { showDebtModal = false })
    QuickExpenseModal(isOpen = showExpenseModal, onClose = { showExpenseModal = false })
    StockAuditQuickModal(isOpen = showStockAuditModal, onClose = { showStockAuditModal = false })
}

@Composable
fun QuickActionTile(
    title: String,
    desc: String,
    icon: @Composable () -> Unit,
    color: Color,
    onClick: () -> Unit
) {
    val palette = ColorMode.current.toSitePalette()
    Box(
        ActionCardStyle.toModifier().onClick { onClick() }
    ) {
        Column(Modifier.gap(0.4.cssRem)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(2.2.cssRem)
                        .borderRadius(8.px)
                        .backgroundColor(color.toRgb().copyf(alpha = 0.15f))
                        .color(color)
                        .fontSize(1.1.cssRem),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Spacer()
                FaArrowRight(Modifier.fontSize(0.75.cssRem).color(palette.textMuted))
            }
            Span(Modifier.fontWeight(FontWeight.Bold).fontSize(0.95.cssRem).toAttrs()) {
                Text(title)
            }
            Span(Modifier.fontSize(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                Text(desc)
            }
        }
    }
}

// Quick Modals for Dashboard
@Composable
fun QuickSaleModal(isOpen: Boolean, onClose: () -> Unit) {
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableStateOf("1") }
    var destination by remember { mutableStateOf("Dépôt") }
    var customerName by remember { mutableStateOf("Comptoir") }
    var paymentMethod by remember { mutableStateOf("Espèces") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    val qtyInt = quantity.toIntOrNull() ?: 1
    val pu = prod?.unitPrice ?: 0.0
    val total = qtyInt * pu

    ModalDialog(title = "Enregistrer une Vente", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Produit") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) selected() }) {
                            Text("${p.name} — ${formatFcfa(p.unitPrice)}")
                        }
                    }
                }
            }

            FormField(label = "Quantité (en Casiers / Cartons)") {
                AppTextInput(
                    value = quantity,
                    onValueChange = { quantity = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Destination") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { destination = it.value ?: "Dépôt" }
                    }
                ) {
                    Option("Dépôt") { Text("Vente Directe Dépôt") }
                    Option("Bar") { Text("Transfert / Consommation Bar") }
                    Option("Client Gros") { Text("Client Grossiste") }
                }
            }

            FormField(label = "Client / Acheteur") {
                AppTextInput(
                    value = customerName,
                    onValueChange = { customerName = it },
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Mode de Paiement") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { paymentMethod = it.value ?: "Espèces" }
                    }
                ) {
                    Option("Espèces") { Text("Espèces (Cash)") }
                    Option("Mobile Money") { Text("Mobile Money (OM/MOMO)") }
                    Option("À Crédit") { Text("À Crédit (Ajouter aux Dettes)") }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.rgb(0xFEF3C7))
                    .padding(0.75.cssRem)
                    .borderRadius(8.px)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x92400E)).toAttrs()) {
                        Text("Montant Total : ")
                    }
                    Spacer()
                    Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.2.cssRem).color(Color.rgb(0x92400E)).toAttrs()) {
                        Text(formatFcfa(total))
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Confirmer la Vente",
                    onClick = {
                        if (selectedProductId.isNotBlank() && qtyInt > 0) {
                            StockRepository.recordSale(
                                productId = selectedProductId,
                                quantityCrates = qtyInt,
                                destination = destination,
                                customerName = customerName,
                                paymentMethod = paymentMethod
                            )
                            onClose()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuickEntryModal(isOpen: Boolean, onClose: () -> Unit) {
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableStateOf("10") }
    var supplier by remember { mutableStateOf("Boissons du Cameroun") }
    var notes by remember { mutableStateOf("") }

    ModalDialog(title = "Nouvel Arrivage / Entrée en Stock", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Produit Réceptionné") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) selected() }) {
                            Text(p.name)
                        }
                    }
                }
            }

            FormField(label = "Quantité Reçue (en Casiers / Cartons)") {
                AppTextInput(
                    value = quantity,
                    onValueChange = { quantity = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Fournisseur / Brasserie") {
                AppTextInput(
                    value = supplier,
                    onValueChange = { supplier = it },
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Notes / Bordereau de livraison") {
                AppTextInput(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholderText = "Ex: Camion #3, BL 14890",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Enregistrer l'Entrée",
                    onClick = {
                        val qty = quantity.toIntOrNull() ?: 0
                        if (selectedProductId.isNotBlank() && qty > 0) {
                            StockRepository.addEntry(
                                productId = selectedProductId,
                                quantityCrates = qty,
                                supplier = supplier,
                                notes = notes
                            )
                            onClose()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuickSpoiledModal(isOpen: Boolean, onClose: () -> Unit) {
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantityBottles by remember { mutableStateOf("1") }
    var comment by remember { mutableStateOf("") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    val qb = quantityBottles.toIntOrNull() ?: 1
    val bpc = if ((prod?.bottlesPerCrate ?: 12) > 0) prod!!.bottlesPerCrate else 12
    val loss = ((prod?.unitPrice ?: 0.0) / bpc) * qb

    ModalDialog(title = "Déclarer un Produit Gâté / Avarie", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Sélectionner le Produit") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) selected() }) {
                            Text("${p.name} (${p.bottlesPerCrate} btles/casier)")
                        }
                    }
                }
            }

            FormField(label = "Quantité en Bouteilles") {
                AppTextInput(
                    value = quantityBottles,
                    onValueChange = { quantityBottles = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Commentaire / Cause de l'avarie") {
                AppTextInput(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholderText = "Ex: Cassée lors de la manutention camion, capsule défectueuse...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.rgb(0xFEE2E2))
                    .padding(0.75.cssRem)
                    .borderRadius(8.px)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).toAttrs()) {
                        Text("Perte Financière Estimée : ")
                    }
                    Spacer()
                    Span(Modifier.fontWeight(FontWeight.Bold).fontSize(1.1.cssRem).color(Color.rgb(0x991B1B)).toAttrs()) {
                        Text(formatFcfa(loss))
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Enregistrer l'Avarie",
                    onClick = {
                        if (selectedProductId.isNotBlank() && qb > 0) {
                            StockRepository.recordSpoiled(
                                productId = selectedProductId,
                                quantityBottles = qb,
                                comment = comment
                            )
                            onClose()
                        }
                    },
                    backgroundColor = Color.rgb(0xDC2626),
                    textColor = Colors.White
                )
            }
        }
    }
}

@Composable
fun QuickDebtModal(isOpen: Boolean, onClose: () -> Unit) {
    var debtorName by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantityCrates by remember { mutableStateOf("1") }
    var amountPaid by remember { mutableStateOf("0") }
    var phone by remember { mutableStateOf("") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    val cratesInt = quantityCrates.toIntOrNull() ?: 1
    val pu = prod?.unitPrice ?: 0.0
    val total = cratesInt * pu
    val paid = amountPaid.toDoubleOrNull() ?: 0.0
    val remaining = (total - paid).coerceAtLeast(0.0)

    ModalDialog(title = "Enregistrer une Dette Client", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Nom du Débiteur") {
                AppTextInput(
                    value = debtorName,
                    onValueChange = { debtorName = it },
                    placeholderText = "Ex: Franky, Bar Le Repère, etc.",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Produit dû") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) selected() }) {
                            Text("${p.name} — ${formatFcfa(p.unitPrice)}/casier")
                        }
                    }
                }
            }

            FormField(label = "Quantité en Casiers dus") {
                AppTextInput(
                    value = quantityCrates,
                    onValueChange = { quantityCrates = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Montant Avancé / Payé (FCFA)") {
                AppTextInput(
                    value = amountPaid,
                    onValueChange = { amountPaid = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Téléphone Débiteur (Optionnel)") {
                AppTextInput(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholderText = "Ex: 699 00 00 00",
                    type = "tel",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.rgb(0xFEF2F2))
                    .padding(0.75.cssRem)
                    .borderRadius(8.px)
            ) {
                Column(Modifier.fillMaxWidth().gap(0.3.cssRem)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.color(Color.rgb(0x991B1B)).fontSize(0.9.cssRem).toAttrs()) {
                            Text("Prix Calculé Automatiquement ($cratesInt casiers × ${formatFcfa(pu)}) : ")
                        }
                        Spacer()
                        Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).toAttrs()) {
                            Text(formatFcfa(total))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).fontSize(1.05.cssRem).toAttrs()) {
                            Text("Reste à Payer : ")
                        }
                        Spacer()
                        Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.15.cssRem).color(Color.rgb(0xDC2626)).toAttrs()) {
                            Text(formatFcfa(remaining))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Enregistrer la Dette",
                    onClick = {
                        if (debtorName.isNotBlank() && selectedProductId.isNotBlank() && cratesInt > 0) {
                            StockRepository.addDebt(
                                debtorName = debtorName,
                                productId = selectedProductId,
                                quantityCrates = cratesInt,
                                amountPaid = paid,
                                phone = phone
                            )
                            onClose()
                        }
                    },
                    backgroundColor = Color.rgb(0xDC2626),
                    textColor = Colors.White
                )
            }
        }
    }
}

@Composable
fun QuickExpenseModal(isOpen: Boolean, onClose: () -> Unit) {
    var motive by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Manutention") }
    var amount by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("") }

    ModalDialog(title = "Enregistrer une Dépense", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Motif de la Dépense") {
                AppTextInput(
                    value = motive,
                    onValueChange = { motive = it },
                    placeholderText = "Ex: Déchargement camion brasserie, achat blocs de glace...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Catégorie") {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { category = it.value ?: "Manutention" }
                    }
                ) {
                    Option("Manutention") { Text("Manutention / Déchargement") }
                    Option("Transport") { Text("Transport & Logistique") }
                    Option("Factures") { Text("Factures (Électricité / Eau / Glace)") }
                    Option("Personnel") { Text("Personnel & Rations") }
                    Option("Divers") { Text("Charges Diverses") }
                }
            }

            FormField(label = "Montant Dépensé (FCFA)") {
                AppTextInput(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholderText = "Ex: 5000",
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Bénéficiaire (Optionnel)") {
                AppTextInput(
                    value = beneficiary,
                    onValueChange = { beneficiary = it },
                    placeholderText = "Ex: Débardeurs, Chauffeur, Enéo",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Enregistrer la Dépense",
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (motive.isNotBlank() && amt > 0.0) {
                            StockRepository.addExpense(
                                motive = motive,
                                category = category,
                                amount = amt,
                                beneficiary = beneficiary
                            )
                            onClose()
                        }
                    },
                    backgroundColor = Color.rgb(0x9333EA),
                    textColor = Colors.White
                )
            }
        }
    }
}

@Composable
fun StockAuditQuickModal(isOpen: Boolean, onClose: () -> Unit) {
    val rows = StockRepository.getInventoryRows()
    val palette = ColorMode.current.toSitePalette()

    ModalDialog(title = "État Instantané des Stocks & Audit", isOpen = isOpen, onClose = onClose, maxWidth = 55.cssRem) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                Text("Vue globale du stock théorique calculé vs stock physique compté. Saisissez le reste physique pour calculer l'écart en direct.")
            }

            Div(
                Modifier
                    .fillMaxWidth()
                    .maxHeight(50.vh)
                    .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                    .toAttrs()
            ) {
                Table(
                    Modifier.fillMaxWidth().borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse).fontSize(0.85.cssRem).toAttrs()
                ) {
                    Thead {
                        Tr(Modifier.backgroundColor(palette.nearBackground).toAttrs()) {
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("Produit") }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("Initial + Entrées") }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("Sorties") }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("Reste Théorique") }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("Reste Physique") }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("Écart") }
                        }
                    }
                    Tbody {
                        rows.forEach { r ->
                            Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                Td(Modifier.padding(0.5.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.product.name) }
                                Td(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${r.totalStock}") }
                                Td(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${r.totalSorties}") }
                                Td(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text("${r.resteTheorique}") }
                                Td(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                    AppTextInput(
                                        value = r.restePhysique.toString(),
                                        onValueChange = { vStr ->
                                            val v = vStr.toDoubleOrNull() ?: r.resteTheorique
                                            StockRepository.updatePhysicalCount(r.product.id, v)
                                        },
                                        type = "number",
                                        modifier = Modifier.width(5.cssRem).padding(0.2.cssRem)
                                    )
                                }
                                Td(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                    val ecartColor = when {
                                        r.ecart < 0 -> palette.danger
                                        r.ecart > 0 -> palette.brand.primary
                                        else -> palette.success
                                    }
                                    Span(Modifier.color(ecartColor).fontWeight(FontWeight.Bold).toAttrs()) {
                                        Text(if (r.ecart > 0) "+${r.ecart}" else "${r.ecart}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppButton(
                    text = "Fermer l'Audit",
                    onClick = onClose
                )
            }
        }
    }
}
