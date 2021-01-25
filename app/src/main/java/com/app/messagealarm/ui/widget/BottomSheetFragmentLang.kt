package com.app.messagealarm.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.app.messagealarm.R
import com.app.messagealarm.utils.SupportUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragmentLang : BottomSheetDialogFragment(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.bottom_sheet_language, container, false)
        v.findViewById<AppCompatTextView>(R.id.text_view_contact).setOnClickListener {
            SupportUtils.sendEmailLanguage(requireActivity())
        }
        return v
    }

}