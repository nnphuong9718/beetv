package com.example.android.architecture.blueprints.beetv.modules

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseActivity
import com.example.android.architecture.blueprints.beetv.modules.splash.SplashActivity
import com.example.android.architecture.blueprints.beetv.receiver.NetworkChangeReceiver

class NoInternetActivity : BaseActivity() {

    private lateinit var mNetworkReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        mNetworkReceiver = NetworkChangeReceiver()
        mNetworkReceiver.mOnConnectListener = {
            startActivity(Intent(this,SplashActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            mNetworkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mNetworkReceiver)
    }
}