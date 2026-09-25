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
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
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
    var showNewSaleModal by remember { mutableStateOf(false) }

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

    val totalAmount = filteredSales.sumOf { it.totalAmount }
    val totalCrates = filteredSales.sumOf { it.quantityCrates }
    val barSalesAmount = filteredSales.filter { it.destination == "Bar" }.sumOf { it.totalAmount }
    val creditSalesAmount = filteredSales.filter { it.isDebt || it.paymentMethod == "À Crédit" }.sumOf { it.totalAmount }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text("Registre des Ventes")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Enregistrement de toutes les sorties du dépôt (Dépôt direct, Bar, Grossistes, Crédits).")
                }
            }

            Spacer()

            AppButton(
                text = "Enregistrer une Vente",
                onClick = { showNewSaleModal = true },
                icon = { FaCartPlus() },
                backgroundColor = palette.success,
                textColor = Colors.White
            )
        }

        // Summary KPI cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))") }
                .gap(1.2.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = "Total Recettes Ventes",
                value = formatFcfa(totalAmount),
                subtitle = "$totalCrates casiers au total",
                icon = { FaCashRegister() },
                accentColor = palette.success
            )

            StatCard(
                title = "Ventes Comptant / Dépôt",
                value = formatFcfa(totalAmount - creditSalesAmount),
                subtitle = "Encaissé en espèces / mobile",
                icon = { FaCoins() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = "Ventes À Crédit",
                value = formatFcfa(creditSalesAmount),
                subtitle = "Enregistrées dans les Dettes",
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            StatCard(
                title = "Transferts Bar",
                value = formatFcfa(barSalesAmount),
                subtitle = "Consommation / Sortie vers bar",
                icon = { FaWineGlass() },
                accentColor = Color.rgb(0x8B5CF6)
            )
        }

        // Filters Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().gap(1.cssRem),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter date
                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                        Text("Date :")
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
                            Text("Toutes les dates")
                        }
                    }
                }

                // Destination filter
                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                        Text("Destination :")
                    }
                    Select(
                        Modifier.padding(0.4.cssRem).borderRadius(6.px).toAttrs {
                            onChange { filterDestination = it.value ?: "Tous" }
                        }
                    ) {
                        Option("Tous") { Text("Toutes destinations") }
                        Option("Dépôt") { Text("Dépôt") }
                        Option("Bar") { Text("Bar") }
                        Option("Client Gros") { Text("Client Gros") }
                    }
                }

                Spacer()

                // Search
                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = "Chercher produit ou client...",
                    type = "search",
                    modifier = Modifier.width(16.cssRem)
                )
            }
        }

        // Sales Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("DATE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("PRODUIT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("CLIENT / ACHETEUR") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("DESTINATION") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("QUANTITÉ (CASIERS)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("P.U (FCFA)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("MONTANT TOTAL") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("PAIEMENT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTION") }
                            }
                        }
                        Tbody {
                            if (filteredSales.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text("Aucune vente enregistrée pour ces critères.")
                                    }
                                }
                            } else {
                                filteredSales.forEach { sale ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(sale.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(sale.productName) }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) { Text(sale.customerName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Box(
                                                Modifier
                                                    .backgroundColor(if (sale.destination == "Bar") Color.rgb(0xEDE9FE) else palette.nearBackground)
                                                    .color(if (sale.destination == "Bar") Color.rgb(0x6D28D9) else palette.textMuted)
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(sale.destination)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Text("${sale.quantityCrates} casiers")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) {
                                            Text(formatFcfa(sale.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.success).toAttrs()) {
                                            Text(formatFcfa(sale.totalAmount))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            val isCredit = sale.isDebt || sale.paymentMethod == "À Crédit"
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
                                                Text(sale.paymentMethod)
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

    QuickSaleModal(isOpen = showNewSaleModal, onClose = { showNewSaleModal = false })
}
