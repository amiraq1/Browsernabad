// الاستماع لرسائل من تطبيق أندرويد (سنقوم بتفعيل هذا الاتصال لاحقاً)
console.log("Nabdh Extractor is running...");

// دالة استخراج النص (جاهزة للاستدعاء)
function extractPageContent() {
    // 1. إزالة العناصر المشتتة
    const clones = document.body.cloneNode(true);
    const distractions = clones.querySelectorAll('nav, footer, script, style, iframe, .ad, .advertisement');
    distractions.forEach(el => el.remove());

    // 2. استخراج النص الصافي
    let text = clones.innerText || "";

    // 3. تنظيف الفراغات الزائدة
    return text.replace(/\s+/g, ' ').trim().substring(0, 5000);
}
