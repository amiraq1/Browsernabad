package com.nabdh.browser.ui.main

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout

// نموذج بيانات بسيط
data class Bookmark(val name: String, val url: String, val colorHex: String)

class SpeedDialAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SpeedDialAdapter.ViewHolder>() {

    // قائمة المواقع الافتراضية
    private val bookmarks = listOf(
        Bookmark("Google", "https://google.com", "#4285F4"),
        Bookmark("YouTube", "https://youtube.com", "#FF0000"),
        Bookmark("Twitter", "https://twitter.com", "#1DA1F2"),
        Bookmark("Facebook", "https://facebook.com", "#1877F2"),
        Bookmark("Instagram", "https://instagram.com", "#C13584"),
        Bookmark("Reddit", "https://reddit.com", "#FF4500"),
        Bookmark("News", "https://bbc.com/news", "#BBC111"),
        Bookmark("Amazon", "https://amazon.com", "#FF9900")
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: TextView = view.findViewById(1) // معرف وهمي
        val labelView: TextView = view.findViewById(2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        
        // إنشاء Layout للعنصر برمجياً (حاوية رأسية: دائرة + نص)
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(24, 24, 24, 24) }
            gravity = Gravity.CENTER
        }

        // الدائرة (الأيقونة)
        val circle = TextView(context).apply {
            id = 1
            layoutParams = LinearLayout.LayoutParams(160, 160) // حجم الدائرة
            gravity = Gravity.CENTER
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // النص (الاسم)
        val label = TextView(context).apply {
            id = 2
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        container.addView(circle)
        container.addView(label)

        return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bookmarks[position]
        
        // إعداد النص
        holder.labelView.text = item.name
        holder.iconView.text = item.name.first().toString() // الحرف الأول

        // رسم الدائرة الملونة برمجياً
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(item.colorHex))
            setStroke(2, Color.parseColor("#33FFFFFF")) // حدود خفيفة
        }
        holder.iconView.background = shape

        // عند الضغط
        holder.itemView.setOnClickListener { 
            // تأثير تصغير بسيط عند الضغط
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                onClick(item.url)
            }.start()
        }
    }

    override fun getItemCount() = bookmarks.size
}
