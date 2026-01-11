package com.nabdh.browser.ui.main

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nabdh.browser.R

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. تعريف WebView
        webView = findViewById(R.id.webView)

        // 2. إعدادات المتصفح (تفعيل JavaScript)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // ضروري للمواقع الحديثة مثل فيسبوك وتويتر

        // 3. ضمان فتح الروابط داخل التطبيق
        webView.webViewClient = object : WebViewClient() {
            // يمكننا ترك هذا فارغاً، فالافتراضي يفتح الروابط في نفس الـ WebView
            // ولكن وضعه يضمن عدم فتح كروم الخارجي
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: return false)
                return true
            }
        }

        // 4. معالجة زر الرجوع (Back Button)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // العودة للصفحة السابقة
                } else {
                    isEnabled = false // تعطيل هذا الـ Callback مؤقتاً
                    onBackPressedDispatcher.onBackPressed() // تنفيذ الخروج الافتراضي من التطبيق
                }
            }
        })

        // تحميل صفحة افتراضية (جوجل مثلاً)
        webView.loadUrl("https://www.google.com")
    }
}
