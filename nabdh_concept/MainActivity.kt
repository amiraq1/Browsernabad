package com.nabdh.browser.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nabdh.browser.R
import com.nabdh.browser.ui.components.PulseIndicatorView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoRuntime

class MainActivity : AppCompatActivity() {

    // نستخدم ViewModel الذي أنشأته مسبقاً
    private val viewModel: PulseViewModel by viewModels()
    
    private lateinit var geckoView: GeckoView
    private lateinit var pulseView: PulseIndicatorView
    private lateinit var etAddress: EditText

    // نحتاج Runtime لكي يعمل المحرك
    private val geckoRuntime by lazy { GeckoRuntime.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // تعريف العناصر
        geckoView = findViewById(R.id.geckoView)
        pulseView = findViewById(R.id.pulseView)
        etAddress = findViewById(R.id.etAddress)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        setupAddressBar()
        setupObservers()
        
        // زر القائمة يفتح الإعدادات (مؤقتاً)
        btnMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // تحميل الصفحة الافتراضية
        if (savedInstanceState == null) {
            viewModel.loadUrl("https://www.google.com")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSettings()
    }

    private fun setupAddressBar() {
        // عند الضغط على زر Go في الكيبورد
        etAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                viewModel.loadUrl(etAddress.text.toString())
                etAddress.clearFocus()
                true
            } else false
        }

        // الاستماع لتغيير النص (لاقتراحات البحث لاحقاً)
        etAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etAddress.hasFocus()) {
                    viewModel.onQueryChanged(s.toString())
                }
            }
        })
    }

    private fun setupObservers() {
        // 1. ربط جلسة المتصفح (GeckoSession) بالواجهة (GeckoView)
        lifecycleScope.launch {
            // ملاحظة: collect تحتاج لاستيراد kotlinx.coroutines.flow.collect
            viewModel.currentSession.collect { session ->
                if (session != null) {
                    // مهم: يجب فتح الجلسة باستخدام Runtime
                    if (!session.isOpen) {
                        session.open(geckoRuntime)
                    }
                    geckoView.setSession(session)
                }
            }
        }

        // 2. تحديث الرابط في شريط العنوان
        lifecycleScope.launch {
            viewModel.url.collect { url ->
                if (!etAddress.hasFocus() && url.isNotEmpty()) {
                    etAddress.setText(url)
                }
            }
        }

        // 3. تحريك مؤشر النبض بناءً على البيانات
        lifecycleScope.launch {
            viewModel.pulseIntensity.collect { intensity ->
                // تحديث سرعة وقوة النبض بناءً على استهلاك الإنترنت
                pulseView.updateIntensity(intensity)
            }
        }
    }

    override fun onBackPressed() {
        // التعامل مع زر الرجوع داخل المتصفح
        val session = viewModel.currentSession.value
        if (session != null && session.canGoBack()) {
            session.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
