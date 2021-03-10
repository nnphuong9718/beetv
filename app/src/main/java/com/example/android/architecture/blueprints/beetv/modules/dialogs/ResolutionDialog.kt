package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.MediaPlayerManager
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.widgets.metro.DrawingOrderRelativeLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl

class ResolutionDialog : DialogFragment() {

    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    var onResolutionSelectedListener: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false

        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_resolution)
        val btAutomation = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_automation)
        val bt16p9 = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_16_9)
        val bt4p3 = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_4_3)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        metroViewBorderImpl.attachTo(main)
        btAutomation.setOnClickListener {
            MediaPlayerManager.getInstance().setResolution(Constants.MediaResolution.AUTOMATION)
            onResolutionSelectedListener?.invoke("automation")
            dismiss()
        }

        bt16p9.setOnClickListener {
            MediaPlayerManager.getInstance().setResolution(Constants.MediaResolution.R16P9)
            onResolutionSelectedListener?.invoke("16:9")
            dismiss()
        }

        bt4p3.setOnClickListener {
            MediaPlayerManager.getInstance().setResolution(Constants.MediaResolution.R4P3)
            onResolutionSelectedListener?.invoke("4:3")
            dismiss()
        }
        dialog.show()

        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            metroViewBorderImpl.view.tag = newFocus
            changeBackgroundButton(oldFocus, newFocus)
        }
        return dialog
    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {
        if (oldView != null) {
            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }

        if (newView != null) {
            if (newView is MetroItemFrameLayout) {
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alto))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }
    }
}