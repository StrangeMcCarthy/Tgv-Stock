package org.app.tgvstock.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
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
import org.app.tgvstock.Strings
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.models.Packaging
import org.app.tgvstock.strings
import org.app.tgvstock.toSitePalette

// ─── Movement type metadata ────────────────────────────────────────────────

data class MovementTypeInfo(
    val key: String,
    val label: (Strings) -> String,
    val shortLabel: (Strings) -> String,
    val deltaEmptySign: Int,  // +1 = increases empty, -1 = decreases, 0 = no effect
    val deltaFullSign: Int,
    val color: Color,
    val description: (Strings) -> String
)

val packagingMovementTypes = listOf(
    MovementTypeInfo("RECEPTION_PLEIN",    { it.movReceptionPlein },    { it.movReceptionPleinShort },    0, +1, Color.rgb(0x16A34A), { it.movReceptionPleinDesc }),
    MovementTypeInfo("RETOUR_VIDE_CLIENT", { it.movRetourVideClient }, { it.movRetourVideClientShort }, +1,  0, Color.rgb(0x2563EB), { it.movRetourVideClientDesc }),
    MovementTypeInfo("RETOUR_BRASSERIE",   { it.movRetourBrasserie },   { it.movRetourBrasserieShort },  -1,  0, Color.rgb(0x7C3AED), { it.movRetourBrasserieDesc }),
    MovementTypeInfo("VENTE_CASIER_VIDE",  { it.movVenteCasierVide },  { it.movVenteCasierVideShort },  -1,  0, Color.rgb(0xEA580C), { it.movVenteCasierVideDesc }),
    MovementTypeInfo("CONVERSION",         { it.movConversion },        { it.movConversionShort },       +1, -1, Color.rgb(0x0D9488), { it.movConversionDesc }),
    MovementTypeInfo("AJUSTEMENT_VIDE",    { it.movAjustementVide },    { it.movAjustementVideShort },   +1,  0, Color.rgb(0x92400E), { it.movAjustementVideDesc }),
    MovementTypeInfo("AJUSTEMENT_PLEIN",   { it.movAjustementPlein },   { it.movAjustementPleinShort },   0, +1, Color.rgb(0x1D4ED8), { it.movAjustementPleinDesc })
)

fun movementTypeInfo(key: String) = packagingMovementTypes.find { it.key == key }
    ?: MovementTypeInfo(key, { key }, { key }, 0, 0, Color.rgb(0x6B7280), { "" })

// ─── Page ──────────────────────────────────────────────────────────────────

