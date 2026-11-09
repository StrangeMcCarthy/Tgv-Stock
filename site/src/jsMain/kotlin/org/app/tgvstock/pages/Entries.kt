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
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette

@InitRoute
fun initEntriesPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Arrivals & Stock Entries"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun EntriesPage() {
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()
    var showNewEntryModal by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterDate by remember { mutableStateOf("") }

    val entries = StockRepository.entries
    val filteredEntries = entries.filter { e ->
        val matchesDate = filterDate.isBlank() || e.date == filterDate
        val matchesSearch = searchQuery.isBlank() || e.productName.contains(searchQuery, ignoreCase = true) || e.supplier.contains(searchQuery, ignoreCase = true)
        matchesDate && matchesSearch
    }

    val totalCratesReceived = filteredEntries.sumOf { it.effectiveCrates }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.entriesPageTitle)
                }
                Span(SubheadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.entriesPageSubtitle)
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnNewEntry,
                    onClick = { showNewEntryModal = true },
                    icon = { FaTruck() },
                    backgroundColor = palette.brand.primary
                )
            }
        }

        val totalBonusBottles = filteredEntries.filter { it.calculatedTotalExtraBottles > 0 }.sumOf { it.calculatedTotalExtraBottles }

        // Summary Cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(150px, 1fr))") }
                .gap(0.85.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = strings.statTotalReceived,
                value = "$totalCratesReceived ${strings.crates}",
                subtitle = "${filteredEntries.size} ${strings.statTotalReceivedSub}",
                icon = { FaBoxesStacked() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = strings.statBonusReceived,
                value = "+$totalBonusBottles ${strings.bottlesAbbr}",
                subtitle = strings.statBonusReceivedSub,
                icon = { FaBoxesStacked() },
                accentColor = palette.success
            )

            val todayEntriesCrates = entries.filter { it.date == StockRepository.todayString() }.sumOf { it.quantityCrates }
            val todayEntriesCratesStr = if (todayEntriesCrates == todayEntriesCrates.toLong().toDouble()) todayEntriesCrates.toLong().toString() else todayEntriesCrates.toString()
            StatCard(
                title = strings.statTodayEntries,
                value = "$todayEntriesCratesStr ${strings.crates}",
                subtitle = "${strings.statTodayEntriesSub} ${StockRepository.todayString()}",
                icon = { FaTruck() },
                accentColor = Color.rgb(0x3B82F6)
            )

            StatCard(
                title = strings.statMainSuppliers,
                value = "${entries.map { it.supplier }.distinct().size} ${strings.statMainSuppliersSub}",
                subtitle = "Boissons du Cameroun, Diageo Guinness, etc.",
                icon = { FaBuilding() },
                accentColor = Color.rgb(0x8B5CF6)
            )
        }

        // Filter & Search Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Div(FilterBarStyle.toAttrs()) {
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

                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = strings.placeholderSearchEntry,
                    type = "search",
                    modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                )
            }
        }

        // Entries Table
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                ResponsiveTableContainer(minWidth = 800.px) {
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.date) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProduct) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colQuantityEntered) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colExtrasGaps) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colSupplier) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.notes) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.actions) }
                            }
                        }
                        Tbody {
                            if (filteredEntries.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "7") }) {
                                        Text(strings.noEntriesRecorded)
                                    }
                                }
                            } else {
                                filteredEntries.forEach { entry ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(entry.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(entry.productName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                            val displayQty = if (entry.effectiveCrates == entry.effectiveCrates.toLong().toDouble()) entry.effectiveCrates.toLong().toString() else entry.effectiveCrates.toString()
                                            Text("+$displayQty ${strings.crates}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            val totalBonus = entry.calculatedTotalExtraBottles
                                            if (entry.extras != 0 || totalBonus != 0) {
                                                val isPositive = totalBonus > 0 || entry.extras > 0
                                                val sign = if (isPositive) "+" else ""
                                                val extrasColor = if (isPositive) palette.success else palette.danger
                                                Column(
                                                    Modifier.fillMaxWidth(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        Modifier
                                                            .backgroundColor(if (isPositive) Color.rgb(0xD1FAE5) else Color.rgb(0xFEE2E2))
                                                            .color(extrasColor)
                                                            .borderRadius(10.px)
                                                            .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                            .fontSize(0.78.cssRem)
                                                            .fontWeight(FontWeight.Bold)
                                                            .display(DisplayStyle.InlineBlock)
                                                    ) {
                                                        if (entry.extras != 0) {
                                                            Text("$sign${entry.extras} ${strings.extrasBtlPerCrate}")
                                                        } else {
                                                            Text("$sign$totalBonus ${strings.bottlesAbbr}")
                                                        }
                                                    }
                                                    if (entry.extras != 0 && entry.quantityCrates > 1.0) {
                                                        Span(Modifier.fontSize(0.72.cssRem).color(extrasColor).fontWeight(FontWeight.SemiBold).margin(top = 0.1.cssRem).toAttrs()) {
                                                            Text("($sign$totalBonus ${strings.extrasBtlTotal})")
                                                        }
                                                    }
                                                    if (entry.motif.isNotBlank()) {
                                                        Span(Modifier.fontSize(0.72.cssRem).color(palette.textMuted).margin(top = 0.1.cssRem).toAttrs()) {
                                                            Text(entry.motif)
                                                        }
                                                    }
                                                }
                                            } else {
                                                Span(Modifier.color(palette.textMuted).fontSize(0.8.cssRem).toAttrs()) { Text("—") }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) { Text(entry.supplier) }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(entry.notes.ifBlank { "—" })
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Button(
                                                onClick = { StockRepository.deleteEntry(entry.id) },
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

    QuickEntryModal(isOpen = showNewEntryModal, onClose = { showNewEntryModal = false })
}
