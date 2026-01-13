package com.nabdh.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BrowserState())
    val uiState = _uiState.asStateFlow()

    val runtime: GeckoRuntime by lazy { GeckoRuntime.create(application) }
    val session: GeckoSession by lazy { GeckoSession() }
    
    private var extension: WebExtension? = null

    init {
        session.open(runtime)
        session.loadUri(_uiState.value.url)
        setupDelegates()
        installExtractorExtension()
    }

    private fun installExtractorExtension() {
        viewModelScope.launch {
            // تثبيت الإضافة من مجلد assets
            // Note: Correcting path to match the actual created path
            runtime.webExtensionController
                .ensureBuiltIn("resource://android/assets/extensions/nabdh_extractor/", "nabdh-extractor@nabdh.com")
                .accept(
                    { ext -> extension = ext },
                    { e -> e.printStackTrace() }
                )
        }
    }

    private fun setupDelegates() {
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                _uiState.update { it.copy(isLoading = true, url = url, summaryResult = null, progress = 0) }
            }
            override fun onPageStop(session: GeckoSession, success: Boolean) {
                _uiState.update { it.copy(isLoading = false) }
            }
            override fun onSecurityChange(session: GeckoSession, securityInfo: GeckoSession.ProgressDelegate.SecurityInformation) {}
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _uiState.update { it.copy(progress = progress) }
            }
        }
        
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _uiState.update { it.copy(title = title ?: "Nabdh") }
            }
            override fun onFocusRequest(session: GeckoSession) {}
            override fun onCloseRequest(session: GeckoSession) {}
            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {}
            override fun onMetaViewportFitChange(session: GeckoSession, viewportFit: String) {}
            override fun onCrash(session: GeckoSession) {} 
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

    // تهيئة نموذج Gemini
    val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // 🔥 دالة الذكاء الاصطناعي
    fun summarizePage() {
        _uiState.update { it.copy(isSummarizing = true, showSummarySheet = true) }

        viewModelScope.launch {
            try {
                // النص المستخرج (حالياً نستخدم نصاً تجريبياً لأن الاتصال بالإضافة لم يتم تفعيله بالكامل بعد)
                // في الوضع الطبيعي سنستقبل النص من الإضافة هنا
                val demoText = "نبض هو متصفح عربي جديد يهدف لتقديم تجربة مستخدم فريدة وسريعة مع التركيز على الخصوصية والتصميم العصري. يعتمد المتصفح على محرك GeckoView القوي ويستخدم الذكاء الاصطناعي لتلخيص المحتوى."
                
                val pageText = _uiState.value.summaryResult ?: demoText
                
                val prompt = "لخص هذا المقال باللغة العربية في نقاط رئيسية:\n$pageText"

                // 2. الاتصال بـ Gemini
                val response = generativeModel.generateContent(prompt)
                
                _uiState.update { 
                    it.copy(
                        isSummarizing = false, 
                        summaryResult = response.text ?: "لم أتمكن من التلخيص"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSummarizing = false, 
                        summaryResult = "خطأ في الاتصال: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    fun closeSummary() {
        _uiState.update { it.copy(showSummarySheet = false) }
    }

    override fun onCleared() {
        super.onCleared()
        session.close()
    }
}
