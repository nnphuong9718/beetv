package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.metro.DrawingOrderRelativeLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl


class UpdateAppDialog : DialogFragment() {
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    var onClickConfirmListener : (() -> Unit) ?=null
    var onBackClickListener :  (() -> Unit) ?=null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false

        isCancelable = false
        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_update_app)
        val btConfirm = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_confirm)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        val displayMetrics = DisplayMetrics()
        requireParentFragment().activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        if (displayMetrics.heightPixels > 0) {
            main.layoutParams.width = (displayMetrics.widthPixels * 0.5).toInt()
            main.layoutParams.height = (displayMetrics.heightPixels * 0.7).toInt()
        } else {
            main.layoutParams.width = requireContext().resources.getDimensionPixelOffset(R.dimen.size_450)
        }
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        metroViewBorderImpl.attachTo(main)
        btConfirm.setOnClickListener {
//            dismiss()
            progressBar.show()
            btConfirm.hide()
            onClickConfirmListener?.invoke()
        }

        dialog.setOnKeyListener { p0, keyCode, p2 ->
            if (keyCode == KeyEvent.KEYCODE_BACK && p2.action == KeyEvent.ACTION_DOWN){
                onBackClickListener?.invoke()
            }
            keyCode == KeyEvent.KEYCODE_BACK
        }

        dialog.show()
        btConfirm.requestFocus()
        changeBackgroundButton(null, btConfirm)
        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            metroViewBorderImpl.view.tag = newFocus
            changeBackgroundButton(oldFocus, newFocus)
        }
        return dialog


    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {

        if(oldView != null){

            if (oldView is MetroItemFrameLayout){
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chapter_bkg_color))
                if (oldView.getChildAt(0) is TextView){
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }

        if(newView != null){
            if (newView is MetroItemFrameLayout){
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alto))
                if (newView.getChildAt(0) is TextView){
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }



    }


}