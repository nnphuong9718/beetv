package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.AccountManager

class SuccessDialog : DialogFragment() {


    var title: String? = null
    var icon: Int? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_success)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        val ivIcon = dialog.findViewById<ImageView>(R.id.iv_icon)
        val main = dialog.findViewById<LinearLayout>(R.id.main)
        main.requestFocus()
        title?.apply {
            tvTitle.text = this
        }
        icon?.apply {
            ivIcon.setImageResource(this)
        }

        main.setOnClickListener {

        }

        Handler().postDelayed(Runnable {
            dismiss()
        }, 3000)
        return dialog


    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (AccountManager.getInstance().canShowNotice()) {
            val expirationDialog = ExpirationDialog()
            expirationDialog.show(requireParentFragment().childFragmentManager, "expiration")
        }
    }
}