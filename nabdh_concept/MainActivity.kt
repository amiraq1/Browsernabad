package com.nabdh.browser.ui.main

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.nabdh.browser.R
import com.nabdh.browser.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import org.mozilla.geckoview.GeckoRuntime

class MainActivity : AppCompatActivity(), BrowserMenuFragment.MenuListener {

    // Note: In a real project, ViewBinding is generated from XML.
    // Assuming ActivityMainBinding exists mapping to activity_main.xml
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PulseViewModel by viewModels()
    private val geckoRuntime by lazy { GeckoRuntime.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Avant-Garde Mode: Full Immersion
        // This makes the app draw behind status and navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEngine()
        setupUI()
        observePulse()
    }

    override fun onResume() {
        super.onResume()
        // إعادة تحميل الإعدادات عند العودة من شاشة Settings
        viewModel.refreshSettings() 
    }

    private fun setupEngine() {
        // Initialize the Mozilla Engine
        geckoRuntime = GeckoRuntime.create(this)
    }

    private fun setupUI() {
        // Handle "Go" action on the keyboard
        binding.bottomAddressBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                viewModel.loadUrl(v.text.toString())
                // Hide keyboard and focusing logic (omitted)
                binding.geckoView.requestFocus()
                true
            } else {
                false
            }
        }

        // عند الضغط على أيقونة القفل، يتم تفعيل الوضع الشبحي
        binding.ivSecurity.setOnClickListener {
            viewModel.toggleGhostMode()
            
            // تأثير اهتزاز بسيط (Haptic Feedback) ليشعر المستخدم بالتغيير
            it.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        }

        // زر درع AdBlock
        // الحالة الافتراضية: مفعل (مطابق للـ ViewModel)
        binding.btnShield.alpha = 1.0f
        binding.btnShield.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))

        binding.btnShield.setOnClickListener {
            // ... (نفس الكود السابق)
             val isActive = binding.btnShield.alpha == 1.0f
            if (isActive) {
                binding.btnShield.alpha = 0.3f
                binding.btnShield.setColorFilter(android.graphics.Color.GRAY)
                android.widget.Toast.makeText(this, "AdBlocker OFF ⚠️", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.toggleAdBlock(false)
            } else {
                binding.btnShield.alpha = 1.0f
                binding.btnShield.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
                android.widget.Toast.makeText(this, "AdBlocker ON 🛡️", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.toggleAdBlock(true)
            }
        }
        
        // === إعداد Speed Dial (صفحة البداية) ===
        val speedDialAdapter = SpeedDialAdapter { url ->
            // عند الضغط على أيقونة:
            binding.bottomAddressBar.setText(url) // اكتب الرابط
            viewModel.loadUrl(url) // حمله
        }
        
        binding.rvSpeedDial.adapter = speedDialAdapter
        // تحديد عدد الأعمدة (للتأكيد، رغم وجوده في XML)
        binding.rvSpeedDial.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        // ربط زر القائمة لفتح الـ Bottom Sheet
        binding.ivMenu.setOnClickListener {
            val menuFragment = BrowserMenuFragment()
            menuFragment.listener = this // ربط هذا الـ Activity كمستمع
            menuFragment.show(supportFragmentManager, "BrowserMenu")
        }
    }

    // تم حذف showMenu() لأننا نستخدم الفرامنت مباشرة الآن

    // === تنفيذ أوامر القائمة (Menu Implementation) ===

    override fun onBackClicked() {
        viewModel.currentSession.value?.let { session ->
            session.goBack()
        }
    }

    override fun onForwardClicked() {
        viewModel.currentSession.value?.goForward()
    }

    override fun onReloadClicked() {
        viewModel.currentSession.value?.reload()
    }
    
    override fun onHomeClicked() {
        // العودة لصفحة البداية (Speed Dial)
        viewModel.loadUrl("") 
    }

    override fun onShareClicked() {
        val currentUrl = viewModel.url.value
        if (currentUrl.isNotEmpty()) {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, currentUrl)
            }
            startActivity(android.content.Intent.createChooser(intent, "Share Link via"))
        }
    }
    
    override fun onSettingsClicked() {
        val intent = android.content.Intent(this, com.nabdh.browser.ui.main.SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun observePulse() {
        // Connect engine
        lifecycleScope.launchWhenStarted {
            viewModel.currentSession.collectLatest { session ->
                session?.let { 
                    it.open(geckoRuntime)
                    binding.geckoView.setSession(it)
                }
            }
        }

        // Pulse Animation
        lifecycleScope.launchWhenStarted {
            viewModel.pulseIntensity.collectLatest { intensity ->
                binding.pulseIndicator.updateIntensity(intensity)
                
                val alpha = if (intensity > 0.6f) 0.8f else 1.0f
                binding.addressBarLayout.animate()
                    .alpha(alpha)
                    .setDuration(200)
                    .start()
            }
        }

        // مراقبة وضع الشبح لتغيير الألوان
        lifecycleScope.launchWhenStarted {
            viewModel.isGhostMode.collectLatest { isGhost ->
                if (isGhost) {
                    binding.pulseIndicator.setPulseColor("#00FFFF") 
                    binding.ivSecurity.setColorFilter(android.graphics.Color.parseColor("#00FFFF"))
                    binding.addressBarLayout.setBackgroundColor(android.graphics.Color.parseColor("#0D0D0D"))
                    android.widget.Toast.makeText(this@MainActivity, "Ghost Mode Active 👻", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    binding.pulseIndicator.setPulseColor("#E53935")
                    binding.ivSecurity.setColorFilter(android.graphics.Color.parseColor("#E53935"))
                    binding.addressBarLayout.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))
                    android.widget.Toast.makeText(this@MainActivity, "Standard Mode", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        // === مراقبة الرابط لإخفاء/إظهار صفحة البداية ===
        lifecycleScope.launchWhenStarted {
            viewModel.url.collectLatest { currentUrl ->
                if (currentUrl.isEmpty()) {
                    // نحن في صفحة البداية
                    binding.rvSpeedDial.visibility = android.view.View.VISIBLE
                    binding.rvSpeedDial.alpha = 0f
                    binding.rvSpeedDial.animate().alpha(1f).setDuration(500).start()
                    
                    binding.geckoView.visibility = android.view.View.INVISIBLE
                } else {
                    // تم تحميل صفحة
                    binding.rvSpeedDial.visibility = android.view.View.GONE
                    binding.geckoView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}
