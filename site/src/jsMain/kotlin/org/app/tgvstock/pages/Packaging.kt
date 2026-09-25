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
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.models.Packaging
import org.app.tgvstock.toSitePalette

@InitRoute
fun initPackagingPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Gestion des Emballages"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun PackagingPage() {
    val palette = ColorMode.current.toSitePalette()

    var showAddModal by remember { mutableStateOf(false) }
    var packagingToEdit by remember { mutableStateOf<Packaging?>(null) }
    var packagingToDelete by remember { mutableStateOf<Packaging?>(null) }

    val packagingList = StockRepository.packaging
    val totalEmpty = packagingList.sumOf { it.emptyCrates }
    val totalFull = packagingList.sumOf { it.fullCrates }
    val totalCrates = totalEmpty + totalFull
    val totalDeposit = packagingList.sumOf { it.totalDepositValue }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text("Gestion des Emballages (Casiers & Bouteilles)")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Suivi de tous les emballages disponibles au dépôt, qu'ils soient vides ou pleins, et gestion de la consignation.")
                }
            }

            Spacer()

            AppButton(
                text = "Ajouter un Emballage",
                onClick = { showAddModal = true },
                icon = { FaPlus() },
                backgroundColor = palette.brand.primary
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
                title = "Casiers Vides",
                value = "$totalEmpty casiers",
                subtitle = "Prêts pour retour ou consignation",
                icon = { FaBoxOpen() },
                accentColor = Color.rgb(0x06B6D4)
            )

            StatCard(
                title = "Casiers Pleins",
                value = "$totalFull casiers",
                subtitle = "Disponibles en dépôt pour la vente",
                icon = { FaBoxArchive() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = "Parc Total d'Emballages",
                value = "$totalCrates casiers",
                subtitle = "Casiers vides + casiers pleins",
                icon = { FaBoxesStacked() },
                accentColor = palette.success
            )

            StatCard(
                title = "Valeur Totale Consignation",
                value = formatFcfa(totalDeposit),
                subtitle = "Capital immobilisé en emballages",
                icon = { FaCoins() },
                accentColor = Color.rgb(0xEAB308)
            )
        }

        // Packaging CRUD Table
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("LIBELLÉ EMBALLAGE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("FORMAT / TYPE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("CASIERS VIDES") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("CASIERS PLEINS") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("TOTAL CASIERS") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("CONSIGNATION / CASIER") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("VALEUR TOTALE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTIONS") }
                            }
                        }
                        Tbody {
                            if (packagingList.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "8") }) {
                                        Text("Aucun emballage enregistré.")
                                    }
                                }
                            } else {
                                packagingList.forEach { pkg ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.5.cssRem)) {
                                                Box(
                                                    Modifier
                                                        .size(1.8.cssRem)
                                                        .borderRadius(6.px)
                                                        .backgroundColor(Color.rgb(0x06B6D4).toRgb().copyf(alpha = 0.15f))
                                                        .color(Color.rgb(0x06B6D4))
                                                        .fontSize(0.85.cssRem),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FaBoxesStacked()
                                                }
                                                Text(pkg.name)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(pkg.crateType)
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.4.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = {
                                                        if (pkg.emptyCrates > 0) {
                                                            pkg.emptyCrates--
                                                            StockRepository.updatePackaging(pkg)
                                                        }
                                                    },
                                                    Modifier.padding(leftRight = 0.4.cssRem, topBottom = 0.2.cssRem).borderRadius(4.px).fontSize(0.75.cssRem)
                                                ) { Text("-") }
                                                Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x0284C7)).toAttrs()) {
                                                    Text("${pkg.emptyCrates}")
                                                }
                                                Button(
                                                    onClick = {
                                                        pkg.emptyCrates++
                                                        StockRepository.updatePackaging(pkg)
                                                    },
                                                    Modifier.padding(leftRight = 0.4.cssRem, topBottom = 0.2.cssRem).borderRadius(4.px).fontSize(0.75.cssRem)
                                                ) { Text("+") }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.4.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = {
                                                        if (pkg.fullCrates > 0) {
                                                            pkg.fullCrates--
                                                            StockRepository.updatePackaging(pkg)
                                                        }
                                                    },
                                                    Modifier.padding(leftRight = 0.4.cssRem, topBottom = 0.2.cssRem).borderRadius(4.px).fontSize(0.75.cssRem)
                                                ) { Text("-") }
                                                Span(Modifier.fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                                    Text("${pkg.fullCrates}")
                                                }
                                                Button(
                                                    onClick = {
                                                        pkg.fullCrates++
                                                        StockRepository.updatePackaging(pkg)
                                                    },
                                                    Modifier.padding(leftRight = 0.4.cssRem, topBottom = 0.2.cssRem).borderRadius(4.px).fontSize(0.75.cssRem)
                                                ) { Text("+") }
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Text("${pkg.totalCrates}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).color(palette.textMuted).toAttrs()) {
                                            Text(formatFcfa(pkg.depositValuePerCrate))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(Color.rgb(0xD97706)).toAttrs()) {
                                            Text(formatFcfa(pkg.totalDepositValue))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.4.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AppIconButton(
                                                    onClick = { packagingToEdit = pkg },
                                                    icon = { FaPenToSquare(Modifier.color(palette.brand.primary)) }
                                                )
                                                AppIconButton(
                                                    onClick = { packagingToDelete = pkg },
                                                    icon = { FaTrash(Modifier.color(palette.danger)) }
                                                )
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

    // Modal: Ajouter Emballage
    if (showAddModal) {
        PackagingFormModal(
            title = "Ajouter un Type d'Emballage",
            initial = null,
            isOpen = true,
            onClose = { showAddModal = false },
            onSave = { name, crateType, emptyCrates, fullCrates, deposit ->
                StockRepository.addPackaging(name, crateType, emptyCrates, fullCrates, deposit)
                showAddModal = false
            }
        )
    }

    // Modal: Modifier Emballage
    packagingToEdit?.let { pkg ->
        PackagingFormModal(
            title = "Modifier l'Emballage : ${pkg.name}",
            initial = pkg,
            isOpen = true,
            onClose = { packagingToEdit = null },
            onSave = { name, crateType, emptyCrates, fullCrates, deposit ->
                pkg.name = name
                pkg.crateType = crateType
                pkg.emptyCrates = emptyCrates
                pkg.fullCrates = fullCrates
                pkg.depositValuePerCrate = deposit
                StockRepository.updatePackaging(pkg)
                packagingToEdit = null
            }
        )
    }

    // Modal: Supprimer Emballage
    packagingToDelete?.let { pkg ->
        ModalDialog(
            title = "Supprimer cet Emballage ?",
            isOpen = true,
            onClose = { packagingToDelete = null }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Span(Modifier.toAttrs()) {
                    Text("Êtes-vous sûr de vouloir supprimer ${pkg.name} ?")
                }
                Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Spacer()
                    AppSecondaryButton(text = "Annuler", onClick = { packagingToDelete = null })
                    AppButton(
                        text = "Supprimer",
                        onClick = {
                            StockRepository.deletePackaging(pkg.id)
                            packagingToDelete = null
                        },
                        backgroundColor = Color.rgb(0xDC2626),
                        textColor = Colors.White
                    )
                }
            }
        }
    }
}

