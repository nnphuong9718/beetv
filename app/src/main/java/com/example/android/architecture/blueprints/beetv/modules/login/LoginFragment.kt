package com.example.android.architecture.blueprints.beetv.modules.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.beetv.BuildConfig
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.models.Status
import com.example.android.architecture.blueprints.beetv.databinding.FragmentLoginBinding
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import com.example.android.architecture.blueprints.beetv.manager.NoticeManager
import com.example.android.architecture.blueprints.beetv.manager.WatchHistoryManager
import com.example.android.architecture.blueprints.beetv.modules.dialogs.SuccessDialog
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.home.HomeFragment
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.getViewModelFactory
import com.example.android.architecture.blueprints.beetv.util.setupSnackbar
import com.example.android.architecture.blueprints.beetv.util.setupSnackbarString
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl
import com.google.android.material.snackbar.Snackbar


class LoginFragment : BaseFragment() {
    companion object {
        val TAG = LoginFragment::class.java.name
    }

    private val viewModel by viewModels<LoginViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentLoginBinding
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    private var isFavorite = false

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var isWatchHistory = false
    private var isLoadNotice = false
    private var isLoadPersionalNotice = false

    var onLoginSuccessListener: (() -> Unit)? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            click = ClickProxy(viewModel, this@LoginFragment)
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        metroViewBorderImpl = MetroViewBorderImpl(context)
        metroViewBorderImpl.attachTo(viewDataBinding.main)
        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            if (isVisible) {
                metroViewBorderImpl.view.tag = newFocus
                changeBackgroundButton(oldFocus, newFocus)
            }
        }
        viewDataBinding.etID.requestFocus()

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


    private fun getDataList() {
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

                    }
                }
            }
        })

        // Get notice
        viewModel.getNoticeList().observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get notice success ")
                        NoticeManager.getInstance().setData(it.data?.results?.objects?.rows)
                        isLoadNotice = true
                        getPersonalNotices()
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



//        viewModel.loadMyLocation()
//        viewModel.mLocationResult.observe(this, Observer {
        var version = ""
        try {
            val pInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        viewModel.updateMacAddress(0.0, 0.0, version).observe(this, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d("yenyen", "update mac address")

                        checkLoadData()
                    }
                    Status.LOADING -> {

                    }
                    Status.ERROR -> {
                        Log.d("yenyen", "error ${it.message}")
                        checkLoadData()
                    }
                }
            }
        })

//        })
    }

    fun getPersonalNotices() {
        // Get personal notice
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

    private fun checkLoadData() {
        if (isFavorite && isWatchHistory && isLoadNotice && isLoadPersionalNotice) {
            activity?.supportFragmentManager?.popBackStack()
            val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = requireActivity().currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            onLoginSuccessListener?.invoke()

//            activity?.let {
//                it.supportFragmentManager.fragments.forEach { fragment ->
//                    it.supportFragmentManager.beginTransaction().remove(fragment).commit()
//                }
//            }
//            val fragment2 = HomeFragment()
//            val args = Bundle().apply {
//                putString("type", Constants.LOGIN)
//            }
//            fragment2.arguments = args
//            (requireActivity() as HomeActivity).addFragment(fragment2, HomeFragment.TAG)

        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbarString(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        view?.setupSnackbar(this, viewModel.snackbarInt, Snackbar.LENGTH_SHORT)
    }

    public class ClickProxy(val viewModel: LoginViewModel, val fragment: LoginFragment) {

        fun login() {
            val id = viewModel.id.value
            val password = viewModel.password.value

//            val id = "aws20"
//            val password = "28558507"
            if (id == null) {
                viewModel.snackbarInt.value = Event(R.string.empty_id_message)
                return
            }

            if (password == null) {
                viewModel.snackbarInt.value = Event(R.string.empty_password_message)
                return
            }
            viewModel.login(id, password).observe(fragment.viewLifecycleOwner, Observer { it ->
                run {
                    when (it.status) {
                        Status.SUCCESS -> {
                            fragment.getDataList()
                            var version = ""
                            try {
                                val pInfo: PackageInfo = fragment.requireContext().packageManager.getPackageInfo(fragment.requireContext().packageName, 0)
                                version = pInfo.versionName
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }
//                            viewModel.loadMyLocation()
//                            viewModel.mLocationResult.observe(fragment.viewLifecycleOwner, Observer {
                            viewModel.updateMacAddress(0.0, 0.0, version)
//                            })
                        }
                        Status.LOADING -> {


                        }
                        Status.ERROR -> {
                            viewModel.snackbarText.value = Event(it.message!!)
                        }
                    }
                }
            })

        }

        fun cancel() {
            (fragment.requireActivity() as HomeActivity).backFragment()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    View.OnClickListener {
                        // Request permission
                        ActivityCompat.requestPermissions(requireActivity(),
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    })

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")

        if (requestCode != REQUEST_PERMISSIONS_REQUEST_CODE) return

        when {
            grantResults.isEmpty() ->
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
            } // Permission granted.

            else -> // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
        }

    }

    override fun onResume() {
        super.onResume()
//        if (!checkPermissions()){
//            requestPermissions()
//        }
    }

    fun back() {
        activity?.supportFragmentManager?.popBackStack()
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showSnackbar(
            mainTextStringId: Int,
            actionStringId: Int,
            listener: View.OnClickListener
    ) {
        Snackbar.make(viewDataBinding.container, getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener)
                .show()
    }
}