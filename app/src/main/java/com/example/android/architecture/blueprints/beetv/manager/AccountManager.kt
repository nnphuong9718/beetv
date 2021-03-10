package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import android.util.Log
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.data.api.ChangeableBaseUrlInterceptor
import com.example.android.architecture.blueprints.beetv.data.api.RetrofitBuilder
import com.example.android.architecture.blueprints.beetv.data.models.BAccount
import com.example.android.architecture.blueprints.beetv.data.models.BBilling
import com.example.android.architecture.blueprints.beetv.util.toExpiredDate
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

enum class AccountType {
    TRIAL, VIP, NORMAL
}

class AccountManager {

    private var mAccount: BAccount? = null
    private var mExpiredDate: Long = 0
    private var mBillingType: String? = null
    private var mAccountType: AccountType = AccountType.NORMAL

    companion object {
        private var mInstance: AccountManager? = null
        fun getInstance(): AccountManager {
            if (mInstance == null) {
                mInstance = AccountManager()
            }
            return mInstance!!
        }
    }

    fun reset() {
        mAccount = null
        mExpiredDate = 0
        mBillingType = null
        mAccountType = AccountType.NORMAL
    }

    fun saveToken(token: String) {
        Log.d("BeeTV", "Token $token")
        val sharedPref = BeeTVApplication.context.getSharedPreferences(
                "BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            commit()
        }
    }

    fun getToken(): String? {
        val sharedPref = BeeTVApplication.context.getSharedPreferences(
                "BeeTV", Context.MODE_PRIVATE) ?: return null
        val token = sharedPref.getString("token", null)
        Log.d("BeeTV", "Token $token")
        return token
    }

    fun setAccount(account: BAccount) {
        mAccount = account

        // Todo change API domain
        if (mAccount?.node != null) {
            Log.d("BeeTV", "Logged in " + mAccount!!.node.node_ip)
            ChangeableBaseUrlInterceptor.getInstance().setApiDomain(mAccount!!.node.node_ip + RetrofitBuilder.BASE_PATH)
        }
        mExpiredDate = account.expiration_date

        var billing: BBilling? = null
        mAccount?.recharge_cards?.forEach {
            if (it.status && it.enable) {
                billing = it
//                mExpiredDate = mExpiredDate.coerceAtLeast(it.expired_date)
                return@forEach
            }
        }

        // VIP case
        if (billing != null && getVIPDuration() > 0) {
            mBillingType = billing?.media
            mAccountType = AccountType.VIP
        } else if (getTrialDuration() > 0) {
            mBillingType = mAccount?.trial_type
            mAccountType = AccountType.TRIAL
        } else {
            mAccountType = AccountType.NORMAL
        }
    }

    fun isLoggedIn(): Boolean {
        return mAccount != null
    }

    fun getAccount(): BAccount? {
        return mAccount
    }

    fun logout() {
        mAccount = null
        val sharedPref = BeeTVApplication.context.getSharedPreferences(
                "BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("account")
            remove("token")
            commit()
        }
    }

    fun getExpiredDateFormatted(formatType: String = "yyyy.MM.dd"): String? {
        val sdf = SimpleDateFormat(formatType)
        sdf.timeZone = TimeZoneManager.getInstance().getData()
        return sdf.format(Date(mExpiredDate))
    }

    fun getAccountCardStatus(isSettingDialog: Boolean = false): String {
        if (mAccount?.recharge_cards.isNullOrEmpty()) {
            return (if (isSettingDialog) "" else BeeTVApplication.context.resources.getString(R.string.remaining_date) + " ") + BeeTVApplication.context.resources.getString(R.string.no_cards)
        } else if (mExpiredDate == 0.toLong() && !mAccount?.recharge_cards.isNullOrEmpty()) {
            return BeeTVApplication.context.resources.getString(R.string.expired)
        }
        return BeeTVApplication.context.resources.getString(R.string.remaining_date) + " " + getExpiredDateFormatted("yyyy-MM-dd")
    }

    fun isPremiumAccount(): Boolean {
        return mAccountType != AccountType.NORMAL
    }

    fun getPremiumDuration(): Long {
        return when (mAccountType) {
            AccountType.VIP -> getVIPDuration()
            AccountType.TRIAL -> getTrialDuration()
            else -> 0
        }
//        val premiumDuration = TimeUnit.MILLISECONDS.toDays(mExpiredDate - Calendar.getInstance(TimeZoneManager.getInstance().getData()).timeInMillis)
//        if (premiumDuration > 0) return premiumDuration;
//
//        val trialDuration = TimeUnit.MILLISECONDS.toDays(mAccount?.trial_exp_time?.toLong() ?: 0 - Calendar.getInstance(TimeZoneManager.getInstance().getData()).timeInMillis)
//        if (trialDuration > 0) return trialDuration
//        return 0
    }

    fun getVIPDuration(): Long {
        val expirationAt12AM = Calendar.getInstance(TimeZoneManager.getInstance().getData())
        expirationAt12AM.timeInMillis = mExpiredDate
        val timeInLong = expirationAt12AM.timeInMillis - Calendar.getInstance(TimeZoneManager.getInstance().getData()).timeInMillis
        val days = Math.ceil((timeInLong.toDouble() / (1000 * 60 * 60 * 24))).toLong()
        if (timeInLong <= 0) return 0
        return days
    }

    fun getTrialDuration(): Long {
        val expirationAt12AM = Calendar.getInstance(TimeZoneManager.getInstance().getData())
        expirationAt12AM.timeInMillis = mAccount?.trial_exp_time?.toLong() ?: 0
        val timeInLong = expirationAt12AM.timeInMillis - Calendar.getInstance(TimeZoneManager.getInstance().getData()).timeInMillis
        val days = Math.ceil((timeInLong.toDouble() / (1000 * 60 * 60 * 24))).toLong()
        if (timeInLong <= 0) return 0
        return days
    }

    fun supportPremiumMovie(): Boolean {
        return mBillingType == Const.MEDIA_MOVIE_ONLY || mBillingType == Const.MEDIA_FULL
    }

    fun supportPremiumLive(): Boolean {
        return mBillingType == Const.MEDIA_LIVE_ONLY || mBillingType == Const.MEDIA_FULL
    }

    fun canShowNotice(): Boolean {
        if (mAccount?.recharge_cards.isNullOrEmpty()) {
            return false
        }
        return isLoggedIn() && !mAccount?.recharge_cards.isNullOrEmpty() && getPremiumDuration() <= 15
    }
}