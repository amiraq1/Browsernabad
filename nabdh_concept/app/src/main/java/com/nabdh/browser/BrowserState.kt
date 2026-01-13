package com.nabdh.browser

data class BrowserState(
    val url: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val title: String = ""
)
