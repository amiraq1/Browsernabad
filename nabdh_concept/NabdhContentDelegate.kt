package com.nabdh.browser.core

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import org.mozilla.geckoview.GeckoSession

class NabdhContentDelegate(
    private val context: Context,
    private val isAdBlockEnabled: Boolean = true
) : GeckoSession.ContentDelegate {

    // كود حجب الإعلانات (نفس الكود السابق)
    private val adHideScript = """
        (function() {
            const adSelectors = ['.adsbygoogle', '.ad-banner', 'iframe[src*="ads"]', 'div[id*="google_ads"]', '.cookie-consent-banner'];
            function nukeAds() {
                adSelectors.forEach(sel => document.querySelectorAll(sel).forEach(el => el.style.display = 'none'));
            }
            nukeAds();
            setTimeout(nukeAds, 2000);
        })();
    """

    // 1. وظيفة حجب الإعلانات (عند انتهاء تحميل الصفحة)
    override fun onPageStop(session: GeckoSession, success: Boolean) {
        if (success && isAdBlockEnabled) {
            session.loadUri("javascript:$adHideScript")
        }
    }

    // 2. وظيفة التنزيل (عندما يواجه المتصفح ملفاً لا يستطيع عرضه)
    override fun onExternalResponse(session: GeckoSession, response: GeckoSession.WebResponse) {
        try {
            val url = response.uri
            val filename = getFileNameFromUrl(url)

            // إعداد طلب التنزيل باستخدام مدير النظام (DownloadManager)
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(filename)
            request.setDescription("Downloading file via Nabdh Browser...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            
            // الحفظ في مجلد التنزيلات العام
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            // تنفيذ التنزيل
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            Toast.makeText(context, "⬇️ Downloading: $filename", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "Download Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // دالة مساعدة لاستخراج اسم الملف من الرابط
    private fun getFileNameFromUrl(url: String): String {
        return try {
            var name = url.substring(url.lastIndexOf('/') + 1)
            if (name.contains("?")) name = name.substring(0, name.indexOf("?"))
            if (name.isEmpty()) "downloaded_file" else name
        } catch (e: Exception) {
            "nabdh_file"
        }
    }
}
