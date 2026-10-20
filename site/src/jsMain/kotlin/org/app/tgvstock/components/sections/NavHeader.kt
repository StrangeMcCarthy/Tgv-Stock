package org.app.tgvstock.components.sections

import androidx.compose.runtime.*
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
import com.varabyte.kobweb.compose.ui.toAttrs
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
import org.app.tgvstock.AppLanguage
import org.app.tgvstock.LocalLanguage
import org.app.tgvstock.appLanguage
import org.app.tgvstock.components.widgets.IconButton
import org.app.tgvstock.strings
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

data class NavItem(val path: String, val labelKey: (org.app.tgvstock.Strings) -> String, val icon: @Composable () -> Unit)

val navItems = listOf(
    NavItem("/", { it.navDashboard }, { FaChartLine(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/sales", { it.navSales }, { FaCashRegister(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/entries", { it.navEntries }, { FaTruck(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/packaging", { it.navPackaging }, { FaBoxesStacked(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/spoiled", { it.navSpoiled }, { FaWineGlass(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/debts", { it.navDebts }, { FaHandHoldingDollar(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/expenses", { it.navExpenses }, { FaReceipt(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/inventory", { it.navInventory }, { FaClipboardList(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/reports", { it.navReports }, { FaFilePdf(Modifier.margin(right = 0.4.cssRem)) }),
    NavItem("/products", { it.navProducts }, { FaBeerMugEmpty(Modifier.margin(right = 0.4.cssRem)) })
)

@Composable
private fun NavLinkItem(item: NavItem, isMobile: Boolean = false) {
    val ctx = rememberPageContext()
    val isCurrent = ctx.route.path == item.path
    val palette = ColorMode.current.toSitePalette()
    val strings = LocalLanguage.current.strings()

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
            .transition(com.varabyte.kobweb.compose.css.Transition.of("all", 150.ms))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.icon()
            Text(item.labelKey(strings))
        }
    }
}

@Composable
private fun ColorModeButton() {
    var colorMode by ColorMode.currentState
    IconButton(onClick = { colorMode = colorMode.opposite }) {
        if (colorMode.isLight) MoonIcon() else SunIcon()
    }
}

@Composable
private fun LanguageToggleButton() {
    var language by appLanguage
    val current = LocalLanguage.current
    val next = if (current == AppLanguage.FRENCH) AppLanguage.ENGLISH else AppLanguage.FRENCH
    val palette = ColorMode.current.toSitePalette()

    Box(
        modifier = Modifier
            .padding(leftRight = 0.5.cssRem, topBottom = 0.3.cssRem)
            .borderRadius(20.px)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .backgroundColor(palette.nearBackground)
            .cursor(com.varabyte.kobweb.compose.css.Cursor.Pointer)
            .fontSize(0.82.cssRem)
            .fontWeight(FontWeight.SemiBold)
            .transition(com.varabyte.kobweb.compose.css.Transition.of("all", 150.ms))
            .onClick { language = next },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.3.cssRem)) {
            Span(Modifier.fontSize(1.0.cssRem).toAttrs()) { Text(next.flag) }
            Span(Modifier.toAttrs()) { Text(next.label) }
        }
    }
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
    val strings = LocalLanguage.current.strings()

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
                        Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.2.cssRem).color(if (ColorMode.current.isDark) Colors.White else Color.rgb(0x0f172a)).letterSpacing(0.5.px).toAttrs()) {
                            Text("TGV")
                        }
                        Span(Modifier.fontWeight(FontWeight.ExtraBold).fontSize(1.2.cssRem).color(palette.brand.primary).letterSpacing(0.5.px).toAttrs()) {
                            Text("STOCK")
                        }
                    }
                    Span(Modifier.fontSize(0.7.cssRem).color(palette.textMuted).letterSpacing(0.4.px).toAttrs()) {
                        Text(strings.navSubtitle)
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
            LanguageToggleButton()
            ColorModeButton()
        }

        // Mobile / Tablet Navigation Button
        Row(
            Modifier.gap(0.75.cssRem).displayUntil(Breakpoint.LG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var menuState by remember { mutableStateOf(SideMenuState.CLOSED) }

            LanguageToggleButton()
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
    val strings = LocalLanguage.current.strings()

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
                    Span(Modifier.fontWeight(FontWeight.Bold).fontSize(1.1.cssRem).color(palette.brand.primary).toAttrs()) {
                        Text(strings.navMenuTitle)
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
