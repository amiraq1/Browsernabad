package com.nabdh.browser.ui.main

import android.app.Application
import android.content.Context
import android.net.TrafficStats
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentSession = MutableStateFlow<GeckoSession?>(null)
    val currentSession = _currentSession.asStateFlow()

    private val _pulseIntensity = MutableStateFlow(0f)
    val pulseIntensity = _pulseIntensity.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url

    // 1. أضف متغير حالة للوضع الشبحي
    private val _isGhostMode = MutableStateFlow(false)
    val isGhostMode = _isGhostMode.asStateFlow()

    // أضف متغيراً للتحكم في مانع الإعلانات (يمكن ربطه بزر لاحقاً)
    private val _isAdBlockEnabled = MutableStateFlow(true) // مفعل افتراضياً

    // متغير لتخزين حالة الوضع الليلي
    private var isForceDarkEnabled = false

    private var previousRxBytes = 0L

    init {
        initializeEngine()
        startHeartbeatMonitor()
    }

    // 2. دالة التبديل (يستدعيها الزر في الواجهة)
    fun toggleGhostMode() {
        val newMode = !_isGhostMode.value
        _isGhostMode.value = newMode
        initializeEngine(isGhostMode = newMode)
    }

    // دالة تبديل AdBlock
    fun toggleAdBlock(enable: Boolean) {
        _isAdBlockEnabled.value = enable
        // نعيد تهيئة المحرك لتطبيق الإعدادات الجديدة (أو نحدث الإعدادات الحالية لـ Session)
        // الأفضل هو تحديث الجلسة الحالية لتجنب إعادة التحميل، لكن للتبسيط:
        initializeEngine(isGhostMode = _isGhostMode.value)
    }

    // 3. تحديث دالة تهيئة المحرك
    private fun initializeEngine(isGhostMode: Boolean = false) {
        _currentSession.value?.close()
        val session = GeckoSession()

        // === تفويض إدارة المحتوى (AdBlock + Downloads) ===
        val delegate = com.nabdh.browser.core.NabdhContentDelegate(
            getApplication(), 
            isAdBlockEnabled = _isAdBlockEnabled.value
        )
        session.contentDelegate = delegate

        // === إعدادات حجب الإعلانات ===
        if (_isAdBlockEnabled.value) {
            session.settings.useTrackingProtection = true
            session.settings.allowJavascript = true
        } else {
            session.settings.useTrackingProtection = false
        }

        // === الوضع الليلي ===
        val prefs = getApplication<Application>().getSharedPreferences("NabdhPrefs", Context.MODE_PRIVATE)
        isForceDarkEnabled = prefs.getBoolean("force_dark_mode", false)

        if (isForceDarkEnabled) {
             // FORCE_DARK: يجبر الصفحات على التحول
             session.settings.displayMode = GeckoSessionSettings.DISPLAY_MODE_FORCE_DARK
        } else {
             session.settings.displayMode = GeckoSessionSettings.DISPLAY_MODE_NORMAL
        }
        
        // === إعدادات الوضع الشبحي ===
        session.settings.usePrivateMode = isGhostMode 
        
        if (isGhostMode) {
            session.settings.fullAccessibilityTree = false
            session.settings.useTrackingProtection = true 
        }

        session.open(null)
        _currentSession.value = session
        
        // إعادة تحميل الرابط الحالي
        if (_url.value.isNotEmpty()) {
            session.loadUri(_url.value)
        }
    }
    
    // دالة لتحديث الإعداد دون إعادة تشغيل التطبيق بالكامل
    fun refreshSettings() {
        initializeEngine(isGhostMode = _isGhostMode.value)
    }

    // === Search Suggestions ===
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()
    
    // لإلغاء طلبات البحث السابقة إذا كتب المستخدم بسرعة
    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChanged(query: String) {
        // إلغاء البحث السابق
        searchJob?.cancel()
        
        if (query.length < 2) {
            _suggestions.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce: انتظر 300ms قبل الطلب لتقليل استهلاك الشبكة
            val results = com.nabdh.browser.data.SearchRepository.getSuggestions(query)
            _suggestions.emit(results)
        }
    }

    fun loadUrl(inputUrl: String) {
        // تنظيف القائمة عند الانتقال
        _suggestions.value = emptyList()
        
        if (inputUrl.isEmpty()) {
            _url.value = ""
            // التحقق إذا كان الرابط فارغاً لتحميل الصفحة الرئيسية أو About:blank
            _currentSession.value?.loadUri("about:blank") 
            return
        }

        val target = if (inputUrl.contains("://")) {
             inputUrl
        } else if (inputUrl.startsWith("www.")) {
             "https://$inputUrl"
        } else if (inputUrl.contains(".") && !inputUrl.contains(" ")) {
             "https://$inputUrl"
        } else {
            // استرجاع محرك البحث المفضل من SharedPreferences باستخدام Application Context
            val prefs = getApplication<Application>().getSharedPreferences("NabdhPrefs", Context.MODE_PRIVATE)
            val engine = prefs.getString("search_engine", "google")
            
            when (engine) {
                "duckduckgo" -> "https://duckduckgo.com/?q=$inputUrl"
                "bing" -> "https://www.bing.com/search?q=$inputUrl"
                else -> "https://www.google.com/search?q=$inputUrl"
            }
        }
        
        _url.value = target
        _currentSession.value?.loadUri(target)
    }

    private fun startHeartbeatMonitor() {
        viewModelScope.launch {
            previousRxBytes = TrafficStats.getTotalRxBytes()
            
            while (true) {
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val delta = currentRxBytes - previousRxBytes
                previousRxBytes = currentRxBytes
                
                // Sensitivity calculation: 
                // Normalize chaotic network traffic into a smooth 0.0 - 1.0 curve
                // 50KB/s considered "High Activity" for visual flair
                val intensity = (delta / (50 * 1024f)).coerceIn(0f, 1f)
                
                _pulseIntensity.emit(intensity)

                delay(200) // 5Hz sampling rate
            }
        }
    }
}
