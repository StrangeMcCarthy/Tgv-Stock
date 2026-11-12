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
        cssRule(".no-print") {
            Modifier.display(DisplayStyle.None)
        }
        cssRule(".print-only") {
            Modifier.display(DisplayStyle.Block)
        }
        cssRule("body") {
            Modifier.backgroundColor(Colors.White).color(Colors.Black).fontSize(9.pt)
        }
        cssRule(".print-table th, .print-table td") {
            Modifier.padding(2.px).border(1.px, LineStyle.Solid, Colors.Black)
        }
    }

    // Direct @page and print stylesheet injection for browser PDF generation & printing
    js("""
        if (typeof document !== 'undefined' && !document.getElementById('tgv-pdf-print-rules')) {
            var styleEl = document.createElement('style');
            styleEl.id = 'tgv-pdf-print-rules';
            styleEl.textContent = `
                @page {
                    size: landscape;
                    margin: 6mm 8mm;
                }
                @media print {
                    html, body {
                        width: 100% !important;
                        min-width: 0 !important;
                        margin: 0 !important;
                        padding: 0 !important;
                        background: #ffffff !important;
                        color: #000000 !important;
                        font-size: 8pt !important;
                        -webkit-print-color-adjust: exact !important;
                        print-color-adjust: exact !important;
                    }
                    .no-print, header, nav, footer, .silk-navigation {
                        display: none !important;
                    }
                    *, *:before, *:after {
                        overflow: visible !important;
                        box-shadow: none !important;
                        text-shadow: none !important;
                    }
                    div[class*="PageContentStyle"], .print-sheet {
                        padding: 0 !important;
                        margin: 0 !important;
                        border: none !important;
                        box-shadow: none !important;
                        max-width: 100% !important;
                        width: 100% !important;
                    }
                    .print-table-container, .print-table-inner {
                        width: 100% !important;
                        min-width: 0 !important;
                        max-width: 100% !important;
                        overflow: visible !important;
                        display: block !important;
                    }
                    .print-recon-grid {
                        display: flex !important;
                        flex-direction: row !important;
                        gap: 12px !important;
                        width: 100% !important;
                    }
                    .print-recon-col {
                        flex: 1 1 50% !important;
                        max-width: 50% !important;
                    }
                    table.print-table {
                        width: 100% !important;
                        min-width: 0 !important;
                        max-width: 100% !important;
                        table-layout: auto !important;
                        border-collapse: collapse !important;
                        font-size: 7pt !important;
                        line-height: 1.15 !important;
                    }
                    table.print-table th, table.print-table td {
                        padding: 2.5px 3px !important;
                        border: 1px solid #000000 !important;
                        white-space: nowrap !important;
                    }
                    table.print-table th {
                        background-color: #f3f4f6 !important;
                        font-weight: 700 !important;
                        font-size: 7.2pt !important;
                        text-align: center !important;
                    }
                    table.print-table th:first-child, table.print-table td:first-child {
                        text-align: left !important;
                    }
                    table.print-table td.prod-name {
                        white-space: normal !important;
                        max-width: 130px !important;
                        word-break: break-word !important;
                        font-weight: 600 !important;
                    }
                    table.print-table thead {
                        display: table-header-group !important;
                    }
                    table.print-table tr {
                        page-break-inside: avoid !important;
                        break-inside: avoid !important;
                    }
                    .avoid-page-break {
                        page-break-inside: avoid !important;
                        break-inside: avoid !important;
                    }
                }
            `;
            document.head.appendChild(styleEl);
        }
    """)
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
