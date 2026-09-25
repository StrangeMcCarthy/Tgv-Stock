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
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.Input
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.toSitePalette
import kotlin.math.abs

@InitRoute
fun initInventoryPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Fiche d'Inventaire"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun InventoryPage() {
    val router = rememberPageContext().router
    val palette = ColorMode.current.toSitePalette()

    var selectedPeriod by remember { mutableStateOf("Journalier") }
    var selectedDate by remember { mutableStateOf(StockRepository.todayString()) }
    var sellerName by remember { mutableStateOf("Vendeur ZAMBOY") }
    var versementInput by remember { mutableStateOf("") }

    val dateFilter = if (selectedPeriod == "Journalier") selectedDate else null
    val rows = StockRepository.getInventoryRows(dateFilter)
    val recon = StockRepository.getFinancialReconciliation(dateFilter)

    val customVersement = versementInput.toDoubleOrNull()
    val finalVersement = customVersement ?: recon.totalVersement
    val finalDiff = (finalVersement + recon.totalDepenses + recon.totalDettes) - recon.totalVentes

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toAttrs()) {
                    Text("Fiche d'Inventaire & Bilan du Dépôt")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Calcul automatisé des stocks, sorties, ventes et réconciliation financière selon la fiche physique du dépôt.")
                }
            }

            Spacer()

            AppButton(
                text = "Exporter en PDF",
                onClick = { router.tryRoutingTo("/reports") },
                icon = { FaFilePdf() },
                backgroundColor = Color.rgb(0xDC2626),
                textColor = Colors.White
            )
        }

        // Period & Date Filter Card
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().gap(1.2.cssRem),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Period tabs
                Row(Modifier.gap(0.4.cssRem)) {
                    listOf("Journalier", "Hebdomadaire", "Mensuel", "Annuel").forEach { p ->
                        val isSelected = selectedPeriod == p
                        Button(
                            onClick = { selectedPeriod = p },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.4.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(p)
                        }
                    }
                }

                if (selectedPeriod == "Journalier") {
                    Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                            Text("Date :")
                        }
                        Input(
                            type = InputType.Date,
                            value = selectedDate,
                            attrs = Modifier
                                .padding(0.4.cssRem)
                                .borderRadius(6.px)
                                .border(1.px, LineStyle.Solid, palette.cardBorder)
                                .backgroundColor(palette.inputBackground)
                                .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .toAttrs {
                                    onInput { selectedDate = it.value }
                                }
                        )
                    }
                }

                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                        Text("Vendeur :")
                    }
                    Input(
                        type = InputType.Text,
                        value = sellerName,
                        attrs = Modifier
                            .width(12.cssRem)
                            .padding(0.4.cssRem)
                            .borderRadius(6.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                onInput { sellerName = it.value }
                            }
                    )
                }

                Spacer()

                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                        Text("Versement Caisse (FCFA) :")
                    }
                    Input(
                        type = InputType.Number,
                        value = versementInput,
                        attrs = Modifier
                            .width(10.cssRem)
                            .padding(0.4.cssRem)
                            .borderRadius(6.px)
                            .border(1.px, LineStyle.Solid, palette.brand.primary)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                placeholder("${finalVersement.toLong()}")
                                onInput { versementInput = it.value }
                            }
                    )
                }
            }
        }

        // Financial Summary Section (Matching Sheet 1: INVENTAIRE | MONTANT)
        Box(
            Modifier
                .fillMaxWidth()
                .backgroundColor(palette.cardBackground)
                .border(2.px, LineStyle.Solid, palette.cardBorder)
                .borderRadius(14.px)
                .padding(1.5.cssRem)
        ) {
            Column(Modifier.fillMaxWidth().gap(1.2.cssRem)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(2.2.cssRem)
                            .borderRadius(8.px)
                            .backgroundColor(palette.brand.primary.toRgb().copyf(alpha = 0.2f))
                            .color(palette.brand.primary)
                            .fontSize(1.1.cssRem)
                            .margin(right = 0.6.cssRem),
                        contentAlignment = Alignment.Center
                    ) {
                        FaScaleBalanced()
                    }
                    Column {
                        Span(Modifier.fontSize(1.15.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("RÉCONCILIATION FINANCIÈRE DE CAISSE (FICHE D'INVENTAIRE)")
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("Vérification de l'équilibre : Total Ventes vs (Versements + Dépenses + Dettes Franky)")
                        }
                    }
                }

                Div(
                    Modifier
                        .fillMaxWidth()
                        .display(DisplayStyle.Grid)
                        .gridTemplateColumns("repeat(auto-fit, minmax(200px, 1fr))")
                        .gap(1.cssRem)
                        .toAttrs()
                ) {
                    ReconBox(title = "TOTAL VENTES", amount = recon.totalVentes, color = palette.brand.primary)
                    ReconBox(title = "TOTAL VERSEMENT", amount = finalVersement, color = palette.success)
                    ReconBox(title = "PERTES AVARIS", amount = recon.totalAvarisValeur, color = palette.danger)
                    ReconBox(title = "DÉPENSES DU DÉPÔT", amount = recon.totalDepenses, color = Color.rgb(0x9333EA))
                    ReconBox(title = "DETTES CLIENTS", amount = recon.totalDettes, color = Color.rgb(0xEA580C))
                }

                // Balance difference alert
                val isBalanced = abs(finalDiff) < 1.0
                val diffColor = when {
                    isBalanced -> palette.success
                    finalDiff > 0 -> Color.rgb(0x2563EB) // Surplus
                    else -> palette.danger // Manquant
                }
                val diffBg = when {
                    isBalanced -> Color.rgb(0xDCFCE7)
                    finalDiff > 0 -> Color.rgb(0xDBEAFE)
                    else -> Color.rgb(0xFEE2E2)
                }
                val diffText = when {
                    isBalanced -> "Caisse Parfaitement Équilibrée (Écart: 0 FCFA)"
                    finalDiff > 0 -> "Excédent de Caisse : +${formatFcfa(finalDiff)} (Versements + Charges > Ventes)"
                    else -> "Déficit / Manquant de Caisse : ${formatFcfa(finalDiff)} (Manque à justifier)"
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .backgroundColor(diffBg)
                        .padding(1.cssRem)
                        .borderRadius(10.px)
                        .border(1.px, LineStyle.Solid, diffColor)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.1.cssRem).color(diffColor).toAttrs()) {
                            Text("DIFFÉRENCE / ÉCART : ")
                        }
                        Span(Modifier.fontWeight(FontWeight.Bold).fontSize(1.05.cssRem).color(diffColor).margin(left = 0.5.cssRem).toAttrs()) {
                            Text(diffText)
                        }
                        Spacer()
                        Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.3.cssRem).color(diffColor).toAttrs()) {
                            Text(formatFcfa(finalDiff))
                        }
                    }
                }
            }
        }

        // Detailed Inventory Table (Matching Sheet 2: FICHE D'INVENTAIRE DEPOT ZAMBOY)
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Span(Modifier.fontSize(1.1.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("Détail des Stocks & Sorties de Boissons")
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("Produits, stocks initiaux, entrées brasserie, consommation bar, ventes et reste théorique/physique.")
                        }
                    }
                    Spacer()
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.SemiBold).color(palette.brand.primary).toAttrs()) {
                        Text("${rows.size} lignes d'inventaire")
                    }
                }

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
                            .fontSize(0.85.cssRem)
                            .toAttrs()
                    ) {
                        Thead {
                            Tr(
                                Modifier
                                    .backgroundColor(palette.nearBackground)
                                    .borderBottom(2.px, LineStyle.Solid, palette.cardBorder)
                                    .toAttrs()
                            ) {
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("PRODUITS") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("STOCK INITIAL") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ENTRÉES") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("TOTAL STOCK") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("BAR") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("VENTES QT") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("TOTAL SORTIES") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("AVARIS (BTLS)") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("RESTE EN STOCK") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("RESTE PHYSIQUE") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ÉCART") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("P.U (FCFA)") }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("P. TOTAL VENTES") }
                            }
                        }
                        Tbody {
                            rows.forEach { r ->
                                Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                    Td(Modifier.padding(0.6.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.product.name) }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${r.stockInitial}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) { Text("+${r.entrees}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text("${r.totalStock}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("${r.sortiesBar}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.SemiBold).toAttrs()) { Text("${r.ventesQty}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.danger).fontWeight(FontWeight.Bold).toAttrs()) { Text("${r.totalSorties}") }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                        if (r.avariesBottles > 0) {
                                            Span(Modifier.color(palette.danger).fontWeight(FontWeight.Bold).toAttrs()) {
                                                Text("${r.avariesBottles}")
                                            }
                                        } else {
                                            Text("0")
                                        }
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                        Text("${r.resteTheorique}")
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                        Input(
                                            type = InputType.Number,
                                            value = r.restePhysique.toString(),
                                            attrs = Modifier
                                                .width(4.cssRem)
                                                .textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
                                                .padding(0.2.cssRem)
                                                .borderRadius(4.px)
                                                .border(1.px, LineStyle.Solid, palette.cardBorder)
                                                .toAttrs {
                                                    onInput { ev ->
                                                        val v = ev.value.toDoubleOrNull() ?: r.resteTheorique
                                                        StockRepository.updatePhysicalCount(r.product.id, v)
                                                    }
                                                }
                                        )
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                        val ecartColor = when {
                                            r.ecart < 0 -> palette.danger
                                            r.ecart > 0 -> palette.brand.primary
                                            else -> palette.success
                                        }
                                        Span(Modifier.color(ecartColor).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Text(if (r.ecart > 0) "+${r.ecart}" else "${r.ecart}")
                                        }
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) {
                                        Text(formatFcfa(r.unitPrice))
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.success).toAttrs()) {
                                        Text(formatFcfa(r.montantVentes))
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

@Composable
fun ReconBox(title: String, amount: Double, color: Color) {
    val palette = ColorMode.current.toSitePalette()
    Box(
        Modifier
            .backgroundColor(palette.nearBackground)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .borderRadius(10.px)
            .padding(1.cssRem)
    ) {
        Column(Modifier.gap(0.3.cssRem)) {
            Span(Modifier.fontSize(0.8.cssRem).fontWeight(FontWeight.SemiBold).color(palette.textMuted).toAttrs()) {
                Text(title)
            }
            Span(Modifier.fontSize(1.2.cssRem).fontWeight(FontWeight.ExtraBold).color(color).toAttrs()) {
                Text(formatFcfa(amount))
            }
        }
    }
}
