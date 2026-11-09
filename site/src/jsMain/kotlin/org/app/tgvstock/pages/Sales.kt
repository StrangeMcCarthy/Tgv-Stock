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
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.FilterBarStyle
import org.app.tgvstock.FilterControlsGroupStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.SubheadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette

@InitRoute
fun initSalesPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Enregistrement des Ventes"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun SalesPage() {
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()
    var showNewSaleModal by remember { mutableStateOf(false) }
    var saleModalDestination by remember { mutableStateOf("Dépôt") }

    var filterDate by remember { mutableStateOf("") }
    var filterDestination by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }

    val sales = StockRepository.sales
    val filteredSales = sales.filter { s ->
        val matchesDate = filterDate.isBlank() || s.date == filterDate
        val matchesDest = (filterDestination == "Tous") || (s.destination == filterDestination)
        val matchesSearch = searchQuery.isBlank() || s.productName.contains(searchQuery, ignoreCase = true) || s.customerName.contains(searchQuery, ignoreCase = true)
        matchesDate && matchesDest && matchesSearch
    }

    // Bar transfers are internal movements — not counted in revenue
    val totalAmount = filteredSales.filter { it.destination != "Bar" }.sumOf { it.totalAmount }
    val totalCrates = filteredSales.filter { it.destination != "Bar" }.sumOf { it.quantityCrates }
    val barTransferAmount = filteredSales.filter { it.destination == "Bar" }.sumOf { it.totalAmount }
    val barTransferCrates = filteredSales.filter { it.destination == "Bar" }.sumOf { it.quantityCrates }
    val creditSalesAmount = filteredSales.filter { it.isDebt || it.paymentMethod == "À Crédit" }.sumOf { it.totalAmount }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.salesPageTitle)
                }
                Span(SubheadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.salesPageSubtitle)
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnBarTransfer,
                    onClick = {
                        saleModalDestination = "Bar"
                        showNewSaleModal = true
                    },
                    icon = { FaWineGlass() },
                    backgroundColor = Color.rgb(0x7C3AED),
                    textColor = Colors.White
                )
                AppButton(
                    text = strings.btnRecordSale,
                    onClick = {
                        saleModalDestination = "Dépôt"
                        showNewSaleModal = true
                    },
                    icon = { FaCartPlus() },
                    backgroundColor = palette.success,
                    textColor = Colors.White
                )
            }
        }

        // Summary KPI cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(150px, 1fr))") }
                .gap(0.85.cssRem)
                .toAttrs()
        ) {
            val totalCratesStr = if (totalCrates == totalCrates.toLong().toDouble()) totalCrates.toLong().toString() else totalCrates.toString()
            StatCard(
                title = strings.statTotalSalesRevenue,
                value = formatFcfa(totalAmount),
                subtitle = "$totalCratesStr ${strings.statTotalSalesRevenueSub}",
                icon = { FaCashRegister() },
                accentColor = palette.success
            )

            StatCard(
                title = strings.statCashSales,
                value = formatFcfa(totalAmount - creditSalesAmount),
                subtitle = strings.statCashSalesSub,
                icon = { FaCoins() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = strings.statCreditSales,
                value = formatFcfa(creditSalesAmount),
                subtitle = strings.statCreditSalesSub,
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            val barCratesStr = if (barTransferCrates == barTransferCrates.toLong().toDouble()) barTransferCrates.toLong().toString() else barTransferCrates.toString()
            StatCard(
                title = strings.statBarTransfers,
                value = formatFcfa(barTransferAmount),
                subtitle = "$barCratesStr ${strings.statBarTransfersSub}",
                icon = { FaWineGlass() },
                accentColor = Color.rgb(0x8B5CF6)
            )
        }

        // Filters Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Div(FilterBarStyle.toAttrs()) {
                Div(FilterControlsGroupStyle.toAttrs()) {
                    // Filter date
                    Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                            Text(strings.filterByDate)
                        }
                        AppTextInput(
                            value = filterDate,
                            onValueChange = { filterDate = it },
                            type = "date"
                        )
                        if (filterDate.isNotBlank()) {
                            Button(
                                onClick = { filterDate = "" },
                                Modifier.fontSize(0.75.cssRem).padding(leftRight = 0.5.cssRem, topBottom = 0.3.cssRem)
                            ) {
                                Text(strings.allDates)
                            }
                        }
                    }

                    // Destination filter
                    Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                            Text(strings.filterDestination)
                        }
                        Select(
                            Modifier.padding(0.4.cssRem).borderRadius(6.px).toAttrs {
                                onChange { filterDestination = it.value ?: "Tous" }
                            }
                        ) {
                            Option("Tous") { Text(strings.destAll) }
                            Option("Dépôt") { Text(strings.destDirectOption) }
                            Option("Bar") { Text(strings.destBarOption) }
                            Option("Client Gros") { Text(strings.destWholesaleOption) }
                        }
                    }
                }

                // Search
                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = strings.placeholderSearchSales,
                    type = "search",
                    modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                )
            }
        }

        // Sales Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                ResponsiveTableContainer(minWidth = 850.px) {
                    Table(
                        Modifier
                            .fillMaxWidth()
                            .borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse)
                            .fontSize(0.9.cssRem)
                            .toAttrs()
                    ) {
                        Thead {
                            Tr(
                                Modifier
                                    .backgroundColor(palette.nearBackground)
                                    .borderBottom(2.px, LineStyle.Solid, palette.cardBorder)
                                    .toAttrs()
                            ) {
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colSaleDate) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colSaleProduct) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colSaleCustomer) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSaleDestination) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSaleQty) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colSaleUnitPrice) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colSaleTotal) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSalePayment) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSaleAction) }
                            }
                        }
                        Tbody {
                            if (filteredSales.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text(strings.noSalesFound)
                                    }
                                }
                            } else {
                                filteredSales.forEach { sale ->
                                    Tr(
                                        Modifier
                                            .backgroundColor(if (sale.destination == "Bar") Color.rgba(139, 92, 246, 0.06f) else Colors.Transparent)
                                            .borderBottom(1.px, LineStyle.Solid, palette.cardBorder)
                                            .toAttrs()
                                    ) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(sale.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(sale.productName) }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) { Text(sale.customerName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            val destLabel = when (sale.destination) {
                                                "Bar" -> strings.destBarOption
                                                "Dépôt" -> strings.destDirectOption
                                                "Client Gros" -> strings.destWholesaleOption
                                                else -> sale.destination
                                            }
                                            Box(
                                                Modifier
                                                    .backgroundColor(if (sale.destination == "Bar") Color.rgb(0xEDE9FE) else palette.nearBackground)
                                                    .color(if (sale.destination == "Bar") Color.rgb(0x6D28D9) else palette.textMuted)
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(destLabel)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            val qtyStr = if (sale.quantityCrates == sale.quantityCrates.toLong().toDouble())
                                                sale.quantityCrates.toLong().toString()
                                            else sale.quantityCrates.toString()
                                            Text("$qtyStr ${strings.crates}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) {
                                            Text(formatFcfa(sale.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) {
                                            if (sale.destination == "Bar") {
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Span(Modifier.color(Color.rgb(0x7C3AED)).fontSize(0.85.cssRem).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                                        Text(formatFcfa(sale.totalAmount))
                                                    }
                                                    Span(Modifier.fontSize(0.7.cssRem).color(palette.textMuted).toAttrs()) {
                                                        Text("(${strings.badgeBarTransfer})")
                                                    }
                                                }
                                            } else {
                                                Span(Modifier.fontWeight(FontWeight.Bold).color(palette.success).toAttrs()) {
                                                    Text(formatFcfa(sale.totalAmount))
                                                }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            val isCredit = sale.isDebt || sale.paymentMethod == "À Crédit"
                                            val paymentText = when (sale.paymentMethod) {
                                                "À Crédit" -> strings.paymentCredit
                                                "Espèces" -> strings.paymentCash
                                                "Mobile Money" -> strings.paymentMobile
                                                "Transfert Interne" -> strings.paymentInternal
                                                else -> sale.paymentMethod
                                            }
                                            Box(
                                                Modifier
                                                    .backgroundColor(if (isCredit) Color.rgb(0xFEE2E2) else Color.rgb(0xDCFCE7))
                                                    .color(if (isCredit) Color.rgb(0x991B1B) else Color.rgb(0x166534))
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .fontWeight(FontWeight.Bold)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(paymentText)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            AppIconButton(
                                                onClick = { StockRepository.deleteSale(sale.id) },
                                                icon = { FaTrash(Modifier.color(palette.danger)) }
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
        isOpen = showNewSaleModal,
        initialDestination = saleModalDestination,
        onClose = { showNewSaleModal = false }
    )
}
