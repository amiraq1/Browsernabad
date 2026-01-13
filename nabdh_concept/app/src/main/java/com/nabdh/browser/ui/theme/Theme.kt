package com.nabdh.browser.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// تعريف مخطط الألوان الداكن فقط
private val DarkColorScheme = darkColorScheme(
    primary = NabdhPulseRed,
    secondary = NabdhCyan,
    background = NabdhBlack,
    surface = NabdhSurface,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NabdhBrowserTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // جعل شريط الحالة يندمج مع الخلفية
            window.statusBarColor = NabdhBlack.toArgb()
            window.navigationBarColor = NabdhSurface.toArgb()
            
            // إجبار الأيقونات على أن تكون فاتحة
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NabdhTypography, // استخدام الطباعة المخصصة المعرفة سابقاً
        content = content
    )
}
