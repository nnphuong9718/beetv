package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.widgets.metro.*

class ExpirationDialog : DialogFragment() {


    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false

        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_expiration)
        val btConfirm = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_confirm)
        val expirationText = dialog.findViewById<TextView>(R.id.expiration_title)
        val expirationTitle = dialog.findViewById<TextView>(R.id.expiration_text)
        expirationTitle.text = if (AccountManager.getInstance().getAccount()?.expiration_date == 0.toLong()) getString(R.string.expiration3) else getString(R.string.expiration1)
        val main = dialog.findViewById<LinearLayout>(R.id.main)
        expirationText.text = String.format(getString(R.string.manager_phone), AccountManager.getInstance().getAccount()?.agent?.phone?.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "$1-$2-$3"))
        metroViewBorderImpl.attachTo(main)
        btConfirm.requestFocus()

        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            metroViewBorderImpl.view.tag = newFocus
            changeBackgroundButton(oldFocus, newFocus)
        }
        btConfirm.setOnClickListener {
            dismiss()
        }

        dialog.show()

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
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }


    }
}