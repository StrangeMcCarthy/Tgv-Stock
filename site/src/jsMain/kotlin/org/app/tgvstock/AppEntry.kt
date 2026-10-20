package org.app.tgvstock

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.scrollBehavior
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.loadFromLocalStorage
import com.varabyte.kobweb.silk.theme.colors.saveToLocalStorage
import com.varabyte.kobweb.silk.theme.colors.systemPreference
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.vh

private const val COLOR_MODE_KEY = "tgvstock:colorMode"
const val LANGUAGE_STORAGE_KEY = "tgvstock:language"

// Global language state — readable/writable from anywhere in the composition tree
val appLanguage: MutableState<AppLanguage> by lazy {
    val saved = localStorage.getItem(LANGUAGE_STORAGE_KEY)
    mutableStateOf(AppLanguage.entries.find { it.code == saved } ?: AppLanguage.FRENCH)
}

@InitSilk
fun initColorMode(ctx: InitSilkContext) {
    ctx.config.initialColorMode = ColorMode.loadFromLocalStorage(COLOR_MODE_KEY) ?: ColorMode.systemPreference
}

@InitSilk
fun initStyles(ctx: InitSilkContext) {
    js("""
        window.addEventListener('error', function(e) {
            var s = e.error && e.error.stack ? e.error.stack : (e.message || String(e));
            console.error('CRITICAL_ERR:', s);
            window.__lastCrash = s;
        });
        window.addEventListener('unhandledrejection', function(e) {
            var s = e.reason && e.reason.stack ? e.reason.stack : String(e.reason);
            console.error('CRITICAL_REJ:', s);
            window.__lastCrash = s;
        });
    """)
    ctx.stylesheet.apply {
        registerStyleBase("body") { Modifier.scrollBehavior(ScrollBehavior.Smooth) }
    }
}

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    var language by appLanguage

    LaunchedEffect(language) {
        localStorage.setItem(LANGUAGE_STORAGE_KEY, language.code)
    }

    CompositionLocalProvider(LocalLanguage provides language) {
        SilkApp {
            val colorMode = ColorMode.current
            LaunchedEffect(colorMode) {
                colorMode.saveToLocalStorage(COLOR_MODE_KEY)
            }
            Surface(SmoothColorStyle.toModifier().minHeight(100.vh)) {
                content()
            }
        }
    }
}
