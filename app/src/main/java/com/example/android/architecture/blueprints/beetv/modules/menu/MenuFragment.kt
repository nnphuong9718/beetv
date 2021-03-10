package com.example.android.architecture.blueprints.beetv.modules.menu

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.example.android.architecture.blueprints.beetv.*
import com.example.android.architecture.blueprints.beetv.common.BeeMediaType
import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.adapter.*
import com.example.android.architecture.blueprints.beetv.data.api.RecorderServer
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.example.android.architecture.blueprints.beetv.databinding.FragmentMenuBinding
import com.example.android.architecture.blueprints.beetv.manager.*
import com.example.android.architecture.blueprints.beetv.modules.dialogs.LogoutDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.RequireLoginDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.SettingDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.SuccessDialog
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.login.LoginFragment
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailFragment
import com.example.android.architecture.blueprints.beetv.modules.player.BeeVideoPlayerActivity
import com.example.android.architecture.blueprints.beetv.modules.search.SearchFragment
import com.example.android.architecture.blueprints.beetv.modules.splash.SplashActivity
import com.example.android.architecture.blueprints.beetv.util.*
import com.example.android.architecture.blueprints.beetv.widgets.AutoLayoutManager
import com.example.android.architecture.blueprints.beetv.widgets.CenterSmoothScroller
import com.example.android.architecture.blueprints.beetv.widgets.MovieListView
import com.google.android.flexbox.FlexboxLayout
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


class MenuFragment : BaseFragment() {
    companion object {
        val TAG = "MenuFragment"
    }

