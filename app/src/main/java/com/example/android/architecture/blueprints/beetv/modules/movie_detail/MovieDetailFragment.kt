package com.example.android.architecture.blueprints.beetv.modules.movie_detail

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.EventObserver
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.BeeMediaType
import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.adapter.ChapterAdapter
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.BVideo
import com.example.android.architecture.blueprints.beetv.data.models.Status
import com.example.android.architecture.blueprints.beetv.databinding.FragmentMovieDetailBinding
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import com.example.android.architecture.blueprints.beetv.modules.dialogs.NoVideoDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.RequireLoginDialog
import com.example.android.architecture.blueprints.beetv.modules.dialogs.SuccessDialog
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.login.LoginFragment
import com.example.android.architecture.blueprints.beetv.modules.player.BeeVideoPlayerActivity
import com.example.android.architecture.blueprints.beetv.util.*
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable


class MovieDetailFragment : BaseFragment(), ChapterAdapter.IChapterFocusListener {
    companion object {

        val TAG = "MovieDetailFragment"
        fun newInstance() = MovieDetailFragment()
    }

    private val viewModel by viewModels<MovieDetailViewModel> { getViewModelFactory() }

    private var videoPositionCurrent = 0

    //    private val args: MovieDetailFragmentArgs by navArgs()
    private var movieID: String? = null
    private var mType: String? = null
    private var isFocus :Boolean = true
    private lateinit var viewDataBinding: FragmentMovieDetailBinding
    private lateinit var mAdapter: ChapterAdapter

    private var isShowChapter = false

