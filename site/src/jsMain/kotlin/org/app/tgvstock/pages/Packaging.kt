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
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.models.Packaging
import org.app.tgvstock.toSitePalette

// ─── Movement type metadata ────────────────────────────────────────────────

data class MovementTypeInfo(
    val key: String,
    val label: String,
    val shortLabel: String,
    val deltaEmptySign: Int,  // +1 = increases empty, -1 = decreases, 0 = no effect
    val deltaFullSign: Int,
    val color: Color,
    val description: String
)

val packagingMovementTypes = listOf(
    MovementTypeInfo("RECEPTION_PLEIN",    "Réception casiers pleins",      "Réception",    0, +1, Color.rgb(0x16A34A), "Livraison de casiers pleins depuis la brasserie ou fournisseur"),
    MovementTypeInfo("RETOUR_VIDE_CLIENT", "Retour vides d'un client",      "Retour client",+1,  0, Color.rgb(0x2563EB), "Un client rapporte des casiers vides consignés"),
    MovementTypeInfo("RETOUR_BRASSERIE",   "Envoi vides à la brasserie",    "→ Brasserie",  -1,  0, Color.rgb(0x7C3AED), "Expédition de casiers vides à la brasserie pour remboursement de consignation"),
    MovementTypeInfo("VENTE_CASIER_VIDE",  "Vente casiers vides à un tiers","Vente vides",  -1,  0, Color.rgb(0xEA580C), "Vente directe de casiers vides à un client ou tiers"),
    MovementTypeInfo("CONVERSION",         "Conversion pleins → vides",     "Conversion",   +1, -1, Color.rgb(0x0D9488), "Casiers débouchés / distribués : plein devient vide"),
    MovementTypeInfo("AJUSTEMENT_VIDE",    "Ajustement manuel vides",       "Ajust. vides", +1,  0, Color.rgb(0x92400E), "Correction manuelle du comptage des casiers vides"),
    MovementTypeInfo("AJUSTEMENT_PLEIN",   "Ajustement manuel pleins",      "Ajust. pleins", 0, +1, Color.rgb(0x1D4ED8), "Correction manuelle du comptage des casiers pleins")
)

fun movementTypeInfo(key: String) = packagingMovementTypes.find { it.key == key }
    ?: MovementTypeInfo(key, key, key, 0, 0, Color.rgb(0x6B7280), "")

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
                    Text("Gestion des Emballages (Casiers & Bouteilles)")
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("Suivi complet du parc d'emballages : réceptions, retours clients, envois brasserie et consignations.")
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = "Nouveau Mouvement",
                    onClick = { movementTargetPkg = null; showMovementModal = true },
                    icon = { FaArrowRightArrowLeft() },
                    backgroundColor = Color.rgb(0x0D9488)
                )
                AppButton(
                    text = "Ajouter un Emballage",
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
                title = "Parc Total",
                value = "$totalCrates casiers",
                subtitle = "Casiers vides + casiers pleins",
                icon = { FaBoxesStacked() },
                accentColor = palette.success
            )

            StatCard(
                title = "Valeur Consignation",
                value = formatFcfa(totalDeposit),
                subtitle = "Capital immobilisé en emballages",
                icon = { FaCoins() },
                accentColor = Color.rgb(0xEAB308)
            )

            StatCard(
                title = "Mouvements Aujourd'hui",
                value = "$totalMovementsToday opérations",
                subtitle = "${movements.size} mouvements au total",
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
                            Text("Parc d'Emballages")
                        }
                        Span(Modifier.fontSize(0.82.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("Utilisez ± pour ajuster manuellement ou \"Mouvement\" pour un enregistrement tracé.")
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
                                Text("Afficher tous les mouvements")
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("LIBELLÉ EMBALLAGE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("FORMAT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("CASIERS VIDES") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("CASIERS PLEINS") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("TOTAL") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).toAttrs()) { Text("CONSIGNATION / CASIER") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.End).toAttrs()) { Text("VALEUR TOTALE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("MÀJ") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("ACTIONS") }
                            }
                        }
                        Tbody {
                            if (packagingList.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(TextAlign.Center).toAttrs { attr("colspan", "9") }) {
                                        Text("Aucun emballage enregistré.")
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
                                Text("Historique des Mouvements")
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
                                    Text("Filtré : $name")
                                }
                            }
                        }
                        Span(Modifier.fontSize(0.82.cssRem).color(palette.textMuted).toAttrs()) {
                            Text("${filteredMovements.size} mouvement(s) enregistré(s)${if (historyFilterId != null) " pour cet emballage" else " au total"}.")
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
                                Text("Aucun mouvement enregistré. Cliquez sur \"Nouveau Mouvement\" pour commencer.")
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
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("DATE") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("EMBALLAGE") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("TYPE DE MOUVEMENT") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("Δ VIDES") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Center).toAttrs()) { Text("Δ PLEINS") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("CONTACT") }
                                    Th(Modifier.padding(0.65.cssRem).textAlign(TextAlign.Start).toAttrs()) { Text("NOTES") }
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
                                                Text(info.shortLabel)
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

    // ── Modal: Modifier Emballage ──────────────────────────────────────────
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

    // ── Modal: Supprimer Emballage ─────────────────────────────────────────
    packagingToDelete?.let { pkg ->
        ModalDialog(
            title = "Supprimer cet Emballage ?",
            isOpen = true,
            onClose = { packagingToDelete = null }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Span(Modifier.toAttrs()) {
                    Text("Êtes-vous sûr de vouloir supprimer \"${pkg.name}\" et tout son historique de mouvements ?")
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

// ─── Packaging Movement Modal ──────────────────────────────────────────────

@Composable
fun PackagingMovementModal(
    isOpen: Boolean,
    initialPackagingId: String?,
    onClose: () -> Unit
) {
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

    ModalDialog(title = "Enregistrer un Mouvement d'Emballages", isOpen = isOpen, onClose = onClose, maxWidth = 34.cssRem) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {

            // Packaging selector
            FormField(label = "Emballage concerné") {
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
            FormField(label = "Type de mouvement") {
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
                                    Text(typeInfo.label)
                                }
                            }
                        }
                    }
                }
            }

            // Quantity
            FormField(label = "Quantité de casiers") {
                AppTextInput(
                    value = quantity,
                    onValueChange = { quantity = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Contact
            FormField(label = "Client / Fournisseur (optionnel)") {
                AppTextInput(
                    value = contactName,
                    onValueChange = { contactName = it },
                    placeholderText = "Ex: Bar Le Palmier, Boissons du Cameroun...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes
            FormField(label = "Notes / Observations (optionnel)") {
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
                            Text("Aperçu de l'impact — ${selectedPkg.name}")
                        }
                        Row(Modifier.fillMaxWidth().gap(1.5.cssRem)) {
                            Column(Modifier.gap(0.2.cssRem)) {
                                Span(Modifier.fontSize(0.75.cssRem).color(ColorMode.current.toSitePalette().textMuted).toAttrs()) { Text("Casiers Vides") }
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
                                Span(Modifier.fontSize(0.75.cssRem).color(ColorMode.current.toSitePalette().textMuted).toAttrs()) { Text("Casiers Pleins") }
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
                            Text(info.description)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = "Enregistrer le Mouvement",
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
