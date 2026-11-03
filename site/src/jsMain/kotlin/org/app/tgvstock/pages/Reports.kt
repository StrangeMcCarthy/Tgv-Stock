package org.app.tgvstock.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
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
import com.varabyte.kobweb.compose.ui.styleModifier
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.FilterBarStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.toSitePalette

@InitRoute
fun initReportsPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Générateur de Rapports PDF"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun ReportsPage() {
    val palette = ColorMode.current.toSitePalette()

    var selectedPeriod by remember { mutableStateOf("Journalier") }
    var selectedDate by remember { mutableStateOf(StockRepository.todayString()) }
    var sellerName by remember { mutableStateOf("Vendeur ZAMBOY") }

    val dateFilter = if (selectedPeriod == "Journalier") selectedDate else null
    val rows = StockRepository.getInventoryRows(dateFilter)
    val recon = StockRepository.getFinancialReconciliation(dateFilter)

    val totalVentes = recon.totalVentes
    val totalVersements = recon.totalVersement
    val totalAvaries = recon.totalAvarisValeur
    val totalDepenses = recon.totalDepenses
    val totalDettes = recon.totalDettes
    val difference = recon.difference

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Controls bar (hidden during print)
        Box(GlassCardStyle.toModifier().fillMaxWidth().classNames("no-print")) {
            Column(Modifier.fillMaxWidth().gap(1.2.cssRem)) {
                Div(PageHeaderStyle.toAttrs()) {
                    Column(Modifier.gap(0.2.cssRem)) {
                        Span(HeadlineTextStyle.toModifier().toAttrs()) {
                            Text("Générateur de Rapports d'Inventaire")
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("Générez des rapports journaliers, hebdomadaires ou annuels conformes à la fiche du dépôt et exportez-les au format PDF.")
                        }
                    }

                    Div(HeaderActionsStyle.toAttrs()) {
                        AppButton(
                            text = "Exporter / Imprimer en PDF",
                            onClick = {
                                window.print()
                            },
                            icon = { FaPrint() },
                            backgroundColor = Color.rgb(0xDC2626),
                            textColor = Colors.White
                        )
                    }
                }

                Div(FilterBarStyle.toAttrs()) {
                    // Period Selector
                    Row(
                        Modifier
                            .gap(0.4.cssRem)
                            .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                            .styleModifier {
                                property("-webkit-overflow-scrolling", "touch")
                                property("white-space", "nowrap")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.textMuted).toAttrs()) {
                            Text("Période :")
                        }
                        listOf("Journalier", "Hebdomadaire", "Mensuel", "Annuel").forEach { p ->
                            val isSelected = selectedPeriod == p
                            Button(
                                onClick = { selectedPeriod = p },
                                Modifier
                                    .fontSize(0.85.cssRem)
                                    .padding(leftRight = 0.8.cssRem, topBottom = 0.35.cssRem)
                                    .borderRadius(20.px)
                                    .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                    .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                    .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                            ) {
                                Text(p)
                            }
                        }
                    }

                    Row(Modifier.gap(0.75.cssRem).flexWrap(FlexWrap.Wrap), verticalAlignment = Alignment.CenterVertically) {
                        if (selectedPeriod == "Journalier") {
                            Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                                Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.textMuted).toAttrs()) {
                                    Text("Date :")
                                }
                                AppTextInput(
                                    value = selectedDate,
                                    onValueChange = { selectedDate = it },
                                    type = "date"
                                )
                            }
                        }

                        Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                            Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.textMuted).toAttrs()) {
                                Text("Nom Vendeur :")
                            }
                            AppTextInput(
                                value = sellerName,
                                onValueChange = { sellerName = it },
                                type = "text",
                                modifier = Modifier.width(9.cssRem)
                            )
                        }
                    }
                }
            }
        }

        // Printable Document Sheet (Exact layout of physical sheets)
        Box(
            Modifier
                .fillMaxWidth()
                .backgroundColor(Colors.White)
                .color(Colors.Black)
                .padding(2.cssRem)
                .borderRadius(8.px)
                .boxShadow(0.px, 4.px, 20.px, 0.px, rgba(0, 0, 0, 0.15))
                .border(1.px, LineStyle.Solid, Color.rgb(0xD1D5DB))
        ) {
            Column(Modifier.fillMaxWidth().gap(1.5.cssRem)) {

                // Official Header
                Row(
                    Modifier.fillMaxWidth().borderBottom(2.px, LineStyle.Solid, Colors.Black).padding(bottom = 0.8.cssRem),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Span(Modifier.fontSize(1.4.cssRem).fontWeight(FontWeight.ExtraBold).letterSpacing(1.px).toAttrs()) {
                            Text("FICHE D'INVENTAIRE DEPOT — TGV STOCK")
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(Color.rgb(0x4B5563)).toAttrs()) {
                            Text("Système de Gestion Commerciale & Comptabilité Matière")
                        }
                    }
                    Spacer()
                    Column(Modifier.textAlign(TextAlign.End)) {
                        Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("VENDEUR : $sellerName")
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(Color.rgb(0x4B5563)).toAttrs()) {
                            Text("PÉRIODE : $selectedPeriod ($selectedDate)")
                        }
                    }
                }

                // Section 1: Financial Reconciliation (Sheet 1 reproduction)
                Div(Modifier.fillMaxWidth().display(DisplayStyle.Flex).flexWrap(FlexWrap.Wrap).gap(1.5.cssRem).toAttrs()) {
                    Box(Modifier.fillMaxWidth().maxWidth(34.cssRem).styleModifier { property("flex", "1 1 300px") }) {
                        ResponsiveTableContainer(minWidth = 320.px) {
                            Table(
                                Modifier
                                    .fillMaxWidth()
                                    .borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse)
                                    .fontSize(0.82.cssRem)
                                    .classNames("print-table")
                                    .toAttrs()
                            ) {
                                Thead {
                                    Tr(Modifier.backgroundColor(Color.rgb(0xF3F4F6)).toAttrs()) {
                                        Th(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Start).toAttrs()) { Text("RUBRIQUE INVENTAIRE") }
                                        Th(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text("MONTANT (FCFA)") }
                                    }
                                }
                                Tbody {
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).fontWeight(FontWeight.Bold).toAttrs()) { Text("TOTAL VENTES") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).fontWeight(FontWeight.Bold).toAttrs()) { Text(formatFcfa(totalVentes)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("TOTAL VERSEMENT") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalVersements)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("AVARIS (PERTE CASSE)") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalAvaries)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("DÉPENSES DU DÉPÔT") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalDepenses)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("DETTES / CRÉANCES") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalDettes)) }
                                    }
                                    Tr(Modifier.backgroundColor(Color.rgb(0xFEF3C7)).toAttrs()) {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).fontWeight(FontWeight.ExtraBold).toAttrs()) { Text("DIFFÉRENCE / ÉCART") }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).fontWeight(FontWeight.ExtraBold).toAttrs()) { Text(formatFcfa(difference)) }
                                    }
                                }
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth().maxWidth(34.cssRem).styleModifier { property("flex", "1 1 280px") }) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .border(1.px, LineStyle.Solid, Colors.Black)
                                .padding(1.cssRem)
                                .gap(0.6.cssRem)
                        ) {
                            Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                Text("NOTE DE CONTRÔLE DE CAISSE")
                            }
                            Span(Modifier.fontSize(0.8.cssRem).toAttrs()) {
                                Text("La différence correspond à : (Total Versements + Dépenses + Dettes) - Total Ventes.")
                            }
                            Span(Modifier.fontSize(0.8.cssRem).toAttrs()) {
                                Text(if (difference >= 0) "Résultat conforme : les versements et charges couvrent l'intégralité des ventes." else "ATTENTION : Déficit constaté à justifier par le vendeur.")
                            }
                            Spacer()
                            Row(Modifier.fillMaxWidth().padding(top = 1.cssRem)) {
                                Column {
                                    Span(Modifier.fontSize(0.8.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text("Visa Vendeur :") }
                                }
                                Spacer()
                                Column {
                                    Span(Modifier.fontSize(0.8.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text("Visa Responsable :") }
                                }
                            }
                        }
                    }
                }

                // Section 2: Detailed Inventory Table (Sheet 2 reproduction)
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    Span(Modifier.fontSize(1.05.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                        Text("DÉTAIL DES SORTIES DE STOCK ET DES VENTES")
                    }

                    ResponsiveTableContainer(minWidth = 850.px) {
                        Table(
                            Modifier
                                .fillMaxWidth()
                                .borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse)
                                .fontSize(0.75.cssRem)
                                .classNames("print-table")
                                .toAttrs()
                        ) {
                            Thead {
                                Tr(Modifier.backgroundColor(Color.rgb(0xF3F4F6)).toAttrs()) {
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Start).toAttrs()) { Text("PRODUITS") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("STOCK INITIAL") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("ENTREES") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("TOTAL STOCK") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("RESTE STOCK") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("TOTAL SORTIES") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("BAR") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text("QT VENTES") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text("P.U (FCFA)") }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text("P. TOTAL") }
                                }
                            }
                            Tbody {
                                fun Double.fmt() = if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
                                rows.forEach { r ->
                                    Tr {
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).fontWeight(FontWeight.SemiBold).toAttrs()) { Text(r.product.name) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(r.stockInitial.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) {
                                            Text(r.entrees.fmt())
                                            if (r.entreesBonusBottles > 0) {
                                                Span(Modifier.fontSize(0.68.cssRem).color(Color.rgb(0x16A34A)).display(DisplayStyle.Block).toAttrs()) {
                                                    Text("(+${r.entreesBonusBottles} btl)")
                                                }
                                            }
                                        }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.totalStock.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(r.resteTheorique.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.totalSorties.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(r.sortiesBar.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.ventesQty.fmt()) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(r.unitPrice)) }
                                        Td(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).fontWeight(FontWeight.Bold).toAttrs()) { Text(formatFcfa(r.montantVentes)) }
                                    }
                                }

                                // Total Row
                                val totalBonusInReport = rows.sumOf { it.entreesBonusBottles }
                                Tr(Modifier.backgroundColor(Color.rgb(0xF3F4F6)).fontWeight(FontWeight.ExtraBold).toAttrs()) {
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("TOTAL GÉNÉRAL") }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.stockInitial }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) {
                                        Text(rows.sumOf { it.entrees }.fmt())
                                        if (totalBonusInReport > 0) {
                                            Span(Modifier.fontSize(0.68.cssRem).color(Color.rgb(0x16A34A)).display(DisplayStyle.Block).toAttrs()) {
                                                Text("(+$totalBonusInReport btl)")
                                            }
                                        }
                                    }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.totalStock }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.resteTheorique }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.totalSorties }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.sortiesBar }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.ventesQty }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("—") }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalVentes)) }
                                }
                            }
                        }
                    }
                }

                // Signatures Footer
                Row(Modifier.fillMaxWidth().padding(top = 2.cssRem)) {
                    Column(Modifier.gap(2.cssRem)) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("Signature du Vendeur :")
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(Color.rgb(0x6B7280)).toAttrs()) {
                            Text("......................................................")
                        }
                    }
                    Spacer()
                    Column(Modifier.gap(2.cssRem).textAlign(TextAlign.End)) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("Signature du Responsable de Dépôt :")
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(Color.rgb(0x6B7280)).toAttrs()) {
                            Text("......................................................")
                        }
                    }
                }
            }
        }
    }
}
