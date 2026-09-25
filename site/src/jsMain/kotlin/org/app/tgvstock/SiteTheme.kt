package org.app.tgvstock

import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color

class SitePalette(
    val nearBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val inputBackground: Color,
    val textMuted: Color,
    val brand: Brand,
    val success: Color = Color.rgb(0x10B981),
    val danger: Color = Color.rgb(0xEF4444),
    val warning: Color = Color.rgb(0xF59E0B),
    val info: Color = Color.rgb(0x3B82F6)
) {
    class Brand(
        val primary: Color = Color.rgb(0xF59E0B),    // Amber gold
        val secondary: Color = Color.rgb(0xD97706),  // Deep amber
        val accent: Color = Color.rgb(0xFBBF24),     // Malt glow
    )
}

object SitePalettes {
    val light = SitePalette(
        nearBackground = Color.rgb(0xF8FAFC),
        cardBackground = Color.rgb(0xFFFFFF),
        cardBorder = Color.rgb(0xE2E8F0),
        inputBackground = Color.rgb(0xF1F5F9),
        textMuted = Color.rgb(0x64748B),
        brand = SitePalette.Brand(
            primary = Color.rgb(0xD97706),
            secondary = Color.rgb(0xB45309),
            accent = Color.rgb(0xF59E0B)
        )
    )
    val dark = SitePalette(
        nearBackground = Color.rgb(0x0B0F17),
        cardBackground = Color.rgb(0x131B2A),
        cardBorder = Color.rgb(0x1F2E47),
        inputBackground = Color.rgb(0x182235),
        textMuted = Color.rgb(0x94A3B8),
        brand = SitePalette.Brand(
            primary = Color.rgb(0xF59E0B),
            secondary = Color.rgb(0xD97706),
            accent = Color.rgb(0xFBBF24)
        )
    )
}

fun ColorMode.toSitePalette(): SitePalette {
    return when (this) {
        ColorMode.LIGHT -> SitePalettes.light
        ColorMode.DARK -> SitePalettes.dark
    }
}

@InitSilk
fun initTheme(ctx: InitSilkContext) {
    ctx.theme.palettes.light.background = Color.rgb(0xF1F5F9)
    ctx.theme.palettes.light.color = Color.rgb(0x0F172A)
    ctx.theme.palettes.dark.background = Color.rgb(0x070A0F)
    ctx.theme.palettes.dark.color = Color.rgb(0xF8FAFC)
}
