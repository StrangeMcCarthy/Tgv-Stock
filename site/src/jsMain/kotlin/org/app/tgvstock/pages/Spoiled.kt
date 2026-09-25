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
import com.varabyte.kobweb.compose.ui.styleModifier
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
import kotlin.math.roundToInt

@InitRoute
fun initSpoiledPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Produits Gâtés & Avaries"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun SpoiledPage() {
    val palette = ColorMode.current.toSitePalette()
    var showNewSpoiledModal by remember { mutableStateOf(false) }

    val spoiledList = StockRepository.spoiled
    val totalBottles = spoiledList.sumOf { it.quantityBottles }
    val totalLoss = spoiledList.sumOf { it.estimatedLoss }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text("Gestion des Produits Gâtés & Avaries")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Déclarez les bouteilles cassées, altérées ou défectueuses, déduites automatiquement du stock.")
                }
            }

            Spacer()

            AppButton(
                text = "Déclarer une Avarie",
                onClick = { showNewSpoiledModal = true },
                icon = { FaWineGlass() },
                backgroundColor = palette.danger,
                textColor = Colors.White
            )
        }

        // Summary KPI cards
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))") }
                .gap(1.2.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = "Total Bouteilles Avariées",
                value = "$totalBottles bouteilles",
                subtitle = "${spoiledList.size} déclarations d'avaries",
                icon = { FaWineBottle() },
                accentColor = palette.danger
            )

            StatCard(
                title = "Perte Financière Totale",
                value = formatFcfa(totalLoss),
                subtitle = "Calculée au prorata du prix par bouteille",
                icon = { FaMoneyBillWave() },
                accentColor = Color.rgb(0xDC2626)
            )

            StatCard(
                title = "Équivalent Casiers",
                value = "~${((totalBottles / 12.0) * 10.0).roundToInt() / 10.0} casiers",
                subtitle = "Impact sur le stock théorique",
                icon = { FaBoxOpen() },
                accentColor = palette.brand.primary
            )
        }

        // Table of spoiled products
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("QUANTITÉ EN BOUTEILLES") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("PERTE ESTIMÉE (FCFA)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("MOTIF / COMMENTAIRE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTION") }
                            }
                        }
                        Tbody {
                            if (spoiledList.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "6") }) {
                                        Text("Aucune avarie enregistrée. Le stock est intact.")
                                    }
                                }
                            } else {
                                spoiledList.forEach { sp ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) { Text(sp.date) }
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) { Text(sp.productName) }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).color(palette.danger).toAttrs()) {
                                            Text("${sp.quantityBottles} bouteilles")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.danger).toAttrs()) {
                                            Text(formatFcfa(sp.estimatedLoss))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).toAttrs()) {
                                            Text(sp.comment.ifBlank { "Aucun commentaire" })
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Button(
                                                onClick = { StockRepository.deleteSpoiled(sp.id) },
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

    QuickSpoiledModal(isOpen = showNewSpoiledModal, onClose = { showNewSpoiledModal = false })
}
