package com.nabdh.browser.ui.main

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nabdh.browser.R
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.StorageController

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("NabdhPrefs", Context.MODE_PRIVATE)

        // 1. استرجاع وعرض محرك البحث المحفوظ
        val currentEngine = prefs.getString("search_engine", "google")
        when (currentEngine) {
            "duckduckgo" -> findViewById<RadioButton>(R.id.rbDuckDuckGo).isChecked = true
            "bing" -> findViewById<RadioButton>(R.id.rbBing).isChecked = true
            else -> findViewById<RadioButton>(R.id.rbGoogle).isChecked = true
        }

        // حفظ التغيير عند اختيار محرك جديد
        findViewById<RadioGroup>(R.id.rgSearchEngine).setOnCheckedChangeListener { _, checkedId ->
            val engine = when (checkedId) {
                R.id.rbDuckDuckGo -> "duckduckgo"
                R.id.rbBing -> "bing"
                else -> "google"
            }
            prefs.edit().putString("search_engine", engine).apply()
        }

        // 4. إعداد الوضع الليلي
        val switchDarkMode = findViewById<android.widget.Switch>(R.id.switchDarkMode)
        
        // استرجاع الحالة المحفوظة
        val isDarkModeEnabled = prefs.getBoolean("force_dark_mode", false)
        switchDarkMode.isChecked = isDarkModeEnabled

        // حفظ التغيير
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("force_dark_mode", isChecked).apply()
            
            // تلميح للمستخدم
            if (isChecked) {
                Toast.makeText(this, "Dark Mode Forced! 🌑", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. زر تنظيف البيانات
        findViewById<Button>(R.id.btnClearData).setOnClickListener {
            // مسح بيانات المتصفح باستخدام GeckoRuntime
            // ملاحظة: getDefault قد يرجع null إذا لم يتم إنشاء Runtime مسبقاً،
            // لكن بما أننا أتينا من MainActivity، فهو موجود بالتأكيد.
            GeckoRuntime.getDefault(this)?.storageController?.clearData(
                StorageController.ClearFlags.ALL
            )
            
            Toast.makeText(this, "All data cleared! 🧹", Toast.LENGTH_SHORT).show()
        }

        // 3. زر الخروج
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish() // العودة للمتصفح
        }
    }
}
