package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.LanguageManager
import com.example.android.architecture.blueprints.beetv.manager.MediaPlayerManager
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import com.example.android.architecture.blueprints.beetv.util.formatOffset
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.invisible
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.SettingItemView
import com.example.android.architecture.blueprints.beetv.widgets.metro.DrawingOrderRelativeLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl
import java.util.*

class SettingDialog : DialogFragment() {

    var onClickLoginListener: (() -> Unit)? = null
    var onClickLogoutListener: (() -> Unit)? = null
    var onTimeZoneSelectedListener: ((TimeZone) -> Unit)? = null
    var onChangeLanguageListener: (() -> Unit)? = null
    var onDeleteFavoriteListener: (() -> Unit)? = null
    var onDeleteHistoryListener: (() -> Unit)? = null

    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val roundedFrameLayout = FrameLayout(requireContext())
        roundedFrameLayout.clipChildren = false
        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_setting)
        val btAuthority = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_authority)
        val tvID = dialog.findViewById<TextView>(R.id.tv_id)
        val btLogout = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_log_out)
        val btLanguage = dialog.findViewById<SettingItemView>(R.id.bt_language)
        val btScreen = dialog.findViewById<SettingItemView>(R.id.bt_screen)
        val btSlot = dialog.findViewById<SettingItemView>(R.id.bt_slot)
        val btDeleteFavorite = dialog.findViewById<SettingItemView>(R.id.bt_favorite)
        val btDeleteHistory = dialog.findViewById<SettingItemView>(R.id.bt_playback)
        val btPlayer = dialog.findViewById<SettingItemView>(R.id.bt_player)
        val btCharge = dialog.findViewById<MetroItemFrameLayout>(R.id.bt_charge)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        val layoutID = dialog.findViewById<LinearLayout>(R.id.layoutID)
        val tvNumberDate = dialog.findViewById<TextView>(R.id.tv_number_date)
        val tvDate = dialog.findViewById<TextView>(R.id.tv_expired_date)
        val tvVip = dialog.findViewById<TextView>(R.id.tv_vip)
        val appVersion = dialog.findViewById<TextView>(R.id.app_version)
        val language = LanguageManager.getInstance().getData(requireContext())
        var version = ""
        try {
            val pInfo: PackageInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        appVersion.text = String.format(getString(R.string.version), version)
        btLanguage.setValue(
                if (language.equals("ko")) getString(R.string.korean)
                else getString(R.string.english))
        btScreen.setValue(parseResolutionText(MediaPlayerManager.getInstance().getResolution(requireContext()).type))
//        btPlayer.setValue("시스템 플레이어")
//        val timeZone = TimeZoneManager.getInstance().getData()
        btSlot.setValue(TimeZoneManager.getInstance().getTimezoneName())
        btPlayer.setValue(getString(R.string.system_player))
        btDeleteFavorite.setValue("")
        btDeleteHistory.setValue("")

        metroViewBorderImpl.attachTo(main)
        btAuthority.setOnClickListener {
            dismiss()
            onClickLoginListener?.invoke()
        }

        btLogout.setOnClickListener {
//            dismiss()
            onClickLogoutListener?.invoke()

        }

        btScreen.setOnClickListener {
            val dlg = ResolutionDialog()
            dlg.show(childFragmentManager, "resolution")
            dlg.onResolutionSelectedListener = {
                btScreen.setValue(parseResolutionText(it))
            }
        }

        btLanguage.setOnClickListener {
            val languageDialog = LanguageDialog()
            languageDialog.show(childFragmentManager, "language")
            languageDialog.onLanguageSelectedListener = {
                btLanguage.setValue(
                        if (it == "ko")
                            getString(R.string.korean)
                        else getString(R.string.english))
                onChangeLanguageListener?.invoke()
            }
        }

        btSlot.setOnClickListener {
            val timeZonesDialog = TimeZonesDialog()
            timeZonesDialog.show(childFragmentManager, "timezone")
            timeZonesDialog.onTimeZoneSelectedListener = {
                btSlot.setValue(
                        if (it == TimeZone.getDefault()) {
                            getString(R.string.sytem_time)
                        } else {
                            TimeZoneManager.getInstance().getTimezoneName()
                        }
                )
                onTimeZoneSelectedListener?.invoke(it)
            }
        }

        btDeleteFavorite.setOnClickListener {
            if (AccountManager.getInstance().isLoggedIn()) {
                val delFavDialog = DeleteFavoriteDialog()
                delFavDialog.show(childFragmentManager, "favorite")
                delFavDialog.onClickConfirmListener = {
                    onDeleteFavoriteListener?.invoke()
                }
            } else {
                val dialogLogin = RequireLoginDialog()
                dialogLogin.show(childFragmentManager, "login")
                dialogLogin.onClickLoginListener = {
                    dismiss()
                    onClickLoginListener?.invoke()

                }
            }
        }

        btDeleteHistory.setOnClickListener {
            if (AccountManager.getInstance().isLoggedIn()) {

                val delHisDialog = DeleteHistoryDialog()
                delHisDialog.show(childFragmentManager, "history")
                delHisDialog.onClickConfirmListener = {
                    onDeleteHistoryListener?.invoke()
                }
            } else {
                val dialogLogin = RequireLoginDialog()
                dialogLogin.show(childFragmentManager, "login")
                dialogLogin.onClickLoginListener = {
                    dismiss()
                    onClickLoginListener?.invoke()

                }
            }
        }

        if (!AccountManager.getInstance().isLoggedIn()) {
            btLogout.hide()
            layoutID.invisible()
            btCharge.nextFocusRightId = R.id.bt_authority
            btSlot.nextFocusDownId = R.id.bt_authority
            btDeleteHistory.nextFocusDownId = R.id.bt_authority
        } else {
            btCharge.nextFocusRightId = R.id.bt_log_out
            btSlot.nextFocusDownId = R.id.bt_log_out
            btDeleteHistory.nextFocusDownId = R.id.bt_log_out
            btAuthority.hide()
            tvID.text = AccountManager.getInstance().getAccount()?.getRawID()
            if (AccountManager.getInstance().isPremiumAccount()) {
                val duration = AccountManager.getInstance().getPremiumDuration()
                tvNumberDate.text = String.format(if (duration > 1) getString(R.string.format_dates) else getString(R.string.format_date), AccountManager.getInstance().getPremiumDuration())
                tvDate.text = AccountManager.getInstance().getExpiredDateFormatted()
                tvNumberDate.show()
                tvVip.show()
                layoutID.layoutParams.width = requireContext().resources.getDimensionPixelOffset(R.dimen.size_250)
            } else {
                tvNumberDate.hide()
                tvDate.text = AccountManager.getInstance().getAccountCardStatus(true)
                tvVip.hide()
                layoutID.layoutParams.width = requireContext().resources.getDimensionPixelOffset(R.dimen.size_200)
            }
        }
        dialog.show()

        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            metroViewBorderImpl.view.tag = newFocus
            changeBackgroundButton(oldFocus, newFocus)
        }
