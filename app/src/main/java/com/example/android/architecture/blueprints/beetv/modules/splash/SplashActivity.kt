package com.example.android.architecture.blueprints.beetv.modules.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.beetv.BuildConfig
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.Status
import com.example.android.architecture.blueprints.beetv.manager.*
import com.example.android.architecture.blueprints.beetv.modules.NoInternetActivity
import com.example.android.architecture.blueprints.beetv.modules.ads.AdsActivity
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.login.LoginFragment
import com.example.android.architecture.blueprints.beetv.util.getViewModelFactory
import com.example.android.architecture.blueprints.beetv.util.toMillisecond
import com.google.android.material.snackbar.Snackbar
import java.io.IOException


class SplashActivity : AppCompatActivity() {
    val TAG = SplashActivity::class.java.name
    private val viewModel by viewModels<SplashViewModel> { getViewModelFactory() }
    private var isLoadCategories = false
    private var isLoadTopMovie = false
    private var isLoadNotice = false
    private var isLoadPersionalNotice = false
    private var isUpdateProfile = false
    private var isLoadADS = false
    private var isFavorite = false
    private var isWatchHistory = false
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        checkConnection()
    }


    override fun onResume() {
        super.onResume()
//        if (!checkPermissions()) {
//            requestPermissions()
//        } else {
//
//
//        }
    }


    private fun checkConnection() {
        if (checkInternet())
            getData()
        else {
            startActivity(Intent(this, NoInternetActivity::class.java))
            finish()
        }
    }

    private fun getMasterData() {
        viewModel.getCategories().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get category success ")
                        CategoryManager.getInstance().setData(it.data?.results?.objects?.rows)
                        isLoadCategories = true
                        checkLoadData()
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })
        viewModel.getTopMovie().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        val list = it.data?.results?.objects?.rows
//                        val list = it.data?.results?.objects?.rows!!.sortedByDescending {
//                            bMovie -> bMovie.updated_at.toMillisecond()
//                        }
                        MovieManager.getInstance().setData(list)
                        isLoadTopMovie = true
                        checkLoadData()
                        Log.d(TAG, "Get top movie success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get top movie loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get top movie error ")
                    }
                }
            }
        })
        viewModel.getAdsList().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        isLoadADS = true
                        ADSManager.getInstance().setData(it.data?.results?.objects?.rows)

                        checkLoadData()
                        Log.d(TAG, "Get ads success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get ads loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get ads error ")
                    }
                }
            }
        })
        viewModel.getNoticeList().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get notice success ")
                        NoticeManager.getInstance().setData(it.data?.results?.objects?.rows)
                        isLoadNotice = true
                        checkLoadData()
                        if (AccountManager.getInstance().isLoggedIn()){
                            getPersonalNotice()
                        }else isLoadPersionalNotice = true
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get notice loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get notice error ")
                    }
                }
            }
        })
        checkUpdateProfile()
    }

    private fun getPersonalData() {
        viewModel.getFavoriteList().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get favorite success ")
                        FavoriteManager.getInstance().setData(it.data?.results?.objects?.rows)
                        isFavorite = true

                        checkLoadData()
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get favorite loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get favorite error ")
                        isFavorite = true

                    }
                }
            }
        })
        viewModel.getWatchHistories().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        WatchHistoryManager.getInstance().setData(it.data?.results?.objects?.rows)
                        isWatchHistory = true
                        checkLoadData()
                    }
                    Status.LOADING -> {

                    }
                    Status.ERROR -> {
                        isWatchHistory = true
                    }
                }
            }
        })

    }

    private fun getPersonalNotice(){
        viewModel.getPersonalNotices().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get notice success ")
                        val list =  NoticeManager.getInstance().getData()
                        list.addAll(it.data?.results?.objects?.rows?: mutableListOf())
                        NoticeManager.getInstance().setData(list)
                        isLoadPersionalNotice = true

                        checkLoadData()
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get notice loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get notice error ")
                    }
                }
            }
        })
    }
    private fun getData() {
        viewModel.getAccountInfo().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Login by token success")
                        if (it.data != null) {
                            AccountManager.getInstance().setAccount(it.data.results.one)
                            getMasterData()
                            getPersonalData()
                        } else {
                            isFavorite = true
                            isWatchHistory = true
                            getMasterData()
                        }
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get profile loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, it.message)
                        isFavorite = true
                        isWatchHistory = true
                        getMasterData()
                    }
                }
            }
        })
    }

    private fun checkLoadData() {
        if (isLoadCategories && isLoadTopMovie && isLoadNotice && isLoadADS && isFavorite && isWatchHistory && isUpdateProfile && isLoadPersionalNotice) {
            if (ADSManager.getInstance().hasAds()) {
                startActivity(Intent(this@SplashActivity, AdsActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            }
            finish()
        }

    }

    private fun checkInternet(): Boolean {
//        var connected = false
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
//        return connected

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected

//        return true
    }


    private fun checkUpdateProfile() {
        if (AccountManager.getInstance().isLoggedIn()) {
//            viewModel.loadMyLocation()
//            viewModel.mLocationResult.observe(this, Observer {
            var version = ""
            try {
                val pInfo: PackageInfo =  this.packageManager.getPackageInfo(this.packageName, 0)
                version = pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
                viewModel.updateMacAddress(0.0, 0.0, version).observe(this, Observer { it ->
                    run {
                        when (it.status) {
                            Status.SUCCESS -> {
                                isUpdateProfile = true
                                Log.d("yenyen", "update mac address")

                                checkLoadData()
                            }
                            Status.LOADING -> {

                            }
                            Status.ERROR -> {
                                Log.d("yenyen", "error ${it.message}")
                                isUpdateProfile = true
                                checkLoadData()
                            }
                        }
                    }
                })

//            })

        } else isUpdateProfile = true

    }


}