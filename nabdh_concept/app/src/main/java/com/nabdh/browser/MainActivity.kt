package com.nabdh.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.nabdh.browser.ui.theme.NabdhTheme
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val runtime = GeckoRuntime.create(this)

        setContent {
            BrowserScreen(runtime)
        }
    }
}

@Composable
fun BrowserScreen(runtime: GeckoRuntime) {
    var urlText by remember { mutableStateOf("https://duckduckgo.com") }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    
    // Gecko Session State
    val session = remember { GeckoSession() }
    
    // Effect to open initial session
    DisposableEffect(runtime) {
        session.open(runtime)
        session.loadUri(urlText)
        
        val progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                isLoading = true
                progress = 0
            }
            override fun onPageStop(session: GeckoSession, success: Boolean) {
                isLoading = false
            }
            override fun onProgressChange(session: GeckoSession, progressInt: Int) {
                progress = progressInt
            }
        }
        
        session.progressDelegate = progressDelegate
        
        onDispose {
            session.close()
        }
    }

    NabdhTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Browser Engine View
                AndroidView(
                    factory = { context ->
                        GeckoView(context).apply {
                            setSession(session)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Minimalist Search/Address Bar (Floating)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                     MinimalSearchBar(
                         currentUrl = urlText,
                         onUrlChange = { urlText = it },
                         onGo = { 
                             val finalUrl = if (it.contains(".")) {
                                 if (it.startsWith("http")) it else "https://$it"
                             } else {
                                 "https://duckduckgo.com/?q=$it"
                             }
                             session.loadUri(finalUrl) 
                         },
                         isLoading = isLoading,
                         progress = progress
                     )
                }
            }
        }
    }
}

@Composable
fun MinimalSearchBar(
    currentUrl: String,
    onUrlChange: (String) -> Unit,
    onGo: (String) -> Unit,
    isLoading: Boolean,
    progress: Int
) {
    val accentColor = MaterialTheme.colorScheme.primary
    
    // Glassmorphism container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xCC111111), Color(0xF0000000))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(2.dp) // Border space
    ) {
        // Subtle Border Gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF333333), accentColor.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // Loading Indicator vs Search Icon
            Box(
                 modifier = Modifier.size(24.dp),
                 contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                   CircularProgressIndicator(
                       progress = { progress / 100f },
                       modifier = Modifier.size(20.dp),
                       color = accentColor,
                       trackColor = MaterialTheme.colorScheme.surfaceVariant,
                       strokeWidth = 2.dp,
                   )
                } else {
                    // Minimal decorative dot or icon
                    Box(modifier = Modifier
                        .size(8.dp)
                        .background(accentColor, RoundedCornerShape(50))
                    )
                }
            }

            // Input Field
            BasicTextField(
                value = currentUrl,
                onValueChange = onUrlChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                cursorBrush = SolidColor(accentColor),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onGo(currentUrl) }),
                decorationBox = { innerTextField ->
                     if (currentUrl.isEmpty()) {
                         androidx.compose.material3.Text(
                             "Enter URL or Search...",
                             style = MaterialTheme.typography.bodyMedium
                         )
                     }
                     innerTextField()
                }
            )
        }
    }
}
