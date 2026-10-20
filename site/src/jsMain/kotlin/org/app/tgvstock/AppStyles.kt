package org.app.tgvstock

import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.modifyStyleBase
import org.jetbrains.compose.web.css.*

@InitSilk
fun initSiteStyles(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyle("html") {
        cssRule(CSSMediaQuery.MediaFeature("prefers-reduced-motion", StylePropertyValue("no-preference"))) {
            Modifier.scrollBehavior(ScrollBehavior.Smooth)
        }
    }

    ctx.stylesheet.registerStyleBase("body") {
        Modifier
            .fontFamily(
                "Inter", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
                "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", "sans-serif"
            )
            .fontSize(15.px)
            .lineHeight(1.5)
    }

    ctx.theme.modifyStyleBase(HorizontalDividerStyle) {
        Modifier.fillMaxWidth()
    }

    // Print optimization styling for official Depot Inventory sheets
    ctx.stylesheet.registerStyle("@media print") {
        // In print mode, hide navigation, footers, interactive action buttons
        cssRule(".no-print") {
            Modifier.display(DisplayStyle.None)
        }
        cssRule(".print-only") {
            Modifier.display(DisplayStyle.Block)
        }
        cssRule("body") {
            Modifier.backgroundColor(Colors.White).color(Colors.Black).fontSize(11.px)
        }
        cssRule(".print-table th, .print-table td") {
            Modifier.padding(4.px).border(1.px, LineStyle.Solid, Colors.Black)
        }
    }
}

val HeadlineTextStyle = CssStyle.base {
    Modifier
        .fontSize(2.4.cssRem)
        .fontWeight(700)
        .textAlign(TextAlign.Start)
        .lineHeight(1.2)
}

val SubheadlineTextStyle = CssStyle.base {
    Modifier
        .fontSize(1.05.cssRem)
        .textAlign(TextAlign.Start)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.75f))
}

val CircleButtonVariant = ButtonStyle.addVariantBase {
    Modifier.padding(0.px).borderRadius(50.percent)
}

val UncoloredButtonVariant = ButtonStyle.addVariantBase {
    Modifier.setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
}

val GlassCardStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .backgroundColor(palette.cardBackground)
        .border(1.px, LineStyle.Solid, palette.cardBorder)
        .borderRadius(14.px)
        .padding(1.25.cssRem)
        .boxShadow(0.px, 4.px, 20.px, 0.px, rgba(0, 0, 0, 0.06))
}

val ActionCardStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .backgroundColor(palette.cardBackground)
        .border(1.px, LineStyle.Solid, palette.cardBorder)
        .borderRadius(12.px)
        .padding(1.cssRem)
        .cursor(com.varabyte.kobweb.compose.css.Cursor.Pointer)
        .transition(
            com.varabyte.kobweb.compose.css.Transition.of("transform", 150.ms),
            com.varabyte.kobweb.compose.css.Transition.of("border-color", 150.ms),
            com.varabyte.kobweb.compose.css.Transition.of("box-shadow", 150.ms)
        )
}
