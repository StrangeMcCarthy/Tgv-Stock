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
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import com.varabyte.kobweb.compose.ui.styleModifier
import org.app.tgvstock.FilterBarStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.models.Debt
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
                    Text("Gestion des Dettes & Créances Clients")
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Saisissez le nom du débiteur et le nombre de casiers. Le montant total est calculé automatiquement à partir du prix du casier.")
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = "Nouvelle Dette",
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
                title = "Dettes Restantes à Recouvrer",
                value = formatFcfa(totalOutstanding),
                subtitle = "${debtsList.filter { it.status != "Réglé" }.size} créances non soldées",
                icon = { FaHandHoldingDollar() },
                accentColor = palette.danger
            )

            StatCard(
                title = "Total Remboursé / Encaissé",
                value = formatFcfa(totalSettled),
                subtitle = "Paiements partiels et complets",
                icon = { FaCoins() },
                accentColor = palette.success
            )

            StatCard(
                title = "Montant Total des Crédits",
                value = formatFcfa(debtsList.sumOf { it.totalAmount }),
                subtitle = "Cumul global des dettes accordées",
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
                    listOf("Tous", "Non Payé", "Partiel", "Réglé").forEach { st ->
                        val isSelected = filterStatus == st
                        Button(
                            onClick = { filterStatus = st },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.35.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(st)
                        }
                    }
                }

                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = "Chercher débiteur ou produit...",
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("DATE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("NOM DU DÉBITEUR") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("PRODUIT DÛ") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("CASIERS DUS") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("P.U CASIER") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("TOTAL CALCULÉ") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("AVANCÉ / PAYÉ") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("RESTE DÛ") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("STATUT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTIONS") }
                            }
                        }
                        Tbody {
                            if (filteredDebts.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "10") }) {
                                        Text("Aucune dette enregistrée pour ce filtre.")
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
                                            Text("$qtyStr casiers")
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
                                                Text(debt.status)
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
                                                        Text("Régler")
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
    var paymentAmount by remember { mutableStateOf(debt.remainingAmount.toString()) }

    ModalDialog(title = "Enregistrer un Remboursement pour ${debt.debtorName}", isOpen = isOpen, onClose = onClose) {
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
                        Text("Dette initiale : ${formatFcfa(debt.totalAmount)} ($initQtyStr casiers de ${debt.productName})")
                    }
                    Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).color(Color.rgb(0x92400E)).toAttrs()) {
                        Text("Reste à payer actuel : ${formatFcfa(debt.remainingAmount)}")
                    }
                }
            }

            FormField(label = "Montant du Versement / Remboursement (FCFA)") {
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
                    Text("Solder la totalité")
                }
                Spacer()
                Button(onClick = { onClose() }, Modifier.borderRadius(8.px).padding(leftRight = 1.cssRem, topBottom = 0.5.cssRem)) {
                    Text("Annuler")
                }
                AppButton(
                    text = "Valider le Paiement",
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
