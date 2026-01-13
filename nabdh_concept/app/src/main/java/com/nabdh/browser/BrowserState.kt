package com.nabdh.browser

data class BrowserState(
    val url: String = "https://duckduckgo.com",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val title: String = "Nabdh",
    val isSummarizing: Boolean = false,
    val summaryResult: String? = null,
    val showSummarySheet: Boolean = false
)