    private val viewModel by viewModels<MenuViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentMenuBinding
    private lateinit var mAdapter: MenuAdapter2
    private lateinit var mMovieAdapter: MenuMovieAdapter2
    private var mType: String? = null
    private var mPage: Int = 1
    private var isLoadMore = false
    private var livePositionCurrent = 0
    private val mLives = mutableListOf<BLive>()
    private var mLastId: String = ""
    private var mLastIdChannel: String = ""
    private var mLastIdProgram: Long = 0
    private var mRecyclerViewLive: ScrollView? = null
    private var mRecyclerViewLiveTime: RecyclerView? = null
    private var mRecyclerViewCalendar: RecyclerView? = null
    private var positionFocusMenu = 0
    private var mCategoryLast: BCategory? = null
    private var mLiveLast: BLive? = null
    private var isFirst = true
    private var didRunGuideAnimation = false
    private var didCloseGuideAnimation = false
    private var isFocusFirst = true
    private var timer = Timer()
    private var isRequestLiveView = false
    private var isRequestLiveFirstView = true
    private var isDupLiveView = true
    private var isSortUpdate = false
    private var isSortPopular = false
    private var isShowCalendarList = false
    private var DELAY: Long = 500
    private var positionCategory = 0
    private var mPosition = 0
    private var mCategorySelected: BCategory? = null
    private var isRequesting: Boolean = false
    private val someHandler = Handler(Looper.getMainLooper())
    private val someHandler2 = Handler(Looper.getMainLooper())
    private val hideMenuHandler = Handler()
    private var isAddView = false

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent?.hasExtra(BeeVideoPlayerActivity.NEXT_ACTION) == true) {
                val action = intent.getIntExtra(BeeVideoPlayerActivity.NEXT_ACTION, -1)
                val live = intent.getSerializableExtra(Const.MEDIA_DATA_ARG) as BLive
                mLives.forEachIndexed { i: Int, bLive: BLive ->
                    if (live.id == bLive.id) {
                        livePositionCurrent = i
                    }
                }
                if (action == BeeVideoPlayerActivity.FINISH_WITH_CHANNEL_UP) {
                    if (livePositionCurrent < mLives.size - 1) {
                        livePositionCurrent++
                        openPlay(mLives[livePositionCurrent])
                    }
                    print("UP")
                } else if (action == BeeVideoPlayerActivity.FINISH_WITH_CHANNEL_DOWN) {
                    // Todo handle play previous chapter video
                    if (livePositionCurrent > 0) {
                        livePositionCurrent--
                        openPlay(mLives[livePositionCurrent])
                    }
                    print("DOWN")
                }
            }
        }
    }

    override fun getMyTag(): String {
        return "MenuFragment"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        viewDataBinding = FragmentMenuBinding.inflate(inflater, container, false).apply {
            click = ClickProxy(viewModel, this@MenuFragment)
            viewmodel = viewModel
        }
        isAddView = true
        mType = arguments?.getString("type")
        viewModel.start(mType)
        val typeface = context?.let { ResourcesCompat.getFont(it, R.font.notosans_regular) }
        viewDataBinding.tvClock.setTypeface(typeface)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        if (activity is HomeActivity) {
            (activity as HomeActivity).mAppEvent.refreshAddFavoriteList.observe(this.viewLifecycleOwner) {
                if (mType == Constants.TYPE_CATEGORY.FAVORITE.type) {
                    mMovieAllList.add(0, it!!.movie!!)
                    viewDataBinding.rvDetailList2.setMovieList(mMovieAllList, mType)
                }
            }

            (activity as HomeActivity).mAppEvent.refreshRemoveFavoriteList.observe(this.viewLifecycleOwner) {
                if (mType == Constants.TYPE_CATEGORY.FAVORITE.type) {
                    var position = -1
                    mMovieAllList.forEachIndexed { index, bMovie ->
                        if (bMovie.id == it) {
                            position = index
                        }
                    }

                    if (position >= 0) {
                        mMovieAllList.removeAt(position)
                        viewDataBinding.rvDetailList2.removeItemAt(position)
                    }
                }
            }
        }

        showTime()
        setupMenuList()
        setupMovieList2()
        display()
        setupNavigation()
        setupSnackbar()
    }

    private fun getCategoryByName(): BCategory? {
        return CategoryManager.getInstance().getCategoryByName(mType!!)
    }

    private fun display() {
        if (mType == Constants.TYPE_CATEGORY.TV.type) {
            viewDataBinding.list.hide()
//            viewDataBinding.btSearch.isFocusable = false
//            viewDataBinding.btSetting.isFocusable = false
//            viewDataBinding.btPlayback.isFocusable = false
//            viewDataBinding.btFavorite.isFocusable = false

        } else {
            viewDataBinding.dynamicList.hide()
            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
            viewDataBinding.tvTypeMovie.text =
                    when (mType) {
                        Constants.TYPE_CATEGORY.MOVIE.type -> getString(R.string.movie)
                        Constants.TYPE_CATEGORY.DRAMA.type -> getString(R.string.drama)
                        Constants.TYPE_CATEGORY.ENTERTAINMENT.type -> getString(R.string.entertainment)
                        Constants.TYPE_CATEGORY.EDUCATION.type -> getString(R.string.preview_education)
                        Constants.TYPE_CATEGORY.FAVORITE.type -> getString(R.string.favorites)
                        Constants.TYPE_CATEGORY.PLAYBACK.type -> getString(R.string.playback_record)
                        else -> getString(R.string.child)
                    }
        }

        viewDataBinding.rvMenuItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

            }
        })
    }

    private fun setupMenuList() {
        viewDataBinding.rvMenuItem.layoutManager = GridLayoutManager(context, 1)
        if (mType == Constants.TYPE_CATEGORY.TV.type) {
            val data = LiveManager.getInstance().getData()

            mCategoryLast = if (data.first != null) data.first else {
                positionCategory++
                CategoryManager.getInstance().getData(mType!!)[positionCategory]
            }
            mLiveLast = data.second
            getLiveByCategoryId(mCategoryLast!!.id)
            viewDataBinding.btMore.show()
        } else {
            viewModel.categories.observe(viewLifecycleOwner) {
                viewModel.totalList.value = it.size.toNumberString()
                if (!isLastMenuVisible()) {
                    viewDataBinding.ivBottom.show()
                }
            }


        }
        initAdapterMenu()
    }


    private fun initAdapterMenu() {
        mAdapter = MenuAdapter2(viewModel, BeeTVApplication.context)
        mAdapter.type = mType!!
        viewDataBinding.rvMenuItem.adapter = mAdapter
        mAdapter.onFocusListener = { position, bCategory, viewSelected ->
            if (viewModel.mLiveRecyclerView != null) {
                mAdapter.viewFocus?.nextFocusRightId = (viewModel.mLiveRecyclerView as LiveAdapter2).positonRequest
                        ?: if ((viewModel.mLiveRecyclerView as LiveAdapter2).itemCount > 0) 400000 else 0
            }
        }

        mAdapter.submitList(viewModel.categories.value)
        viewDataBinding.rvMenuItem.requestFocus()
        viewModel.mMenuRecyclerView = viewDataBinding.rvMenuItem
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupMovieList2() {
        viewDataBinding.rvDetailList2.viewModel = viewModel
        val widthItem = (DisplayAdaptive.getDisplayMetrics(requireActivity()).widthPixels
                - requireContext().resources.getDimensionPixelOffset(R.dimen.size_190) // me
                - requireContext().resources.getDimensionPixelOffset(R.dimen.size_40) * 2 // outside padding
                - requireContext().resources.getDimensionPixelOffset(R.dimen.size_16) * 4) / 5// between padding item

        val heightItem = (widthItem * DisplayAdaptive.THUMB_RATIO).toInt()

        viewDataBinding.rvDetailList2.onFocusListener = {
            val position: Int = it.tag as Int
            viewDataBinding.tvCurrentPage.text = ceil((position + 1).toDouble() / Constants.LIMIT).toInt().toNumberString()
            val row = ceil((viewDataBinding.rvDetailList2.itemCount.toDouble() / 5)).toInt()
            val curRow = ceil(((position + 1).toDouble() / 5)).toInt()
            if (curRow == 1) {
                viewDataBinding.rvDetailList2.smoothScrollTo(0, 0)
            } else if (curRow == row && !isLoadMore) {
                viewDataBinding.rvDetailList2.scrollToBottom()
            } else if (curRow > 2) {
                viewDataBinding.rvDetailList2.viewFocus?.let { it1 -> viewDataBinding.rvDetailList2.smoothScrollTo(0, it1.top - 250) }
            }
            if (viewDataBinding.rvDetailList2.lastFocus >= viewDataBinding.rvDetailList2.itemCount - 5 && isLoadMore && !isRequesting) {
                isRequesting = true
                if (mType == Constants.TYPE_CATEGORY.FAVORITE.type) {
                    when (mPosition) {
                        0 -> {
                            getAllMyFavorites()
                        }
                        1 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.MOVIE.type)
                            getFavoritesWithPaging(categoryIdList)
                        }
                        2 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.DRAMA.type)
                            getFavoritesWithPaging(categoryIdList)
                        }
                        3 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.ENTERTAINMENT.type)
                            getFavoritesWithPaging(categoryIdList)
                        }
                        4 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.EDUCATION.type)
                            getFavoritesWithPaging(categoryIdList)
                        }
                        5 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.KID.type)
                            getFavoritesWithPaging(categoryIdList)
                        }
                    }
                } else if (mType == Constants.TYPE_CATEGORY.PLAYBACK.type) {
                    when (mPosition) {
                        0 -> {
                            getAllMyWatchHistory()
                        }
                        1 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.MOVIE.type)
                            getWatchHistoryWithPaging(categoryIdList)
                        }
                        2 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.DRAMA.type)
                            getWatchHistoryWithPaging(categoryIdList)
                        }
                        3 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.ENTERTAINMENT.type)
                            getWatchHistoryWithPaging(categoryIdList)
                        }
                        4 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.EDUCATION.type)
                            getWatchHistoryWithPaging(categoryIdList)
                        }
                        5 -> {
                            val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.KID.type)
                            getWatchHistoryWithPaging(categoryIdList)
                        }
                    }
                } else {
                    if (mPosition == 0) {
                        if (mCategorySelected != null)
                            getListMovieByUpdateAt(mCategorySelected!!.id)
                    } else if (mPosition == 1) {
                        if (mCategorySelected != null)
                            getListMovieByViews(mCategorySelected!!.id)
                    } else
                        getListMovie(mCategorySelected!!.id)
                }
            }
        }
        viewDataBinding.rvDetailList2.lastFocus = -1
        viewDataBinding.rvDetailList2.setMovieList(mMovieAllList, mType, widthItem, heightItem)
    }

    private fun showTime() {
        someHandler.postDelayed(object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("HH:mm")
                sdf.timeZone = TimeZoneManager.getInstance().getData()
                viewModel.time.value = sdf.format(Date())
                someHandler.postDelayed(this, 1000)
            }
        }, 10)

        val sdf3 = SimpleDateFormat("yyyy.MM.dd")
        sdf3.timeZone = TimeZoneManager.getInstance().getData()
        viewDataBinding.tvId.text = if (AccountManager.getInstance().isLoggedIn())
            AccountManager.getInstance().getAccount()?.getUserIdentify() else
            FormatUtils.getDate(requireContext()) + "\n" + sdf3.format(Date())

        viewDataBinding.tvDate.text = AccountManager.getInstance().getAccountCardStatus()

        someHandler2.postDelayed(object : Runnable {
            override fun run() {

                val sdf3 = SimpleDateFormat("yyyy.MM.dd")
                sdf3.timeZone = TimeZoneManager.getInstance().getData()
                viewDataBinding.tvId.text = if (AccountManager.getInstance().isLoggedIn())
                    AccountManager.getInstance().getAccount()?.getUserIdentify() else
                    FormatUtils.getDate(requireContext()) + "\n" + sdf3.format(Date())

                viewDataBinding.tvDate.text = AccountManager.getInstance().getAccountCardStatus()
                someHandler2.postDelayed(this, 43200000)
            }
        }, 43200000)


    }

    private fun openLiveChannel(channel: BLive) {
        if (!AccountManager.getInstance().isLoggedIn()) {
            openDialogRequireLogin()
        } else {
            if (AccountManager.getInstance().supportPremiumLive()) {
                openPlay(channel)
            } else {
                viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
            }
        }
    }

    private fun openDialogRequireLogin() {
        val dialog = RequireLoginDialog()
        dialog.show(childFragmentManager, "login")
        dialog.onClickLoginListener = {
            val loginFragment = LoginFragment()
            loginFragment.onLoginSuccessListener = {
                reloadId()
                val successDialog = SuccessDialog()
                successDialog.show(childFragmentManager, "success")
            }
            (requireActivity() as HomeActivity).addFragment(loginFragment, LoginFragment.TAG, false)
        }
    }

    private fun reloadId() {
        if (AccountManager.getInstance().isLoggedIn()) {
            val sdf3 = SimpleDateFormat("yyyy.MM.dd")
            sdf3.timeZone = TimeZoneManager.getInstance().getData()
            viewDataBinding.tvDate.text = AccountManager.getInstance().getAccountCardStatus()
            viewDataBinding.tvId.text = if (AccountManager.getInstance().isLoggedIn())
                AccountManager.getInstance().getAccount()?.getUserIdentify() else
                FormatUtils.getDate(requireContext()) + "\n" + sdf3.format(Date())
        }
    }

    private fun openPlay(channel: BLive, timeLive: BLiveTime? = null, time: Long? = null, isHideMenu: Boolean = false) {
        mLiveLast = channel
        mLives.forEachIndexed { i: Int, bLive: BLive ->
            if (bLive.id == channel.id) {
                livePositionCurrent = i
                return@forEachIndexed
            }
        }
        viewModel.isPlayRecord = false
        viewModel.timeRecordSelect = 0
        viewModel.timeStartRecordSelect = 0
        if (requireActivity() is HomeActivity) {
            val intent = Intent(activity, BeeVideoPlayerActivity::class.java).apply {
                putExtra(Const.MEDIA_TYPE_ARG, BeeMediaType.LIVE.toString())
                putExtra(Const.MEDIA_DATA_ARG, channel as Serializable)

                putExtra(Const.IS_NEXT_ARG, livePositionCurrent < mLives.size - 1)
                putExtra(Const.IS_PREVIOUS_ARG, livePositionCurrent > 0)
            }
            startForResult.launch(intent)
//            startActivity(intent)
        }

        LiveManager.getInstance().setData(mLiveLast!!, mCategoryLast!!)
        if (requireActivity() is BeeVideoPlayerActivity) {
            if (timeLive == null)
                (requireActivity() as BeeVideoPlayerActivity).setLive(channel, livePositionCurrent < mLives.size - 1, livePositionCurrent > 0, isHideMenu)
            else {
                val c = Calendar.getInstance()

                val now = c.timeInMillis
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = time!!
                if (timeLive.start_time < now && timeLive.end_time > now && c.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    if (isShowCalendarList) {
                        startAnimationHide()
                    }
                    (requireActivity() as BeeVideoPlayerActivity).setLive(channel, livePositionCurrent < mLives.size - 1, livePositionCurrent > 0, true)

                } else if (timeLive.start_time > now && c.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    viewModel.snackbarInt.value = Event(R.string.error_cannot_play)
                } else if (timeLive.is_exist) {

                    if (isShowCalendarList) {
                        startAnimationHide()
                    }
                    viewModel.isPlayRecord = true

                    if (timeLive != null && time != null) {
                        viewModel.timeRecordSelect = time
                        viewModel.timeStartRecordSelect = timeLive.start_time
                        (requireActivity() as BeeVideoPlayerActivity).playAsRecordLive(channel, timeLive, time)
                    }
//                    // Todo check recored existed
//                    var recordName = "${channel!!.name.filter { !it.isWhitespace() }}" + "_${time.toYMD()}_${timeLive.start_time}"
//                    RecorderServer.instance.checkRecorderExists(
//                            recordName
//                    ) {
//                        val mainHandler = Handler(Looper.getMainLooper());
//                        val myRunnable: Runnable
//                        if (it.results.item.exist) {
//                            myRunnable = Runnable {
//                                if (isShowCalendarList) {
//                                    startAnimationHide()
//                                }
//                                viewModel.isPlayRecord = true
//
//                                if (timeLive != null && time != null) {
//                                    viewModel.timeRecordSelect = time
//                                    viewModel.timeStartRecordSelect = timeLive.start_time
//                                    (requireActivity() as BeeVideoPlayerActivity).playAsRecordLive(channel, timeLive, time)
//                                }
//                            }
//                        } else {
//                            myRunnable = Runnable {
//                                viewModel.snackbarInt.value = Event(R.string.video_file_unavailable)
//                            }
//                        }
//                        mainHandler.post(myRunnable);
//                    }
                }
            }
            viewModel.positionCurrent.value = (livePositionCurrent + 1).toNumberString()
        }
    }

    fun setCurrentLive(live: BLive, liveList: List<BLive>) {
        mLiveLast = live
        Log.d("yenyenlive", Gson().toJson(live))
        mLives.clear()
        mLives.addAll(liveList)
        Log.d("yenyenliveList", Gson().toJson(liveList))
    }


    private fun openMovieDetail(movieID: String) {
        val fragment = MovieDetailFragment()
        val args = Bundle().apply {
            putString("movie_id", movieID)
            putString("type", mType)
        }
        fragment.arguments = args
        (requireActivity() as HomeActivity).addFragment(fragment, MovieDetailFragment.TAG)

    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarInt, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.openMovieDetailEvent.observe(viewLifecycleOwner, EventObserver {
            openMovieDetail(it)
        })
        viewModel.openCategoryDetailEvent.observe(viewLifecycleOwner, EventObserver {
            openLiveChannel(it)
        })
        viewModel.hideMenuEvent.observe(viewLifecycleOwner, EventObserver {

            if (activity is BeeVideoPlayerActivity) {
                if (it) {
                    hideMenuHandler.removeCallbacksAndMessages(null)
                    hideMenuHandler.postDelayed({
                        if (activity != null) {
                            (activity as BeeVideoPlayerActivity)?.hideFragment()
                        }
                    }, 15000)
                }


            }
        })
        viewModel.loadLiveEvent.observe(viewLifecycleOwner, EventObserver {
            mCategoryLast = it
            if (it.id == "0") {
//                val liveFavoriteList = FavoriteManager.getInstance().getDataByType(Constants.TYPE_FILE.LIVE.toString())
//                val liveList = mutableListOf<BLive>()
//                liveFavoriteList.forEach {
//                    if (it.live != null)
//                        liveList.add(it.live!!)
//                }
//                setUpLive(liveList, "0")

                getLiveFavorite()
            } else {

                loadLive(it)
            }

        })
        viewModel.loadMovieByCategoryEvent.observe(viewLifecycleOwner, EventObserver {

            mPage = 1
            mPosition = it.first
            if (mType == Constants.TYPE_CATEGORY.FAVORITE.type) {
                when (mPosition) {
                    0 -> {
                        getAllMyFavorites()
                    }
                    1 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.MOVIE.type)
                        getFavoritesWithPaging(categoryIdList)
                    }
                    2 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.DRAMA.type)
                        getFavoritesWithPaging(categoryIdList)
                    }
                    3 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.ENTERTAINMENT.type)
                        getFavoritesWithPaging(categoryIdList)
                    }
                    4 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.EDUCATION.type)
                        getFavoritesWithPaging(categoryIdList)
                    }
                    5 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.KID.type)
                        getFavoritesWithPaging(categoryIdList)
                    }
                }
            } else if (mType == Constants.TYPE_CATEGORY.PLAYBACK.type) {
                when (mPosition) {
                    0 -> {
                        getAllMyWatchHistory()
                    }
                    1 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.MOVIE.type)
                        getWatchHistoryWithPaging(categoryIdList)
                    }
                    2 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.DRAMA.type)
                        getWatchHistoryWithPaging(categoryIdList)
                    }
                    3 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.ENTERTAINMENT.type)
                        getWatchHistoryWithPaging(categoryIdList)
                    }
                    4 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.EDUCATION.type)
                        getWatchHistoryWithPaging(categoryIdList)
                    }
                    5 -> {
                        val categoryIdList = CategoryManager.getInstance().getCategoryIDByType(Constants.TYPE_CATEGORY.KID.type)
                        getWatchHistoryWithPaging(categoryIdList)
                    }
                }
            } else {
                mCategorySelected = getCategoryByName()

                if (it.first == 0) {
                    if (mCategorySelected != null)
                        getListMovieByUpdateAt(mCategorySelected!!.id)
                } else if (it.first == 1) {
                    if (mCategorySelected != null)
                        getListMovieByViews(mCategorySelected!!.id)
                } else {
                    mCategorySelected = it.second
                    loadMovieByCategory(it.second)
                }
                viewModel.positionCurrent.value = (it.first + 1).toNumberString()
            }

        })

        viewModel.loadTimeLiveEvent.observe(viewLifecycleOwner, EventObserver {
            loadTimeLive(it)
        })

        viewModel.loadTimeLiveWithDateEvent.observe(viewLifecycleOwner, EventObserver {
            getTimeTableByLiveID(it.first, it.second, it.third, (mRecyclerViewLive as LiveAdapter2).viewSelected!!, false)
        })

        viewModel.refreshLiveTimeEvent.observe(viewLifecycleOwner, EventObserver {
            (mRecyclerViewLiveTime?.adapter as LiveTimeAdapter).notifyDataSetChanged()
        })


        viewModel.openLiveTimeEvent.observe(viewLifecycleOwner, EventObserver {


            openLiveTime(it.first, it.second)
//            val x = viewDataBinding.btMore.x - 400
//            viewDataBinding.btMore.animate()
//                    .x(x)
//                    .y(viewDataBinding.btMore.y)
//                    .setDuration(1000)
//                    .withEndAction {
//                        viewDataBinding.dynamicList.hideProgram()
//                        rotationButton(0)
//
//
//                    }
//                    .start()

            viewDataBinding.dynamicList.hideProgram()
            rotationButton(0)
        })


        viewModel.openCalendarEvent.observe(viewLifecycleOwner, EventObserver {
            openLiveTimeWithDate(it.first, it.second, it.third)

        })
    }


    private fun loadMovieByCategory(bCategory: BCategory) {
//        mMovieAllList.clear()
//        val pair = MovieManager.getInstance().getMovieListAndPage(bCategory.id)
//        if (pair == null) {
        getListMovie(bCategory.id)

//        } else {
//            viewDataBinding.progressBar.hide()
//            mPage = pair.first.first
//            viewDataBinding.tvCurrentPage.text = pair.first.first.toNumberString()
//            viewDataBinding.tvTotalPage.text = pair.first.second.toNumberString()
//
//            isLoadMore = mPage < pair.first.second
//            if (isLoadMore) mPage++
//            setupMovieList()
//            mMovieAdapter.viewMenuFocus = mAdapter.viewSelected
//
//            var movieList = pair.second
//            mMovieAllList.addAll(movieList)
//            mMovieAdapter.submitList(movieList)
//            mMovieAdapter.notifyDataSetChanged()
//            if (pair.second.isEmpty()) {
//                viewDataBinding.tvNoData.show()
//                viewDataBinding.rvDetailList.hide()
//            } else {
//                viewDataBinding.rvDetailList.show()
//                viewDataBinding.tvNoData.hide()
//                viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList.id
//            }
////            if (mPage == 1) {
//            Handler().postDelayed({ viewDataBinding.rvDetailList.scrollToPosition(0) }, 200)
////            }
//        }
    }

    private fun loadLive(bCategory: BCategory) {
        viewModel.categoryId.value = bCategory.id
        viewModel.positionCurrent.value = (0).toNumberString()
        viewDataBinding.dynamicList.hideCalendar()

        rotationButton(0)
        val liveList = LiveManager.getInstance().getLiveList(bCategory.id)
        if (liveList != null) {
            setUpLive(liveList, bCategory.id)
        } else {

            getLiveByCategoryId(bCategory.id)
        }


    }

    private fun loadTimeLive(pair: Triple<String, BLive, Pair<Int, View>>) {

        viewModel.isLiveLastSelected = (mLiveLast?.id == pair.second.id)

        viewModel.positionCurrent.value = (pair.third.first + 1).toNumberString()
        val liveTimeList = LiveManager.getInstance().getLiveAndTimeList(pair.first, pair.second)
        if (viewModel.isPlayRecord && viewModel.isLiveLastSelected) {
            if (!isAnimation)
                runOpenMenuAnimation()
            getTimeTableByLiveID(pair.first, pair.second, viewModel.timeStartRecordSelect, pair.third.second, true)
            Log.d("yenyenGetList", "1")

        } else {
            if (liveTimeList == null || liveTimeList.isEmpty()) {
                Log.d("yenyenGetList", "2")

                val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                if (!isAnimation)
                    runOpenMenuAnimation()
                getTimeTableByLiveID(pair.first, pair.second, calendar.timeInMillis, pair.third.second, true)

            } else {
                Log.d("yenyenGetList", "3")
                if (!isAnimation)
                    runOpenMenuAnimation()
                mRecyclerViewLiveTime = viewDataBinding.dynamicList.initLiveTimeList(liveTimeList, viewModel, pair.second, pair.third.second, true) { viewTime, i: Int, bLiveTime: BLiveTime ->

                    (mRecyclerViewCalendar?.adapter as? CalendarAdapter)?.viewTimeLiveFocus = viewTime
                    (mRecyclerViewCalendar?.adapter as? CalendarAdapter)?.notifyDataSetChanged()

                }
//                if (!isAnimation) {
//                    runOpenMenuAnimation()
//                }

                mRecyclerViewCalendar = viewDataBinding.dynamicList.initCalendarList(pair.first, viewModel, pair.second, null)

                viewModel.mCalendarRecyclerView = mRecyclerViewCalendar
                liveTimeList.forEachIndexed { index, bLiveTime ->
                    val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).timeInMillis

                    if ((now >= bLiveTime.start_time.parseTimeLocal() && now <= bLiveTime.end_time.parseTimeLocal())) {
                        Handler().postDelayed({
                            val smoothScroller: SmoothScroller = CenterSmoothScroller(requireContext())

                            smoothScroller.targetPosition = index

                            mRecyclerViewLiveTime?.layoutManager?.startSmoothScroll(smoothScroller)

                        }, 200)
                        return@forEachIndexed
                    }

                }
            }
        }


    }

    private fun openLiveTime(bLive: BLive, bLiveTime: BLiveTime) {
        if (!AccountManager.getInstance().isLoggedIn()) {
            openDialogRequireLogin()
        } else {
            if (AccountManager.getInstance().supportPremiumLive()) {
                openPlay(
                        bLive, isHideMenu = true)
            } else {
                viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
            }
        }
    }

    private fun openLiveTimeWithDate(time: Long, bLive: BLive, bLiveTime: BLiveTime) {
        if (!AccountManager.getInstance().isLoggedIn()) {
            openDialogRequireLogin()
        } else {
            if (AccountManager.getInstance().supportPremiumLive()) {
                openPlay(
                        bLive, bLiveTime, time
                )
            } else {
                viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
            }
        }
    }

    private var mMovieAllList = mutableListOf<BMovie>()
    private fun getListMovie(categoryId: String) {

//        mPage = 1
        viewDataBinding.tvNoData.hide()
        viewModel.getMoviesBySubCategoryIdAndSortUpdate(categoryId, mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val movieList = it.data!!.results?.objects?.rows?.toMutableList()
                                ?: mutableListOf()
//                        var movieList = nestItems.map {
//                            it.movie
//                        }

                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected
                            MovieManager.getInstance().addMovieList(categoryId, this@MenuFragment.mPage, it.data.pagination.total, movieList)
                            viewModel.movieList.value = mMovieAllList
                        } else {
                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }

                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.pagination.current_page == 1 && movieList.isEmpty()) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()

                            Log.d("yenyen", "hide list")
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id

                            Log.d("yenyen", "show list")
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false
                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {
                        isRequesting = false
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getListMovieByUpdateAt(categoryId: String) {

        viewDataBinding.tvNoData.hide()
        viewModel.getMoviesByCategoryIdAndSortUpdate(categoryId, mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val movieList = it.data!!.results?.objects?.rows?.toMutableList()
                                ?: mutableListOf()
//                        var movieList = nestItems.map {
//                            it.movie
//                        }

                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected
                            MovieManager.getInstance().addMovieList(categoryId, this@MenuFragment.mPage, it.data.pagination.total, movieList)
                            viewModel.movieList.value = mMovieAllList
                        } else {
                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }

                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.pagination.current_page == 1 && movieList.isEmpty()) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()

                            Log.d("yenyen", "hide list")
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id

                            Log.d("yenyen", "show list")
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false
                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {
                        isRequesting = false
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getListMovieByViews(categoryId: String) {

        viewDataBinding.tvNoData.hide()

        viewModel.getMoviesByCategoryIdAndSortView(categoryId, mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {

                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val movieList = it.data!!.results?.objects?.rows ?: mutableListOf()
//                        var movieList = nestItems.map {
//                            it.movie
//                        }
                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected

                            MovieManager.getInstance().addMovieList(categoryId, this@MenuFragment.mPage, it.data.pagination.total, movieList)
                            viewModel.movieList.value = mMovieAllList
                        } else {

                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }

                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.results!!.objects.count == 0 && it.data.pagination.current_page == 1) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false

                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {

                        isRequesting = false
                        viewDataBinding.progressBar.hide()
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getAllMyFavorites() {
        viewDataBinding.tvNoData.hide()
        viewModel.getAllMyFavorites(mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {

                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val nestItems = it.data!!.results?.objects?.rows ?: mutableListOf()
                        var movieList = nestItems.map {
                            it.movie!!
                        }
                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected

                            viewModel.movieList.value = mMovieAllList
                        } else {

                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }


                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.results!!.objects.count == 0 && it.data.pagination.current_page == 1) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false

                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {

                        isRequesting = false
                        viewDataBinding.progressBar.hide()
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getFavoritesWithPaging(categoryIdList: MutableList<String>) {

        viewDataBinding.tvNoData.hide()
        viewModel.getFavoritesWithPaging(categoryIdList.toJson(), mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {

                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val nestItems = it.data!!.results?.objects?.rows ?: mutableListOf()
                        var movieList = nestItems.map {
                            it.movie!!
                        }
                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected

                            viewModel.movieList.value = mMovieAllList
                        } else {

                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }


                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.results!!.objects.count == 0 && it.data.pagination.current_page == 1) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false

                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {

                        isRequesting = false
                        viewDataBinding.progressBar.hide()
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getWatchHistoryWithPaging(categoryIdList: MutableList<String>) {
        viewDataBinding.tvNoData.hide()
        viewModel.getWatchHistoryWithPaging(categoryIdList.toJson(), mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {

                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val nestItems = it.data!!.results?.objects?.rows ?: mutableListOf()
                        var movieList = nestItems.map {
                            it.movie!!
                        }
                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected

                            viewModel.movieList.value = mMovieAllList
                        } else {

                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }

                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.results!!.objects.count == 0 && it.data.pagination.current_page == 1) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false

                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        }
                        Log.d(TAG, "Get category loading")
                    }
                    Status.ERROR -> {

                        isRequesting = false
                        viewDataBinding.progressBar.hide()
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun getAllMyWatchHistory() {
        viewDataBinding.tvNoData.hide()
        viewModel.getAllMyWatchHistory(mPage).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {

                        viewDataBinding.progressBar.hide()
//                        viewModel.totalList.value = (it.data!!.results?.objects?.count ?: 0).toString()
                        val nestItems = it.data!!.results?.objects?.rows ?: mutableListOf()
                        var movieList = nestItems.map {
                            it.movie!!
                        }
                        if (it.data.pagination.total == 0) {
                            viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                        } else
                            viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                        viewDataBinding.tvTotalPage.text = (it.data.pagination.total
                                ?: 0).toNumberString()
                        if (it.data.pagination.current_page == 1) {
                            mMovieAllList.clear()
                            mMovieAllList.addAll(movieList)
                            setupMovieList2()
                            viewDataBinding.rvDetailList2.viewMenuFocus = mAdapter.viewSelected

                            viewModel.movieList.value = mMovieAllList
                        } else {

                            mMovieAllList.addAll(movieList)
                            viewDataBinding.rvDetailList2.updateMovieList(movieList)

                        }

                        isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                        if (it.data.results!!.objects.count == 0 && it.data.pagination.current_page == 1) {
                            viewDataBinding.tvNoData.show()
//                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.btSetting.id
                            viewDataBinding.rvDetailList2.hide()
                        } else {
                            viewDataBinding.rvDetailList2.show()
                            viewDataBinding.tvNoData.hide()
                            viewDataBinding.rvMenuItem.nextFocusRightId = viewDataBinding.rvDetailList2.id
                        }

                        if (mPage == 1) {
                            Handler().postDelayed({ viewDataBinding.rvDetailList2.scrollTo(0, 0) }, 200)
                        } else {
                        }
                        this@MenuFragment.mPage = it.data.pagination.next_page

                        isRequesting = false

                    }
                    Status.LOADING -> {
                        if (mPage == 1) {
                            viewDataBinding.rvDetailList2.invisible()
                            viewDataBinding.progressBar.show()
                        } else {
                        }
                    }
                    Status.ERROR -> {

                        isRequesting = false
                        viewDataBinding.progressBar.hide()
                        viewDataBinding.rvDetailList2.invisible()
                        viewDataBinding.progressBar.hide()
                        Log.d(TAG, "Get category error ")
                    }
                }
            }
        })

    }

    private fun restartApp() {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }

    private fun isLastMenuVisible(): Boolean {
        val numItems: Int = viewDataBinding.rvMenuItem.adapter!!.itemCount

        val layoutManager = viewModel.mMenuRecyclerView!!.layoutManager as GridLayoutManager
        val pos = layoutManager.findLastVisibleItemPosition()
        Log.d("isLastMenuVisible", pos.toString())

        return pos >= numItems
    }

    class ClickProxy(val viewModel: MenuViewModel, val fragment: MenuFragment) {
        fun openSearch() {

            val fragment2 = SearchFragment()
            val args = Bundle().apply {
                putString("type", Constants.TYPE_CATEGORY.SEARCH.type)
            }
            fragment2.arguments = args
            (fragment.requireActivity() as HomeActivity).addFragment(fragment2, SearchFragment.TAG)
        }

        fun openFavorite() {

            if (AccountManager.getInstance().getAccount() != null) {

                val fragment2 = SearchFragment()
                val args = Bundle().apply {
                    putString("type", Constants.TYPE_CATEGORY.FAVORITE.type)
                }
                fragment2.arguments = args
                (fragment.requireActivity() as HomeActivity).addFragment(fragment2, SearchFragment.TAG)

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
                    fragment.showTime()
                }

            }
            settingDialog.onTimeZoneSelectedListener = {
                fragment.showTime()
            }


            settingDialog.onChangeLanguageListener = {
                fragment.restartApp()
            }
        }

        fun openPlayback() {
            if (AccountManager.getInstance().getAccount() != null) {

                val fragment2 = SearchFragment()
                val args = Bundle().apply {
                    putString("type", Constants.TYPE_CATEGORY.PLAYBACK.type)
                }
                fragment2.arguments = args
                (fragment.requireActivity() as HomeActivity).addFragment(fragment2, SearchFragment.TAG)
            } else openDialogRequireLogin()
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

        fun sort() {
//            fragment.isSortUpdate = true
//            fragment.isSortPopular = false
//            viewModel.isSortPopularSelected = false
//            viewModel.isSortUpdateSelected = true
//            val category = fragment.getCategoryByName()
//            if (category != null) {
//                fragment.page = 1
//                fragment.getListMovieByUpdateAt(categoryId = category.id, page = fragment.page)
//            }
//
//            fragment.mAdapter.viewSelected?.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), android.R.color.transparent))
//            fragment.mAdapter.viewSelected = null
//            fragment.mAdapter.idSelected = null
//            fragment.viewDataBinding.tvSortPopular.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), android.R.color.transparent))
//            viewModel.positionCurrent.value = (1).toNumberString()
        }


        fun sortPopular() {
//            fragment.isSortPopular = true
//            fragment.isSortUpdate = false
//            viewModel.isSortPopularSelected = true
//            viewModel.isSortUpdateSelected = false
//            val category = fragment.getCategoryByName()
//            if (category != null) {
//                fragment.page = 1
//                fragment.getListMovieByViews(categoryId = category.id, page = fragment.page)
//            }
//            fragment.mAdapter.viewSelected?.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), android.R.color.transparent))
//            fragment.mAdapter.viewSelected = null
//            fragment.mAdapter.idSelected = null
//            fragment.viewDataBinding.tvSort.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), android.R.color.transparent))
//            viewModel.positionCurrent.value = (2).toNumberString()
        }

        fun goBack() {
            if (fragment.activity is BeeVideoPlayerActivity) {
                (fragment.activity as BeeVideoPlayerActivity).hideFragment()
            } else {
                fragment.activity?.supportFragmentManager?.popBackStack()
            }

        }

        fun hideCalendar() {
            fragment.hideCalendar()
        }

    }

    private fun hideCalendar() {
        if (isShowCalendarList) {
            rotationButton(0)
            viewDataBinding.dynamicList.hideProgram()
        } else {
            openLiveTime()
        }
    }


    private fun getLiveByCategoryId(categoryId: String) {
        viewModel.positionCurrent.value = (0).toNumberString()

        viewDataBinding.dynamicList.showLiveLoadingView()
        viewModel.getLivesByCategoryId(categoryId).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        val lives = it.data ?: listOf()
                        LiveManager.getInstance().addLiveList(categoryId, lives)
                        setUpLive(lives, categoryId)


//                        viewDataBinding.tvTotal.text = mLives.size.toNumberString()


//                        viewDataBinding.rvMenuItem.nextFocusUpId = recyclerView.id


                        Log.d(TAG, "Get success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Loading...")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get error ")
                    }
                }
            }
        })

    }

    private val mLiveFavoriteList = mutableListOf<BLive>()
    private fun getLiveFavorite() {
        viewModel.positionCurrent.value = (0).toNumberString()

        viewDataBinding.dynamicList.showLiveLoadingView()
        viewModel.getAllMyLiveFavorites().observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        val nestItems = it.data!!.results?.objects?.rows ?: mutableListOf()
                        var liveList = nestItems.map {
                            it.live!!
                        }
                        mLiveFavoriteList.clear()
                        mLiveFavoriteList.addAll(liveList.sortedBy { it -> it.channel_number })
//                        LiveManager.getInstance().addLiveList("0", liveList)
                        setUpLive(mLiveFavoriteList, "0")


//                        viewDataBinding.tvTotal.text = mLives.size.toNumberString()


//                        viewDataBinding.rvMenuItem.nextFocusUpId = recyclerView.id


                        Log.d(TAG, "Get success ")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "Loading...")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get error ")
                    }
                }
            }
        })

    }

    private fun setUpLive(lives: List<BLive>, categoryId: String) {
        lives.forEach {
            it.is_favorite = FavoriteManager.getInstance().checkLiveIsFavorite(it.id)
        }

        mLives.clear()
        mLives.addAll(lives.sortedBy { it -> it.position })
        var positionRequest = -1
        if (mLiveLast == null)
            if (mLives.isNotEmpty())
                mLiveLast = mLives[0]
            else {
                positionCategory++
                mAdapter.idSelected = positionCategory + 23456
//                mAdapter.notifyDataSetChanged()
                viewDataBinding.rvMenuItem.post {
                    mAdapter.notifyDataSetChanged()
                }

                mCategoryLast = CategoryManager.getInstance().getData(mType!!)[positionCategory]
                getLiveByCategoryId(mCategoryLast!!.id)
            }
        mLives.forEachIndexed { index, bLive ->
            if (bLive.id == mLiveLast?.id) {
                positionRequest = index
                return@forEachIndexed
            }
        }

        if (isFirst) openPlay(mLiveLast!!)
        isFirst = false
        viewModel.positionCurrent.value = (positionRequest + 1).toNumberString()

        viewModel.totalList.value = mLives.size.toNumberString()
        mRecyclerViewLive = viewDataBinding.dynamicList.initLiveList(categoryId,
                mLives,
                viewModel, mAdapter.viewSelected!!, if (isFocusFirst) positionRequest else null, { i: Int, bLive: BLive, view: View ->
            if (viewDataBinding.dynamicList.isShowLoading)
                viewDataBinding.dynamicList.hideLoadingTimeLive()
            runCloseMenuAnimation()

        }, { i: Int, bLive: BLive ->
            if (bLive.is_favorite == true)
                unlikeLive(i, bLive)
            else likeLive(i, bLive)

        })


        viewModel.mLiveRecyclerView = mRecyclerViewLive

        if (!isRequestLiveFirstView && isDupLiveView) {
            isDupLiveView = false
            Log.d("yenyen", "reinit view")
            setUpLive(lives, categoryId)
        }


    }


    private fun getTimeTableByLiveID(idCategory: String, live: BLive, time: Long, viewFocus: View, isFromChanel: Boolean) {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val today = simpleDateFormat.format(Date(time))
        viewModel.getTimetable(
                live = live.channel_code ?: "", // name is vtv
                date = today // 2020-09-1

        ).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        val timeLiveList = it.data?.results?.one?.rows ?: listOf()
//                        val timeLiveList: List<BLiveTime> = listOf()
//                        val viewCalendarList =  (mRecyclerViewCalendar?.adapter as CalendarAdapter).viewList
//                        Log.d("yenyen","size calendar list ${viewCalendarList?.size}")

                        mRecyclerViewLiveTime = viewDataBinding.dynamicList.initLiveTimeList(timeLiveList, viewModel, live, viewFocus, isFromChanel) { viewTime, i: Int, bLiveTime: BLiveTime ->
                            (mRecyclerViewCalendar?.adapter as? CalendarAdapter)?.viewTimeLiveFocus = viewTime
                            (mRecyclerViewCalendar?.adapter as? CalendarAdapter)?.notifyDataSetChanged()
                        }
                        if (!isAnimation) {
                            runOpenMenuAnimation()
                        }

                        if (isFromChanel) {

                            mRecyclerViewCalendar = viewDataBinding.dynamicList.initCalendarList(idCategory, viewModel, live, null)


                            viewModel.mCalendarRecyclerView = mRecyclerViewCalendar
                        }

                        timeLiveList.forEachIndexed { index, bLiveTime ->
                            val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).timeInMillis
                            if (viewModel.isPlayRecord && viewModel.isLiveLastSelected) {
                                val startTimeRecord = SimpleDateFormat("yyyy-MM-dd").format(viewModel.timeStartRecordSelect.parseTimeLocal())
                                val startTime = SimpleDateFormat("yyyy-MM-dd").format(bLiveTime.start_time)

                                if (startTime == startTimeRecord) {
                                    if (bLiveTime.start_time.parseTimeLocal() == viewModel.timeStartRecordSelect.parseTimeLocal()) {
                                        Handler().postDelayed({
                                            val smoothScroller: SmoothScroller = CenterSmoothScroller(requireContext())

                                            smoothScroller.targetPosition = index

                                            mRecyclerViewLiveTime?.layoutManager?.startSmoothScroll(smoothScroller)
                                        }, 200)
                                        return@forEachIndexed
                                    }
                                } else {
                                    if ((now >= bLiveTime.start_time.parseTimeLocal() && now <= bLiveTime.end_time.parseTimeLocal())) {
                                        val timeCurrent = simpleDateFormat.format(now)
                                        val timeSelected = simpleDateFormat.format(time)
                                        if (timeCurrent == timeSelected)
                                            LiveManager.getInstance().addLiveTimeList(idCategory, live, timeLiveList)
                                        Handler().postDelayed({
                                            val smoothScroller: SmoothScroller = CenterSmoothScroller(requireContext())

                                            smoothScroller.targetPosition = index

                                            mRecyclerViewLiveTime?.layoutManager?.startSmoothScroll(smoothScroller)
                                        }, 200)
                                        return@forEachIndexed
                                    }
                                }

                            } else {
                                if ((now >= bLiveTime.start_time.parseTimeLocal() && now <= bLiveTime.end_time.parseTimeLocal())) {
                                    val timeCurrent = simpleDateFormat.format(now)
                                    val timeSelected = simpleDateFormat.format(time)
                                    if (timeCurrent == timeSelected)
                                        LiveManager.getInstance().addLiveTimeList(idCategory, live, timeLiveList)
                                    Handler().postDelayed({
                                        val smoothScroller: SmoothScroller = CenterSmoothScroller(requireContext())

                                        smoothScroller.targetPosition = index

                                        mRecyclerViewLiveTime?.layoutManager?.startSmoothScroll(smoothScroller)
                                    }, 200)
                                    return@forEachIndexed
                                }
                            }


                        }
                        viewModel.mCalendarRecyclerView = mRecyclerViewCalendar
                        Handler().postDelayed({
                            if (timeLiveList.isEmpty()) {
                                (mRecyclerViewCalendar?.adapter as CalendarAdapter).requestFocusViewSelected()
                            }
                        }, 200)
                        Log.d("yenyen", " live time list size ${timeLiveList.size}")
                    }
                    Status.LOADING -> {
                        viewDataBinding.dynamicList.showLiveTimeLoadingView()
                        Log.d(TAG, "Loading...")
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "Get error ")
                    }
                }
            }
        })
    }

    override fun onBackPress() {
        super.onBackPress()
        showTime()
    }

    fun changeChannel(action: Int) {
        mLives.forEachIndexed { i: Int, bLive: BLive ->
            if (mLiveLast?.id == bLive.id) {
                livePositionCurrent = i
            }
        }
        if (action == BeeVideoPlayerActivity.FINISH_WITH_CHANNEL_UP) {
            if (livePositionCurrent < mLives.size - 1) {
                livePositionCurrent++
                openPlay(mLives[livePositionCurrent])
            }
            print("UP")
        } else if (action == BeeVideoPlayerActivity.FINISH_WITH_CHANNEL_DOWN) {
            // Todo handle play previous chapter video
            if (livePositionCurrent > 0) {
                livePositionCurrent--
                openPlay(mLives[livePositionCurrent])
            }
            print("DOWN")
        }
    }

    fun requestViewFocus() {
        if (!didRunGuideAnimation) {
            Handler().postDelayed({ runOpenMenuAnimation() }, 500)
        }

        isRequestLiveView = true
        mCategoryLast = LiveManager.getInstance().getData().first
        mLiveLast = LiveManager.getInstance().getData().second
        isFocusFirst = true
        if (isRequestLiveFirstView) {
            isRequestLiveFirstView = false
        }
//        if (BuildConfig.VARIANT == "beetv"){
        initAdapterMenu()
//        }


    }

    private fun likeLive(position: Int, live: BLive) {

        viewModel?.likeLive(live!!.id)?.observe(viewLifecycleOwner) { (status, data) ->
            when (status) {
                Status.SUCCESS -> {

                    (mRecyclerViewLive as LiveAdapter2).updateFavorite(position, true)
                    val mFavorite =
                            data?.results?.one
                    mFavorite?.live = live
                    FavoriteManager.getInstance().addFavorite(mFavorite!!)
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
                else -> {
                }

            }

        }
    }

    private fun unlikeLive(position: Int, live: BLive) {
        viewModel?.unlikeLive(live!!.id)?.observe(viewLifecycleOwner) { (status, data) ->
            when (status) {
                Status.SUCCESS -> {

                    FavoriteManager.getInstance().removeFavoriteLive(live!!.id)
                    if (mCategoryLast?.id == "0") {
                        if (mLiveFavoriteList.isNotEmpty()) {

                            var position = -1
                            mLiveFavoriteList.forEachIndexed { index, bLive ->
                                if (bLive.id == live.id) {
                                    position = index
                                    return@forEachIndexed
                                }

                            }
                            if (position != -1) mLiveFavoriteList.removeAt(position)
                        }
                        if (mLiveFavoriteList.isNotEmpty()) {
//                            (mRecyclerViewLive?.adapter as LiveAdapter).submitList(mLiveFavoriteList)
//                            (mRecyclerViewLive?.adapter as LiveAdapter).notifyDataSetChanged()
                        } else
                            setUpLive(mLiveFavoriteList, mCategoryLast?.id ?: "0")

                    } else
                        (mRecyclerViewLive as LiveAdapter2).updateFavorite(position, false)


                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
                else -> {
                }
            }
        }

    }

    private fun rotationButton(degree: Int) {
        val rotate = RotateAnimation(0f, degree.toFloat(),
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

// prevents View from restoring to original direction.

// prevents View from restoring to original direction.
        rotate.fillAfter = true

        viewDataBinding.btMore.startAnimation(rotate)
    }

    private var isAnimation = false
    private var isStartAnimation = false
    override fun myOnKeyDown(keyCode: Int) {
        val liveAdapter = mRecyclerViewLive as? LiveAdapter2
//        if (!isAnimation) {
        if (!isShowCalendarList && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isVisible && liveAdapter?.viewFocus != null) {
//                if (isStartAnimation)
            openLiveTime()
            loadLiveTime()
//                isStartAnimation = true
        } else {
            super.myOnKeyDown(keyCode)
        }
//        }
    }

    private fun openLiveTime() {
        val viewFocus = if (BuildConfig.FLAVOR.equals("beetv"))
            (mRecyclerViewLive as? LiveAdapter2)?.viewFocus
        else
            (mRecyclerViewLive as? LiveAdapter2)?.viewSelected

        if (viewFocus != null) {
            runOpenMenuAnimation()
        }
    }

    private fun runOpenMenuAnimation() {
        if (isAnimation) {
            return
        }
        isAnimation = true
        if (!didRunGuideAnimation) {
            val x = viewDataBinding.moreLayout.x + 400
            shakeTheArrow(x)
            fadeInAndZoomInCircleRedButton()
        } else {
            rotateTheArrowToLeft()
        }
    }

    private fun runCloseMenuAnimation() {
        if (!isShowCalendarList || isAnimation) {
            return
        }
        isAnimation = true

        if (!didCloseGuideAnimation) {
            didCloseGuideAnimation = true
            fadeOutAndSlideRightText()
            viewDataBinding.dynamicList.hideProgram()
        } else {
            viewDataBinding.dynamicList.hideProgram()
            Handler().postDelayed({
                rotateTheArrowToRight();
            }, 100)
        }
    }

    private fun rotateTheArrowToRight() {
        ViewAnimator.animate(viewDataBinding.btMore)
                .rotation(180f, 0f)
                .duration(600)
                .onStop {
                    viewDataBinding.btMore.rotation = 0f

                    Handler().postDelayed({
                        isAnimation = false
                        isShowCalendarList = false
                    }, 100)

                }
                .start()
    }


    private fun rotateTheArrowToLeft() {
        ViewAnimator.animate(viewDataBinding.btMore)
                .rotation(0f, 180f)
                .duration(600)
                .onStop {
                    // Todo app crash some time, please check fragment mounted when do it
                    viewDataBinding.btMore.rotation = 180f
                    isShowCalendarList = true
                    isAnimation = false
                }
                .start()
    }

    private fun fadeInAndSlideLeftText() {
        ViewAnimator.animate(viewDataBinding.tvExpand)
                .onStart {
                    viewDataBinding.tvExpand.show()
                }
                .onStop {
                    isAnimation = false
                    didRunGuideAnimation = true
                }
                .translationX(150f, 0f)
                .alpha(0f, 1f)
                .duration(1000)
                .start()
    }

    private fun fadeOutAndSlideRightText() {
        ViewAnimator.animate(viewDataBinding.tvExpand)
                .onStop {
                    viewDataBinding.tvExpand.hide()
                    scaleSmallWidthOfTheBackgroundOfButtonMore()
                }
                .translationX(0f, 150f)
                .alpha(1f, 0f)
                .fadeOut()
                .duration(1000)
                .start()


    }

    private fun scaleBigWidthOfTheBackgroundOfButtonMore() {
        ViewAnimator.animate(viewDataBinding.viewBackgroundOfBtMore)
                .width(requireContext().resources.getDimension(R.dimen.size_36), requireContext().resources.getDimension(R.dimen.size_206))
                .duration(600)
                .onStop {
                    fadeInAndSlideLeftText()
                }
                .start()
    }

    private fun scaleSmallWidthOfTheBackgroundOfButtonMore() {
        ViewAnimator.animate(viewDataBinding.viewBackgroundOfBtMore)
                .width(requireContext().resources.getDimension(R.dimen.size_206), requireContext().resources.getDimension(R.dimen.size_36))
                .duration(600)
                .onStop {
//                    Handler().postDelayed({
//                        viewDataBinding.dynamicList.hideProgram()
//                    }, 100)

                    Handler().postDelayed({
                        fadeOutAndZoomOutCircleRedButton()
                    }, 500)
                }
                .start()
    }

    private fun fadeOutAndZoomOutCircleRedButton() {
        ViewAnimator.animate(viewDataBinding.viewBackgroundOfBtMore)
                .fadeOut()
                .zoomOut()
                .duration(600)
                .onStart {
                }
                .onStop {
                    viewDataBinding.viewBackgroundOfBtMore.hide()
                    rotateTheArrowToRight()
                }
                .start()
    }

    private fun fadeInAndZoomInCircleRedButton() {
        ViewAnimator.animate(viewDataBinding.viewBackgroundOfBtMore)
                .fadeIn()
                .zoomIn()
                .duration(1000)
                .onStart {
                    viewDataBinding.viewBackgroundOfBtMore.show()
                }
                .onStop {
                    scaleBigWidthOfTheBackgroundOfButtonMore()
                    if (isShowCalendarList) {
                        rotateTheArrowToLeft()
                    }
                }
                .start()
    }

    private fun shakeTheArrow(x: Float) {
        ViewAnimator.animate(viewDataBinding.btMore)
                .shake()
                .interpolator(LinearInterpolator())
                .duration(1000)
                .start()
    }

    private fun loadLiveTime() {
        val liveAdapter = mRecyclerViewLive as LiveAdapter2
        val viewFocus = liveAdapter.viewFocus ?: liveAdapter.viewSelected
        var viewSelected = liveAdapter.viewSelected
        isShowCalendarList = true
        if (viewFocus != null) {

            liveAdapter.positonRequest = 400000 + (viewFocus.tag as Pair<Int, BLive>).first
            if (viewSelected is LinearLayout) {
                val viewChildMain = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                if (viewChildMain.getChildAt(0) is LinearLayout) {
                    val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                    (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.alto))
                    val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                    (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.alto))
                    ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.alto))
                }
                viewSelected.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

            }
            liveAdapter.viewSelected = viewFocus
            val viewChildMain = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
            if (viewChildMain.getChildAt(0) is LinearLayout) {
                val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
                val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
                ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
            }
            viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)

            if (viewSelected == null) {
                viewSelected = liveAdapter.viewSelected
            }

            viewModel.loadTimeLive(liveAdapter.bCategoryID, (liveAdapter.viewSelected!!.tag as Pair<Int, BLive>).second, viewSelected!!, (liveAdapter.viewSelected!!.tag as Pair<Int, BLive>).first)
        }
    }

    override fun onDestroyView() {
        someHandler2.removeCallbacksAndMessages(null)
        someHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    fun startAnimationHide() {
        if (isAddView) {
            isAnimation = false
            isShowCalendarList = false
            viewDataBinding.btMore.rotation = 0f
            viewDataBinding.tvExpand.hide()
            viewDataBinding.viewBackgroundOfBtMore.layoutParams.width = resources.getDimensionPixelOffset(R.dimen.size_36)
            viewDataBinding.viewBackgroundOfBtMore.hide()
            viewDataBinding.dynamicList.hideProgram()
        }
    }

}