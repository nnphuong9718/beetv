package com.example.android.architecture.blueprints.beetv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager


public class NetworkChangeReceiver : BroadcastReceiver() {

    var mOnConnectListener: (() -> Unit) ? = null

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            if (isOnline(context)) {
                mOnConnectListener?.invoke()
            } else {
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}