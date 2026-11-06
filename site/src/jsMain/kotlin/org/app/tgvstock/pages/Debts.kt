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
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.models.Debt
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette

@InitRoute
fun initDebtsPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Gestion des Dettes"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun DebtsPage() {
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

    var showNewDebtModal by remember { mutableStateOf(false) }
    var debtToPay by remember { mutableStateOf<Debt?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("Tous") }

    val debtsList = StockRepository.debts
    val filteredDebts = debtsList.filter { d ->
        val matchesStatus = (filterStatus == "Tous") || (d.status == filterStatus)
        val matchesSearch = searchQuery.isBlank() || d.debtorName.contains(searchQuery, ignoreCase = true) || d.productName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    val totalOutstanding = debtsList.filter { it.status != "Réglé" }.sumOf { it.remainingAmount }
    val totalSettled = debtsList.sumOf { it.amountPaid }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.debtsPageTitle)
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text(strings.debtsPageSubtitle)
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnNewDebt,
                    onClick = { showNewDebtModal = true },
                    icon = { FaPlus() },
                    backgroundColor = palette.danger,
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
            StatCard(
                title = strings.statTotalDebtsDue,
                value = formatFcfa(totalOutstanding),
                subtitle = "${debtsList.filter { it.status != "Réglé" }.size} ${strings.statTotalDebtsDueSub}",
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            StatCard(
                title = strings.statTotalRecovered,
                value = formatFcfa(totalSettled),
                subtitle = strings.statTotalRecoveredSub,
                icon = { FaCoins() },
                accentColor = palette.success
            )

            StatCard(
                title = strings.statDebtorsCount,
                value = "${debtsList.map { it.debtorName }.distinct().size}",
                subtitle = strings.statDebtorsCountSub,
                icon = { FaReceipt() },
                accentColor = palette.brand.primary
            )
        }

        // Filter and Search Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Div(FilterBarStyle.toAttrs()) {
                // Filter status
                Row(
                    Modifier
                        .gap(0.5.cssRem)
                        .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                        .styleModifier {
                            property("-webkit-overflow-scrolling", "touch")
                            property("white-space", "nowrap")
                        }
                ) {
                    listOf(
                        "Tous" to strings.statusAll,
                        "Non Payé" to strings.statusPending,
                        "Partiel" to strings.statusPartial,
                        "Réglé" to strings.statusSettled
                    ).forEach { (stKey, stLabel) ->
                        val isSelected = filterStatus == stKey
                        Button(
                            onClick = { filterStatus = stKey },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.35.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(stLabel)
                        }
                    }
                }

                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = strings.placeholderSearchDebts,
                    type = "search",
                    modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                )
            }
        }

        // Debts Table
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colDebtorName) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProductOwed) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colCratesOwed) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colSaleUnitPrice) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colTotalOwed) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colAmountPaid) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colRemainingOwed) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.status) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.actions) }
                            }
                        }
                        Tbody {
                            if (filteredDebts.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "10") }) {
                                        Text(strings.noDebtsRecorded)
                                    }
                                }
                            } else {
                                filteredDebts.forEach { debt ->
                                    val isSettled = debt.status == "Réglé"
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(debt.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Column {
                                                Text(debt.debtorName)
                                                if (debt.phone.isNotBlank()) {
                                                    Span(Modifier.fontSize(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                                        Text("Tél: ${debt.phone}")
                                                    }
                                                }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) { Text(debt.productName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            val qtyStr = if (debt.quantityCrates == debt.quantityCrates.toLong().toDouble()) debt.quantityCrates.toLong().toString() else debt.quantityCrates.toString()
                                            Text("$qtyStr ${strings.crates}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) {
                                            Text(formatFcfa(debt.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                            Text(formatFcfa(debt.totalAmount))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).color(palette.success).toAttrs()) {
                                            Text(formatFcfa(debt.amountPaid))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(if (isSettled) palette.success else palette.danger).toAttrs()) {
                                            Text(formatFcfa(debt.remainingAmount))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            val badgeBg = when (debt.status) {
                                                "Réglé" -> Color.rgb(0xDCFCE7)
                                                "Partiel" -> Color.rgb(0xFEF3C7)
                                                else -> Color.rgb(0xFEE2E2)
                                            }
                                            val badgeFg = when (debt.status) {
                                                "Réglé" -> Color.rgb(0x166534)
                                                "Partiel" -> Color.rgb(0x92400E)
                                                else -> Color.rgb(0x991B1B)
                                            }
                                            val badgeText = when (debt.status) {
                                                "Réglé" -> strings.statusBadgeSettled
                                                "Partiel" -> strings.statusBadgePartial
                                                else -> strings.statusBadgePending
                                            }
                                            Box(
                                                Modifier
                                                    .backgroundColor(badgeBg)
                                                    .color(badgeFg)
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .fontWeight(FontWeight.Bold)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(badgeText)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.4.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!isSettled) {
                                                    Button(
                                                        onClick = { debtToPay = debt },
                                                        Modifier
                                                            .padding(leftRight = 0.6.cssRem, topBottom = 0.3.cssRem)
                                                            .borderRadius(6.px)
                                                            .backgroundColor(palette.success)
                                                            .color(Colors.White)
                                                            .fontSize(0.8.cssRem)
                                                    ) {
                                                        Text(strings.btnSettleDebt)
                                                    }
                                                }
                                                Button(
                                                    onClick = { StockRepository.deleteDebt(debt.id) },
                                                    Modifier
                                                        .padding(leftRight = 0.5.cssRem, topBottom = 0.3.cssRem)
                                                        .borderRadius(6.px)
                                                        .backgroundColor(palette.nearBackground)
                                                        .border(1.px, LineStyle.Solid, palette.cardBorder)
                                                        .fontSize(0.8.cssRem)
                                                ) {
                                                    FaTrash(Modifier.color(palette.danger))
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
        }
    }

    QuickDebtModal(isOpen = showNewDebtModal, onClose = { showNewDebtModal = false })

    // Modal: Enregistrer un Remboursement / Règlement
    debtToPay?.let { d ->
        DebtPaymentModal(
            debt = d,
            isOpen = true,
            onClose = { debtToPay = null },
            onConfirm = { amt ->
                StockRepository.payDebt(d.id, amt)
                debtToPay = null
            }
        )
    }
}

@Composable
fun DebtPaymentModal(
    debt: Debt,
    isOpen: Boolean,
    onClose: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val strings = LocalLanguage.current.strings()
    var paymentAmount by remember { mutableStateOf(debt.remainingAmount.toString()) }

    ModalDialog(title = "${strings.modalSettleDebtTitle} : ${debt.debtorName}", isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Color.rgb(0xFEF3C7))
                    .padding(0.75.cssRem)
                    .borderRadius(8.px)
            ) {
                Column(Modifier.fillMaxWidth().gap(0.3.cssRem)) {
                    val initQtyStr = if (debt.quantityCrates == debt.quantityCrates.toLong().toDouble()) debt.quantityCrates.toLong().toString() else debt.quantityCrates.toString()
                    Span(Modifier.fontSize(0.9.cssRem).color(Color.rgb(0x92400E)).toAttrs()) {
                        Text("${strings.colTotalOwed} : ${formatFcfa(debt.totalAmount)} ($initQtyStr ${strings.crates} - ${debt.productName})")
                    }
                    Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).color(Color.rgb(0x92400E)).toAttrs()) {
                        Text("${strings.labelRemainingToPay} ${formatFcfa(debt.remainingAmount)}")
                    }
                }
            }

            FormField(label = strings.fieldPaymentAmount) {
                AppTextInput(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { paymentAmount = debt.remainingAmount.toString() },
                    Modifier.fontSize(0.8.cssRem).padding(leftRight = 0.6.cssRem, topBottom = 0.4.cssRem)
                ) {
                    Text(strings.btnSettleDebt)
                }
                Spacer()
                Button(onClick = { onClose() }, Modifier.borderRadius(8.px).padding(leftRight = 1.cssRem, topBottom = 0.5.cssRem)) {
                    Text(strings.cancel)
                }
                AppButton(
                    text = strings.btnSavePayment,
                    onClick = {
                        val amt = paymentAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            onConfirm(amt)
                        }
                    },
                    backgroundColor = Color.rgb(0x10B981),
                    textColor = Colors.White
                )
            }
        }
    }
}
