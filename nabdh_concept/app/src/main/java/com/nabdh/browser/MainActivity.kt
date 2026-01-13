package com.nabdh.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nabdh.browser.ui.theme.NabdhBrowserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NabdhBrowserTheme {
                BrowserScreen()
            }
        }
    }
}
