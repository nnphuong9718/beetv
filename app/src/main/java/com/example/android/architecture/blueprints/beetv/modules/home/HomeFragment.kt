package com.example.android.architecture.blueprints.beetv.modules.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Looper.getMainLooper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.android.architecture.blueprints.beetv.*
import com.example.android.architecture.blueprints.beetv.common.BeeMediaType
import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter2
import com.example.android.architecture.blueprints.beetv.data.models.BNotice
import com.example.android.architecture.blueprints.beetv.data.models.BVersion
import com.example.android.architecture.blueprints.beetv.data.models.Status
import com.example.android.architecture.blueprints.beetv.databinding.FragmentHomeBinding
import com.example.android.architecture.blueprints.beetv.manager.*
import com.example.android.architecture.blueprints.beetv.modules.dialogs.*
import com.example.android.architecture.blueprints.beetv.modules.login.LoginFragment
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuFragment
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailFragment
import com.example.android.architecture.blueprints.beetv.modules.player.BeeVideoPlayerActivity
import com.example.android.architecture.blueprints.beetv.modules.search.SearchFragment
import com.example.android.architecture.blueprints.beetv.modules.splash.SplashActivity
import com.example.android.architecture.blueprints.beetv.util.*
import com.example.android.architecture.blueprints.beetv.util.download.DownloadController
import com.example.android.architecture.blueprints.beetv.util.download.DownloadTask
import com.example.android.architecture.blueprints.beetv.widgets.AnimationAutoTextScroller
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : BaseFragment() {
    val viewModel by viewModels<HomeViewModel> { getViewModelFactory() }

    companion object {
        val TAG = "HomeFragment"
        var TOP_MOVIE_UPDATE_TIME: Long = 30 * 60 * 1000
        var NOTICE_UPDATE_TIME: Long = 20 * 1000
        var SCRIPT_UPDATE_TIME: Long = 12 * 60 * 60 * 1000
    }

    private var mType: String? = null
    private lateinit var viewDataBinding: FragmentHomeBinding
    private lateinit var adapter: TopMovieAdapter2
    private var mNotices = mutableListOf<BNotice>()
    private var positionNotice = 0
    private val someHandler2 = Handler(Looper.getMainLooper())
    private val someHandler = Handler(Looper.getMainLooper())
    private val someHandlerTime = Handler(getMainLooper())

       val dialog = UpdateAppDialog()
    private lateinit var downloadTask: DownloadTask

    private var version : BVersion ?= null
    var isShowUpdateDialog = false
     var isDownloading = false

    private var isGetNotice = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            click = ClickProxy(viewModel, this@HomeFragment)
            viewmodel = viewModel
        }
        viewDataBinding.notiLayout.isFocusable = false
        viewDataBinding.notiScrollview.isFocusable = false
        viewDataBinding.tvNoti.isFocusable = false
        val typeface = context?.let { ResourcesCompat.getFont(it, R.font.notosans_regular) }
        viewDataBinding.tvClock.setTypeface(typeface)
        mType = arguments?.getString("type")
        return viewDataBinding.root
    }

    private fun setupGUI() {
        viewModel.setId(requireContext())
        if (AccountManager.getInstance().isLoggedIn()) {
            viewModel.dateExpired.value = AccountManager.getInstance().getAccountCardStatus()
        }
    }

    override fun getMyTag(): String {
        return "HomeFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        viewDataBinding.tvNoti.isSelected = true
        setupGUI()
//        showDialog()
        setupSnackbar()
        showTime()
        setUpTopMovie()
        setupNavigation()
        showNotice()

        someHandler.postDelayed(object : Runnable {
            override fun run() {
                getTopMovie()
                someHandler.postDelayed(this, TOP_MOVIE_UPDATE_TIME)
            }
        }, TOP_MOVIE_UPDATE_TIME)


        someHandler2.postDelayed(object : Runnable {
            override fun run() {
                getNotice()


                val sdf3 = SimpleDateFormat("yyyy.MM.dd")
                sdf3.timeZone = TimeZoneManager.getInstance().getData()
                viewDataBinding.tvId.text = if (AccountManager.getInstance().isLoggedIn())
                    AccountManager.getInstance().getAccount()?.getUserIdentify() else
                    FormatUtils.getDate(requireContext()) + "\n" + sdf3.format(Date())

                if (AccountManager.getInstance().isLoggedIn())
                    viewDataBinding.tvDate.text = AccountManager.getInstance().getPremiumDuration().toExpiredDate()
                someHandler2.postDelayed(this, SCRIPT_UPDATE_TIME)
            }
        }, SCRIPT_UPDATE_TIME)
        updateVersionApp()
    }

    private fun showNotice() {
        NoticeManager.getInstance().noticeList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            mNotices.clear()
            mNotices.addAll(NoticeManager.getInstance().noticeList.value?.toMutableList() ?: mutableListOf())

            positionNotice = 0
            if (mNotices.isNotEmpty()) {
                animationNoti()
            }
        })
    }

    var isFirst = true

    private fun animationNoti() {
        val notice = mNotices[positionNotice].title
//        val notice = "a_-----------------------------------------------------------------------------------q"
        var textWidth = viewDataBinding.tvNoti.getPaint().measureText(notice).toInt()
        val textscroller = AnimationAutoTextScroller(viewDataBinding.tvNoti, resources.getDimensionPixelOffset(R.dimen.size_550).toFloat(), textWidth * 20,

//        val ratio = 1000/mNotices[positionNotice].title.length*10
//        var duration = (mNotices[positionNotice].title.length * (ratio))
//        val ratio = 1000/textWidth*10
//        var duration = (textWidth * (ratio))
//
//        if (mNotices[positionNotice].title.length > 30) {
//            duration = mNotices[positionNotice].title.length * 700
//        }
//        if (mNotices[positionNotice].title.length > 50) {
//            duration = mNotices[positionNotice].title.length * 500
//        }
//        if (mNotices[positionNotice].title.length > 100) {
//            duration = mNotices[positionNotice].title.length * 300
//        }
//        if (mNotices[positionNotice].title.length > 200) {
//            duration = mNotices[positionNotice].title.length * 100
//        }
//        val textscroller = AnimationAutoTextScroller(viewDataBinding.tvNoti, resources.getDimensionPixelOffset(R.dimen.size_550).toFloat(), duration,
                object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        viewDataBinding.tvNoti.clearAnimation()
                        animationNoti()
                    }

                    override fun onAnimationStart(p0: Animation?) {

                    }
                },viewDataBinding.tvNoti.getPaint().measureText(notice).toInt(), isFirst)
        isFirst = false
        textscroller.setScrollingText(notice)
