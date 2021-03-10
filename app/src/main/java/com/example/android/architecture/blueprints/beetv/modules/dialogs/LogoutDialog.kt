package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.*
import com.example.android.architecture.blueprints.beetv.widgets.metro.*

class LogoutDialog : DialogFragment() {
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    var onClickLogoutListener: (() -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false

        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_logout)
        val btConfirm = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_confirm)
        val btCancel = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_cancel)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        val btView = dialog.findViewById<DrawingOrderLinearLayout>(R.id.button_view)
        val displayMetrics = DisplayMetrics()
        requireParentFragment().activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        if (displayMetrics.heightPixels > 0) {
            main.layoutParams.width = (displayMetrics.widthPixels * 0.5).toInt()
            main.layoutParams.height = (displayMetrics.heightPixels * 0.6).toInt()
        } else {
            main.layoutParams.width = requireContext().resources.getDimensionPixelOffset(R.dimen.size_450)
        }
        metroViewBorderImpl.attachTo(btView)
        btConfirm.setOnClickListener {
            AccountManager.getInstance().logout()
            NoticeManager.getInstance().clearPersonalNotices()
            FavoriteManager.getInstance().setData(mutableListOf())
            WatchHistoryManager.getInstance().setData(mutableListOf())
            MovieManager.getInstance().resetSeekList()
            dismiss()
            onClickLogoutListener?.invoke()
        }

        btCancel.setOnClickListener {
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

        if (oldView != null) {

            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_text_focus))
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