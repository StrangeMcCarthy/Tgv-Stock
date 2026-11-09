package org.app.tgvstock.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.OverflowWrap
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobwebx.markdown.markdown
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.app.tgvstock.toSitePalette

val MarkdownStyle = CssStyle {
    base { Modifier.fillMaxSize() }

    cssRule("h1") {
        Modifier
            .fontWeight(400)
            .margin(bottom = 1.5.cssRem)
            .lineHeight(1.2)
            .styleModifier {
                property("font-size", "clamp(1.75rem, 5vw, 3rem)")
                property("overflow-wrap", "break-word")
            }
    }

    cssRule("h2") {
        Modifier
            .fontWeight(300)
            .margin(topBottom = 1.25.cssRem)
            .lineHeight(1.25)
            .styleModifier {
                property("font-size", "clamp(1.4rem, 4vw, 2.4rem)")
                property("overflow-wrap", "break-word")
            }
    }

    cssRule("h3") {
        Modifier
            .fontWeight(300)
            .margin(topBottom = 1.cssRem)
            .lineHeight(1.3)
            .styleModifier {
                property("font-size", "clamp(1.15rem, 3vw, 1.8rem)")
                property("overflow-wrap", "break-word")
            }
    }

    cssRule("h4") {
        Modifier
            .fontWeight(FontWeight.Bolder)
            .margin(top = 1.cssRem, bottom = 0.5.cssRem)
            .styleModifier {
                property("font-size", "clamp(0.95rem, 2vw, 1.2rem)")
                property("overflow-wrap", "break-word")
            }
    }

    cssRule("ul") {
        Modifier.fillMaxWidth().overflowWrap(OverflowWrap.BreakWord)
    }

    cssRule(" :is(li,ol,ul)") {
        Modifier.margin(bottom = 0.25.cssRem)
    }

    cssRule("code") {
        Modifier
            .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.8f))
            .fontWeight(FontWeight.Bolder)
    }

    cssRule("pre") {
        Modifier
            .margin(top = 0.5.cssRem, bottom = 2.cssRem)
            .fillMaxWidth()
    }
    cssRule("pre > code") {
        Modifier
            .display(DisplayStyle.Block)
            .fillMaxWidth()
            .backgroundColor(colorMode.toSitePalette().nearBackground)
            .border(1.px, LineStyle.Solid, colorMode.toPalette().color)
            .borderRadius(0.25.cssRem)
            .padding(0.5.cssRem)
            .fontSize(1.cssRem)
            .overflow { x(Overflow.Auto) }
    }
}

@InitRoute
fun initMarkdownLayout(ctx: InitRouteContext) {
    val title = ctx.markdown!!.frontMatter["title"]?.singleOrNull()
    require(title != null) { "Markdown file must set \"title\" in frontmatter" }

    ctx.data.add(PageLayoutData(title))
}

@Composable
@Layout(".components.layouts.PageLayout")
fun MarkdownLayout(content: @Composable () -> Unit) {
    Div(MarkdownStyle.toAttrs()) {
        content()
    }
}
