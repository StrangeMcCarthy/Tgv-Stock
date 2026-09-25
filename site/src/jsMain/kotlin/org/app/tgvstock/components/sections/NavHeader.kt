package org.app.tgvstock.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.dom.ElementTarget
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.functions.clamp
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.CloseIcon
import com.varabyte.kobweb.silk.components.icons.HamburgerIcon
import com.varabyte.kobweb.silk.components.icons.MoonIcon
import com.varabyte.kobweb.silk.components.icons.SunIcon
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UncoloredLinkVariant
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.overlay.OverlayVars
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.animation.Keyframes
import com.varabyte.kobweb.silk.style.animation.toAnimation
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.app.tgvstock.components.widgets.IconButton
import org.app.tgvstock.toSitePalette

val NavHeaderStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .fillMaxWidth()
        .padding(leftRight = 1.5.cssRem, topBottom = 0.8.cssRem)
        .backgroundColor(palette.cardBackground)
        .borderBottom(1.px, LineStyle.Solid, palette.cardBorder)
        .boxShadow(0.px, 2.px, 12.px, 0.px, rgba(0, 0, 0, 0.05))
}

data class NavItem(val path: String, val label: String, val icon: @Composable () -> Unit)

val navItems = listOf(
    NavItem("/", "Dashboard", { FaChartLine(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/sales", "Ventes", { FaCashRegister(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/entries", "Arrivages", { FaTruckLoading(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/packaging", "Emballages", { FaBoxesStacked(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/spoiled", "Avaries", { FaWineGlass(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/debts", "Dettes", { FaHandHoldingDollar(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/expenses", "Dépenses", { FaReceipt(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/inventory", "Inventaire", { FaClipboardList(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/reports", "Rapports PDF", { FaFilePdf(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/products", "Produits", { FaBeerMugEmpty(Modifier.margin(right = 0.4.cssRem)) })
)

@Composable
private fun NavLinkItem(item: NavItem, isMobile: Boolean = false) {
    val ctx = rememberPageContext()
    val isCurrent = ctx.route.path == item.path
    val palette = ColorMode.current.toSitePalette()

    Link(
        item.path,
        variant = UndecoratedLinkVariant.then(UncoloredLinkVariant),
        modifier = Modifier
            .padding(leftRight = 0.75.cssRem, topBottom = 0.4.cssRem)
            .borderRadius(8.px)
            .fontWeight(if (isCurrent) FontWeight.Bold else FontWeight.Medium)
            .fontSize(if (isMobile) 1.05.cssRem else 0.9.cssRem)
            .backgroundColor(if (isCurrent) palette.brand.primary.toRgb().copyf(alpha = 0.15f) else Colors.Transparent)
            .color(if (isCurrent) palette.brand.primary else if (ColorMode.current.isDark) Color.rgb(0xCBD5E1) else Color.rgb(0x334155))
            .transition(com.varabyte.kobweb.compose.css.CSSTransition("all", 150.ms))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.icon()
            Text(item.label)
        }
    }
}

@Composable
private fun ColorModeButton() {
    var colorMode by ColorMode.currentState
    IconButton(onClick = { colorMode = colorMode.opposite }) {
        if (colorMode.isLight) MoonIcon() else SunIcon()
    }
    Tooltip(ElementTarget.PreviousSibling, "Changer de thème", placement = PopupPlacement.BottomRight)
}

@Composable
private fun HamburgerButton(onClick: () -> Unit) {
    IconButton(onClick) {
        HamburgerIcon()
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick) {
        CloseIcon()
    }
}

val SideMenuSlideInAnim = Keyframes {
    from { Modifier.translateX(100.percent) }
    to { Modifier }
}

enum class SideMenuState {
    CLOSED, OPEN, CLOSING;
    fun close() = when (this) {
        CLOSED -> CLOSED
        OPEN -> CLOSING
        CLOSING -> CLOSING
    }
}

@Composable
fun NavHeader() {
    val palette = ColorMode.current.toSitePalette()

    Row(
        NavHeaderStyle.toModifier(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Logo & Title
        Link("/", variant = UndecoratedLinkVariant.then(UncoloredLinkVariant)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.6.cssRem)) {
                Box(
                    modifier = Modifier
                        .size(2.4.cssRem)
                        .borderRadius(8.px)
                        .backgroundColor(palette.brand.primary)
                        .color(Colors.Black)
                        .fontSize(1.2.cssRem),
                    contentAlignment = Alignment.Center
                ) {
                    FaBeerMugEmpty()
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.3.cssRem)) {
                        Span({
                            style {
                                fontWeight("800")
                                fontSize(1.2.cssRem)
                                color(if (ColorMode.current.isDark) "white" else "#0f172a")
                                letterSpacing("0.5px")
                            }
                        }) {
                            Text("TGV")
                        }
                        Span({
                            style {
                                fontWeight("800")
                                fontSize(1.2.cssRem)
                                color(palette.brand.primary.toString())
                            }
                        }) {
                            Text("STOCK")
                        }
                    }
                    Span({
                        style {
                            fontSize(0.7.cssRem)
                            color(palette.textMuted.toString())
                            letterSpacing("0.4px")
                        }
                    }) {
                        Text("Gestion de Dépôt")
                    }
                }
            }
        }

        Spacer()

        // Desktop Navigation Links
        Row(
            Modifier.gap(0.2.cssRem).displayIfAtLeast(Breakpoint.LG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                NavLinkItem(item)
            }
            ColorModeButton()
        }

        // Mobile / Tablet Navigation Button
        Row(
            Modifier.gap(0.75.cssRem).displayUntil(Breakpoint.LG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var menuState by remember { mutableStateOf(SideMenuState.CLOSED) }

            ColorModeButton()
            HamburgerButton(onClick = { menuState = SideMenuState.OPEN })

            if (menuState != SideMenuState.CLOSED) {
                SideMenu(
                    menuState,
                    close = { menuState = menuState.close() },
                    onAnimationEnd = { if (menuState == SideMenuState.CLOSING) menuState = SideMenuState.CLOSED }
                )
            }
        }
    }
}

@Composable
private fun SideMenu(menuState: SideMenuState, close: () -> Unit, onAnimationEnd: () -> Unit) {
    val palette = ColorMode.current.toSitePalette()

    Overlay(
        Modifier
            .setVariable(OverlayVars.BackgroundColor, rgba(0, 0, 0, 0.5))
            .onClick { close() }
    ) {
        key(menuState) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(clamp(16.cssRem, 75.percent, 22.cssRem))
                    .align(Alignment.CenterEnd)
                    .padding(1.5.cssRem)
                    .gap(1.2.cssRem)
                    .backgroundColor(palette.cardBackground)
                    .borderLeft(1.px, LineStyle.Solid, palette.cardBorder)
                    .boxShadow((-4).px, 0.px, 20.px, 0.px, rgba(0, 0, 0, 0.25))
                    .animation(
                        SideMenuSlideInAnim.toAnimation(
                            duration = 200.ms,
                            timingFunction = if (menuState == SideMenuState.OPEN) AnimationTimingFunction.EaseOut else AnimationTimingFunction.EaseIn,
                            direction = if (menuState == SideMenuState.OPEN) AnimationDirection.Normal else AnimationDirection.Reverse,
                            fillMode = AnimationFillMode.Forwards
                        )
                    )
                    .onClick { it.stopPropagation() }
                    .onAnimationEnd { onAnimationEnd() },
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Span({
                        style {
                            fontWeight("bold")
                            fontSize(1.1.cssRem)
                            color(palette.brand.primary.toString())
                        }
                    }) {
                        Text("Menu TGV Stock")
                    }
                    Spacer()
                    CloseButton(onClick = { close() })
                }

                Column(
                    Modifier.fillMaxWidth().gap(0.5.cssRem),
                    horizontalAlignment = Alignment.Start
                ) {
                    navItems.forEach { item ->
                        Box(Modifier.fillMaxWidth().onClick { close() }) {
                            NavLinkItem(item, isMobile = true)
                        }
                    }
                }
            }
        }
    }
}
