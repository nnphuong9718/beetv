//package com.example.android.architecture.blueprints.beetv.manager
//
//import com.example.android.architecture.blueprints.beetv.data.models.BBilling
//
//class BillingManager {
//
//    private lateinit var expiredDate: String
//    private var numberDays: Long = 0
//    private var mBillings: MutableList<BBilling> = mutableListOf()
//
//    companion object {
//        private var mInstance: BillingManager? = null
//        fun getInstance(): BillingManager {
//
//            if (mInstance == null) {
//                mInstance = BillingManager()
//            }
//            return mInstance!!
//        }
//    }
//
//    fun setData(expiredDate: String, numberDays: Long, billings: List<BBilling>) {
//        this.expiredDate = expiredDate
//        this.numberDays = numberDays
//        this.mBillings.clear()
//        this.mBillings.addAll(billings)
//    }
//
//    fun checkBillingExist(billing: String): Boolean {
//        mBillings.forEach {
//            if (it.enable) {
//                if (billing == "TOLL") {
//                    if (it.media == "M" || it.media == "LM") {
//                        return true
//                    }
//                } else {
//                    if (it.media == "L" || it.media == "LM") {
//                        return true
//                    }
//                }
//            }
//        }
//        return false
//    }
//
//    fun getNumberDays() = numberDays
//    fun getExpiredDate() = expiredDate
//
//}