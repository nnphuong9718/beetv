/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.beetv.modules.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseActivity
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.helpers.AppEvent
import com.example.android.architecture.blueprints.beetv.modules.dialogs.ExitAppDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.SettingDialog
import com.example.android.architecture.blueprints.beetv.modules.login.LoginFragment
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuFragment
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailFragment
import com.example.android.architecture.blueprints.beetv.modules.search.SearchFragment
import com.example.android.architecture.blueprints.beetv.util.ToastUtils.toast
import com.example.android.architecture.blueprints.beetv.util.download.DownloadController

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts


/**
 * Main activity for the todoapp. Holds the Navigation Host Fragment and the Drawer, Toolbar, etc.
 */
class HomeActivity : BaseActivity() {

    private var mExitTime: Long = 0
    private lateinit var homeFragment: HomeFragment
    private var currentFragment: BaseFragment? = null
    private var mFocusMap: MutableMap<String, View> = mutableMapOf()
    lateinit var mAppEvent: AppEvent

    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        mAppEvent = AppEvent()
        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            currentFragment = homeFragment
            supportFragmentManager.fragments?.forEach { fragment ->
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_content, homeFragment, HomeFragment.TAG)
                    .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            val fr: BaseFragment? = supportFragmentManager.findFragmentById(R.id.main_content) as BaseFragment?
            currentFragment = fr

            if (currentFragment?.isHidden == true) showFragment()

            currentFragment?.onBackPress()
//            if (currentFragment is HomeFragment)
//                homeFragment.onBackPress()
//            else if (currentFragment is MenuFragment)
//                (currentFragment as MenuFragment).onBackPress()
//            else if (currentFragment is SearchFragment)
//                (currentFragment as SearchFragment).onBackPress()
//            else if(currentFragment is MovieDetailFragment)
//                (currentFragment as MovieDetailFragment).onBackPress()
        }

        checkStoragePermission()

    }

    fun addFragment(fragment: BaseFragment, TAG: String, shouldHideCurrent: Boolean = true) {
        val focusView = currentFragment?.view?.findFocus()
        if (currentFragment != null && focusView != null) {
            mFocusMap[currentFragment!!.getMyTag()] = focusView
        }
        supportFragmentManager.beginTransaction()
                .addToBackStack(TAG)
                .add(R.id.main_content, fragment, fragment.getMyTag())
                .commit()
        if (shouldHideCurrent) {
            supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .commit()
        }
    }

    private fun showFragment() {
        supportFragmentManager.beginTransaction()
//            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(currentFragment!!)
                .commit()
        val focusView = mFocusMap[currentFragment!!.getMyTag()]
//        focusView?.animation?.cancel()
//        focusView?.animation = null
        focusView?.requestFocus()
    }

    override fun onBackPressed() {
        if (currentFragment is MovieDetailFragment) {
            (currentFragment as MovieDetailFragment).back()
        } else if (currentFragment is LoginFragment) {
            (currentFragment as LoginFragment).back()
        }  else {
            val homeFrag = supportFragmentManager.findFragmentByTag(HomeFragment.TAG)
            if (supportFragmentManager.backStackEntryCount > 0 && !homeFrag!!.isVisible) {
                supportFragmentManager.popBackStack()
            } else {
                val exitAppDialog = ExitAppDialog()
                exitAppDialog.show(supportFragmentManager, "exitApp")
                exitAppDialog.onClickExitListener = {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }
                    finish()
                }
            }

        }

    }

    fun backFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        currentFragment?.myOnKeyDown(keyCode)
        return super.onKeyUp(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (DownloadController.IS_INSTALL) {
            (currentFragment as HomeFragment).onInstallFunc();
            DownloadController.IS_INSTALL = false;
        } else if (DownloadController.IS_UNINSTALL) {
//            (currentFragment as HomeFragment).onUninstallFunc();
            DownloadController.IS_UNINSTALL = false;
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                Toast.makeText(baseContext, "permission granted", Toast.LENGTH_LONG).show()
                if (currentFragment is HomeFragment) {
                    (currentFragment as HomeFragment).onStartDownloadAndInstallApk()
                }
            } else {
                // Permission request was denied.
                Toast.makeText(baseContext, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
    }


    fun checkStoragePermission(): Boolean {
        // Check if the storage permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            // start downloading
            return true
        }
        return false
    }

    fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(baseContext, R.string.storage_access_required, Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
            )
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
            )
        }
    }


}