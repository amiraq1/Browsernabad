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

class MainActivity : AppCompatActivity() {

    // Note: In a real project, ViewBinding is generated from XML.
    // Assuming ActivityMainBinding exists mapping to activity_main.xml
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PulseViewModel by viewModels()
    private lateinit var geckoRuntime: GeckoRuntime

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
        binding.btnShield.setOnClickListener {
            val isActive = binding.btnShield.alpha == 1.0f
            if (isActive) {
                binding.btnShield.alpha = 0.3f // باهت يعني مغلق
                binding.btnShield.setColorFilter(android.graphics.Color.GRAY)
                android.widget.Toast.makeText(this, "AdBlocker OFF ⚠️", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.toggleAdBlock(false)
            } else {
                binding.btnShield.alpha = 1.0f
                binding.btnShield.setColorFilter(android.graphics.Color.parseColor("#4CAF50")) // أخضر
                android.widget.Toast.makeText(this, "AdBlocker ON 🛡️", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.toggleAdBlock(true)
            }
        }
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
                    // تفعيل الألوان الجليدية
                    binding.pulseIndicator.setPulseColor("#00FFFF") // سماوي مشع (Ice Blue)
                    binding.ivSecurity.setColorFilter(android.graphics.Color.parseColor("#00FFFF"))
                    
                    // خلفية أغمق قليلاً للبار عند الشبح (برمجياً لتجنب فقدان Drawable)
                    binding.addressBarLayout.setBackgroundColor(android.graphics.Color.parseColor("#0D0D0D"))

                    // رسالة تأكيد للمستخدم
                    android.widget.Toast.makeText(this@MainActivity, "Ghost Mode Active 👻", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    // العودة للأحمر النبضي
                    binding.pulseIndicator.setPulseColor("#E53935")
                    binding.ivSecurity.setColorFilter(android.graphics.Color.parseColor("#E53935"))
                    
                    // استعادة لون الخلفية الأصلي
                    binding.addressBarLayout.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))

                    android.widget.Toast.makeText(this@MainActivity, "Standard Mode", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
