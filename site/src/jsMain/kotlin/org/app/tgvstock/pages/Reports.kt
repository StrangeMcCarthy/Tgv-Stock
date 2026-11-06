package org.app.tgvstock.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
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
import com.varabyte.kobweb.silk.components.graphics.Image
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
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.strings
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
    val strings = LocalLanguage.current.strings()

    var selectedPeriodKey by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableStateOf(StockRepository.todayString()) }
    var sellerName by remember { mutableStateOf("Vendeur ZAMBOY") }

    val dateFilter = if (selectedPeriodKey == "Daily") selectedDate else null
    val rows = StockRepository.getInventoryRows(dateFilter)
    val recon = StockRepository.getFinancialReconciliation(dateFilter)

    val totalVentes = recon.totalVentes
    val totalVersements = recon.totalVersement
    val totalAvaries = recon.totalAvarisValeur
    val totalDepenses = recon.totalDepenses
    val totalDettes = recon.totalDettes
    val difference = recon.difference

    val periodLabel = when (selectedPeriodKey) {
        "Daily" -> strings.periodDaily
        "Weekly" -> strings.periodWeekly
        "Monthly" -> strings.periodMonthly
        "Yearly" -> strings.periodYearly
        else -> selectedPeriodKey
    }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Controls bar (hidden during print)
        Box(GlassCardStyle.toModifier().fillMaxWidth().classNames("no-print")) {
            Column(Modifier.fillMaxWidth().gap(1.2.cssRem)) {
                Div(PageHeaderStyle.toAttrs()) {
                    Column(Modifier.gap(0.2.cssRem)) {
                        Span(HeadlineTextStyle.toModifier().toAttrs()) {
                            Text(strings.reportsPageTitle)
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.reportsPageSubtitle)
                        }
                    }

                    Div(HeaderActionsStyle.toAttrs()) {
                        AppButton(
                            text = strings.btnPrintPdf,
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
                            Text(strings.reportPeriodLabel)
                        }
                        listOf(
                            "Daily" to strings.periodDaily,
                            "Weekly" to strings.periodWeekly,
                            "Monthly" to strings.periodMonthly,
                            "Yearly" to strings.periodYearly
                        ).forEach { (pKey, pLabel) ->
                            val isSelected = selectedPeriodKey == pKey
                            Button(
                                onClick = { selectedPeriodKey = pKey },
                                Modifier
                                    .fontSize(0.85.cssRem)
                                    .padding(leftRight = 0.8.cssRem, topBottom = 0.35.cssRem)
                                    .borderRadius(20.px)
                                    .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                    .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                    .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                            ) {
                                Text(pLabel)
                            }
                        }
                    }

                    Row(Modifier.gap(0.75.cssRem).flexWrap(FlexWrap.Wrap), verticalAlignment = Alignment.CenterVertically) {
                        if (selectedPeriodKey == "Daily") {
                            Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                                Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(palette.textMuted).toAttrs()) {
                                    Text(strings.reportDateLabel)
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
                                Text(strings.reportSellerLabel)
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
                    Image(
                        src = "/favicon.ico",
                        description = "TGV Stock Logo",
                        modifier = Modifier
                            .size(2.6.cssRem)
                            .margin(right = 0.75.cssRem)
                            .objectFit(ObjectFit.Contain)
                    )
                    Column {
                        Span(Modifier.fontSize(1.4.cssRem).fontWeight(FontWeight.ExtraBold).letterSpacing(1.px).toAttrs()) {
                            Text(strings.docSheetTitle)
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(Color.rgb(0x4B5563)).toAttrs()) {
                            Text(strings.docSheetSubtitle)
                        }
                    }
                    Spacer()
                    Column(Modifier.textAlign(TextAlign.End)) {
                        Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("${strings.docSellerLabel} $sellerName")
                        }
                        Span(Modifier.fontSize(0.85.cssRem).color(Color.rgb(0x4B5563)).toAttrs()) {
                            Text("${strings.docPeriodLabel} $periodLabel ($selectedDate)")
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
                                        Th(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Start).toAttrs()) { Text(strings.docRubricTitle) }
                                        Th(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(strings.docAmountTitle) }
                                    }
                                }
                                Tbody {
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).fontWeight(FontWeight.Bold).toAttrs()) { Text(strings.docTotalSales) }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).fontWeight(FontWeight.Bold).toAttrs()) { Text(formatFcfa(totalVentes)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text(strings.docTotalDeposit) }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalVersements)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text(strings.docSpoiledLoss) }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalAvaries)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text(strings.docExpenses) }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalDepenses)) }
                                    }
                                    Tr {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text(strings.docClientDebts) }
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(formatFcfa(totalDettes)) }
                                    }
                                    Tr(Modifier.backgroundColor(Color.rgb(0xFEF3C7)).toAttrs()) {
                                        Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).fontWeight(FontWeight.ExtraBold).toAttrs()) { Text(strings.docDiscrepancy) }
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
                                Text(strings.docControlNoteTitle)
                            }
                            Span(Modifier.fontSize(0.8.cssRem).toAttrs()) {
                                Text("${strings.docDiscrepancy} : (${strings.docTotalDeposit} + ${strings.docExpenses} + ${strings.docClientDebts}) - ${strings.docTotalSales}.")
                            }
                            Span(Modifier.fontSize(0.8.cssRem).toAttrs()) {
                                val noteText = when {
                                    kotlin.math.abs(difference) < 1.0 -> strings.docControlNoteBalanced
                                    difference > 0 -> strings.docControlNoteSurplus
                                    else -> strings.docControlNoteDeficit
                                }
                                Text(noteText)
                            }
                            Spacer()
                            Row(Modifier.fillMaxWidth().padding(top = 1.cssRem)) {
                                Column {
                                    Span(Modifier.fontSize(0.8.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text("${strings.docSellerSignature} :") }
                                }
                                Spacer()
                                Column {
                                    Span(Modifier.fontSize(0.8.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text("${strings.docManagerSignature} :") }
                                }
                            }
                        }
                    }
                }

                // Section 2: Detailed Inventory Table (Sheet 2 reproduction)
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    Span(Modifier.fontSize(1.05.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                        Text(strings.stockDetailTitle)
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
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colProducts) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colInitStock) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colEntries) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colTotalStock) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colRemainingTheoretical) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colTotalOutflows) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colBarOutflows) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colSalesQty) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(strings.colUnitPrice) }
                                    Th(Modifier.padding(0.3.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.End).toAttrs()) { Text(strings.colTotalSalesPrice) }
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
                                                    Text("(+${r.entreesBonusBottles} ${strings.bottlesAbbr})")
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
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).toAttrs()) { Text("${strings.actions.uppercase()} / TOTAL") }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) { Text(rows.sumOf { it.stockInitial }.fmt()) }
                                    Td(Modifier.padding(0.4.cssRem).border(1.px, LineStyle.Solid, Colors.Black).textAlign(TextAlign.Center).toAttrs()) {
                                        Text(rows.sumOf { it.entrees }.fmt())
                                        if (totalBonusInReport > 0) {
                                            Span(Modifier.fontSize(0.68.cssRem).color(Color.rgb(0x16A34A)).display(DisplayStyle.Block).toAttrs()) {
                                                Text("(+$totalBonusInReport ${strings.bottlesAbbr})")
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
                            Text("${strings.docSellerSignature} :")
                        }
                        Span(Modifier.fontSize(0.8.cssRem).color(Color.rgb(0x6B7280)).toAttrs()) {
                            Text("......................................................")
                        }
                    }
                    Spacer()
                    Column(Modifier.gap(2.cssRem).textAlign(TextAlign.End)) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text("${strings.docManagerSignature} :")
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
