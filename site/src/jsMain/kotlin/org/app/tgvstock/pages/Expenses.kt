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
import org.app.tgvstock.toSitePalette

@InitRoute
fun initExpensesPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Registre des Dépenses"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun ExpensesPage() {
    val palette = ColorMode.current.toSitePalette()
    var showNewExpenseModal by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("Tous") }
    var filterDate by remember { mutableStateOf("") }

    val expenses = StockRepository.expenses
    val filteredExpenses = expenses.filter { ex ->
        val matchesCat = (filterCategory == "Tous") || (ex.category == filterCategory)
        val matchesDate = filterDate.isBlank() || ex.date == filterDate
        val matchesSearch = searchQuery.isBlank() || ex.motive.contains(searchQuery, ignoreCase = true) || ex.beneficiary.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesDate && matchesSearch
    }

    val totalExpensesAmount = filteredExpenses.sumOf { it.amount }
    val todayExpensesAmount = expenses.filter { it.date == StockRepository.todayString() }.sumOf { it.amount }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text("Registre des Dépenses & Charges")
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Suivi de tous les frais opérationnels du dépôt (manutention, transport, glace, factures, salaires).")
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = "Ajouter une Dépense",
                    onClick = { showNewExpenseModal = true },
                    icon = { FaPlus() },
                    backgroundColor = Color.rgb(0x9333EA),
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
                title = "Total des Dépenses",
                value = formatFcfa(totalExpensesAmount),
                subtitle = "${filteredExpenses.size} lignes de dépenses",
                icon = { FaReceipt() },
                accentColor = Color.rgb(0x9333EA)
            )

            StatCard(
                title = "Dépenses Aujourd'hui",
                value = formatFcfa(todayExpensesAmount),
                subtitle = "Charges du jour ${StockRepository.todayString()}",
                icon = { FaCalendarDay() },
                accentColor = palette.danger
            )

            StatCard(
                title = "Manutention & Logistique",
                value = formatFcfa(expenses.filter { it.category == "Manutention" }.sumOf { it.amount }),
                subtitle = "Frais de déchargement et portage",
                icon = { FaTruck() },
                accentColor = palette.brand.primary
            )
        }

        // Filter Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Div(FilterBarStyle.toAttrs()) {
                // Category buttons
                Row(
                    Modifier
                        .gap(0.5.cssRem)
                        .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                        .styleModifier {
                            property("-webkit-overflow-scrolling", "touch")
                            property("white-space", "nowrap")
                        }
                ) {
                    listOf("Tous", "Manutention", "Transport", "Factures", "Personnel", "Divers").forEach { cat ->
                        val isSelected = filterCategory == cat
                        Button(
                            onClick = { filterCategory = cat },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.75.cssRem, topBottom = 0.35.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) Color.rgb(0x9333EA) else palette.nearBackground)
                                .color(if (isSelected) Colors.White else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) Color.rgb(0x9333EA) else palette.cardBorder)
                        ) {
                            Text(cat)
                        }
                    }
                }

                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = "Chercher motif ou bénéficiaire...",
                    type = "search",
                    modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                )
            }
        }

        // Expenses Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                ResponsiveTableContainer(minWidth = 750.px) {
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("MOTIF DE LA DÉPENSE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("CATÉGORIE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("BÉNÉFICIAIRE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("MONTANT (FCFA)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTION") }
                            }
                        }
                        Tbody {
                            if (filteredExpenses.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "6") }) {
                                        Text("Aucune dépense enregistrée pour ce filtre.")
                                    }
                                }
                            } else {
                                filteredExpenses.forEach { ex ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(ex.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(ex.motive) }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) {
                                            Box(
                                                Modifier
                                                    .backgroundColor(palette.nearBackground)
                                                    .border(1.px, LineStyle.Solid, palette.cardBorder)
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(ex.category)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(ex.beneficiary.ifBlank { "—" })
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(Color.rgb(0x9333EA)).toAttrs()) {
                                            Text(formatFcfa(ex.amount))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Button(
                                                onClick = { StockRepository.deleteExpense(ex.id) },
                                                Modifier.padding(leftRight = 0.5.cssRem, topBottom = 0.25.cssRem).borderRadius(6.px).fontSize(0.75.cssRem)
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

    QuickExpenseModal(isOpen = showNewExpenseModal, onClose = { showNewExpenseModal = false })
}
