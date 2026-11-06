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
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.FilterBarStyle
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.HeaderActionsStyle
import org.app.tgvstock.HeadlineTextStyle
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.PageHeaderStyle
import org.app.tgvstock.components.layouts.PageLayoutData
import org.app.tgvstock.components.widgets.*
import org.app.tgvstock.data.StockRepository
import org.app.tgvstock.localizeCategory
import org.app.tgvstock.models.Product
import org.app.tgvstock.models.supportsHalfCrate
import org.app.tgvstock.strings
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
    val strings = LocalLanguage.current.strings()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }

    var showAddModal by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categories = listOf(
        "Tous" to strings.prodCatAll,
        "Bières" to strings.prodCatBeers,
        "Guinness" to strings.prodCatGuinness,
        "Gazeuses" to strings.prodCatSodas,
        "Canettes" to strings.prodCatCans,
        "Alcopops" to strings.prodCatAlcopops,
        "Eaux" to strings.prodCatWaters,
        "Non-Alcoolisé" to strings.prodCatNonAlcoholic
    )
    val products = StockRepository.products

    val filteredProducts = products.filter { p ->
        val matchesCategory = (selectedCategory == "Tous") || (p.category == selectedCategory)
        val matchesSearch = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(Modifier.fillMaxWidth().gap(1.8.cssRem)) {

        // Header
        Div(PageHeaderStyle.toAttrs()) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(HeadlineTextStyle.toModifier().toAttrs()) {
                    Text(strings.productsPageTitle)
                }
                Span(Modifier.fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                    Text("${products.size} ${strings.productsPageSubtitle}")
                }
            }

            Div(HeaderActionsStyle.toAttrs()) {
                AppButton(
                    text = strings.btnAddProduct,
                    onClick = { showAddModal = true },
                    icon = { FaPlus() },
                    backgroundColor = palette.brand.primary
                )
            }
        }

        // Filters Bar
        Box(GlassCardStyle.toModifier().fillMaxWidth()) {
            Div(FilterBarStyle.toAttrs()) {
                // Category buttons (horizontal touch scroll on mobile)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .gap(0.5.cssRem)
                        .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                        .styleModifier {
                            property("-webkit-overflow-scrolling", "touch")
                            property("white-space", "nowrap")
                        }
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        Button(
                            onClick = { selectedCategory = catKey },
                            Modifier
                                .fontSize(0.85.cssRem)
                                .padding(leftRight = 0.8.cssRem, topBottom = 0.4.cssRem)
                                .borderRadius(20.px)
                                .backgroundColor(if (isSelected) palette.brand.primary else palette.nearBackground)
                                .color(if (isSelected) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                .border(1.px, LineStyle.Solid, if (isSelected) palette.brand.primary else palette.cardBorder)
                        ) {
                            Text(catLabel)
                        }
                    }
                }

                // Search input
                AppTextInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = strings.placeholderSearchProducts,
                    type = "search",
                    modifier = Modifier.fillMaxWidth().maxWidth(18.cssRem)
                )
            }
        }

        // Products Table
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
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProdName) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start).toAttrs()) { Text(strings.colProdCategory) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colProdBottlesPerCrate) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.colProdUnitPrice) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colProdStock) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).toAttrs()) { Text(strings.pkgDepositValue) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.colProdHalfCrate) }
                                Th(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) { Text(strings.actions) }
                            }
                        }
                        Tbody {
                            if (filteredProducts.isEmpty()) {
                                Tr {
                                    Td(Modifier.padding(2.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs { attr("colspan", "8") }) {
                                        Text(strings.noProductsFound)
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
                                                Text(strings.localizeCategory(p.category))
                                            }
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Text("${p.bottlesPerCrate} ${strings.bottlesAbbr}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                                            Text(formatFcfa(p.unitPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            Text("${p.stockInitial} ${strings.crates}")
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.End).color(palette.textMuted).toAttrs()) {
                                            Text(formatFcfa(p.consignationPrice))
                                        }
                                        Td(Modifier.padding(0.75.cssRem).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center).toAttrs()) {
                                            if (p.supportsHalfCrate()) {
                                                Box(
                                                    Modifier
                                                        .backgroundColor(Color.rgb(0xDCFCE7))
                                                        .color(Color.rgb(0x166534))
                                                        .borderRadius(10.px)
                                                        .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                        .fontSize(0.78.cssRem)
                                                        .fontWeight(FontWeight.Bold)
                                                        .display(DisplayStyle.InlineBlock)
                                                ) {
                                                    Text("✓ ${strings.yes}")
                                                }
                                            } else {
                                                Box(
                                                    Modifier
                                                        .backgroundColor(palette.nearBackground)
                                                        .color(palette.textMuted)
                                                        .borderRadius(10.px)
                                                        .padding(leftRight = 0.5.cssRem, topBottom = 0.15.cssRem)
                                                        .fontSize(0.78.cssRem)
                                                        .display(DisplayStyle.InlineBlock)
                                                ) {
                                                    Text(strings.no)
                                                }
                                            }
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
            title = strings.modalAddProductTitle,
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
            title = "${strings.modalEditProductTitle} : ${p.name}",
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
            title = strings.modalDeleteProductTitle,
            isOpen = true,
            onClose = { productToDelete = null }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Span(Modifier.toAttrs()) {
                    Text("${strings.deleteProductConfirmMsg} (${p.name})")
                }
                Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Spacer()
                    AppSecondaryButton(text = strings.cancel, onClick = { productToDelete = null })
                    AppButton(
                        text = strings.delete,
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
    val strings = LocalLanguage.current.strings()
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var unitPrice by remember { mutableStateOf(initialProduct?.unitPrice?.toString() ?: "9500") }
    var bottlesPerCrate by remember { mutableStateOf(initialProduct?.bottlesPerCrate?.toString() ?: "12") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Bières") }
    var stockInitial by remember { mutableStateOf(initialProduct?.stockInitial?.toString() ?: "20") }
    var consignationPrice by remember { mutableStateOf(initialProduct?.consignationPrice?.toString() ?: "3000") }

    // Check if current category supports half-crate
    val tempProduct = Product("temp", name, unitPrice.toDoubleOrNull() ?: 0.0, bottlesPerCrate.toIntOrNull() ?: 12, category)
    val categorySupportsHalfCrate = tempProduct.supportsHalfCrate()

    val palette = ColorMode.current.toSitePalette()

    ModalDialog(title = title, isOpen = isOpen, onClose = onClose) {
        Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
            FormField(label = strings.fieldProdName) {
                AppTextInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholderText = "Ex: 33 EXPORT 65, P GUINNESS...",
                    type = "text",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormField(label = strings.fieldProdUnitPrice) {
                AppTextInput(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    type = "number",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem)) {
                Box(Modifier.weight(1f)) {
                    FormField(label = strings.fieldProdBottlesPerCrate) {
                        AppTextInput(
                            value = bottlesPerCrate,
                            onValueChange = { bottlesPerCrate = it },
                            type = "number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = strings.fieldProdCategory) {
                        Select(
                            Modifier.fillMaxWidth().padding(0.5.cssRem).borderRadius(8.px).toAttrs {
                                onChange { category = it.value ?: "Bières" }
                            }
                        ) {
                            listOf("Bières", "Guinness", "Gazeuses", "Canettes", "Alcopops", "Eaux", "Non-Alcoolisé").forEach { cat ->
                                Option(cat, attrs = { if (cat == category) attr("selected", "") }) { Text(strings.localizeCategory(cat)) }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().gap(0.75.cssRem)) {
                Box(Modifier.weight(1f)) {
                    FormField(label = if (categorySupportsHalfCrate) "${strings.fieldProdInitialStock} — ${strings.halfCrateOk} (0.5, 1.5...)" else strings.fieldProdInitialStock) {
                        Column(Modifier.fillMaxWidth().gap(0.4.cssRem)) {
                            org.jetbrains.compose.web.dom.Input(
                                type = org.jetbrains.compose.web.attributes.InputType.Number,
                                attrs = Modifier.fillMaxWidth()
                                    .padding(leftRight = 0.75.cssRem, topBottom = 0.45.cssRem)
                                    .borderRadius(8.px)
                                    .border(1.px, LineStyle.Solid, palette.cardBorder)
                                    .backgroundColor(palette.inputBackground)
                                    .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                    .toAttrs {
                                        value(stockInitial)
                                        attr("step", if (categorySupportsHalfCrate) "0.5" else "1")
                                        attr("min", "0")
                                        onInput { stockInitial = it.value?.toString() ?: "0" }
                                    }
                            )
                            if (categorySupportsHalfCrate) {
                                Row(Modifier.fillMaxWidth().gap(0.35.cssRem).flexWrap(FlexWrap.Wrap), verticalAlignment = Alignment.CenterVertically) {
                                    Span(Modifier.fontSize(0.75.cssRem).color(palette.brand.primary).fontWeight(FontWeight.SemiBold).toAttrs()) {
                                        Text(strings.labelHalfCrate)
                                    }
                                    listOf("0.5", "1", "1.5", "2", "3", "5", "10").forEach { preset ->
                                        Button(
                                            onClick = { stockInitial = preset },
                                            Modifier
                                                .fontSize(0.72.cssRem)
                                                .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                                .borderRadius(12.px)
                                                .backgroundColor(if (stockInitial == preset) palette.brand.primary else palette.nearBackground)
                                                .color(if (stockInitial == preset) Colors.Black else if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                                .border(1.px, LineStyle.Solid, palette.cardBorder)
                                        ) {
                                            Text("$preset ${strings.cratesAbbr}")
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            val cur = stockInitial.toDoubleOrNull() ?: 0.0
                                            stockInitial = (cur + 0.5).toString()
                                        },
                                        Modifier
                                            .fontSize(0.72.cssRem)
                                            .padding(leftRight = 0.45.cssRem, topBottom = 0.15.cssRem)
                                            .borderRadius(12.px)
                                            .backgroundColor(palette.nearBackground)
                                            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
                                            .border(1.px, LineStyle.Solid, palette.cardBorder)
                                    ) {
                                        Text(strings.btnPlusHalfCrate)
                                    }
                                }
                            }
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    FormField(label = strings.pkgDepositValue) {
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
                AppSecondaryButton(text = strings.cancel, onClick = onClose)
                AppButton(
                    text = strings.btnSaveProduct,
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
