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
import org.app.tgvstock.SubheadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.localizeExpenseCategory
import org.app.tgvstock.strings
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
    val strings = LocalLanguage.current.strings()
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
                    Text(strings.expensesPageTitle)
                }
                Span(SubheadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.expensesPageSubtitle)
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnNewExpense,
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
                title = strings.statTotalExpensesAmount,
                value = formatFcfa(totalExpensesAmount),
                subtitle = "${filteredExpenses.size} ${strings.statTotalExpensesAmountSub}",
                icon = { FaReceipt() },
                accentColor = Color.rgb(0x9333EA)
            )

            StatCard(
                title = strings.statTodayExpensesAmount,
                value = formatFcfa(todayExpensesAmount),
                subtitle = strings.statTodayExpensesAmountSub,
                icon = { FaCalendarDay() },
                accentColor = palette.danger
            )

            StatCard(
                title = strings.catHandling,
                value = formatFcfa(expenses.filter { it.category == "Manutention" }.sumOf { it.amount }),
                subtitle = strings.statExpenseCategoriesSub,
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
                    listOf(
                        "Tous" to strings.catAllExpenses,
                        "Manutention" to strings.catHandling,
                        "Transport" to strings.catTransport,
                        "Factures" to strings.catBills,
                        "Personnel" to strings.catStaff,
                        "Divers" to strings.catMisc
                    ).forEach { (catKey, catLabel) ->
                        val isSelected = filterCategory == catKey
                        Button(
                            onClick = { filterCategory = catKey },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.75.cssRem, topBottom = 0.35.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) Color.rgb(0x9333EA) else palette.nearBackground)
                                .color(if (isSelected) Colors.White else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) Color.rgb(0x9333EA) else palette.cardBorder)
                        ) {
                            Text(catLabel)
                        }
                    }
                }

                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = strings.placeholderSearchExpenses,
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colSaleDate) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colExpenseMotive) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colExpenseCategory) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colExpenseBeneficiary) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colExpenseAmount) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.actions) }
                            }
                        }
                        Tbody {
                            if (filteredExpenses.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "6") }) {
                                        Text(strings.noExpensesRecorded)
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
                                                Text(strings.localizeExpenseCategory(ex.category))
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
