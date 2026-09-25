package org.app.tgvstock.components.widgets

import androidx.compose.runtime.Composable
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
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.icons.CloseIcon
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.overlay.OverlayVars
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.app.tgvstock.GlassCardStyle
import org.app.tgvstock.toSitePalette
import kotlin.math.roundToLong

fun formatFcfa(amount: Double): String {
    val rounded = amount.roundToLong()
    val isNegative = rounded < 0
    val absVal = if (isNegative) -rounded else rounded
    val str = absVal.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        sb.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append(' ')
        }
    }
    val formatted = sb.reverse().toString()
    return (if (isNegative) "-" else "") + formatted + " FCFA"
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val palette = ColorMode.current.toSitePalette()
    Box(
        GlassCardStyle.toModifier().then(modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                Span(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
                    Text(title)
                }
                Span(Modifier.fontSize(1.4.cssRem).fontWeight(FontWeight.Bold).toAttrs()) {
                    Text(value)
                }
                Span(Modifier.fontSize(0.75.cssRem).color(palette.textMuted).toAttrs()) {
                    Text(subtitle)
                }
            }
            Spacer()
            Box(
                Modifier
                    .size(3.cssRem)
                    .borderRadius(12.px)
                    .backgroundColor(accentColor.toRgb().copyf(alpha = 0.15f))
                    .color(accentColor)
                    .fontSize(1.4.cssRem),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val palette = ColorMode.current.toSitePalette()
    val bg = backgroundColor ?: palette.brand.primary
    val fg = textColor ?: if (bg == palette.brand.primary) Colors.Black else Colors.White

    Button(
        onClick = { onClick() },
        modifier = Modifier
            .setVariable(ButtonVars.BackgroundDefaultColor, bg)
            .setVariable(ButtonVars.Color, fg)
            .borderRadius(8.px)
            .padding(leftRight = 1.cssRem, topBottom = 0.5.cssRem)
            .fontWeight(FontWeight.SemiBold)
            .fontSize(0.9.cssRem)
            .boxShadow(0.px, 2.px, 6.px, 0.px, rgba(0, 0, 0, 0.1))
            .then(modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.gap(0.4.cssRem)) {
            if (icon != null) {
                icon()
            }
            Text(text)
        }
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = ColorMode.current.toSitePalette()
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .setVariable(ButtonVars.BackgroundDefaultColor, palette.nearBackground)
            .borderRadius(8.px)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .padding(leftRight = 0.9.cssRem, topBottom = 0.45.cssRem)
            .fontSize(0.85.cssRem)
            .then(modifier)
    ) {
        Text(text)
    }
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = ColorMode.current.toSitePalette()
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .setVariable(ButtonVars.BackgroundDefaultColor, palette.nearBackground)
            .borderRadius(6.px)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .padding(leftRight = 0.5.cssRem, topBottom = 0.3.cssRem)
            .fontSize(0.8.cssRem)
            .then(modifier)
    ) {
        icon()
    }
}

@Composable
fun AppTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String = "",
    type: String = "text",
    modifier: Modifier = Modifier
) {
    val palette = ColorMode.current.toSitePalette()
    val inputType = when (type) {
        "number" -> InputType.Number
        "date" -> InputType.Date
        "search" -> InputType.Search
        "tel" -> InputType.Tel
        else -> InputType.Text
    }

    org.jetbrains.compose.web.dom.Input(
        type = inputType,
        attrs = modifier
            .padding(leftRight = 0.75.cssRem, topBottom = 0.45.cssRem)
            .borderRadius(8.px)
            .border(1.px, LineStyle.Solid, palette.cardBorder)
            .backgroundColor(palette.inputBackground)
            .color(if (ColorMode.current.isDark) Colors.White else Colors.Black)
            .toAttrs {
                if (placeholderText.isNotBlank()) placeholder(placeholderText)
                value(value)
                onInput { onValueChange(it.value) }
            }
    )
}

@Composable
fun ModalDialog(
    title: String,
    isOpen: Boolean,
    onClose: () -> Unit,
    maxWidth: CSSNumericValue<out CSSUnit> = 32.cssRem,
    content: @Composable () -> Unit
) {
    if (!isOpen) return

    val palette = ColorMode.current.toSitePalette()

    Overlay(
        Modifier
            .setVariable(OverlayVars.BackgroundColor, rgba(0, 0, 0, 0.6))
            .onClick { onClose() }
    ) {
        Box(
            Modifier
                .width(maxWidth)
                .maxWidth(90.percent)
                .maxHeight(85.vh)
                .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                .backgroundColor(palette.cardBackground)
                .border(1.px, LineStyle.Solid, palette.cardBorder)
                .borderRadius(16.px)
                .padding(1.5.cssRem)
                .boxShadow(0.px, 12.px, 35.px, 0.px, rgba(0, 0, 0, 0.4))
                .align(Alignment.Center)
                .onClick { it.stopPropagation() }
        ) {
            Column(Modifier.fillMaxWidth().gap(1.2.cssRem)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Span(Modifier.fontSize(1.25.cssRem).fontWeight(FontWeight.Bold).color(palette.brand.primary).toAttrs()) {
                        Text(title)
                    }
                    Spacer()
                    Button(
                        onClick = { onClose() },
                        Modifier
                            .setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
                            .padding(0.px)
                            .borderRadius(50.percent)
                    ) {
                        CloseIcon()
                    }
                }
                content()
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    content: @Composable () -> Unit
) {
    val palette = ColorMode.current.toSitePalette()
    Column(Modifier.fillMaxWidth().gap(0.3.cssRem)) {
        Label(Modifier.fontSize(0.85.cssRem).fontWeight(FontWeight.Medium).color(palette.textMuted).toAttrs()) {
            Text(label)
        }
        content()
    }
}
