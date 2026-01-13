package com.nabdh.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BrowserState())
    val uiState: StateFlow<BrowserState> = _uiState.asStateFlow()

    // محرك المتصفح والجلسة
    private val runtime = GeckoRuntime.create(application)
    val session = GeckoSession()

    init {
        session.open(runtime)
        setupSessionDelegates()
        loadUrl("https://duckduckgo.com") // الصفحة الافتراضية
    }

    private fun setupSessionDelegates() {
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                _uiState.update { it.copy(isLoading = true, url = url, progress = 0) }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                _uiState.update { it.copy(isLoading = false) }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _uiState.update { it.copy(progress = progress) }
            }
        }
    }

    fun loadUrl(url: String) {
        val finalUrl = if (url.contains(".") && !url.contains(" ")) {
            if (url.startsWith("http")) url else "https://$url"
        } else {
            "https://duckduckgo.com/?q=$url"
        }
        session.loadUri(finalUrl)
    }

    override fun onCleared() {
        super.onCleared()
        session.close()
    }
}