//        textscroller.setDuration(mNotices[positionNotice].title.length*1000)
        textscroller.start()
        positionNotice = if (mNotices.size - 1 > positionNotice) positionNotice + 1 else 0
    }

    private fun showTime() {
        someHandlerTime.postDelayed(object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("HH:mm")
                sdf.timeZone = TimeZoneManager.getInstance().getData()
                viewModel.time.value = sdf.format(Date())

                someHandlerTime.postDelayed(this, 1000)
            }
        }, 10)
    }


    private fun setupNavigation() {
        viewModel.openMenuEvent.observe(viewLifecycleOwner, EventObserver {
            openMenu(it)
        })

        viewModel.openMovieDetailEvent.observe(viewLifecycleOwner, EventObserver {
            openMovieDetail(it)
        })
    }

    private fun openMenu(category: String) {
        val fragment = MenuFragment()
        val args = Bundle().apply {
            putString("type", category)
        }
        fragment.arguments = args
        (requireActivity() as HomeActivity).addFragment(fragment, MenuFragment.TAG)
    }

    private fun openMovieDetail(id: String) {
        val fragment = MovieDetailFragment()
        val args = Bundle().apply {
            putString("movie_id", id)
        }
        fragment.arguments = args
        (requireActivity() as HomeActivity).addFragment(fragment, MovieDetailFragment.TAG)
    }

    @SuppressLint("LogNotTimber")
    private fun showDialog() {
        if (!BeeTVApplication.isShowPopup) {
            val dialog = NotiDialog()
            dialog.show(childFragmentManager, "abc")
            BeeTVApplication.isShowPopup = true
        } else if (AccountManager.getInstance().canShowNotice()) {
            val expirationDialog = ExpirationDialog()
            expirationDialog.show(childFragmentManager, "expiration")
        }

        if (!mType.isNullOrEmpty()) {
            if (mType.equals(Constants.REGISTER)) {
                val successDialog = SuccessDialog()
                successDialog.icon = R.drawable.ic_register_success
                successDialog.title = getString(R.string.register_successfully)
                successDialog.show(childFragmentManager, "success")
            }

            if (mType.equals(Constants.LOGIN)) {
                val successDialog = SuccessDialog()
                successDialog.show(childFragmentManager, "success")
            }
            arguments?.remove("type")
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarInt, Snackbar.LENGTH_SHORT)
    }

    private fun showLoginSuccess() {
        val successDialog = SuccessDialog()
        successDialog.show(childFragmentManager, "success")
    }

    private fun restartApp() {
        if (context is Activity) {
            (context as Activity).finish()
        }
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
//        Runtime.getRuntime().exit(0)

//        val mStartActivity = Intent(context, SplashActivity::class.java)
//        val mPendingIntentId = 123456
//        val mPendingIntent: PendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
//        val mgr: AlarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }

    class ClickProxy(val viewModel: HomeViewModel, val fragment: HomeFragment) {
        fun openSearch() {
//            fragment.cancelFocus()
            val fragment2 = SearchFragment()
            val args = Bundle().apply {
                putString("type", Constants.TYPE_CATEGORY.SEARCH.type)
            }
            fragment2.arguments = args
            (fragment.requireActivity() as HomeActivity).addFragment(fragment2, SearchFragment.TAG)
        }

        fun openFavorite() {
            if (AccountManager.getInstance().getAccount() != null) {
                viewModel.openMenu(Constants.TYPE_CATEGORY.FAVORITE.type)
            } else openDialogRequireLogin()
        }

        fun openSetting() {
            val settingDialog = SettingDialog()
            settingDialog.show(fragment.childFragmentManager, "setting")
            settingDialog.onClickLoginListener = {
                val loginFragment = LoginFragment()
                loginFragment.onLoginSuccessListener = {
                    fragment.reloadId()
                    val successDialog = SuccessDialog()
                    successDialog.show(fragment.childFragmentManager, "success")
                }
                (fragment.requireActivity() as HomeActivity).addFragment(loginFragment, LoginFragment.TAG, false)

            }
            settingDialog.onClickLogoutListener = {
                val logoutDialog = LogoutDialog()
                logoutDialog.show(fragment.childFragmentManager, "logout")
                logoutDialog.onClickLogoutListener = {
                    AccountManager.getInstance().reset()
                    viewModel.dateExpired.value = ""
                    viewModel.setId(fragment.requireContext())
                    settingDialog.hideLayoutId()
                }
            }

            settingDialog.onTimeZoneSelectedListener = {
                fragment.showTime()

                viewModel.setId(fragment.requireContext())
            }

            settingDialog.onChangeLanguageListener = {
                fragment.restartApp()
            }

            settingDialog.onDeleteFavoriteListener = {
                if (AccountManager.getInstance().isLoggedIn()) {
                    fragment.deleteFavorite()
                } else openDialogRequireLogin()
            }

            settingDialog.onDeleteHistoryListener = {
                if (AccountManager.getInstance().isLoggedIn()) {
                    fragment.deleteWatchHistory()
                } else openDialogRequireLogin()
            }
        }

        fun openPlayback() {
            if (AccountManager.getInstance().isLoggedIn()) {
                viewModel.openMenu(Constants.TYPE_CATEGORY.PLAYBACK.type)
            } else openDialogRequireLogin()
        }

        fun openLiveMenu() {
            if (AccountManager.getInstance().isLoggedIn()) {
                if (AccountManager.getInstance().supportPremiumLive()) {
                    val intent = Intent(fragment.requireContext(), BeeVideoPlayerActivity::class.java).apply {
                        putExtra(Const.MEDIA_TYPE_ARG, BeeMediaType.LIVE.toString())
                    }
                    fragment.startActivity(intent)
                } else {
                    viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
                }
            } else openDialogRequireLogin()
        }

        fun openMovieMenu() {
            viewModel.openMenu(Constants.TYPE_CATEGORY.MOVIE.type)
        }

        fun openDramaMenu() {
            viewModel.openMenu(Constants.TYPE_CATEGORY.DRAMA.type)
        }

        fun openEntertainmentMenu() {
            viewModel.openMenu(Constants.TYPE_CATEGORY.ENTERTAINMENT.type)
        }

        fun openEducationMenu() {
            viewModel.openMenu(Constants.TYPE_CATEGORY.EDUCATION.type)
        }

        fun openChildrenMenu() {
            viewModel.openMenu(Constants.TYPE_CATEGORY.KID.type)
        }

        private fun openDialogRequireLogin() {
            val dialog = RequireLoginDialog()
            dialog.show(fragment.childFragmentManager, "login")
            dialog.onClickLoginListener = {
                val loginFragment = LoginFragment()
                loginFragment.onLoginSuccessListener = {
                    fragment.reloadId()
                    val successDialog = SuccessDialog()
                    successDialog.show(fragment.childFragmentManager, "success")
                }
                (fragment.requireActivity() as HomeActivity).addFragment(loginFragment, LoginFragment.TAG, false)
            }
        }
    }

    private fun reloadId() {
        if (AccountManager.getInstance().isLoggedIn()) {
            viewModel.dateExpired.value = AccountManager.getInstance().getAccountCardStatus()
            viewModel.setId(requireContext())
        }
    }

    private fun setUpTopMovie() {
        val widthItem: Int
        if (BuildConfig.FLAVOR.equals("mobile")) {
            widthItem = (DisplayAdaptive.getDisplayMetrics(requireActivity()).widthPixels
                    - requireContext().resources.getDimensionPixelOffset(R.dimen.size_8) * 8) / 6
        } else {
            widthItem = (DisplayAdaptive.getDisplayMetrics(requireActivity()).widthPixels
                    - requireContext().resources.getDimensionPixelOffset(R.dimen.size_8) * 6
                    - requireContext().resources.getDimensionPixelOffset(R.dimen.size_24) * 2) / 6
        }
//        val widthItem = DisplayAdaptive.getBestMovieThumbWidth(requireActivity()).toInt() // context!!.resources.getDimensionPixelOffset(R.dimen.size_thumb_width)
        val heightItem = (widthItem * DisplayAdaptive.THUMB_RATIO).toInt()
        adapter = TopMovieAdapter2(viewModel, widthItem, heightItem)
        viewDataBinding.rvMovie.adapter = adapter
        viewDataBinding.rvMovie.scrollToPosition(0)
    }

    override fun onBackPress() {
        super.onBackPress()
        reloadId()
    }


//    fun cancelFocus() {
//        lastFocus = lastView ?: lastView2
//        isCancelFocus = true
//    }
//
//    fun onBackPress() {
//        if (isAdded) {
//            showTime()
//            viewModel.setId(requireContext())
//            isCancelFocus = false
//            if (lastFocus != null) {
//                lastFocus!!.requestFocus()
//                lastFocus = null
//            }
//        }
//    }

    private fun deleteFavorite() {
        viewModel.removeAllFavorite().observe(viewLifecycleOwner) { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        FavoriteManager.getInstance().removeAllFavoriteMovie()
                        Log.d(TAG, "Delete favorites success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Delete favorites loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Delete favorites error ")
                    }
                }
            }
        }
    }

    private fun deleteWatchHistory() {
        viewModel.removeAllWatchHistory().observe(viewLifecycleOwner) { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        WatchHistoryManager.getInstance().removeAllWatchHistory()
                        Log.d(TAG, "Delete watch history success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Delete watch history loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Delete watch history error ")
                    }
                }
            }
        }
    }

    private fun getTopMovie() {
        val lifecycleOwner = requireContext().lifecycleOwner()
        if (lifecycleOwner != null)
            viewModel.getTopMovie().observe(lifecycleOwner) { it ->
                run {
                    when (it.status) {
                        Status.SUCCESS -> {
                            val list = it.data?.results?.objects?.rows!!
//                                .sortedByDescending {
//                            bMovie -> bMovie.updated_at.toMillisecond()
//                        }
                            MovieManager.getInstance().setData(list)
                            setUpTopMovie()
                            adapter.submitList(list)
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
            }
    }

    private fun getNotice() {
        viewModel.getNoticeList().observe(viewLifecycleOwner) { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get notice success ")
                        NoticeManager.getInstance().setData(it.data?.results?.objects?.rows)
                        getPersonalNotice()
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get notice loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get notice error ")
                    }
                }
            }
        }

    }

    private fun getPersonalNotice() {
        if (AccountManager.getInstance().isLoggedIn()) {
            viewModel.getPersonalNotices().observe(viewLifecycleOwner) { it ->
                run {
                    when (it.status) {
                        Status.SUCCESS -> {
                            Log.d(TAG, "Get notice success ")
                            val list = NoticeManager.getInstance().getData()
                            list.addAll(it.data?.results?.objects?.rows ?: mutableListOf())
                            NoticeManager.getInstance().setData(list)
//                            showNotice()
                        }
                        Status.LOADING -> {
                            Log.d(TAG, "Get notice loading")
                        }
                        Status.ERROR -> {
                            Log.d(TAG, "Get notice error ")
                        }
                    }
                }
            }
        }
//        } else showNotice()
    }

    override fun onDestroyView() {
        someHandler2.removeCallbacksAndMessages(null)
        someHandler.removeCallbacksAndMessages(null)
        someHandlerTime.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    fun updateVersionApp() {
        viewModel.getVersion().observe(viewLifecycleOwner) { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "Get notice success ")

                        val versionList = it.data?.results?.objects?.rows ?: mutableListOf()
                        if (versionList.isNotEmpty()) {
                             version = versionList.first()
                            val versionApp = BuildConfig.VERSION_CODE
                            if (version!!.name.toIntOrNull() ?: 0 > versionApp) {
                                dialog.onClickConfirmListener = {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || (Build.VERSION.SDK_INT > Build.VERSION_CODES.O && requireContext().packageManager.canRequestPackageInstalls())) {
                                        if ((activity as HomeActivity).checkStoragePermission()) {
                                            onStartDownloadAndInstallApk()
                                        } else {
                                            (activity as HomeActivity).requestStoragePermission()
                                        }
                                    } else {
                                        grantedUnknownSourceActivityResult.launch(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
                                    }

                                 }

                                dialog.onBackClickListener = {
                                    val exitAppDialog = ExitAppDialog()
                                    exitAppDialog.show(childFragmentManager, "exitApp")
                                    exitAppDialog.onClickExitListener = {

                                        activity?.finishAffinity()
                                    }
                                }
                                isShowUpdateDialog = true
                                dialog.show(childFragmentManager,"update")
                            }else{
                                showDialog()
                            }

                        } else {
                        }
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Get notice loading")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get notice error ")
                    }
                }
            }
        }
    }





    fun installApk(path: String) {
        try {
            val file = File(path);
            val intent = Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {
                val packageName = requireContext().packageName
                val downloaded_apk: Uri = FileProvider.getUriForFile(requireContext(), packageName + ".provider", file);
                intent.setDataAndType(downloaded_apk, DownloadController.MIME_TYPE);
                val resInfoList = requireContext().packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                resInfoList.forEach {
                    val resolveInfo = it
                    requireContext().grantUriPermission(packageName + DownloadController.PROVIDER_PATH, downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireContext().grantUriPermission(packageName + DownloadController.PROVIDER_PATH, downloaded_apk, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(file), DownloadController.MIME_TYPE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            DownloadController.IS_INSTALL = true;
            dialog.dismiss()

            isShowUpdateDialog = false
            installAppActivityResult.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
    val installAppActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        Toast.makeText(requireContext(), "cai xong r ne", Toast.LENGTH_LONG).show()
        activity?.finishAffinity()



    }

    fun onStartDownloadAndInstallApk() {
        val downloadUrl: String ?= version?.url_apk
        if (downloadUrl != null){
        if (DownloadController.getInstance().isApkDownloaded(requireContext(), downloadUrl)) {
            installApk(DownloadController.getInstance().getApkPathByUrl(requireContext(), downloadUrl))
        } else {

            downloadTask = DownloadTask(requireContext())
            downloadTask.execute(downloadUrl)
            isDownloading = true
            downloadTask.onSuccessListener = {
                dialog.dismiss()
                isDownloading = false
                if (DownloadController.getInstance().isApkDownloaded(requireContext(), downloadUrl)) {
                    installApk(DownloadController.getInstance().getApkPathByUrl(requireContext(), downloadUrl))
                }
            }
            downloadTask.onFailedLister = {
                isDownloading = false
                dialog.dismiss()
                ToastUtils.toast(requireContext(),"Download fail")
            }
        }}

    }


    fun onInstallFunc() {
        // App download 1 version của app về từ app store (tên apk: abcd.apk)
        // Nếu đã cài version này rùi thì rename apk thên suffix _done vào. (vd: abcd_done.apk)
        // Nếu check có apk có suffix _done thì chứng tỏ đang cài app, ko yêu cầu download hay update nữa
        // Nếu check
        // Nếu đã cài mà gỡ app thì bỏ suffix _done này ra (abcd.apk)
//        if (DownloadController.getInstance().isPackageInstalled(requireContext(),)) {
//            val downloadUrl = mAppDetail?.versions?.get(0)?.url_apk!!;
//            val strs = downloadUrl?.split("/")?.toTypedArray()
//            val fileName: String = strs!![strs.size-1]
//            val fileNameArr = fileName.split(".apk");
//            if (fileNameArr.isNotEmpty()) {
//                val originalApkNameWithoutExtension = fileNameArr[0];
//                val fullPathOriginApk = DownloadController.getInstance().getApkPathByName(requireContext(), "$originalApkNameWithoutExtension.apk");
//                val fullPathInstalledApk = DownloadController.getInstance().getApkPathByName(requireContext(), "$originalApkNameWithoutExtension${DownloadController.INSTALLED_SUFFIX}.apk");
//                var sourceFile = File(fullPathOriginApk);
//                val desFile = File(fullPathInstalledApk);
//                sourceFile.renameTo(desFile);
//
//
//                Log.d(TAG, "source file:" + sourceFile.name + sourceFile.exists());
//            }
//        }
    }


    private val grantedUnknownSourceActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        Toast.makeText(requireContext(), "dong y unknown source xong r ne", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || (Build.VERSION.SDK_INT > Build.VERSION_CODES.O && requireContext().packageManager.canRequestPackageInstalls())) {
            if ((activity as HomeActivity).checkStoragePermission()) {
                if (version != null)
                onStartDownloadAndInstallApk()
            } else {
                (activity as HomeActivity).requestStoragePermission()
            }
        }
    }



}