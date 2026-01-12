package com.nabdh.browser

import android.app.Application

/**
 * تطبيق نبض - متصفح عربي خفيف وسريع
 * 
 * نقطة الدخول الرئيسية للتطبيق.
 * يُستخدم لتهيئة أي موارد عامة مطلوبة.
 */
class NabdhApp : Application() {
    
    companion object {
        const val TAG = "NabdhBrowser"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // تهيئة الموارد العامة عند بدء التطبيق
    }
}
