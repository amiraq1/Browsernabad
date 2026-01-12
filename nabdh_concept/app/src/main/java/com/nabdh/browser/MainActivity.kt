package com.nabdh.browser

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.bottomappbar.BottomAppBar

class MainActivity : AppCompatActivity() {

    // العناصر الرئيسية
    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomAppBar: BottomAppBar
    
    // أزرار التنقل
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnMenu: ImageButton
    
    // طبقات الحالة
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var errorContainer: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var pulseIndicator: View
    
    // حالة التحميل
    private var isLoading = false
    private var currentUrl: String = ""
    private var pulseAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupWebView()
        setupListeners()
        startPulseAnimation()
        
        // تحميل الصفحة الرئيسية
        loadUrl("https://www.google.com")
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        urlInput = findViewById(R.id.urlInput)
        progressBar = findViewById(R.id.progressBar)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnMenu = findViewById(R.id.btnMenu)
        
        loadingOverlay = findViewById(R.id.loadingOverlay)
        errorContainer = findViewById(R.id.errorContainer)
        retryButton = findViewById(R.id.retryButton)
        pulseIndicator = findViewById(R.id.pulseIndicator)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // الأساسيات
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            
            // الأداء
            cacheMode = WebSettings.LOAD_DEFAULT
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            
            // الأمان
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            
            // تجربة المستخدم
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            
            // الوضع الداكن (Android 13+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                isAlgorithmicDarkeningAllowed = true
            }
        }

        webView.webViewClient = NabdhWebViewClient()
        webView.webChromeClient = NabdhChromeClient()
    }

    private fun setupListeners() {
        // IME Action - عند الضغط على "اذهب" في لوحة المفاتيح
        urlInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || 
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                processUrlInput(urlInput.text.toString())
                hideKeyboard()
                true
            } else false
        }

        // أزرار التنقل
        btnBack.setOnClickListener { 
            if (webView.canGoBack()) webView.goBack() 
        }
        
        btnForward.setOnClickListener { 
            if (webView.canGoForward()) webView.goForward() 
        }
        
        btnRefresh.setOnClickListener {
            if (isLoading) {
                webView.stopLoading()
            } else {
                webView.reload()
            }
        }
        
        btnMenu.setOnClickListener {
            showOptionsMenu()
        }
        
        // زر إعادة المحاولة
        retryButton.setOnClickListener {
            errorContainer.visibility = View.GONE
            webView.reload()
        }

        // تحديث شريط URL عند التركيز
        urlInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                urlInput.setText(currentUrl)
                urlInput.selectAll()
            } else {
                updateUrlDisplay(currentUrl)
            }
        }
    }

    /**
     * معالجة ذكية للمدخلات - يميز بين URL والبحث
     */
    private fun processUrlInput(input: String) {
        val trimmed = input.trim()
        
        when {
            trimmed.isEmpty() -> return
            
            // URL كامل
            URLUtil.isValidUrl(trimmed) -> loadUrl(trimmed)
            
            // يبدو كدومين (يحتوي نقطة ولا مسافات)
            trimmed.contains(".") && !trimmed.contains(" ") -> {
                val url = if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
                loadUrl(url)
            }
            
            // بحث Google
            else -> {
                val searchQuery = Uri.encode(trimmed)
                loadUrl("https://www.google.com/search?q=$searchQuery")
            }
        }
    }

    private fun loadUrl(url: String) {
        currentUrl = url
        webView.loadUrl(url)
    }

    private fun updateUrlDisplay(url: String) {
        // عرض الدومين فقط للاختصار
        try {
            val uri = Uri.parse(url)
            val host = uri.host ?: url
            urlInput.setText(host.removePrefix("www."))
        } catch (e: Exception) {
            urlInput.setText(url)
        }
    }

    private fun updateNavigationButtons() {
        btnBack.isEnabled = webView.canGoBack()
        btnBack.alpha = if (webView.canGoBack()) 1f else 0.3f
        
        btnForward.isEnabled = webView.canGoForward()
        btnForward.alpha = if (webView.canGoForward()) 1f else 0.3f
    }

    private fun updateRefreshButton(loading: Boolean) {
        isLoading = loading
        btnRefresh.setImageResource(
            if (loading) R.drawable.ic_close else R.drawable.ic_refresh
        )
        btnRefresh.contentDescription = if (loading) 
            getString(R.string.btn_stop) else getString(R.string.btn_refresh)
    }

    private fun startPulseAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.3f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.3f, 1f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.6f, 1f)
        
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(pulseIndicator, scaleX, scaleY, alpha).apply {
            duration = 1200
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.isVisible = show
        if (show) {
            pulseAnimator?.start()
        } else {
            pulseAnimator?.cancel()
        }
    }

    private fun showError(errorCode: Int, description: String?) {
        errorContainer.visibility = View.VISIBLE
        webView.visibility = View.INVISIBLE
        
        val errorTitle = findViewById<TextView>(R.id.errorTitle)
        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        
        when (errorCode) {
            WebViewClient.ERROR_HOST_LOOKUP -> {
                errorTitle.text = getString(R.string.error_not_found)
                errorMessage.text = getString(R.string.error_not_found_message)
            }
            WebViewClient.ERROR_CONNECT, WebViewClient.ERROR_TIMEOUT -> {
                errorTitle.text = getString(R.string.error_connection)
                errorMessage.text = getString(R.string.error_connection_message)
            }
            WebViewClient.ERROR_BAD_URL -> {
                errorTitle.text = getString(R.string.error_bad_url)
                errorMessage.text = getString(R.string.error_bad_url_message)
            }
            else -> {
                errorTitle.text = getString(R.string.error_generic)
                errorMessage.text = description ?: getString(R.string.error_generic_message)
            }
        }
    }

    private fun hideError() {
        errorContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun showOptionsMenu() {
        val popup = PopupMenu(this, btnMenu)
        popup.menu.apply {
            add(getString(R.string.menu_new_tab))
            add(getString(R.string.menu_bookmarks))
            add(getString(R.string.menu_history))
            add(getString(R.string.menu_downloads))
            add(getString(R.string.menu_settings))
        }
        popup.show()
    }

    private fun hideKeyboard() {
        urlInput.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(urlInput.windowToken, 0)
    }

    // ═══════════════════════════════════════════════════════════════
    // WebViewClient - معالجة أحداث التنقل
    // ═══════════════════════════════════════════════════════════════
    
    inner class NabdhWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let { 
                currentUrl = it
                updateUrlDisplay(it)
            }
            hideError()
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0
            updateRefreshButton(true)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            showLoading(false)
            updateRefreshButton(false)
            updateNavigationButtons()
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            // تجاهل الأخطاء للموارد الفرعية
            if (request?.isForMainFrame == true) {
                showError(
                    error?.errorCode ?: -1,
                    error?.description?.toString()
                )
                progressBar.visibility = View.GONE
                updateRefreshButton(false)
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            
            // التعامل مع البروتوكولات الخاصة
            return when {
                url.startsWith("tel:") || 
                url.startsWith("mailto:") || 
                url.startsWith("intent:") -> {
                    // فتح في التطبيق المناسب
                    try {
                        startActivity(android.content.Intent.parseUri(url, 0))
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity, 
                            getString(R.string.no_app_to_open), 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                else -> false // تحميل في WebView
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // WebChromeClient - معالجة التقدم والعناوين
    // ═══════════════════════════════════════════════════════════════
    
    inner class NabdhChromeClient : WebChromeClient() {
        
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
            
            // إظهار المحتوى عند 30% لتجربة أسرع
            if (newProgress > 30 && loadingOverlay.isVisible) {
                showLoading(false)
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            // يمكن استخدام العنوان لاحقاً للإشارات المرجعية
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // التعامل مع زر الرجوع
    // ═══════════════════════════════════════════════════════════════
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            urlInput.hasFocus() -> {
                urlInput.clearFocus()
                hideKeyboard()
            }
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        pulseAnimator?.cancel()
        webView.destroy()
        super.onDestroy()
    }
}
