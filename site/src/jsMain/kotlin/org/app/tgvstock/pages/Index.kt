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
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import kotlin.math.roundToInt
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.models.*
import org.app.tgvstock.ActionCardStyle
import org.app.tgvstock.FilterBarStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette

@InitRoute
fun initHomePage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Dashboard"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun HomePage() {
    val router = rememberPageContext().router
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

    // Modals state for quick action on dashboard
    var showSaleModal by remember { mutableStateOf(false) }
    var saleModalDestination by remember { mutableStateOf("Dépôt") }
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
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.5.cssRem).flexWrap(FlexWrap.Wrap)) {
                    Span(HeadlineTextStyle.toModifier().toAttrs()) {
                        Text(strings.dashTitle)
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
                        Text(strings.operational)
                    }
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Date: ${StockRepository.todayString()} — ${strings.dashSubtitle}")
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnInventorySheet,
                    onClick = { router.tryRoutingTo("/inventory") },
                    icon = { FaClipboardList() },
                    backgroundColor = palette.brand.primary
                )
                AppButton(
                    text = strings.btnPdfReports,
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
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(150px, 1fr))") }
                .gap(0.85.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = strings.statStockValue,
                value = formatFcfa(totalValue),
                subtitle = "$totalCrates ${strings.statStockValueSub}",
                icon = { FaWarehouse() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = strings.statTodaySales,
                value = formatFcfa(todaySales),
                subtitle = "${StockRepository.sales.filter { it.date == StockRepository.todayString() }.size} ${strings.statTodaySalesSub}",
                icon = { FaCashRegister() },
                accentColor = palette.success
            )

            StatCard(
                title = strings.statActiveDebts,
                value = formatFcfa(activeDebts),
                subtitle = "${StockRepository.debts.filter { it.status != "Réglé" }.size} ${strings.statActiveDebtsSub}",
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            StatCard(
                title = strings.statTodayExpenses,
                value = formatFcfa(todayExpenses),
                subtitle = "${StockRepository.expenses.filter { it.date == StockRepository.todayString() }.size} ${strings.statTodayExpensesSub}",
                icon = { FaReceipt() },
                accentColor = Color.rgb(0x8B5CF6)
            )

            StatCard(
                title = strings.statPackaging,
                value = "$emptyPackagingCount Vides / $fullPackagingCount Pleins",
                subtitle = "Total: ${emptyPackagingCount + fullPackagingCount} ${strings.statPackagingSub}",
                icon = { FaBoxesStacked() },
                accentColor = Color.rgb(0x06B6D4)
            )
        }

        // Action Buttons Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(1.1.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                        Text(strings.quickActionsTitle)
                    }
                    Spacer()
                    Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                        Text(strings.quickActionsSubtitle)
                    }
                }

                Div(
                    Modifier
                        .fillMaxWidth()
                        .display(DisplayStyle.Grid)
                        .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(135px, 1fr))") }
                        .gap(0.75.cssRem)
                        .toAttrs()
                ) {
                    QuickActionTile(
                        title = strings.qaNewSale,
                        desc = strings.qaNewSaleDesc,
                        icon = { FaCartPlus() },
                        color = palette.success,
                        onClick = {
                            saleModalDestination = "Dépôt"
                            showSaleModal = true
                        }
                    )

                    QuickActionTile(
                        title = "Transfert Bar",
                        desc = "Sortie magasin vers le bar (hors vente)",
                        icon = { FaWineGlass() },
                        color = Color.rgb(0x8B5CF6),
                        onClick = {
                            saleModalDestination = "Bar"
                            showSaleModal = true
                        }
                    )

                    QuickActionTile(
                        title = strings.qaNewEntry,
                        desc = strings.qaNewEntryDesc,
                        icon = { FaTruck() },
                        color = palette.brand.primary,
                        onClick = { showEntryModal = true }
                    )

                    QuickActionTile(
                        title = strings.qaSpoiled,
                        desc = strings.qaSpoiledDesc,
                        icon = { FaWineGlass() },
                        color = palette.danger,
                        onClick = { showSpoiledModal = true }
                    )

                    QuickActionTile(
                        title = strings.qaNewDebt,
                        desc = strings.qaNewDebtDesc,
                        icon = { FaHandHoldingDollar() },
                        color = Color.rgb(0xEA580C),
                        onClick = { showDebtModal = true }
                    )

                    QuickActionTile(
                        title = strings.qaNewExpense,
                        desc = strings.qaNewExpenseDesc,
                        icon = { FaReceipt() },
                        color = Color.rgb(0x9333EA),
                        onClick = { showExpenseModal = true }
                    )

                    QuickActionTile(
                        title = strings.qaInventorySheet,
                        desc = strings.qaInventorySheetDesc,
                        icon = { FaClipboardCheck() },
                        color = Color.rgb(0x2563EB),
                        onClick = { router.tryRoutingTo("/inventory") }
                    )

                    QuickActionTile(
                        title = strings.qaStockAudit,
                        desc = strings.qaStockAuditDesc,
                        icon = { FaBoxesStacked() },
                        color = Color.rgb(0x0D9488),
                        onClick = { showStockAuditModal = true }
                    )

                    QuickActionTile(
                        title = strings.qaExportPdf,
                        desc = strings.qaExportPdfDesc,
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
                    .padding(0.85.cssRem)
            ) {
                Div(PageHeaderStyle.toAttrs()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(2.2.cssRem)
                                .flexShrink(0)
                                .borderRadius(50.percent)
                                .backgroundColor(Color.rgb(0xEF4444))
                                .color(Colors.White)
                                .margin(right = 0.8.cssRem),
                            contentAlignment = Alignment.Center
                        ) {
                            FaTriangleExclamation()
                        }
                        Column {
                            Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).fontSize(0.92.cssRem).toAttrs()) {
                                Text("${strings.lowStockAlert} (${lowStockList.size} ${strings.lowStockAlertSub})")
                            }
                            Span(Modifier.color(Color.rgb(0x7F1D1D)).fontSize(0.8.cssRem).toAttrs()) {
                                Text(lowStockList.take(6).joinToString(", ") { "${it.product.name} (${it.resteTheorique.toInt()} ${strings.crates})" } + if (lowStockList.size > 6) "..." else "")
                            }
                        }
                    }
                    AppButton(
                        text = strings.btnRestock,
                        onClick = { showEntryModal = true },
                        icon = { FaTruck() },
                        backgroundColor = Color.rgb(0xEF4444),
                        textColor = Colors.White
                    )
                }
            }
        }

        // Real-Time Stock Status Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Div(FilterBarStyle.toAttrs()) {
                    Column {
                        Span(Modifier.fontSize(1.15.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text(strings.tableStockTitle)
                        }
                        Span(Modifier.fontSize(0.82.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.tableStockSubtitle)
                        }
                    }

                    AppTextInput(
                        value = stockSearchQuery,
                        onValueChange = { stockSearchQuery = it },
                        placeholderText = strings.searchProduct,
                        type = "search",
                        modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                    )
                }

                // Table
                ResponsiveTableContainer(minWidth = 780.px) {
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProduct) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colCategory) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colInitialStock) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colEntries) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colOutflows) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colRemainingStock) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colUnitPrice) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colStockValue) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colActions) }
                            }
                        }
                        Tbody {
                            val filteredRows = inventoryRows.filter {
                                stockSearchQuery.isBlank() || it.product.name.contains(stockSearchQuery, ignoreCase = true) || it.product.category.contains(stockSearchQuery, ignoreCase = true)
                            }

                            if (filteredRows.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text(strings.noSearchResult)
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
                                                        Text(strings.lowBadge)
                                                    }
                                                }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(row.product.category) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${row.stockInitial} ${strings.crates}") }
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
                                                Text("${row.resteTheorique} ${strings.crates}")
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
                                                text = strings.btnSell,
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

    QuickSaleModal(
        isOpen = showSaleModal,
        initialDestination = saleModalDestination,
        onClose = { showSaleModal = false }
    )
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
fun QuickSaleModal(
    isOpen: Boolean,
    initialDestination: String = "Dépôt",
    onClose: () -> Unit
) {
    if (!isOpen) return
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableStateOf("1") }
    var destination by remember { mutableStateOf(initialDestination) }
    var customerName by remember { mutableStateOf(if (initialDestination == "Bar") "Bar" else "Comptoir") }
    var paymentMethod by remember { mutableStateOf(if (initialDestination == "Bar") "Transfert Interne" else "Esp\u00e8ces") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    // All beers, alcopops, Guinness, canettes, and non-alcoholic products support half-crates
    val supportsHalfCrate = prod?.supportsHalfCrate() == true
    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
    val pu = prod?.unitPrice ?: 0.0
    val total = qtyDouble * pu

    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()
    ModalDialog(
        title = if (destination == "Bar") "Enregistrer un Transfert vers le Bar" else strings.modalSaleTitle,
        isOpen = isOpen,
        onClose = onClose
    ) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldProduct) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) attr("selected", "") }) {
                            Text("${p.name} \u2014 ${formatFcfa(p.unitPrice)}${if (p.supportsHalfCrate()) " [Demi-casier ok]" else ""}")
                        }
                    }
                }
            }

            FormField(label = if (supportsHalfCrate) "${strings.fieldQty} (demi-casier autoris\u00e9 : 0.5, 1.5, 2.5...)" else strings.fieldQty) {
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    org.jetbrains.compose.web.dom.Input(
                        type = org.jetbrains.compose.web.attributes.InputType.Number,
                        attrs = Modifier.fillMaxWidth()
                            .padding(leftRight = 0.75.cssRem, topBottom = 0.45.cssRem)
                            .borderRadius(8.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                value(quantity)
                                attr("step", if (supportsHalfCrate) "0.5" else "1")
                                attr("min", "0.5")
                                onInput { quantity = it.value?.toString() ?: "1" }
                            }
                    )

                    if (supportsHalfCrate) {
                        Row(Modifier.fillMaxWidth().gap(0.35.cssRem).flexWrap(FlexWrap.Wrap), verticalAlignment = Alignment.CenterVertically) {
                            Span(Modifier.fontSize(0.75.cssRem).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                Text("Demi-casier :")
                            }
                            listOf("0.5", "1", "1.5", "2", "3", "5").forEach { preset ->
                                Button(
                                    onClick = { quantity = preset },
                                    Modifier
                                        .fontSize(0.72.cssRem)
                                        .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                        .borderRadius(12.px)
                                        .backgroundColor(if (quantity == preset) palette.brand.primary else palette.nearBackground)
                                        .color(if (quantity == preset) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                        .border(1.px, LineStyle.Solid, palette.cardBorder)
                                ) {
                                    Text("$preset cas.")
                                }
                            }
                            Button(
                                onClick = {
                                    val cur = quantity.toDoubleOrNull() ?: 0.0
                                    quantity = (cur + 0.5).toString()
                                },
                                Modifier
                                    .fontSize(0.72.cssRem)
                                    .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                    .borderRadius(12.px)
                                    .backgroundColor(palette.nearBackground)
                                    .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                    .border(1.px, LineStyle.Solid, palette.cardBorder)
                            ) {
                                Text("+0.5")
                            }
                        }
                    }
                }
            }

            FormField(label = strings.fieldDestination) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange {
                            val newDest = it.value ?: "D\u00e9p\u00f4t"
                            destination = newDest
                            if (newDest == "Bar") {
                                if (customerName == "Comptoir" || customerName.isBlank()) customerName = "Bar"
                                paymentMethod = "Transfert Interne"
                            } else {
                                if (customerName == "Bar") customerName = "Comptoir"
                                if (paymentMethod == "Transfert Interne") paymentMethod = "Esp\u00e8ces"
                            }
                        }
                    }
                ) {
                    Option("D\u00e9p\u00f4t", attrs = { if (destination == "D\u00e9p\u00f4t") attr("selected", "") }) { Text(strings.destDepot) }
                    Option("Bar", attrs = { if (destination == "Bar") attr("selected", "") }) { Text("Bar (Transfert Interne)") }
                    Option("Client Gros", attrs = { if (destination == "Client Gros") attr("selected", "") }) { Text(strings.destWholesale) }
                }
            }

            if (destination == "Bar") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .backgroundColor(if (ColorMode.current.isDark) Color.rgba(139, 92, 246, 0.2f) else Color.rgb(0xEDE9FE))
                        .padding(0.75.cssRem)
                        .borderRadius(8.px)
                        .border(1.px, LineStyle.Solid, Color.rgb(0x8B5CF6))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.6.cssRem)) {
                        FaWineGlass(Modifier.color(Color.rgb(0x7C3AED)).fontSize(1.cssRem))
                        Column(Modifier.gap(0.2.cssRem)) {
                            Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(Color.rgb(0x5B21B6)).toAttrs()) {
                                Text("Transfert Bar \u2014 Mouvement Interne")
                            }
                            Span(Modifier.fontSize(0.78.cssRem).color(Color.rgb(0x6D28D9)).toAttrs()) {
                                Text("Cette sortie r\u00e9duit le stock magasin mais N'EST PAS incluse dans les ventes totales ni dans les recettes financi\u00e8res.")
                            }
                        }
                    }
                }
            }

            FormField(label = if (destination == "Bar") "Service / R\u00e9ceptionnaire" else strings.fieldCustomer) {
                AppTextInput(
                    value = customerName,
                    onValueChange = { customerName = it },
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldPayment) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { paymentMethod = it.value ?: "Esp\u00e8ces" }
                    }
                ) {
                    if (destination == "Bar") {
                        Option("Transfert Interne", attrs = { attr("selected", "") }) { Text("Transfert Interne (Non Encaiss\u00e9)") }
                    } else {
                        Option("Esp\u00e8ces", attrs = { if (paymentMethod == "Esp\u00e8ces") attr("selected", "") }) { Text(strings.paymentCash) }
                        Option("Mobile Money", attrs = { if (paymentMethod == "Mobile Money") attr("selected", "") }) { Text(strings.paymentMobile) }
                        Option("\u00c0 Cr\u00e9dit", attrs = { if (paymentMethod == "\u00c0 Cr\u00e9dit") attr("selected", "") }) { Text(strings.paymentCredit) }
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(if (destination == "Bar") Color.rgb(0xF3E8FF) else Color.rgb(0xFEF3C7))
                    .padding(0.75.cssRem)
                    .borderRadius(8.px)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontWeight(FontWeight.Bold).color(if (destination == "Bar") Color.rgb(0x6B21A8) else Color.rgb(0x92400E)).toAttrs()) {
                        Text(if (destination == "Bar") "Valeur du Transfert Stock" else strings.totalAmount)
                    }
                    Spacer()
                    Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.2.cssRem).color(if (destination == "Bar") Color.rgb(0x6B21A8) else Color.rgb(0x92400E)).toAttrs()) {
                        Text(formatFcfa(total))
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = if (destination == "Bar") "Valider le Transfert Bar" else strings.btnConfirmSale,
                    backgroundColor = if (destination == "Bar") Color.rgb(0x7C3AED) else palette.success,
                    textColor = Colors.White,
                    onClick = {
                        if (selectedProductId.isNotBlank() && qtyDouble > 0) {
                            StockRepository.recordSale(
                                productId = selectedProductId,
                                quantityCrates = qtyDouble,
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
    if (!isOpen) return
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableStateOf("10") }
    var extrasPerCrate by remember { mutableStateOf("0") }
    var customExtras by remember { mutableStateOf("") }
    var motif by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("Boissons du Cameroun") }
    var notes by remember { mutableStateOf("") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    // All beers, alcopops, Guinness, canettes, and non-alcoholic products support half-crates (0.5)
    val supportsHalfCrate = prod?.supportsHalfCrate() == true
    val qtyDouble = quantity.toDoubleOrNull() ?: 0.0

    val extrasInt = extrasPerCrate.toIntOrNull() ?: 0
    val totalBonusBottles = if (extrasPerCrate == "custom") {
        customExtras.toIntOrNull() ?: 0
    } else {
        (extrasInt * qtyDouble).toInt()
    }
    val bpc = prod?.bottlesPerCrate?.takeIf { it > 0 } ?: 12
    val bonusCratesEquiv = if (bpc > 0) ((totalBonusBottles.toDouble() / bpc) * 100.0).roundToInt() / 100.0 else 0.0

    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

    ModalDialog(title = strings.modalEntryTitle, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldReceivedProduct) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) attr("selected", "") }) {
                            Text("${p.name}${if (p.supportsHalfCrate()) " [Demi-casier ok]" else ""}")
                        }
                    }
                }
            }

            FormField(label = if (supportsHalfCrate) "${strings.fieldReceivedQty} (demi-casier autoris\u00e9 : 0.5, 1.5, 2.5...)" else strings.fieldReceivedQty) {
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    org.jetbrains.compose.web.dom.Input(
                        type = org.jetbrains.compose.web.attributes.InputType.Number,
                        attrs = Modifier.fillMaxWidth()
                            .padding(leftRight = 0.75.cssRem, topBottom = 0.45.cssRem)
                            .borderRadius(8.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                value(quantity)
                                attr("step", if (supportsHalfCrate) "0.5" else "1")
                                attr("min", "0.5")
                                onInput { quantity = it.value?.toString() ?: "0" }
                            }
                    )

                    if (supportsHalfCrate) {
                        Row(Modifier.fillMaxWidth().gap(0.35.cssRem), verticalAlignment = Alignment.CenterVertically) {
                            Span(Modifier.fontSize(0.75.cssRem).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                Text("Demi-casier :")
                            }
                            listOf("0.5", "1", "5", "10", "20", "50").forEach { preset ->
                                Button(
                                    onClick = { quantity = preset },
                                    Modifier
                                        .fontSize(0.72.cssRem)
                                        .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                        .borderRadius(12.px)
                                        .backgroundColor(if (quantity == preset) palette.brand.primary else palette.nearBackground)
                                        .color(if (quantity == preset) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                        .border(1.px, LineStyle.Solid, palette.cardBorder)
                                ) {
                                    Text("$preset cas.")
                                }
                            }
                            Button(
                                onClick = {
                                    val cur = quantity.toDoubleOrNull() ?: 0.0
                                    quantity = (cur + 0.5).toString()
                                },
                                Modifier
                                    .fontSize(0.72.cssRem)
                                    .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                    .borderRadius(12.px)
                                    .backgroundColor(palette.nearBackground)
                                    .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                    .border(1.px, LineStyle.Solid, palette.cardBorder)
                            ) {
                                Text("+0.5")
                            }
                        }
                    }
                }
            }

            // Extras: deliveries sometimes include two or three extra bottles per crate
            FormField(label = "Bouteilles suppl\u00e9mentaires / Extras par casier (Livraison)") {
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    Select(
                        Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                            onChange {
                                extrasPerCrate = it.value ?: "0"
                                val v = it.value?.toIntOrNull() ?: 0
                                if (v > 0) {
                                    motif = "Surplus brasserie (+$v bout./casier offertes)"
                                } else if (v < 0) {
                                    motif = "Casse au d\u00e9chargement ($v bout./casier manquantes)"
                                } else if (extrasPerCrate == "0") {
                                    motif = ""
                                }
                            }
                        }
                    ) {
                        Option("0", attrs = { if (extrasPerCrate == "0") attr("selected", "") }) {
                            Text("Aucun extra (livraison standard conforme)")
                        }
                        Option("2", attrs = { if (extrasPerCrate == "2") attr("selected", "") }) {
                            Text("+2 bouteilles en plus par casier (Tr\u00e8s fr\u00e9quent brasserie)")
                        }
                        Option("3", attrs = { if (extrasPerCrate == "3") attr("selected", "") }) {
                            Text("+3 bouteilles en plus par casier (Tr\u00e8s fr\u00e9quent brasserie)")
                        }
                        Option("1", attrs = { if (extrasPerCrate == "1") attr("selected", "") }) {
                            Text("+1 bouteille en plus par casier")
                        }
                        Option("-1", attrs = { if (extrasPerCrate == "-1") attr("selected", "") }) {
                            Text("-1 bouteille manquante par casier (casse / avarie)")
                        }
                        Option("-2", attrs = { if (extrasPerCrate == "-2") attr("selected", "") }) {
                            Text("-2 bouteilles manquantes par casier (casse)")
                        }
                        Option("-3", attrs = { if (extrasPerCrate == "-3") attr("selected", "") }) {
                            Text("-3 bouteilles manquantes par casier (casse)")
                        }
                        Option("custom", attrs = { if (extrasPerCrate == "custom") attr("selected", "") }) {
                            Text("Autre quantit\u00e9 totale de bouteilles extras / manquantes...")
                        }
                    }

                    if (extrasPerCrate == "custom") {
                        AppTextInput(
                            value = customExtras,
                            onValueChange = { customExtras = it },
                            placeholderText = "Nombre total de bouteilles suppl\u00e9mentaires (ex: 15)",
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (totalBonusBottles != 0) {
                        val isPositive = totalBonusBottles > 0
                        val alertColor = if (isPositive) Color.rgb(0x065F46) else Color.rgb(0x991B1B)
                        val alertBg = if (isPositive) Color.rgb(0xD1FAE5) else Color.rgb(0xFEE2E2)
                        Box(
                            Modifier.fillMaxWidth().backgroundColor(alertBg).borderRadius(8.px)
                                .padding(0.6.cssRem).margin(top = 0.2.cssRem)
                        ) {
                            Column(Modifier.gap(0.2.cssRem)) {
                                val sign = if (isPositive) "+" else ""
                                Span(Modifier.fontSize(0.85.cssRem).color(alertColor).fontWeight(FontWeight.Bold).toAttrs()) {
                                    Text(if (isPositive) "\u2713 Bonus de livraison enregistr\u00e9 :" else "\u26a0\ufe0f Manquant de livraison constat\u00e9 :")
                                }
                                Span(Modifier.fontSize(0.8.cssRem).color(alertColor).toAttrs()) {
                                    if (extrasPerCrate != "custom") {
                                        Text("$sign$extrasInt btl/casier \u00d7 $qtyDouble casiers = $sign$totalBonusBottles bouteilles au total ($sign$bonusCratesEquiv casier \u00e9quiv.)")
                                    } else {
                                        Text("$sign$totalBonusBottles bouteilles au total ($sign$bonusCratesEquiv casier \u00e9quiv.)")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (totalBonusBottles != 0) {
                FormField(label = "Motif de l'\u00e9cart / bonus (obligatoire)") {
                    AppTextInput(
                        value = motif,
                        onValueChange = { motif = it },
                        placeholderText = "Ex: 2 bouteilles offertes par casier par la brasserie",
                        type = "text",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            FormField(label = strings.fieldSupplier) {
                AppTextInput(
                    value = supplier,
                    onValueChange = { supplier = it },
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldNotes) {
                AppTextInput(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholderText = "Ex: Camion #3, BL 14890, promotion brasserie",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSaveEntry,
                    onClick = {
                        if (selectedProductId.isNotBlank() && qtyDouble > 0) {
                            StockRepository.addEntry(
                                productId = selectedProductId,
                                quantityCrates = qtyDouble,
                                extras = extrasInt,
                                totalExtras = totalBonusBottles,
                                motif = motif,
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
    if (!isOpen) return
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantityBottles by remember { mutableStateOf("1") }
    var comment by remember { mutableStateOf("") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    val qb = quantityBottles.toIntOrNull() ?: 1
    val bpc = prod?.bottlesPerCrate?.takeIf { it > 0 } ?: 12
    val loss = ((prod?.unitPrice ?: 0.0) / bpc) * qb

    val strings = LocalLanguage.current.strings()
    ModalDialog(title = strings.modalSpoiledTitle, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldSelectProduct) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) attr("selected", "") }) {
                            Text("${p.name} (${p.bottlesPerCrate} btles/casier)")
                        }
                    }
                }
            }

            FormField(label = strings.fieldBottleQty) {
                AppTextInput(
                    value = quantityBottles,
                    onValueChange = { quantityBottles = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldComment) {
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
                        Text(strings.estimatedLoss)
                    }
                    Spacer()
                    Span(Modifier.fontWeight(FontWeight.Bold).fontSize(1.1.cssRem).color(Color.rgb(0x991B1B)).toAttrs()) {
                        Text(formatFcfa(loss))
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSaveSpoiled,
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
    if (!isOpen) return
    var debtorName by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf(StockRepository.products.firstOrNull()?.id ?: "") }
    var quantityCrates by remember { mutableStateOf("1") }
    var amountPaid by remember { mutableStateOf("0") }
    var phone by remember { mutableStateOf("") }

    val prod = StockRepository.products.find { it.id == selectedProductId }
    val supportsHalfCrate = prod?.supportsHalfCrate() == true
    val cratesDbl = quantityCrates.toDoubleOrNull() ?: 1.0
    val pu = prod?.unitPrice ?: 0.0
    val total = cratesDbl * pu
    val paid = amountPaid.toDoubleOrNull() ?: 0.0
    val remaining = (total - paid).coerceAtLeast(0.0)

    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()
    ModalDialog(title = strings.modalDebtTitle, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldDebtorName) {
                AppTextInput(
                    value = debtorName,
                    onValueChange = { debtorName = it },
                    placeholderText = "Ex: Franky, Bar Le Repère, etc.",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldProductOwed) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedProductId = it.value ?: "" }
                    }
                ) {
                    StockRepository.products.forEach { p ->
                        Option(p.id, attrs = { if (p.id == selectedProductId) attr("selected", "") }) {
                            Text("${p.name} — ${formatFcfa(p.unitPrice)}/${strings.crates}${if (p.supportsHalfCrate()) " [Demi-casier ok]" else ""}")
                        }
                    }
                }
            }

            FormField(label = if (supportsHalfCrate) "${strings.fieldCratesOwed} (demi-casier autorisé : 0.5, 1.5...)" else strings.fieldCratesOwed) {
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    org.jetbrains.compose.web.dom.Input(
                        type = org.jetbrains.compose.web.attributes.InputType.Number,
                        attrs = Modifier.fillMaxWidth()
                            .padding(leftRight = 0.75.cssRem, topBottom = 0.45.cssRem)
                            .borderRadius(8.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                value(quantityCrates)
                                attr("step", if (supportsHalfCrate) "0.5" else "1")
                                attr("min", "0.5")
                                onInput { quantityCrates = it.value?.toString() ?: "1" }
                            }
                    )
                    if (supportsHalfCrate) {
                        Row(Modifier.fillMaxWidth().gap(0.35.cssRem), verticalAlignment = Alignment.CenterVertically) {
                            listOf("0.5", "1", "1.5", "2", "3", "5").forEach { preset ->
                                Button(
                                    onClick = { quantityCrates = preset },
                                    Modifier
                                        .fontSize(0.72.cssRem)
                                        .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                        .borderRadius(12.px)
                                        .backgroundColor(if (quantityCrates == preset) palette.brand.primary else palette.nearBackground)
                                        .color(if (quantityCrates == preset) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                        .border(1.px, LineStyle.Solid, palette.cardBorder)
                                ) {
                                    Text("$preset cas.")
                                }
                            }
                        }
                    }
                }
            }

            FormField(label = strings.fieldAmountPaid) {
                AppTextInput(
                    value = amountPaid,
                    onValueChange = { amountPaid = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldPhone) {
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
                            Text(strings.totalDebt)
                        }
                        Spacer()
                        Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).toAttrs()) {
                            Text(formatFcfa(total))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x991B1B)).fontSize(1.05.cssRem).toAttrs()) {
                            Text(strings.remainingDebt)
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
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSaveDebt,
                    onClick = {
                        if (debtorName.isNotBlank() && selectedProductId.isNotBlank() && cratesDbl > 0.0) {
                            StockRepository.addDebt(
                                debtorName = debtorName,
                                productId = selectedProductId,
                                quantityCrates = cratesDbl,
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
    if (!isOpen) return
    var motive by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Manutention") }
    var amount by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("") }

    val strings = LocalLanguage.current.strings()
    ModalDialog(title = strings.modalExpenseTitle, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldExpenseLabel) {
                AppTextInput(
                    value = motive,
                    onValueChange = { motive = it },
                    placeholderText = "Ex: Déchargement camion brasserie, achat blocs de glace...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldExpenseCategory) {
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

            FormField(label = strings.fieldExpenseAmount) {
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
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSaveExpense,
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
    if (!isOpen) return
    val rows = StockRepository.getInventoryRows()
    val palette = ColorMode.current.toSitePalette()

    val strings = LocalLanguage.current.strings()
    ModalDialog(title = strings.modalAuditTitle, isOpen = isOpen, onClose = onClose, maxWidth = 55.cssRem) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                Text("Vue globale du stock théorique calculé vs stock physique compté.")
            }

            ResponsiveTableContainer(minWidth = 620.px, modifier = Modifier.maxHeight(50.vh)) {
                Table(
                    Modifier.fillMaxWidth().borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse).fontSize(0.85.cssRem).toAttrs()
                ) {
                    Thead {
                        Tr(Modifier.backgroundColor(palette.nearBackground).toAttrs()) {
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProduct) }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colInitialStock + " + " + strings.colEntries) }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colOutflows) }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colRemainingTheoretical) }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colRemainingPhysical) }
                            Th(Modifier.padding(0.5.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colDifference) }
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
                    text = strings.cancel,
                    onClick = onClose
                )
            }
        }
    }
}
