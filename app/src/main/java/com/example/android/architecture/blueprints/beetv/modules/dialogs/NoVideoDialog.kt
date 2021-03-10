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
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import com.example.android.architecture.blueprints.beetv.manager.MovieManager
import com.example.android.architecture.blueprints.beetv.manager.WatchHistoryManager
import com.example.android.architecture.blueprints.beetv.widgets.metro.DrawingOrderRelativeLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderHandler
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl

class NoVideoDialog : DialogFragment() {
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false

        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_no_videos)
        val btConfirm = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_confirm)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        metroViewBorderImpl.attachTo(main)
        btConfirm.setOnClickListener {
            dismiss()
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
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.button_text_focus))
                if (oldView.getChildAt(0) is TextView){
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                }
            }
        }

        if(newView != null){
            if (newView is MetroItemFrameLayout){
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.alto))
                if (newView.getChildAt(0) is TextView){
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }



    }
}