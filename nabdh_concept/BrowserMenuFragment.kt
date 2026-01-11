package com.nabdh.browser.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nabdh.browser.R

class BrowserMenuFragment : BottomSheetDialogFragment() {

    // واجهة للتواصل مع Activity
    interface MenuListener {
        fun onBackClicked()
        fun onForwardClicked()
        fun onReloadClicked()
        fun onHomeClicked()
        fun onShareClicked()
        fun onSettingsClicked()
    }

    var listener: MenuListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // تم تصحيح الاسم ليطابق الملف الموجود
        return inflater.inflate(R.layout.layout_menu_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ربط الأزرار
        view.findViewById<View>(R.id.menuBack).setOnClickListener {
            listener?.onBackClicked()
            dismiss() // إغلاق القائمة بعد الضغط
        }

        view.findViewById<View>(R.id.menuForward).setOnClickListener {
            listener?.onForwardClicked()
            dismiss()
        }

        view.findViewById<View>(R.id.menuReload).setOnClickListener {
            listener?.onReloadClicked()
            dismiss()
        }
        
        view.findViewById<View>(R.id.menuHome).setOnClickListener {
            listener?.onHomeClicked()
            dismiss()
        }

        view.findViewById<View>(R.id.menuShare).setOnClickListener {
            listener?.onShareClicked()
            dismiss()
        }
        
        view.findViewById<View>(R.id.menuSettings).setOnClickListener {
            listener?.onSettingsClicked()
            dismiss()
        }
    }
}