@Composable
fun PackagingFormModal(
    title: String,
    initial: Packaging?,
    isOpen: Boolean,
    onClose: () -> Unit,
    onSave: (name: String, crateType: String, emptyCrates: Int, fullCrates: Int, depositValue: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var crateType by remember { mutableStateOf(initial?.crateType ?: "65cl") }
    var emptyCrates by remember { mutableStateOf(initial?.emptyCrates?.toString() ?: "0") }
    var fullCrates by remember { mutableStateOf(initial?.fullCrates?.toString() ?: "0") }
    var depositValue by remember { mutableStateOf(initial?.depositValuePerCrate?.toString() ?: "3000") }

    ModalDialog(title = title, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Nom / Libellé de l'Emballage") {
                AppTextInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholderText = "Ex: Casier 65cl (12 bouteilles)...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Type / Format de Bouteille") {
                AppTextInput(
                    value = crateType,
                    onValueChange = { crateType = it },
                    placeholderText = "Ex: 65cl, 50cl, 33cl, Canettes, 1L...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem)) {
                Box(Modifier.weight(1f)) {
                    FormField(label = "Casiers Vides Disponibles") {
                        AppTextInput(
                            value = emptyCrates,
                            onValueChange = { emptyCrates = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = "Casiers Pleins Disponibles") {
                        AppTextInput(
                            value = fullCrates,
                            onValueChange = { fullCrates = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            FormField(label = "Valeur de Consignation Unitaire par Casier (FCFA)") {
                AppTextInput(
                    value = depositValue,
                    onValueChange = { depositValue = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = if (initial == null) "Ajouter l'Emballage" else "Sauvegarder",
                    onClick = {
                        val emptyVal = emptyCrates.toIntOrNull() ?: 0
                        val fullVal = fullCrates.toIntOrNull() ?: 0
                        val depVal = depositValue.toDoubleOrNull() ?: 3000.0
                        if (name.isNotBlank()) {
                            onSave(name, crateType, emptyVal, fullVal, depVal)
                        }
                    }
                )
            }
        }
    }
}
