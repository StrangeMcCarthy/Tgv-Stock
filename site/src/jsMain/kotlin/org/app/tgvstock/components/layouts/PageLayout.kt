package org.app.tgvstock.components.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.getValue
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toAttrs
import kotlinx.browser.document
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Div
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.strings
import org.app.tgvstock.components.sections.Footer
import org.app.tgvstock.components.sections.NavHeader
import org.app.tgvstock.data.StockRepository

val PageContentStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .padding(leftRight = 0.75.cssRem, top = 1.cssRem, bottom = 2.5.cssRem)
    }
    Breakpoint.MD {
        Modifier
            .maxWidth(88.cssRem)
            .padding(leftRight = 2.cssRem, top = 2.cssRem, bottom = 4.cssRem)
    }
}

class PageLayoutData(val title: String)

@Composable
@Layout
fun PageLayout(ctx: PageContext, content: @Composable ColumnScope.() -> Unit) {
    val data = ctx.data.getValue<PageLayoutData>()
    val strings = LocalLanguage.current.strings()

    LaunchedEffect(Unit) {
        StockRepository.init()
    }

    val pageTitle = when (ctx.route.path.trimEnd('/')) {
        "" -> strings.navDashboard
        "/sales" -> strings.navSales
        "/entries" -> strings.navEntries
        "/packaging" -> strings.navPackaging
        "/spoiled" -> strings.navSpoiled
        "/debts" -> strings.navDebts
        "/expenses" -> strings.navExpenses
        "/inventory" -> strings.navInventory
        "/reports" -> strings.navReports
        "/products" -> strings.navProducts
        else -> data.title
    }

    LaunchedEffect(pageTitle) {
        document.title = "TGV Stock - $pageTitle"
    }

    Box(
        Modifier
            .fillMaxWidth()
            .minHeight(100.vh)
            .gridTemplateRows { size(1.fr); size(minContent) },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.fillMaxSize().gridRow(1),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Div(Modifier.fillMaxWidth().classNames("no-print").toAttrs()) {
                NavHeader()
            }
            Div(PageContentStyle.toAttrs()) {
                content()
            }
        }
        Div(Modifier.fillMaxWidth().gridRow(2).classNames("no-print").toAttrs()) {
            Footer(Modifier.fillMaxWidth())
        }
    }
}
