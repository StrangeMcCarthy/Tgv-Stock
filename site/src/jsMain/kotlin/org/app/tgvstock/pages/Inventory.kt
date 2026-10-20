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
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette
import kotlin.math.abs

@InitRoute
fun initInventoryPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Inventory Sheet"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun InventoryPage() {
    val router = rememberPageContext().router
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

    var selectedPeriod by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableStateOf(StockRepository.todayString()) }
    var sellerName by remember { mutableStateOf("Vendeur ZAMBOY") }
    var versementInput by remember { mutableStateOf("") }

    val dateFilter = if (selectedPeriod == "Daily") selectedDate else null
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
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.inventoryPageTitle)
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text(strings.inventoryPageSubtitle)
                }
            }

            Spacer()

            AppButton(
                text = strings.btnExportPdf,
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
                    listOf(
                        strings.periodDaily to "Daily",
                        strings.periodWeekly to "Weekly",
                        strings.periodMonthly to "Monthly",
                        strings.periodYearly to "Yearly"
                    ).forEach { (label, key) ->
                        val isSelected = selectedPeriod == key
                        Button(
                            onClick = { selectedPeriod = key },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.4.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(label)
                        }
                    }
                }

                if (selectedPeriod == "Daily") {
                    Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                            Text(strings.labelDate)
                        }
                        org.jetbrains.compose.web.dom.Input(
                            type = InputType.Date,
                            attrs = Modifier
                                .padding(0.4.cssRem)
                                .borderRadius(6.px)
                                .border(1.px, LineStyle.Solid, palette.cardBorder)
                                .backgroundColor(palette.inputBackground)
                                .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .toAttrs {
                                    value(selectedDate)
                                    onInput { selectedDate = it.value }
                                }
                        )
                    }
                }

                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                        Text(strings.labelSeller)
                    }
                    org.jetbrains.compose.web.dom.Input(
                        type = InputType.Text,
                        attrs = Modifier
                            .width(12.cssRem)
                            .padding(0.4.cssRem)
                            .borderRadius(6.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                value(sellerName)
                                onInput { sellerName = it.value }
                            }
                    )
                }

                Spacer()

                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                        Text(strings.labelCashDeposit)
                    }
                    org.jetbrains.compose.web.dom.Input(
                        type = InputType.Number,
                        attrs = Modifier
                            .width(10.cssRem)
                            .padding(0.4.cssRem)
                            .borderRadius(6.px)
                            .border(1.px, LineStyle.Solid, palette.brand.primary)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                placeholder("${finalVersement.toLong()}")
                                value(versementInput)
                                onInput { versementInput = it.value?.toString() ?: "" }
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
                            Text(strings.reconTitle)
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.reconSubtitle)
                        }
                    }
                }

                Div(
                    Modifier
                        .fillMaxWidth()
                        .display(DisplayStyle.Grid)
                        .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))") }
                        .gap(1.cssRem)
                        .toAttrs()
                ) {
                    ReconBox(title = strings.reconTotalSales, amount = recon.totalVentes, color = palette.brand.primary)
                    ReconBox(title = strings.reconTotalDeposit, amount = finalVersement, color = palette.success)
                    ReconBox(title = strings.reconSpoiledLoss, amount = recon.totalAvarisValeur, color = palette.danger)
                    ReconBox(title = strings.reconExpenses, amount = recon.totalDepenses, color = Color.rgb(0x9333EA))
                    ReconBox(title = strings.reconClientDebts, amount = recon.totalDettes, color = Color.rgb(0xEA580C))
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
                    isBalanced -> strings.balancedMsg
                    finalDiff > 0 -> strings.surplusMsg.replace("{amount}", formatFcfa(finalDiff))
                    else -> strings.deficitMsg.replace("{amount}", formatFcfa(finalDiff))
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
                            Text(strings.diffLabel)
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
                            Text(strings.stockDetailTitle)
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.stockDetailSubtitle)
                        }
                    }
                    Spacer()
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.SemiBold).color(palette.brand.primary).toAttrs()) {
                        Text("${rows.size} ${strings.inventoryLines}")
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
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProducts) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colInitStock) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colEntries) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colTotalStock) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colTotalOutflows) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).backgroundColor(Color.rgba(139, 92, 246, 0.12f)).toAttrs()) { Text(strings.colBarOutflows) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSalesQty) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colSpoiledBottles) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colRemainingTheoretical) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colRemainingPhysical) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colDifference) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colUnitPrice) }
                                Th(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colTotalSalesPrice) }
                            }
                        }
                        Tbody {
                            rows.forEach { r ->
                                fun Double.fmt() = if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
                                Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                    Td(Modifier.padding(0.6.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.product.name) }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(r.stockInitial.fmt()) }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                        Text("+${r.entrees.fmt()}")
                                        if (r.entreesBonusBottles > 0) {
                                            Span(Modifier.fontSize(0.7.cssRem).color(palette.success).fontWeight(FontWeight.Bold).display(DisplayStyle.Block).toAttrs()) {
                                                Text("(+${r.entreesBonusBottles} btl bonus)")
                                            }
                                        }
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.totalStock.fmt()) }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).color(palette.danger).fontWeight(FontWeight.Bold).toAttrs()) { Text(r.totalSorties.fmt()) }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).backgroundColor(Color.rgba(139, 92, 246, 0.08f)).fontWeight(FontWeight.SemiBold).color(Color.rgb(0x7C3AED)).toAttrs()) {
                                        Text(r.sortiesBar.fmt())
                                    }
                                    Td(Modifier.padding(0.6.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).color(palette.success).toAttrs()) { Text(r.ventesQty.fmt()) }
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
                                        org.jetbrains.compose.web.dom.Input(
                                            type = InputType.Number,
                                            attrs = Modifier
                                                .width(4.cssRem)
                                                .textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
                                                .padding(0.2.cssRem)
                                                .borderRadius(4.px)
                                                .border(1.px, LineStyle.Solid, palette.cardBorder)
                                                .toAttrs {
                                                    value(r.restePhysique.toString())
                                                    onInput { ev ->
                                                        val v = ev.value?.toDouble() ?: r.resteTheorique
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
