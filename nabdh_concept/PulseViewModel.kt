package com.nabdh.browser.ui.main

import android.net.TrafficStats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession

class PulseViewModel : ViewModel() {

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
        // إغلاق الجلسة السابقة لتنظيف الذاكرة
        _currentSession.value?.close()

        val session = GeckoSession()
        
        // === إعدادات حجب الإعلانات ===
        if (_isAdBlockEnabled.value) {
            // نستدعي المانع الذي أنشأناه ليتولى الحماية وحقن السكربت
            com.nabdh.browser.core.AdBlocker.enable(session)
            
            // السماح بالجافاسكريبت ضروري للمواقع الحديثة حتى مع الحجب
            session.settings.allowJavascript = true 
        } else {
            // تعطيل الحجب
            com.nabdh.browser.core.AdBlocker.disable(session)
        }

        // === إعدادات الوضع الشبحي ===
        session.settings.usePrivateMode = isGhostMode 
        
        if (isGhostMode) {
            session.settings.fullAccessibilityTree = false
            // في الوضع الشبحي، نزيد الحماية ونلغي حفظ الكوكيز
            session.settings.useTrackingProtection = true 
        }

        session.open(null)
        _currentSession.value = session
        
        // إعادة تحميل الرابط الحالي
        if (_url.value.isNotEmpty()) {
            session.loadUri(_url.value)
        }
    }

    fun loadUrl(inputUrl: String) {
        // Basic URL normalization
        val target = if (inputUrl.contains("://")) inputUrl else "https://$inputUrl"
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
