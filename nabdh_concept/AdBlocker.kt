package com.nabdh.browser.core

import org.mozilla.geckoview.GeckoSession

object AdBlocker {

    // سكربت التنظيف (Cosmetic Filtering)
    // يقوم بإخفاء العناصر المزعجة التي تفلت من الحجب التقني
    private const val AD_HIDE_SCRIPT = """
        (function() {
            console.log('Nabdh AdBlocker: Cleaning visual clutter...');
            const adSelectors = [
                '.adsbygoogle', 
                '.ad-banner', 
                'iframe[src*="ads"]',
                'div[id*="google_ads"]',
                'div[class*="sponsored"]',
                '[id^="ad-"]',
                '[class^="ad-"]',
                '.cookie-consent-banner',
                '#cookie-banner',
                '.fc-consent-root'
            ];
            
            function nukeAds() {
                adSelectors.forEach(selector => {
                    const elements = document.querySelectorAll(selector);
                    elements.forEach(el => {
                        el.style.display = 'none !important';
                        el.style.visibility = 'hidden !important';
                    });
                });
            }

            // نفذ فوراً
            nukeAds();
            // وأعد التنفيذ بعد قليل للتأكد من العناصر المتأخرة (Dynamic Ads)
            setTimeout(nukeAds, 1000);
            setTimeout(nukeAds, 3000);
        })();
    """

    fun enable(session: GeckoSession) {
        // 1. تفعيل الحماية المدمجة في المحرك (Blocking Trackers)
        // هذا هو خط الدفاع الأول والأقوى
        session.settings.useTrackingProtection = true
        
        // 2. تعيين ContentDelegate لحقن السكربت البصري
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onPageStop(session: GeckoSession, success: Boolean) {
                // عندما ينتهي تحميل الصفحة بنجاح
                if (success) {
                    // نقوم بحقن السكربت كأنه رابط URL يبدأ بـ javascript:
                    // هذه هي الطريقة السريعة في GeckoView بدون بناء Extension
                    session.loadUri("javascript:$AD_HIDE_SCRIPT")
                    
                    // منع تنفيذ هذا السكربت من تغيير العنوان الحالي
                    // (GeckoView قد يحاول الانتقال لصفحة جديدة، لكن JS void يمنع ذلك عادة)
                }
            }
        }
    }

    fun disable(session: GeckoSession) {
        session.settings.useTrackingProtection = false
        session.contentDelegate = null // إزالة المراقب
    }
}