    //    private var isCancelFocus: Boolean = false
    private var isFavorite = false
    private var mMovie: BMovie? = null
    private val startForResult = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent?.hasExtra(BeeVideoPlayerActivity.NEXT_ACTION) == true) {
                val action = intent?.getIntExtra(BeeVideoPlayerActivity.NEXT_ACTION, -1)
                if (action == BeeVideoPlayerActivity.FINISH_WITH_NEXT_CHAPTER) {
                    // Todo handle play next chapter video
                    if (videoPositionCurrent < mMovie!!.getEpisodesAsc().size - 1) {
                        videoPositionCurrent++
                        openPlayerAtVideoId(mMovie?.getEpisodesAsc()?.get(videoPositionCurrent)!!.id)

                    }
                    print("NEXT")
                } else if (action == BeeVideoPlayerActivity.FINISH_WITH_PREVIOUS_CHAPTER) {
                    // Todo handle play previous chapter video
                    if (videoPositionCurrent > 0) {
                        videoPositionCurrent--
                        openPlayerAtVideoId(mMovie?.getEpisodesAsc()?.get(videoPositionCurrent)!!.id)

                    }
                    print("PREVIOUS")
                }
            }
        }
    }

    override fun getMyTag(): String {
        return "MovieDetailFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentMovieDetailBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            click = ClickProxy(viewModel, this@MovieDetailFragment)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.progressBar.isVisible = true
        viewDataBinding.layoutDetail.isVisible = false
        movieID = arguments?.getString("movie_id")
        isFocus = arguments?.getBoolean("is_focus",true) ?: true

        mType = arguments?.getString("type")
        viewModel.mediaID.value = movieID

        isFavorite = FavoriteManager.getInstance().checkMovieIsFavorite(movieID!!)
        if (isFavorite) {
            viewDataBinding.tvFavorite.setText(R.string.unfavorite)
            viewDataBinding.ivFavorite.setImageResource( R.drawable.ic_scrap_yellow)

        }else{
            viewDataBinding.ivFavorite.setImageResource( R.drawable.ic_scrap_white)

        }
//        metroViewBorderImpl = MetroViewBorderImpl(context)
//        metroViewBorderImpl.attachTo(viewDataBinding.main)
//        metroViewBorderImpl.attachTo(viewDataBinding.rvChapter)

//        viewDataBinding.btFavorite.requestFocus()
        mAdapter = ChapterAdapter(requireContext(), viewModel)
        mAdapter.setFocusListener(this)
        viewDataBinding.rvChapter.layoutManager = GridLayoutManager(context, 1)
        viewDataBinding.rvChapter.adapter = mAdapter


        setupNavigation()
        getMovieDetail()
        setupSnackbar()
        setupGUI()
    }

    fun setupGUI() {
        viewDataBinding.btPlay.onFocusChangeListener = View.OnFocusChangeListener { _, k ->
            if (k) resetChapterCount()
        }
        viewDataBinding.btFavorite.onFocusChangeListener = View.OnFocusChangeListener { _, k ->
            if (k) resetChapterCount()
        }

        viewDataBinding.btPlaylist.onFocusChangeListener = View.OnFocusChangeListener { view, k ->
            if (k) {
                resetChapterCount()
                viewDataBinding.btPlaylist.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                if (viewDataBinding.layoutChapter.visibility == View.VISIBLE) {

                    viewDataBinding.btPlaylist.setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))

                }
            }
        }
        viewDataBinding.btPlay.setOnClickListener {
            if (!mMovie?.billing.equals("FREE")) {
                if (!AccountManager.getInstance().isLoggedIn()) {
                    openDialogRequireLogin()
                } else {
                    if (AccountManager.getInstance().supportPremiumMovie()) {
                        openPlay()
                    } else {
                        viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
                    }
                }
            } else {
                openPlay()
            }
        }
    }

    private fun openPlayerAtVideoId(id: String) {
        if (requireActivity() is BeeVideoPlayerActivity) {
            (requireActivity() as BeeVideoPlayerActivity).setVideo(mMovie!!, id)
        } else {
            val intent = Intent(activity, BeeVideoPlayerActivity::class.java).apply {
                putExtra(Const.MEDIA_TYPE_ARG, BeeMediaType.MOVIE.toString())
                putExtra(Const.MEDIA_DATA_ARG, mMovie as Serializable)
                putExtra(Const.MEDIA_VIDEO_ID_ARG, id)
                putExtra(Const.MEDIA_CATEGORY_ARG, mType)
            }
            startForResult.launch(intent)
        }
    }

    fun openPlay() {
        val intent = Intent(requireActivity(), BeeVideoPlayerActivity::class.java).apply {
            putExtra(Const.MEDIA_TYPE_ARG, BeeMediaType.MOVIE.toString())
            putExtra(Const.MEDIA_DATA_ARG, mMovie as Serializable)
            putExtra(Const.MEDIA_VIDEO_ID_ARG, mMovie!!.getEpisodesAsc().last().id)
            putExtra(Const.MEDIA_LATEST_WATCH_ARG, true)
            putExtra(Const.MEDIA_CATEGORY_ARG, mType)
        }
        videoPositionCurrent = mMovie!!.getEpisodesAsc().size - 1
        startForResult.launch(intent)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarInt, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.openVideoEvent.observe(viewLifecycleOwner, EventObserver {
            mMovie?.videos?.forEachIndexed { index, bVideo ->
                if (bVideo.id == it) {
                    videoPositionCurrent = index
                    return@forEachIndexed
                }
            }
            openPlayerAtVideoId(it)
        })
    }

    private fun getMovieDetail() {
        viewModel.getMovieDetail(movieID!!).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        viewDataBinding.movie = it.data
                        mMovie = it.data
                        val total = it.data?.videos?.size ?: 0
                        viewModel.setTotalList(total.toNumberString())
                        viewDataBinding.tvTotal.text = total.toNumberString()
                        viewDataBinding.progressBar.isVisible = false
                        viewDataBinding.layoutDetail.isVisible = true
                        if (isFocus){
                            viewDataBinding.btPlay.isFocusable = true
                            viewDataBinding.btPlay.requestFocus()
                        }
                        if (total == 0) {
                            isShowChapter = false
//                            viewDataBinding.btPlay.isEnabled = false
//                            viewDataBinding.btPlay.isFocusable = false
//                            viewDataBinding.btPlaylist.isFocusable = false
//                            viewDataBinding.btPlaylist.isEnabled = false
                        } else {
                            if (isFocus){
                                viewDataBinding.btPlay.isFocusable = true
                                viewDataBinding.btPlay.requestFocus()
                            }
                            with(total > 1) {
                                isShowChapter = true
                            }
                        }

                        if (it.data?.isSupportPlaylist() == true) {
                            viewDataBinding.btPlaylist.show()
                        } else {
                            viewDataBinding.btPlaylist.hide()
                            viewDataBinding.btPlay.nextFocusRightId = R.id.bt_favorite
                        }
                    }
                    Status.LOADING -> {
                        viewDataBinding.progressBar.isVisible = true
                        viewDataBinding.layoutDetail.isVisible = false
                        Log.d(TAG, "Loading...")
                    }
                    Status.ERROR -> {
                        viewDataBinding.progressBar.isVisible = false
                        viewDataBinding.layoutDetail.isVisible = true
                        Log.d(TAG, "Get error ${it.message}")
                    }
                }
            }
        })
    }

    fun likeMovie() {
        viewModel.likeMovie(movieID!!).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        val mFavorite = it.data?.results?.one
                        mFavorite?.movie = mMovie!!
                        FavoriteManager.getInstance().addFavorite(mFavorite!!)

                        isFavorite = true
                        viewDataBinding.ivFavorite.setImageResource(R.drawable.ic_scrap_yellow)
                        viewDataBinding.tvFavorite.setText(R.string.unfavorite)
                        (activity as HomeActivity).mAppEvent.refreshAddFavoriteList.postValue(mFavorite)
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

    fun unlikeMovie() {
        viewModel.unlikeMovie(movieID!!).observe(viewLifecycleOwner, Observer { it ->
            run {
                when (it.status) {
                    Status.SUCCESS -> {
                        isFavorite = false
                        FavoriteManager.getInstance().removeFavoriteMovie(movieID!!)
                        viewDataBinding.tvFavorite.setText(R.string.favorites)
                        viewDataBinding.ivFavorite.setImageResource(R.drawable.ic_scrap_white)
                        (activity as HomeActivity).mAppEvent.refreshRemoveFavoriteList.postValue(movieID)
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

    fun openDialogRequireLogin() {
        val dialog = RequireLoginDialog()
        dialog.show(childFragmentManager, "login")
        dialog.onClickLoginListener = {
            val loginFragment = LoginFragment()
            loginFragment.onLoginSuccessListener = {
                reloadFragment()
                val successDialog = SuccessDialog()
                successDialog.show(childFragmentManager, "success")
            }
            (requireActivity() as HomeActivity).addFragment(loginFragment, LoginFragment.TAG, false)

        }
    }

    private fun reloadFragment() {
        isFavorite = FavoriteManager.getInstance().checkMovieIsFavorite(movieID!!)
        if (isFavorite) {
            viewDataBinding.tvFavorite.setText(R.string.unfavorite)
            viewDataBinding.ivFavorite.setImageResource( R.drawable.ic_scrap_yellow)

        }else{
            viewDataBinding.ivFavorite.setImageResource( R.drawable.ic_scrap_white)

        }
    }

    override fun onBackPress() {

//        isCancelFocus = false

    }

    fun back() {
        if (viewDataBinding.layoutChapter.visibility == View.VISIBLE) {
            viewDataBinding.layoutChapter.hide()
            if (isFocus)
            viewDataBinding.btPlaylist.requestFocus()
            viewDataBinding.btPlaylist.nextFocusRightId = R.id.bt_favorite
        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    class ClickProxy(val viewModel: MovieDetailViewModel, val fragment: MovieDetailFragment) {
        fun openPlay(movie: BMovie?) {
            if (movie != null) {
                if (!movie.isFree()) {
                    if (!AccountManager.getInstance().isLoggedIn()) {
                        fragment.openDialogRequireLogin()
                    } else if (AccountManager.getInstance().supportPremiumMovie()) {
                        if (!movie.videos.isNullOrEmpty()) {
                            fragment.openPlay()
                        } else {
                            val noVideoDialog = NoVideoDialog()
                            noVideoDialog.show(fragment.childFragmentManager, "no_video")
                        }
                    } else {
                        viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
                    }
                } else {
                    if (!movie.videos.isNullOrEmpty()) {
                        fragment.openPlay()
                    } else {
                        val noVideoDialog = NoVideoDialog()
                        noVideoDialog.show(fragment.childFragmentManager, "no_video")
                    }

                }
            }

        }

        fun clickFavorite() {
            if (AccountManager.getInstance().isLoggedIn()) {
                if (fragment.isFavorite) {
                    fragment.unlikeMovie()
                } else {
                    fragment.likeMovie()
                }
            } else openDialogRequireLogin()
        }

        fun clickPlaylist(movie: BMovie?) {
            if (movie != null) {
                if (!movie.isFree()) {
                    if (!AccountManager.getInstance().isLoggedIn()) {
                        fragment.openDialogRequireLogin()
                    } else if (AccountManager.getInstance().supportPremiumMovie()) {
                        if (!movie.videos.isNullOrEmpty()) {
                            fragment.openPlaylist()
                        } else {
                            val noVideoDialog = NoVideoDialog()
                            noVideoDialog.show(fragment.childFragmentManager, "no_video")
                        }
                    } else {
                        viewModel.snackbarInt.value = Event(R.string.error_upgrade_account)
                    }
                } else {
                    if (!movie.videos.isNullOrEmpty()) {
                        fragment.openPlaylist()
                    } else {
                        val noVideoDialog = NoVideoDialog()
                        noVideoDialog.show(fragment.childFragmentManager, "no_video")
                    }

                }
            }
        }

        fun goBack() {
            fragment.activity?.supportFragmentManager?.popBackStack()
        }

        fun openDialogRequireLogin() {
            val dialog = RequireLoginDialog()
            dialog.show(fragment.childFragmentManager, "login")
            dialog.onClickLoginListener = {
                val loginFragment = LoginFragment()
                loginFragment.onLoginSuccessListener = {
                    fragment.reloadFragment()
                    val successDialog = SuccessDialog()
                    successDialog.show(fragment.childFragmentManager, "success")
                }
                (fragment.requireActivity() as HomeActivity).addFragment(loginFragment, LoginFragment.TAG, false)
            }
        }
    }

    private fun openPlaylist() {
        if (isShowChapter) {
            if (viewDataBinding.layoutChapter.visibility == View.GONE) {
                viewDataBinding.layoutChapter.show()
                viewDataBinding.rvChapter.requestFocus()
                viewDataBinding.btPlaylist.nextFocusRightId = R.id.rv_chapter
                viewDataBinding.btPlaylist.setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
            } else {
                viewDataBinding.layoutChapter.hide()
                viewDataBinding.btPlaylist.nextFocusRightId = R.id.bt_favorite
                viewDataBinding.btPlaylist.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }

    override fun onChapterFocus(position: Int, video: BVideo) {
        viewModel.positionChapterCurrent.value = (position + 1).toNumberString()
        viewDataBinding.tvPosition.text = (position + 1).toNumberString()
    }

    private fun resetChapterCount() {
        viewModel.positionChapterCurrent.value = "00"
        viewDataBinding.tvPosition.text = "00"
    }

    fun requestViewFocus(videoId: String) {
        viewDataBinding.layoutDetail.hide()
        viewDataBinding.main.setBackgroundColor(Color.parseColor("#CC000000"))
        viewDataBinding.btFavorite.hide()
        viewDataBinding.btPlay.hide()
        viewDataBinding.btPlaylist.hide()
        viewDataBinding.summaryLayout.hide()
        viewDataBinding.layoutChapter.show()

        var positionResquest = 0
        mMovie?.getEpisodes()?.forEachIndexed { index, bVideo ->
            if (bVideo.id == videoId) {
                positionResquest = index
                return@forEachIndexed
            }
        }

        mAdapter.positionRequest = positionResquest
        mAdapter.notifyDataSetChanged()
    }

    fun changeVideo(action: Int, videoId: String) {
        mMovie?.getEpisodesAsc()?.forEachIndexed { index, bVideo ->
            if (bVideo.id == videoId) {
                videoPositionCurrent = index
                return@forEachIndexed
            }
        }

        if (action == BeeVideoPlayerActivity.FINISH_WITH_NEXT_CHAPTER) {
            // Todo handle play next chapter video
            if (videoPositionCurrent < mMovie!!.getEpisodesAsc().size - 1) {
                videoPositionCurrent++
                openPlayerAtVideoId(mMovie?.videos?.get(videoPositionCurrent)!!.id)

            }
            print("NEXT")
        } else if (action == BeeVideoPlayerActivity.FINISH_WITH_PREVIOUS_CHAPTER) {
            // Todo handle play previous chapter video
            if (videoPositionCurrent > 0) {
                videoPositionCurrent--
                openPlayerAtVideoId(mMovie?.videos?.get(videoPositionCurrent)!!.id)

            }
            print("PREVIOUS")
        }
    }
}