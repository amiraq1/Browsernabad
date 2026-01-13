package com.nabdh.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nabdh.browser.ui.theme.NabdhBrowserTheme
import com.nabdh.browser.ui.theme.NabdhBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NabdhBrowserTheme {
                // حاوية التطبيق الرئيسية
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NabdhBlack
                ) {
                    // هنا سنضع شاشة المتصفح لاحقاً
                    // حالياً نضع نصاً للتأكد من عمل الثيم
                    GreetingSetup()
                }
            }
        }
    }
}

@Composable
fun GreetingSetup() {
    androidx.compose.foundation.layout.Box(
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "Nabdh Browser Engine: READY")
    }
}