@InitRoute
fun initPackagingPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Gestion des Emballages"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun PackagingPage() {
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

    var showAddModal by remember { mutableStateOf(false) }
    var packagingToEdit by remember { mutableStateOf<Packaging?>(null) }
    var packagingToDelete by remember { mutableStateOf<Packaging?>(null) }
    var showMovementModal by remember { mutableStateOf(false) }
    var movementTargetPkg by remember { mutableStateOf<Packaging?>(null) }
    var historyFilterId by remember { mutableStateOf<String?>(null) }

    val packagingList = StockRepository.packaging
    val movements = StockRepository.packagingMovements
    val filteredMovements = if (historyFilterId != null)
        movements.filter { it.packagingId == historyFilterId }
    else
        movements

    val totalEmpty = packagingList.sumOf { it.emptyCrates }
    val totalFull = packagingList.sumOf { it.fullCrates }
    val totalCrates = totalEmpty + totalFull
    val totalDeposit = packagingList.sumOf { it.totalDepositValue }
    val totalMovementsToday = movements.count { it.date == StockRepository.todayString() }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // ── Header ──────────────────────────────────────────────────────────
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.pkgPageTitle)
                }
                Span(SubheadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.pkgPageSubtitle)
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnNewPkgMovement,
                    onClick = { movementTargetPkg = null; showMovementModal = true },
                    icon = { FaArrowRightArrowLeft() },
                    backgroundColor = Color.rgb(0x0D9488)
                )
                AppButton(
                    text = strings.btnAddPackaging,
                    onClick = { showAddModal = true },
                    icon = { FaPlus() },
                    backgroundColor = palette.brand.primary
                )
            }
        }

        // ── KPI Cards ───────────────────────────────────────────────────────
        Div(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier { property("grid-template-columns", "repeat(auto-fit, minmax(150px, 1fr))") }
                .gap(0.85.cssRem)
                .toAttrs()
        ) {
            StatCard(
                title = strings.pkgEmptyCrates,
                value = "$totalEmpty ${strings.crates}",
                subtitle = strings.pkgEmptyCratesSub,
                icon = { FaBoxOpen() },
                accentColor = Color.rgb(0x06B6D4)
            )

            StatCard(
                title = strings.pkgFullCrates,
                value = "$totalFull ${strings.crates}",
                subtitle = strings.pkgFullCratesSub,
                icon = { FaBoxArchive() },
                accentColor = palette.brand.primary
            )

            StatCard(
                title = strings.pkgTotalCrates,
                value = "$totalCrates ${strings.crates}",
                subtitle = strings.pkgTotalCratesSub,
                icon = { FaBoxesStacked() },
                accentColor = palette.success
            )

            StatCard(
                title = strings.pkgDepositValue,
                value = formatFcfa(totalDeposit),
                subtitle = strings.pkgDepositValueSub,
                icon = { FaCoins() },
                accentColor = Color.rgb(0xEAB308)
            )

            StatCard(
                title = strings.pkgMovementsToday,
                value = "$totalMovementsToday",
                subtitle = "${movements.size} ${strings.pkgMovementsTodaySub}",
                icon = { FaArrowRightArrowLeft() },
                accentColor = Color.rgb(0x7C3AED)
            )
        }

        // ── Packaging Table ─────────────────────────────────────────────────
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Div(FilterBarStyle.toAttrs()) {
                    Column(Modifier.gap(0.2.cssRem)) {
                        Span(Modifier.fontSize(1.1.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                            Text(strings.pkgStockTitle)
                        }
                        Span(Modifier.fontSize(0.82.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.pkgStockSubtitle)
                        }
                    }
                    if (historyFilterId != null) {
                        Button(
                            onClick = { historyFilterId = null },
                            Modifier.fontSize(0.8.cssRem).padding(leftRight = 0.6.cssRem, topBottom = 0.25.cssRem).borderRadius(20.px)
                                .backgroundColor(palette.nearBackground).border(1.px, LineStyle.Solid, palette.cardBorder)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.3.cssRem)) {
                                FaXmark()
                                Text(strings.filterAllBrands)
                            }
                        }
                    }
                }

                ResponsiveTableContainer(minWidth = 800.px) {
                    Table(
                        Modifier
                            .fillMaxWidth()
                            .borderCollapse(com.varabyte.kobweb.compose.css.BorderCollapse.Collapse)
                            .fontSize(0.88.cssRem)
                            .toAttrs()
                    ) {
                        Thead {
                            Tr(
                                Modifier
                                    .backgroundColor(palette.nearBackground)
                                    .borderBottom(2.px, LineStyle.Solid, palette.cardBorder)
                                    .toAttrs()
                            ) {
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colPkgBrand) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colPkgType) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colPkgEmpty) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colPkgFull) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colPkgTotal) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).toAttrs()) { Text(strings.colPkgUnitDeposit) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).toAttrs()) { Text(strings.colPkgTotalDeposit) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.date) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.actions) }
                            }
                        }
                        Tbody {
                            if (packagingList.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text(strings.noPackagingRecorded)
                                    }
                                }
                            } else {
                                packagingList.forEach { pkg ->
                                    val pkgMovements = movements.count { it.packagingId == pkg.id }
                                    val isFiltered = historyFilterId == pkg.id
                                    Tr(
                                        Modifier
                                            .borderBottom(1.px, LineStyle.Solid, palette.cardBorder)
                                            .backgroundColor(if (isFiltered) palette.brand.primary.toRgb().copyf(alpha = 0.06f) else Colors.Transparent)
                                            .toAttrs()
                                    ) {
                                        // Name
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
                                                ) { FaBoxesStacked() }
                                                Text(pkg.name)
                                            }
                                        }
                                        // Format
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(pkg.crateType)
                                        }
                                        // Empty crates with +/-
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) {
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
                                                Span(Modifier.fontWeight(FontWeight.Bold).color(Color.rgb(0x0284C7)).minWidth(2.cssRem).textAlign(TextAlign.Center).toAttrs()) {
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
                                        // Full crates with +/-
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) {
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
                                                Span(Modifier.fontWeight(FontWeight.Bold).color(palette.brand.primary).minWidth(2.cssRem).textAlign(TextAlign.Center).toAttrs()) {
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
                                        // Total
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Text("${pkg.totalCrates}")
                                        }
                                        // Consignation per crate
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).color(palette.textMuted).toAttrs()) {
                                            Text(formatFcfa(pkg.depositValuePerCrate))
                                        }
                                        // Total value
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).fontWeight(FontWeight.Bold).color(Color.rgb(0xD97706)).toAttrs()) {
                                            Text(formatFcfa(pkg.totalDepositValue))
                                        }
                                        // Last updated
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).fontSize(0.78.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(pkg.lastUpdated.ifBlank { "—" })
                                        }
                                        // Actions
                                        Td(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.3.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Mouvement button
                                                AppIconButton(
                                                    onClick = { movementTargetPkg = pkg; showMovementModal = true },
                                                    icon = {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.25.cssRem)) {
                                                            FaArrowRightArrowLeft(Modifier.color(Color.rgb(0x0D9488)))
                                                            if (pkgMovements > 0) {
                                                                Span(Modifier.fontSize(0.7.cssRem).color(palette.textMuted).toAttrs()) {
                                                                    Text("($pkgMovements)")
                                                                }
                                                            }
                                                        }
                                                    }
                                                )
                                                // History filter
                                                AppIconButton(
                                                    onClick = { historyFilterId = if (historyFilterId == pkg.id) null else pkg.id },
                                                    icon = { FaClockRotateLeft(Modifier.color(Color.rgb(0x7C3AED))) }
                                                )
                                                // Edit
                                                AppIconButton(
                                                    onClick = { packagingToEdit = pkg },
                                                    icon = { FaPenToSquare(Modifier.color(palette.brand.primary)) }
                                                )
                                                // Delete
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

        // ── Movement History ─────────────────────────────────────────────────
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.gap(0.2.cssRem)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.5.cssRem)) {
                            Span(Modifier.fontSize(1.1.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                Text(strings.pkgHistoryTitle)
                            }
                            if (historyFilterId != null) {
                                val name = packagingList.find { it.id == historyFilterId }?.name ?: ""
                                Box(
                                    Modifier
                                        .backgroundColor(palette.brand.primary.toRgb().copyf(alpha = 0.15f))
                                        .color(palette.brand.primary)
                                        .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                        .borderRadius(12.px)
                                        .fontSize(0.75.cssRem)
                                        .fontWeight(FontWeight.Bold)
                                ) {
                                    Text("${strings.filterBrandLabel} $name")
                                }
                            }
                        }
                        Span(Modifier.fontSize(0.82.cssRem).color(palette.textMuted).toAttrs()) {
                            Text(strings.pkgHistorySubtitle)
                        }
                    }
                }

                if (filteredMovements.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(2.cssRem)
                            .borderRadius(8.px)
                            .backgroundColor(palette.nearBackground)
                            .textAlign(TextAlign.Center)
                    ) {
                        Column(Modifier.gap(0.5.cssRem), horizontalAlignment = Alignment.CenterHorizontally) {
                            FaArrowRightArrowLeft(Modifier.fontSize(2.cssRem).color(palette.textMuted))
                            Span(Modifier.color(palette.textMuted).fontSize(0.9.cssRem).toAttrs()) {
                                Text(strings.noMovementsRecorded)
                            }
                        }
                    }
                } else {
                    ResponsiveTableContainer(minWidth = 780.px) {
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
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colMovDate) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colMovBrand) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colMovType) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colMovEmptyDelta) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text(strings.colMovFullDelta) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.colMovActor) }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text(strings.notes) }
                                }
                            }
                            Tbody {
                                filteredMovements.take(60).forEach { mv ->
                                    val info = movementTypeInfo(mv.movementType)
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        // Date
                                        Td(Modifier.padding(0.65.cssRem).color(palette.textMuted).fontSize(0.8.cssRem).toAttrs()) {
                                            Text(mv.date)
                                        }
                                        // Packaging name
                                        Td(Modifier.padding(0.65.cssRem).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                            Text(mv.packagingName)
                                        }
                                        // Movement type badge
                                        Td(Modifier.padding(0.65.cssRem).toAttrs()) {
                                            Box(
                                                Modifier
                                                    .display(DisplayStyle.InlineBlock)
                                                    .backgroundColor(info.color.toRgb().copyf(alpha = 0.12f))
                                                    .color(info.color)
                                                    .padding(leftRight = 0.55.cssRem, topBottom = 0.15.cssRem)
                                                    .borderRadius(12.px)
                                                    .fontWeight(FontWeight.SemiBold)
                                                    .fontSize(0.78.cssRem)
                                            ) {
                                                Text(info.shortLabel(strings))
                                            }
                                        }
                                        // Delta empty
                                        Td(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            val delta = mv.quantityEmpty
                                            if (delta != 0) {
                                                Span(Modifier.color(if (delta > 0) Color.rgb(0x2563EB) else Color.rgb(0xDC2626)).toAttrs()) {
                                                    Text(if (delta > 0) "+$delta" else "$delta")
                                                }
                                            } else {
                                                Span(Modifier.color(palette.textMuted).toAttrs()) { Text("—") }
                                            }
                                        }
                                        // Delta full
                                        Td(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).fontWeight(FontWeight.Bold).toAttrs()) {
                                            val delta = mv.quantityFull
                                            if (delta != 0) {
                                                Span(Modifier.color(if (delta > 0) Color.rgb(0x16A34A) else Color.rgb(0xDC2626)).toAttrs()) {
                                                    Text(if (delta > 0) "+$delta" else "$delta")
                                                }
                                            } else {
                                                Span(Modifier.color(palette.textMuted).toAttrs()) { Text("—") }
                                            }
                                        }
                                        // Contact
                                        Td(Modifier.padding(0.65.cssRem).color(palette.textMuted).toAttrs()) {
                                            Text(mv.contactName.ifBlank { "—" })
                                        }
                                        // Notes
                                        Td(Modifier.padding(0.65.cssRem).color(palette.textMuted).fontSize(0.8.cssRem).toAttrs()) {
                                            Text(mv.notes.ifBlank { "—" })
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

    // ── Modal: Nouveau Mouvement ──────────────────────────────────────────
    if (showMovementModal) {
        PackagingMovementModal(
            isOpen = true,
            initialPackagingId = movementTargetPkg?.id,
            onClose = { showMovementModal = false; movementTargetPkg = null }
        )
    }

    // ── Modal: Ajouter Emballage ──────────────────────────────────────────
    if (showAddModal) {
        PackagingFormModal(
            title = strings.modalAddPkgTitle,
            initial = null,
            isOpen = true,
            onClose = { showAddModal = false },
            onSave = { name, crateType, emptyCrates, fullCrates, deposit ->
                StockRepository.addPackaging(name, crateType, emptyCrates, fullCrates, deposit)
                showAddModal = false
            }
        )
    }

    // ── Modal: Modifier Emballage ──────────────────────────────────────────
    packagingToEdit?.let { pkg ->
        PackagingFormModal(
            title = "${strings.modalEditPkgTitle} : ${pkg.name}",
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

    // ── Modal: Supprimer Emballage ─────────────────────────────────────────
    packagingToDelete?.let { pkg ->
        ModalDialog(
            title = strings.modalDeletePkgTitle,
            isOpen = true,
            onClose = { packagingToDelete = null }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Span(Modifier.toAttrs()) {
                    Text(strings.confirmDeletePkgMsg)
                }
                Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Spacer()
                    AppSecondaryButton(text = strings.cancel, onClick = { packagingToDelete = null })
                    AppButton(
                        text = strings.delete,
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

// ─── Packaging Movement Modal ──────────────────────────────────────────────

@Composable
fun PackagingMovementModal(
    isOpen: Boolean,
    initialPackagingId: String?,
    onClose: () -> Unit
) {
    val strings = LocalLanguage.current.strings()
    val packagingList = StockRepository.packaging
    var selectedPkgId by remember { mutableStateOf(initialPackagingId ?: packagingList.firstOrNull()?.id ?: "") }
    var movementType by remember { mutableStateOf("RECEPTION_PLEIN") }
    var quantity by remember { mutableStateOf("1") }
    var contactName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectedPkg = packagingList.find { it.id == selectedPkgId }
    val info = movementTypeInfo(movementType)
    val qty = quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val deltaEmpty = info.deltaEmptySign * qty
    val deltaFull = info.deltaFullSign * qty

    ModalDialog(title = strings.modalMovTitle, isOpen = isOpen, onClose = onClose, maxWidth = 34.cssRem) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {

            // Packaging selector
            FormField(label = strings.fieldMovTargetPkg) {
                Select(
                    Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                        onChange { selectedPkgId = it.value ?: "" }
                    }
                ) {
                    packagingList.forEach { pkg ->
                        Option(pkg.id, attrs = { if (pkg.id == selectedPkgId) attr("selected", "") }) {
                            Text("${pkg.name} (${pkg.emptyCrates}V / ${pkg.fullCrates}P)")
                        }
                    }
                }
            }

            // Movement type selector
            FormField(label = strings.fieldMovType) {
                Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                    packagingMovementTypes.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                            pair.forEach { typeInfo ->
                                val isSelected = movementType == typeInfo.key
                                Button(
                                    onClick = { movementType = typeInfo.key },
                                    Modifier
                                        .weight(1f)
                                        .fontSize(0.78.cssRem)
                                        .padding(leftRight = 0.5.cssRem, topBottom = 0.4.cssRem)
                                        .borderRadius(8.px)
                                        .border(1.5.px, LineStyle.Solid, if (isSelected) typeInfo.color else Color.rgb(0xE2E8F0))
                                        .backgroundColor(if (isSelected) typeInfo.color.toRgb().copyf(alpha = 0.12f) else Colors.Transparent)
                                        .color(if (isSelected) typeInfo.color else ColorMode.current.toSitePalette().textMuted)
                                        .fontWeight(if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                ) {
                                    Text(typeInfo.label(strings))
                                }
                            }
                        }
                    }
                }
            }

            // Quantity
            FormField(label = strings.fieldMovQuantity) {
                AppTextInput(
                    value = quantity,
                    onValueChange = { quantity = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Contact
            FormField(label = strings.fieldMovActor) {
                AppTextInput(
                    value = contactName,
                    onValueChange = { contactName = it },
                    placeholderText = "Ex: Bar Le Palmier, Boissons du Cameroun...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes
            FormField(label = strings.fieldMovMotif) {
                AppTextInput(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholderText = "Ex: BL N°12345, camion #4...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Preview
            if (selectedPkg != null && qty > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .backgroundColor(info.color.toRgb().copyf(alpha = 0.08f))
                        .border(1.px, LineStyle.Solid, info.color.toRgb().copyf(alpha = 0.3f))
                        .borderRadius(10.px)
                        .padding(0.75.cssRem)
                ) {
                    Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                        Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Bold).color(info.color).toAttrs()) {
                            Text("${selectedPkg.name} — ${info.shortLabel(strings)}")
                        }
                        Row(Modifier.fillMaxWidth().gap(1.5.cssRem)) {
                            Column(Modifier.gap(0.2.cssRem)) {
                                Span(Modifier.fontSize(0.75.cssRem).color(ColorMode.current.toSitePalette().textMuted).toAttrs()) { Text(strings.pkgEmptyCrates) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.3.cssRem)) {
                                    Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                        Text("${selectedPkg.emptyCrates}")
                                    }
                                    Span(Modifier.color(ColorMode.current.toSitePalette().textMuted).fontSize(0.8.cssRem).toAttrs()) { Text("→") }
                                    Span(
                                        Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold)
                                            .color(if (deltaEmpty > 0) Color.rgb(0x2563EB) else if (deltaEmpty < 0) Color.rgb(0xDC2626) else ColorMode.current.toSitePalette().textMuted)
                                            .toAttrs()
                                    ) {
                                        Text("${(selectedPkg.emptyCrates + deltaEmpty).coerceAtLeast(0)}")
                                    }
                                    if (deltaEmpty != 0) {
                                        Span(Modifier.fontSize(0.75.cssRem).color(if (deltaEmpty > 0) Color.rgb(0x2563EB) else Color.rgb(0xDC2626)).toAttrs()) {
                                            Text(if (deltaEmpty > 0) "(+$deltaEmpty)" else "($deltaEmpty)")
                                        }
                                    }
                                }
                            }
                            Column(Modifier.gap(0.2.cssRem)) {
                                Span(Modifier.fontSize(0.75.cssRem).color(ColorMode.current.toSitePalette().textMuted).toAttrs()) { Text(strings.pkgFullCrates) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.3.cssRem)) {
                                    Span(Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                        Text("${selectedPkg.fullCrates}")
                                    }
                                    Span(Modifier.color(ColorMode.current.toSitePalette().textMuted).fontSize(0.8.cssRem).toAttrs()) { Text("→") }
                                    Span(
                                        Modifier.fontSize(0.9.cssRem).fontWeight(FontWeight.Bold)
                                            .color(if (deltaFull > 0) Color.rgb(0x16A34A) else if (deltaFull < 0) Color.rgb(0xDC2626) else ColorMode.current.toSitePalette().textMuted)
                                            .toAttrs()
                                    ) {
                                        Text("${(selectedPkg.fullCrates + deltaFull).coerceAtLeast(0)}")
                                    }
                                    if (deltaFull != 0) {
                                        Span(Modifier.fontSize(0.75.cssRem).color(if (deltaFull > 0) Color.rgb(0x16A34A) else Color.rgb(0xDC2626)).toAttrs()) {
                                            Text(if (deltaFull > 0) "(+$deltaFull)" else "($deltaFull)")
                                        }
                                    }
                                }
                            }
                        }
                        Span(Modifier.fontSize(0.75.cssRem).color(ColorMode.current.toSitePalette().textMuted).toAttrs()) {
                            Text(info.description(strings))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnConfirmMov,
                    onClick = {
                        if (selectedPkgId.isNotBlank() && qty > 0) {
                            StockRepository.recordPackagingMovement(
                                packagingId = selectedPkgId,
                                movementType = movementType,
                                quantityEmpty = deltaEmpty,
                                quantityFull = deltaFull,
                                notes = notes,
                                contactName = contactName
                            )
                            onClose()
                        }
                    },
                    backgroundColor = info.color
                )
            }
        }
    }
}

// ─── Packaging Form Modal (Add / Edit) ────────────────────────────────────

@Composable
fun PackagingFormModal(
    title: String,
    initial: Packaging?,
    isOpen: Boolean,
    onClose: () -> Unit,
    onSave: (name: String, crateType: String, emptyCrates: Int, fullCrates: Int, depositValue: Double) -> Unit
) {
    val strings = LocalLanguage.current.strings()
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var crateType by remember { mutableStateOf(initial?.crateType ?: "65cl") }
    var emptyCrates by remember { mutableStateOf(initial?.emptyCrates?.toString() ?: "0") }
    var fullCrates by remember { mutableStateOf(initial?.fullCrates?.toString() ?: "0") }
    var depositValue by remember { mutableStateOf(initial?.depositValuePerCrate?.toString() ?: "3000") }

    ModalDialog(title = title, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {

            FormField(label = strings.fieldPkgBrand) {
                AppTextInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholderText = "Ex: Casier 65cl (12 bouteilles)...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldPkgType) {
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
                    FormField(label = strings.fieldPkgInitialEmpty) {
                        AppTextInput(
                            value = emptyCrates,
                            onValueChange = { emptyCrates = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = strings.fieldPkgInitialFull) {
                        AppTextInput(
                            value = fullCrates,
                            onValueChange = { fullCrates = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            FormField(label = strings.fieldPkgDepositUnit) {
                AppTextInput(
                    value = depositValue,
                    onValueChange = { depositValue = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSavePkg,
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
