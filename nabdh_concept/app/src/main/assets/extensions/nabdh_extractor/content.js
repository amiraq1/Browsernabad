// NabdhExtractor Content Script
// يستخرج النص الأساسي من الصفحة للتلخيص بواسطة الذكاء الاصطناعي

(function () {
    function extractContent() {
        // خوارزمية بسيطة لاستخراج النص
        // (للنسخة القادمة: سأستخدم Readability.js للحصول على "المحتوى الصافي" فقط)
        const content = document.body.innerText || "";

        return {
            title: document.title,
            url: window.location.href,
            // استخراج أول 50 ألف حرف لتجنب إغراق الذاكرة
            text: content.substring(0, 50000).replace(/\s+/g, ' ').trim()
        };
    }

    // الاستماع لرسائل من التطبيق (GeckoView)
    browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
        // التحقق من نوع الرسالة (مطابق لما هو في BrowserViewModel)
        if (message.type === "EXTRACT_CONTENT") {
            try {
                const data = extractContent();
                sendResponse(data);
            } catch (e) {
                sendResponse({ error: e.toString() });
            }
        }
        // إرجاع true يشير إلى أن الرد سيكون غير متزامن (Asynchronous) - ممارسة جيدة
        return true;
    });

    console.log("NabdhExtractor loaded and ready.");
})();
