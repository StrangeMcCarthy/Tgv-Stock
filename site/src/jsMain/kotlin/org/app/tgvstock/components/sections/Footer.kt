package org.app.tgvstock.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Span
import org.app.tgvstock.toSitePalette

val FooterStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .backgroundColor(palette.nearBackground)
        .padding(topBottom = 1.25.cssRem, leftRight = 5.percent)
        .borderTop(1.px, LineStyle.Solid, palette.cardBorder)
}

@Composable
fun Footer(modifier: Modifier = Modifier) {
    val palette = ColorMode.current.toSitePalette()
    Box(FooterStyle.toModifier().then(modifier), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Span(Modifier.textAlign(TextAlign.Center).fontSize(0.85.cssRem).color(palette.textMuted).toAttrs()) {
                SpanText("© 2026 TGV Stock — Système Intégré de Gestion de Dépôt de Boissons (Ventes, Stocks, Emballages, Avaries & Dettes)")
            }
        }
    }
}
