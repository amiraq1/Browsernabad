// NabdhExtractor Content Script
// يستخرج النص الأساسي من الصفحة للتلخيص بواسطة الذكاء الاصطناعي

(function () {
    function extractContent() {
        // خوارزمية بسيطة لاستخراج النص (يمكن تحسينها بـ Readability.js لاحقاً)
        const content = document.body.innerText || "";
        return {
            title: document.title,
            url: window.location.href,
            text: content.substring(0, 50000) // حد أقصى للحجم
        };
    }

    // الاستماع لرسائل من التطبيق (GeckoView)
    browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
        if (message.command === "extract") {
            const data = extractContent();
            sendResponse(data);
        }
    });

    console.log("NabdhExtractor loaded.");
})();
