package com.example.nabad  // تأكد أن هذا السطر يطابق ما هو موجود في ملفك الأصلي

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var goButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // هذا السطر هو الذي يربط الكود بملف التصميم الذي يحتوي على المتصفح
        setContentView(R.layout.activity_main)

        // تعريف العناصر من ملف التصميم
        webView = findViewById(R.id.webView)
        urlInput = findViewById(R.id.urlInput)
        goButton = findViewById(R.id.goButton)

        // إعدادات المتصفح الأساسية
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient() // لفتح الروابط داخل التطبيق

        // تحميل صفحة Google كبداية
        webView.loadUrl("https://www.google.com")

        // برمجة زر "اذهب"
        goButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) {
                if (url.startsWith("http")) {
                    webView.loadUrl(url)
                } else {
                    // إذا لم يكتب http نعتبره بحث في جوجل
                    webView.loadUrl("https://www.google.com/search?q=$url")
                }
            }
        }
    }

    // زر الرجوع في الهاتف يعود للصفحة السابقة في المتصفح
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}