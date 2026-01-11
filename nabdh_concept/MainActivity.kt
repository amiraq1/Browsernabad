package com.nabdh.browser.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nabdh.browser.R

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var etAddress: EditText
    private lateinit var btnGo: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. تعريف العناصر
        webView = findViewById(R.id.webView)
        etAddress = findViewById(R.id.etAddress)
        btnGo = findViewById(R.id.btnGo)

        // 2. إعدادات المتصفح (WebSettings)
        webView.settings.apply {
            javaScriptEnabled = true // تفعيل جافاسكريبت
            domStorageEnabled = true // ضروري للمواقع الحديثة
            builtInZoomControls = true
            displayZoomControls = false
        }

        // 3. منع فتح الروابط خارج التطبيق (WebViewClient)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                
                // السماح ببروتوكولات الويب العادية
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false // المتصفح يعالجها (False = لا تتدخل بالنظام)
                }
                
                // معالجة الروابط الخارجية (مثل tel:, mailto:, intent:)
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true // لقد عالجناها يدوياً
                } catch (e: Exception) {
                    return true // تجاهل الخطأ
                }
            }

            // تحديث شريط العنوان عند تغير الصفحة
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { if (!etAddress.isFocused) etAddress.setText(it) }
            }
        }

        // 4. معالجة شريط التقدم والعناوين
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = android.view.View.GONE
                } else {
                    progressBar.visibility = android.view.View.VISIBLE
                    progressBar.progress = newProgress
                }
            }
        }

        // 5. منطق شريط العنوان (جوجل أو رابط)
        val goAction = {
            val input = etAddress.text.toString().trim()
            if (input.isNotEmpty()) {
                val url = if (input.startsWith("http://") || input.startsWith("https://")) {
                    input
                } else if (input.contains(".")) {
                    "https://$input"
                } else {
                    "https://www.google.com/search?q=$input"
                }
                webView.loadUrl(url)
                webView.clearFocus() // إخفاء الكيبورد (تقريباً)
            }
        }

        btnGo.setOnClickListener { goAction() }

        etAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                goAction()
                true
            } else false
        }

        // 6. زر الرجوع (System Back Button)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // تحميل الصفحة الافتراضية
        webView.loadUrl("https://www.google.com")
    }
}
