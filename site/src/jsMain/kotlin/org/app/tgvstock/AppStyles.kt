package org.app.tgvstock

import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.compose.css.JustifyContent
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.modifyStyleBase
import org.jetbrains.compose.web.css.*

@InitSilk
fun initSiteStyles(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyle("html") {
        Modifier.styleModifier {
            property("font-size", "clamp(13px, 0.8rem + 0.35vw, 16px)")
            property("-webkit-text-size-adjust", "100%")
            property("text-size-adjust", "100%")
            property("text-rendering", "optimizeLegibility")
            property("-webkit-font-smoothing", "antialiased")
        }
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
            .lineHeight(1.5)
            .styleModifier {
                property("overflow-wrap", "break-word")
            }
    }

    ctx.stylesheet.registerStyle("h1, .h1") {
        Modifier.styleModifier {
            property("font-size", "clamp(1.35rem, 3.2vw, 2.1rem)")
            property("line-height", "1.25")
            property("overflow-wrap", "break-word")
        }
    }

    ctx.stylesheet.registerStyle("h2, .h2") {
        Modifier.styleModifier {
            property("font-size", "clamp(1.15rem, 2.5vw, 1.6rem)")
            property("line-height", "1.3")
            property("overflow-wrap", "break-word")
        }
    }

    ctx.stylesheet.registerStyle("h3, .h3") {
        Modifier.styleModifier {
            property("font-size", "clamp(1rem, 2vw, 1.25rem)")
            property("line-height", "1.35")
            property("overflow-wrap", "break-word")
        }
    }

    ctx.stylesheet.registerStyle("p") {
        Modifier.styleModifier {
            property("overflow-wrap", "break-word")
            property("line-height", "1.5")
        }
    }

    ctx.theme.modifyStyleBase(HorizontalDividerStyle) {
        Modifier.fillMaxWidth()
    }

    // Responsive table cells styling
    ctx.stylesheet.registerStyle("th") {
        Modifier.styleModifier {
            property("white-space", "nowrap")
            property("font-size", "clamp(0.75rem, 1.2vw, 0.85rem)")
            property("letter-spacing", "0.2px")
        }
    }

    ctx.stylesheet.registerStyle("td") {
        Modifier.styleModifier {
            property("font-size", "clamp(0.78rem, 1.3vw, 0.88rem)")
            property("line-height", "1.4")
        }
    }

    // Responsive form inputs and buttons
    ctx.stylesheet.registerStyle("input, select, textarea") {
        Modifier.styleModifier {
            property("font-family", "inherit")
            property("font-size", "clamp(0.82rem, 1.4vw, 0.92rem)")
        }
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

val HeadlineTextStyle = CssStyle {
    base {
        Modifier
            .fontWeight(700)
            .textAlign(TextAlign.Start)
            .lineHeight(1.22)
            .styleModifier {
                property("font-size", "clamp(1.25rem, 3.8vw, 2.1rem)")
                property("overflow-wrap", "break-word")
                property("word-break", "break-word")
            }
    }
    Breakpoint.MD {
        Modifier
            .lineHeight(1.2)
    }
}

val SubheadlineTextStyle = CssStyle {
    base {
        Modifier
            .textAlign(TextAlign.Start)
            .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.75f))
            .lineHeight(1.4)
            .styleModifier {
                property("font-size", "clamp(0.78rem, 1.6vw, 0.95rem)")
                property("overflow-wrap", "break-word")
                property("word-break", "break-word")
            }
    }
    Breakpoint.MD {
        Modifier
            .lineHeight(1.4)
    }
}

val SectionTitleStyle = CssStyle {
    base {
        Modifier
            .fontWeight(700)
            .textAlign(TextAlign.Start)
            .lineHeight(1.25)
            .styleModifier {
                property("font-size", "clamp(1.02rem, 2.4vw, 1.25rem)")
                property("overflow-wrap", "break-word")
                property("word-break", "break-word")
            }
    }
}

val SectionSubtitleStyle = CssStyle {
    base {
        val palette = colorMode.toSitePalette()
        Modifier
            .color(palette.textMuted)
            .lineHeight(1.35)
            .styleModifier {
                property("font-size", "clamp(0.75rem, 1.4vw, 0.85rem)")
                property("overflow-wrap", "break-word")
                property("word-break", "break-word")
            }
    }
}

val PageHeaderStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Flex)
            .flexDirection(FlexDirection.Column)
            .alignItems(AlignItems.FlexStart)
            .gap(0.85.cssRem)
    }
    Breakpoint.MD {
        Modifier
            .flexDirection(FlexDirection.Row)
            .alignItems(AlignItems.Center)
            .justifyContent(JustifyContent.SpaceBetween)
            .gap(1.5.cssRem)
    }
}

val HeaderActionsStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Flex)
            .flexWrap(FlexWrap.Wrap)
            .gap(0.5.cssRem)
    }
    Breakpoint.MD {
        Modifier
            .flexWrap(FlexWrap.Nowrap)
            .gap(0.75.cssRem)
    }
}

val FilterBarStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Flex)
            .flexDirection(FlexDirection.Column)
            .gap(0.75.cssRem)
    }
    Breakpoint.MD {
        Modifier
            .flexDirection(FlexDirection.Row)
            .alignItems(AlignItems.Center)
            .justifyContent(JustifyContent.SpaceBetween)
            .gap(1.cssRem)
    }
}

val FilterControlsGroupStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Flex)
            .flexWrap(FlexWrap.Wrap)
            .alignItems(AlignItems.Center)
            .gap(0.6.cssRem)
    }
    Breakpoint.MD {
        Modifier
            .flexWrap(FlexWrap.Nowrap)
            .gap(0.8.cssRem)
    }
}

val CircleButtonVariant = ButtonStyle.addVariantBase {
    Modifier.padding(0.px).borderRadius(50.percent)
}

val UncoloredButtonVariant = ButtonStyle.addVariantBase {
    Modifier.setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
}

val GlassCardStyle = CssStyle {
    base {
        val palette = colorMode.toSitePalette()
        Modifier
            .backgroundColor(palette.cardBackground)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .borderRadius(14.px)
            .padding(0.85.cssRem)
            .boxShadow(0.px, 4.px, 20.px, 0.px, rgba(0, 0, 0, 0.06))
    }
    Breakpoint.MD {
        Modifier.padding(1.25.cssRem)
    }
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