//        changeBackgroundButton(null, btScreen)
        return dialog
    }

    private fun parseResolutionText(key: String): String {
        return when (key) {
            "16:9" -> "16:9"
            "4:3" -> "4:3"
            else -> "16:9"
        }
    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {
        if (oldView != null) {
            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chapter_bkg_color))
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            if (oldView is SettingItemView) {
                oldView.setColor(R.color.chapter_bkg_color)
            }
        }

        if (newView != null) {
            if (newView is MetroItemFrameLayout) {
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alto))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
            if (newView is SettingItemView) {
                newView.setColor(R.color.alto)
            }
        }
    }

    fun hideLayoutId() {
        dialog?.findViewById<LinearLayout>(R.id.layoutID)?.invisible()
        dialog?.findViewById<MetroItemFrameLayout>(R.id.bt_authority)?.show()
        dialog?.findViewById<MetroItemFrameLayout>(R.id.bt_log_out)?.hide()
        dialog?.findViewById<MetroItemFrameLayout>(R.id.bt_charge)?.nextFocusRightId = R.id.bt_authority
        dialog?.findViewById<SettingItemView>(R.id.bt_slot)?.nextFocusDownId = R.id.bt_authority
        dialog?.findViewById<SettingItemView>(R.id.bt_delete)?.nextFocusDownId = R.id.bt_authority
    }
}