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
import org.app.tgvstock.models.Product
import org.app.tgvstock.toSitePalette

@InitRoute
fun initProductsPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Gestion des Produits"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun ProductsPage() {
    val palette = ColorMode.current.toSitePalette()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }

    var showAddModal by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("Tous", "Bières", "Guinness", "Gazeuses", "Canettes", "Alcopops", "Eaux", "Non-Alcoolisé")
    val products = StockRepository.products

    val filteredProducts = products.filter { p ->
        val matchesCategory = (selectedCategory == "Tous") || (p.category == selectedCategory)
        val matchesSearch = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text("Catalogue & CRUD Produits")
                }
                Span(Modifier.fontSize(0.95.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("${products.size} produits référencés dans le dépôt. Ajoutez, modifiez ou ajustez les prix unitaires.")
                }
            }

            Spacer()

            AppButton(
                text = "Ajouter un Produit",
                onClick = { showAddModal = true },
                icon = { FaPlus() },
                backgroundColor = palette.brand.primary
            )
        }

        // Filters Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().gap(1.cssRem),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category buttons
                Row(Modifier.gap(0.5.cssRem).overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Button(
                            onClick = { selectedCategory = cat },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.4.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(cat)
                        }
                    }
                }

                Spacer()

                // Search input
                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = "Rechercher un produit...",
                    type = "search",
                    modifier = Modifier.width(16.cssRem)
                )
            }
        }

        // Products Table
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("NOM DU PRODUIT") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text("CATÉGORIE") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("BOUTEILLES / CASIER") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("PRIX UNITAIRE (PU)") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("STOCK INITIAL") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text("CONSIGNATION CASIER") }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text("ACTIONS") }
                            }
                        }
                        Tbody {
                            if (filteredProducts.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "7") }) {
                                        Text("Aucun produit trouvé.")
                                    }
                                }
                            } else {
                                filteredProducts.forEach { p ->
                                    Tr(Modifier.borderBottom(1.px, LineStyle.Solid, palette.cardBorder).toAttrs()) {
                                        Td(Modifier.padding(0.75.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.5.cssRem)) {
                                                Box(
                                                    Modifier
                                                        .size(1.8.cssRem)
                                                        .borderRadius(6.px)
                                                        .backgroundColor(palette.brand.primary.toRgb().copyf(alpha = 0.15f))
                                                        .color(palette.brand.primary)
                                                        .fontSize(0.85.cssRem),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FaWineBottle()
                                                }
                                                Text(p.name)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                                            Box(
                                                Modifier
                                                    .backgroundColor(palette.nearBackground)
                                                    .border(1.px, LineStyle.Solid, palette.cardBorder)
                                                    .borderRadius(10.px)
                                                    .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                    .fontSize(0.8.cssRem)
                                                    .display(DisplayStyle.InlineBlock)
                                            ) {
                                                Text(p.category)
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Text("${p.bottlesPerCrate} btls")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                            Text(formatFcfa(p.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Text("${p.stockInitial} casiers")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).color(palette.textMuted).toAttrs()) {
                                            Text(formatFcfa(p.consignationPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Row(
                                                Modifier.fillMaxWidth().gap(0.4.cssRem),
                                                horizontalArrangement = com.varabyte.kobweb.compose.foundation.layout.Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AppIconButton(
                                                    onClick = { productToEdit = p },
                                                    icon = { FaPenToSquare(Modifier.color(palette.brand.primary)) }
                                                )
                                                AppIconButton(
                                                    onClick = { productToDelete = p },
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

    // Modal: Ajouter Produit
    if (showAddModal) {
        ProductFormModal(
            title = "Ajouter un Nouveau Produit",
            initialProduct = null,
            isOpen = true,
            onClose = { showAddModal = false },
            onSave = { name, pu, bpc, cat, initStock, consignation ->
                StockRepository.addProduct(name, pu, bpc, cat, initStock, consignation)
                showAddModal = false
            }
        )
    }

    // Modal: Modifier Produit
    productToEdit?.let { p ->
        ProductFormModal(
            title = "Modifier le Produit : ${p.name}",
            initialProduct = p,
            isOpen = true,
            onClose = { productToEdit = null },
            onSave = { name, pu, bpc, cat, initStock, consignation ->
                p.name = name
                p.unitPrice = pu
                p.bottlesPerCrate = bpc
                p.category = cat
                p.stockInitial = initStock
                p.consignationPrice = consignation
                StockRepository.updateProduct(p)
                productToEdit = null
            }
        )
    }

    // Modal: Confirmer Suppression
    productToDelete?.let { p ->
        ModalDialog(
            title = "Supprimer le Produit ?",
            isOpen = true,
            onClose = { productToDelete = null }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Span(Modifier.toAttrs()) {
                    Text("Êtes-vous sûr de vouloir supprimer ${p.name} ? Cette action est irréversible.")
                }
                Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Spacer()
                    AppSecondaryButton(text = "Annuler", onClick = { productToDelete = null })
                    AppButton(
                        text = "Supprimer définitivement",
                        onClick = {
                            StockRepository.deleteProduct(p.id)
                            productToDelete = null
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
fun ProductFormModal(
    title: String,
    initialProduct: Product?,
    isOpen: Boolean,
    onClose: () -> Unit,
    onSave: (name: String, pu: Double, bottlesPerCrate: Int, category: String, stockInitial: Int, consignation: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var unitPrice by remember { mutableStateOf(initialProduct?.unitPrice?.toString() ?: "9500") }
    var bottlesPerCrate by remember { mutableStateOf(initialProduct?.bottlesPerCrate?.toString() ?: "12") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Bières") }
    var stockInitial by remember { mutableStateOf(initialProduct?.stockInitial?.toString() ?: "20") }
    var consignationPrice by remember { mutableStateOf(initialProduct?.consignationPrice?.toString() ?: "3000") }

    ModalDialog(title = title, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = "Nom du Produit") {
                AppTextInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholderText = "Ex: 33 EXPORT 65, P GUINNESS...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = "Prix Unitaire (PU) par Casier / Carton (FCFA)") {
                AppTextInput(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem)) {
                Box(Modifier.weight(1f)) {
                    FormField(label = "Bouteilles par Casier") {
                        AppTextInput(
                            value = bottlesPerCrate,
                            onValueChange = { bottlesPerCrate = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = "Catégorie") {
                        Select(
                            Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                                onChange { category = it.value ?: "Bières" }
                            }
                        ) {
                            listOf("Bières", "Guinness", "Gazeuses", "Canettes", "Alcopops", "Eaux", "Non-Alcoolisé").forEach { cat ->
                                Option(cat, attrs = { if (cat == category) attr("selected", "") }) { Text(cat) }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem)) {
                Box(Modifier.weight(1f)) {
                    FormField(label = "Stock Initial (Casiers)") {
                        AppTextInput(
                            value = stockInitial,
                            onValueChange = { stockInitial = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = "Prix Consignation Casier (FCFA)") {
                        AppTextInput(
                            value = consignationPrice,
                            onValueChange = { consignationPrice = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                Spacer()
                AppSecondaryButton(text = "Annuler", onClick = onClose)
                AppButton(
                    text = if (initialProduct == null) "Ajouter le Produit" else "Enregistrer les Modifications",
                    onClick = {
                        val puVal = unitPrice.toDoubleOrNull() ?: 0.0
                        val bpcVal = bottlesPerCrate.toIntOrNull() ?: 12
                        val initVal = stockInitial.toIntOrNull() ?: 0
                        val consVal = consignationPrice.toDoubleOrNull() ?: 3000.0

                        if (name.isNotBlank() && puVal > 0.0) {
                            onSave(name, puVal, bpcVal, category, initVal, consVal)
                        }
                    }
                )
            }
        }
    }
}
