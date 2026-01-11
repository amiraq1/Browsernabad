package com.nabdh.browser.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

/**
 * PulseIndicatorView
 * عرض خاص لرسم خط النبض الحيوي.
 * تم دمج الكود الخاص بالمستخدم مع تحسينات هندسية للحواف.
 */
class PulseIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // === إعدادات الرسم ===
    private val paint = Paint().apply {
        // تم استخدام اللون الأحمر الحيوي حسب طلبك
        color = Color.parseColor("#E53935") 
        strokeWidth = 5f    
        style = Paint.Style.STROKE
        isAntiAlias = true  
        // إضافة نعومة لأطراف الخط
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    
    // === متغيرات الحركة ===
    private var phase = 0f    
    private var targetSpeed = 0.10f
    private var targetAmplitude = 20f
    
    // القيم الحالية (للانتقال السلس Animation Smoothing)
    private var currentSpeed = 0.10f
    private var currentAmplitude = 20f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val midY = height / 2

        // التحديث الفيزيائي (Physics Update): الانتقال السلس نحو القيم المستهدفة
        // هذا يمنع "الققفزات" المفاجئة عند تغيير السرعة
        currentSpeed += (targetSpeed - currentSpeed) * 0.1f
        currentAmplitude += (targetAmplitude - currentAmplitude) * 0.1f

        path.reset()
        path.moveTo(0f, midY)

        // زيادة دقة الرسم قليلاً للحصول على منحنيات أنعم
        val step = 4 
        
        for (x in 0..width.toInt() step step) {
            val xFloat = x.toFloat()
            val normalizedX = xFloat / width
            
            // معادلة الموجة الأساسية
            val rawY = sin((0.02f * xFloat) + phase)
            
            // === تحسين هندسي (Damping) ===
            // هذه المعادلة تجعل الموجة تضمحل عند الأطراف (0 عند اليسار، 1 في الوسط، 0 عند اليمين)
            // مما يعطي شكلاً أنيقاً "معلقاً" في الهواء
            val damping = (4.0f * normalizedX * (1.0f - normalizedX))
            val finalAmplitude = currentAmplitude * damping

            val y = midY + (finalAmplitude * rawY)
            
            path.lineTo(xFloat, y.toFloat())
        }

        canvas.drawPath(path, paint)

        // تحديث الطور (Phase) للحركة
        phase += currentSpeed
        
        // طلب تحديث الإطار التالي
        postInvalidateOnAnimation()
    }
    
    /**
     * تحديث حالة النبض بناءً على البيانات.
     * يمكن استدعاء هذه الدالة بقيمة "الشدة" (0.0 إلى 1.0) للحصول على تحكم دقيق،
     * أو استخدام updatePulseSpeed البسيطة أدناه.
     */
    fun updateIntensity(intensity: Float) {
        // نحول الشدة (0-1) إلى سرعة وسعة
        val safeIntensity = intensity.coerceIn(0f, 1f)
        targetSpeed = 0.10f + (0.4f * safeIntensity) // سرعة بين 0.10 و 0.50
        targetAmplitude = 20f + (60f * safeIntensity) // ارتفاع بين 20 و 80
    }

    // دالة التوافق مع الكود الأصلي الخاص بك
    fun updatePulseSpeed(isLoading: Boolean) {
        targetSpeed = if (isLoading) 0.35f else 0.10f
        targetAmplitude = if (isLoading) 50f else 20f
    }

    // أضف هذه الدالة داخل كلاس PulseIndicatorView
    fun setPulseColor(colorHex: String) {
        paint.color = Color.parseColor(colorHex)
        // سنجعل الخط يتوهج قليلاً عند التغيير (اختياري)
        // ملاحظة: لكي يعمل التوهج (ShadowLayer) بشكل مثالي، يفضل تفعيل LAYER_TYPE_SOFTWARE في init
        setLayerType(LAYER_TYPE_SOFTWARE, null) 
        paint.setShadowLayer(10f, 0f, 0f, Color.parseColor(colorHex))
        invalidate() // إعادة الرسم باللون الجديد فوراً
    }
}
