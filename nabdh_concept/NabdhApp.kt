package com.nabdh.browser

import android.app.Application
import org.mozilla.geckoview.GeckoRuntime

class NabdhApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // تهيئة محرك Gecko عند بدء التطبيق
        // هذا يضمن أن المتصفح جاهز عند فتح الـ Activity
        GeckoRuntime.create(this)
    }
}
