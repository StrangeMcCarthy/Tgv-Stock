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

@InitRoute
fun initEntriesPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Arrivages & Entrées en Stock"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun EntriesPage() {
    val palette = ColorMode.current.toSitePalette()
    var showNewEntryModal by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterDate by remember { mutableStateOf("") }

    val entries = StockRepository.entries
    val filteredEntries = entries.filter { e ->
        val matchesDate = filterDate.isBlank() || e.date == filterDate
        val matchesSearch = searchQuery.isBlank() || e.productName.contains(searchQuery, ignoreCase = true) || e.supplier.contains(searchQuery, ignoreCase = true)
        matchesDate && matchesSearch
    }

    val totalCratesReceived = filteredEntries.sumOf { it.quantityCrates }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toAttrs()) {
                    Text("Arrivages & Entrées de Stock")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Enregistrement des livraisons de bières et boissons reçues des brasseries et fournisseurs.")
                }
            }

            Spacer()

            AppButton(
                text = "Enregistrer un Arrivage",
                onClick = { showNewEntryModal = true },
                icon = { FaTruckLoading() },
                backgroundColor = palette.brand.primary
            )
        }

        // Summary Cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .gridTemplateColumns("repeat(auto-fit, minmax(220px, 1fr))")
                .gap(1.2.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = "Total Casiers Réceptionnés",
                value = "$totalCratesReceived casiers",
                subtitle = "${filteredEntries.size} bons de livraison enregistrés",
                icon = { FaBoxesStacked() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = "Arrivages Aujourd'hui",
                value = "${entries.filter { it.date == StockRepository.todayString() }.sumOf { it.quantityCrates }} casiers",
                subtitle = "Reçus le ${StockRepository.todayString()}",
                icon = { FaTruck() },
                accentColor = palette.success
            )

            StatCard(
                title = "Fournisseurs Principaux",
                value = "${entries.map { it.supplier }.distinct().size} partenaires",
                subtitle = "Boissons du Cameroun, Diageo Guinness, etc.",
                icon = { FaBuilding() },
                accentColor = Color.rgb(0x3B82F6)
            )
        }

        // Filter & Search Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().gap(1.cssRem),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.gap(0.4.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                        Text("Filtrer par Date :")
                    }
                    Input(
                        type = InputType.Date,
                        value = filterDate,
                        attrs = Modifier
                            .padding(0.4.cssRem)
                            .borderRadius(6.px)
                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                            .backgroundColor(palette.inputBackground)
                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                            .toAttrs {
                                onInput { filterDate = it.value }
                            }
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

                Spacer()

                Input(
                    type = InputType.Search,
                    value = searchQuery,
                    attrs = Modifier
                        .width(16.cssRem)
                        .padding(leftRight = 0.8.cssRem, topBottom = 0.4.cssRem)
                        .borderRadius(8.px)
                        .border(1.px, LineStyle.Solid, palette.cardBorder)
                        .backgroundColor(palette.inputBackground)
                        .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                        .toAttrs {
                            placeholder("Chercher produit ou fournisseur...")
                            onInput { searchQuery = it.value }
                        }
                )
            }
        }

        // Entries Table
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("QUANTITÉ ENTRÉE (CASIERS)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("FOURNISSEUR / BRASSERIE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("NOTES / BORDEREAU") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTION") }
                            }
                        }
                        Tbody {
                            if (filteredEntries.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "6") }) {
                                        Text("Aucun arrivage enregistré.")
                                    }
                                }
                            } else {
                                filteredEntries.forEach { entry ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(entry.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(entry.productName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                            Text("+${entry.quantityCrates} casiers")
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
