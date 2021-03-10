package com.example.android.architecture.blueprints.beetv.modules.player

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.BeeMediaType
import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseActivity
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.api.RetrofitBuilder
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.example.android.architecture.blueprints.beetv.manager.*
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuFragment
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailFragment
import com.example.android.architecture.blueprints.beetv.modules.player.components.ExoVideoView
import com.example.android.architecture.blueprints.beetv.modules.player.components.ExoVideoView.CompleteListener
import com.example.android.architecture.blueprints.beetv.modules.player.components.PopupVideoControl
import com.example.android.architecture.blueprints.beetv.modules.player.inteface.RequestViews
import com.example.android.architecture.blueprints.beetv.modules.player.models.VideoInfo
import com.example.android.architecture.blueprints.beetv.util.*
import com.google.gson.Gson
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import retrofit2.Call
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class BeeVideoPlayerActivity : BaseActivity(), RequestViews, PopupVideoControl.OnItemClickListener {

    companion object {
        val NEXT_ACTION = "next_action"
        val FINISH_WITH_NEXT_CHAPTER = 0
        val FINISH_WITH_PREVIOUS_CHAPTER = 1
        val FINISH_WITH_CHANNEL_UP = 2
        val FINISH_WITH_CHANNEL_DOWN = 3
    }

    private var active: Boolean = false
    private var exoplay: ExoVideoView? = null
    private var popupVideoControl: PopupVideoControl? = null
    private var viewModel: PlayerViewModel? = null
    private var mMovie: BMovie? = null
    private var mVideoId: String? = null
    private var mLive: BLive? = null
    private var mLiveTime: BLiveTime? = null
    private var menuFragment: MenuFragment? = null

    private val someHandler = Handler(Looper.getMainLooper())
    private val startVideoHandler = Handler()
    private var movieDetailFragment: MovieDetailFragment? = null
    private var isFavorite: Boolean = false
    private var btFavorite: ImageButton? = null
    private var mMediaType: BeeMediaType? = null
    private var isShowMenu: Boolean = false
    private var isShowExist: Boolean = false
    private var currentPosition = 0
    var isNextLive: Boolean = true
    var isPreviousLive: Boolean = true
    private var exitLayout: LinearLayout? = null
    private var resumeLayout: LinearLayout? = null
    private var btExit: Button? = null
    private var btCancel: Button? = null
    private var btResume: Button? = null
    private var btFromStart: Button? = null
    private var tvSpeed: TextView? = null
    private var tvMovieName: TextView? = null
    private var ivLoading: ImageView? = null
    private var mCategory: String? = null
    private var isHideDialogNetwork = false
    private var isLoaded = true
    private var isLoadMenuSuccess = false
    private var tvChannel: TextView? = null

    private var mHandlerRemoveChannel: Handler = Handler()
    private var mHandlerGetChannel: Handler = Handler()
    private var mLiveList = mutableListOf<BLive>()
    private var isRecordPause = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beevideoplayer)
        mCategory = intent.getStringExtra(Const.MEDIA_CATEGORY_ARG)
        exoplay = findViewById<View>(R.id.exoplay) as ExoVideoView
        exitLayout = findViewById<View>(R.id.exitLayout) as LinearLayout
        resumeLayout = findViewById<View>(R.id.resumeLayout) as LinearLayout
        btExit = findViewById<View>(R.id.bt_exit) as Button
        btCancel = findViewById<View>(R.id.bt_cancel) as Button
        btResume = findViewById<View>(R.id.bt_resume) as Button
        btFromStart = findViewById<View>(R.id.bt_from_start) as Button
        tvSpeed = findViewById<View>(R.id.tv_speed) as TextView
        tvMovieName = findViewById<View>(R.id.tv_movie_name) as TextView
        ivLoading = findViewById<View>(R.id.iv_loading) as ImageView
        tvChannel = findViewById<View>(R.id.tv_channel_input) as TextView
        popupVideoControl = PopupVideoControl(this)
        popupVideoControl!!.setOnItemClickListener(this)

        Glide.with(this).load(R.drawable.loading).into(ivLoading!!)
        exoplay!!.setCompleteListener(object : CompleteListener {
            override fun onComplete() {
                MovieManager.getInstance().cacheSeekInfo(
                        mMovie?.id ?: "",
                        mVideoId ?: "",
                        0)
                if (currentPosition == (mMovie?.videos?.size?.minus(1)))
                    finish() // Todo back to movie detail pa

                else {
                    val data = Intent()
                    data.putExtra(NEXT_ACTION, FINISH_WITH_NEXT_CHAPTER)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        })
        btExit?.setOnClickListener {
            super.onBackPressed()
            // Todo should keep current duration

            if (mMediaType == BeeMediaType.MOVIE) {
                MovieManager.getInstance().cacheSeekInfo(
                        mMovie?.id ?: "",
                        mVideoId ?: "",
                        exoplay?.getExoPlayer()?.currentPosition ?: 0)
            }
            finish()
        }

        btCancel?.setOnClickListener {
            exitLayout?.hide()
            if (mMovie != null || mMediaType == BeeMediaType.RECORD)
                exoplay?.updatePlayerButton()
            isShowExist = false
            exoplay?.setFocusButton(true)
            btFavorite?.isFocusable = true

        }
        setup()
        getAllLive()
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false
    }

    private fun setup() {
        val app = application as BeeTVApplication
        viewModel = PlayerViewModel(app.accountRepository, app.movieRepository, SavedStateHandle())
        mMediaType = BeeMediaType.valueOf(intent.getStringExtra(Const.MEDIA_TYPE_ARG))
        if (mMediaType == BeeMediaType.MOVIE) {
            mMovie = intent.getSerializableExtra(Const.MEDIA_DATA_ARG) as BMovie
            mVideoId = intent.getStringExtra(Const.MEDIA_VIDEO_ID_ARG)
            val latestWatchRequest = intent.getBooleanExtra(Const.MEDIA_LATEST_WATCH_ARG, false) // make should play latest watch if can
            val movieSeek = MovieManager.getInstance().findMovieSeekInfo(mMovie?.id)
            var seekTime = 0L
            if (latestWatchRequest && movieSeek?.hasSeek() == true) {
                val chapterSeek = movieSeek.getLatestChapter()
                mVideoId = chapterSeek.id
                seekTime = chapterSeek.seekTime
            } else if (movieSeek?.hasChapter(mVideoId) == true) {
//            } else if (movieSeek != null && movieSeek.chapterId == mVideoId) {
                seekTime = movieSeek.getChapterById(mVideoId)?.seekTime ?: 0L
            }
            mMovie?.category_movies?.forEach {
                if (it.category.parent != null) {
                    mCategory = it.category.parent.name
                    return@forEach
                }
            }
            if (mCategory != Constants.TYPE_CATEGORY.MOVIE.type && !mCategory.isNullOrEmpty()) {
                movieDetailFragment = MovieDetailFragment()
                val args = Bundle().apply {
                    putString("movie_id", mMovie!!.id)
                    putBoolean("is_focus", false)
                }
                movieDetailFragment?.arguments = args
                addFragment(movieDetailFragment!!, MovieDetailFragment.TAG)
                hideFragment()
            }
            playAsMovie(mMovie, mVideoId!!, seekTime)


//
        } else if (mMediaType == BeeMediaType.LIVE) {
            menuFragment = MenuFragment()
            val args = Bundle().apply {
                putString("type", Constants.TYPE_CATEGORY.TV.type)
            }
            menuFragment?.arguments = args
            addFragment(menuFragment!!, MenuFragment.TAG)
            hideFragment()
            isLoadMenuSuccess = true
        } else {
            val mLive = intent.getSerializableExtra(Const.MEDIA_DATA_ARG) as BLive
            val mLiveTime = intent.getSerializableExtra(Const.MEDIA_TIME_LIVE_ARG) as BLiveTime
            val time = intent.getLongExtra(Const.MEDIA_DATE_ARG, 0)
            playAsRecordLive(mLive, mLiveTime, time)

        }
    }

    fun setLive(live: BLive, isNext: Boolean, isPrevious: Boolean, isHideMenu: Boolean = false) {
        if (isHideMenu) hideFragment()
        mMediaType = BeeMediaType.LIVE
        isNextLive = isNext
        isPreviousLive = isPrevious
        mLive = live
        mLiveTime = null
        playAsLive(mLive)

//        hideFragment()
    }


    fun setVideo(movie: BMovie, videoId: String) {
        mMovie = movie
        if (mVideoId != videoId) {
            mVideoId = videoId
            playAsMovie(mMovie, videoId)
        } else {
            exoplay?.updatePlayerButton()
        }

        hideFragment()
    }


    private fun playAsMovie(movie: BMovie?, videoId: String, seek: Long = 0) {
        exoplay!!.findViewById<View>(R.id.movie_control_layout).visibility = View.VISIBLE
        exoplay!!.findViewById<View>(R.id.live_control_layout).visibility = View.GONE
        // Find video
        var video: BVideo? = null
        for (i in movie!!.videos.indices) {
            if (movie.videos[i].id == videoId) {
                video = movie.videos[i]
                currentPosition = i
                break
            }
        }

        popupVideoControl?.setEnableNextButton(currentPosition < movie.videos.size - 1)
        popupVideoControl?.setEnablePreviousButton(currentPosition > 0)

        if (video != null) {
            val url = video.name
            val videoInfo = VideoInfo()
            videoInfo.title = movie.name + " (" + video.title + ")"
            videoInfo.playURL = url
//        videoInfo.setPlayURL("https://beetv.s3.ap-northeast-2.amazonaws.com/A.Dirty.Carnival.2006.720p.BluRay.x264.AAC-%5BYTS.MX.ts");
//        videoInfo.setCoverURL("https://4.img-dpreview.com/files/p/E~TS590x0~articles/3925134721/0266554465.jpeg");
//        videoInfo.setDuration((long) (Double.parseDouble("12300")*1000));
            exoplay!!.requestZoomRatio()
//            exoplay!!.play(videoInfo,false)
            if (seek > 0) {
                isLoaded = false
                resumeLayout?.show()
                tvMovieName!!.text = movie.name + " (" + video.title + ")"
                exoplay?.show(true)
                exoplay?.setFocusButton(false)
                popupVideoControl?.setRequestFocus(false)
                btFavorite?.isFocusable = false
                btResume?.requestFocus()
//                Handler().postDelayed(
//                        {
//                            Log.d("yenyenFocus", "view ${findViewById<FrameLayout>(R.id.container).findFocus().toString()}")
//                            Log.d("yenyenFocus","view ${ window.currentFocus.toString()}")
//
//                        },1000
//                )

                initTaskCheckNetwork(movie, video, videoInfo, seek, url)
            } else {
                isLoaded = true
                exoplay!!.play(videoInfo)
            }

            Utils.print("Playing movie: $url")
        }

        if (AccountManager.getInstance().isLoggedIn())
            saveWatched()

        // Support cache view count
        watchMovie()
    }

    private fun initTaskCheckNetwork(movie: BMovie, video: BVideo, videoInfo: VideoInfo, seek: Long, url: String) {
        val task = SpeedTestTask()
        btFromStart?.setOnClickListener {
            resumeLayout?.hide()
            exoplay!!.play(videoInfo)
            exoplay!!.getExoPlayer()?.seekTo(0)
            exoplay?.setFocusButton(true)
            popupVideoControl?.setRequestFocus(true)
            isLoaded = true
            isHideDialogNetwork = true
            startVideoHandler.removeCallbacksAndMessages(null)
            task.cancel(true)
        }

        btResume?.setOnClickListener {
            resumeLayout?.hide()
            exoplay!!.resume()
            exoplay!!.play(videoInfo)
            exoplay!!.getExoPlayer()?.seekTo(seek)
            popupVideoControl?.setRequestFocus(true)
            exoplay?.setFocusButton(true)
            isLoaded = true
            isHideDialogNetwork = true

            startVideoHandler.removeCallbacksAndMessages(null)
            task.cancel(true)
        }
        startVideoHandler.postDelayed({
            if (!isHideDialogNetwork) {
                resumeLayout?.hide()
                exoplay?.resume()
                exoplay?.play(videoInfo)
                exoplay?.getExoPlayer()?.seekTo(seek)
                exoplay?.setFocusButton(true)
                isLoaded = true
                isHideDialogNetwork = true
                task.cancel(true)
            }
        }, 15000)

        task.onResultListener = {
            runOnUiThread {
                val speed = ((it ?: 0.toBigDecimal()).multiply((0.000125).toBigDecimal()))
                tvSpeed?.text = String.format(getString(R.string.format_speed_video), speed.setScale(2, RoundingMode.HALF_EVEN).toString())
            }
        }

        task.onCompleteListener = {
            runOnUiThread {
                if (!isHideDialogNetwork) {
                    task.cancel(true)
                    initTaskCheckNetwork(movie, video, videoInfo, seek, url)
                } else {
                    task.cancel(true)
                }
            }
        }
        task.execute(url)
    }

    fun playAsRecordLive(live: BLive?, liveTime: BLiveTime, time: Long) {
        someHandler.removeCallbacksAndMessages(null)

        isRecordPause = false
        hideFragment()
        mMediaType = BeeMediaType.RECORD
        exoplay?.pause()
        isNextLive = false
        isPreviousLive = false
        exoplay!!.findViewById<View>(R.id.movie_control_layout).visibility = View.VISIBLE

        exoplay!!.findViewById<View>(R.id.live_control_layout).visibility = View.GONE

        popupVideoControl?.setEnableNextButton(false)
        popupVideoControl?.setEnablePreviousButton(false)

        val url = RetrofitBuilder.RECORDER_SERVER_IP + RetrofitBuilder.BASE_PATH + "file/get_record/${live!!.channel_code ?: ""}" +
                "_${time.toYMD()}_${liveTime.start_time}.mp4"
        val videoInfo = VideoInfo()
        videoInfo.title = liveTime.title
        videoInfo.playURL = url
//        videoInfo.setPlayURL("https://beetv.s3.ap-northeast-2.amazonaws.com/A.Dirty.Carnival.2006.720p.BluRay.x264.AAC-%5BYTS.MX.ts");
//        videoInfo.setCoverURL("https://4.img-dpreview.com/files/p/E~TS590x0~articles/3925134721/0266554465.jpeg");
//        videoInfo.setDuration((long) (Double.parseDouble("12300")*1000));
        exoplay!!.requestZoomRatio()
        exoplay!!.play(videoInfo)

        Utils.print("Playing record: $url")
    }

    private fun playAsLive(live: BLive?) {
        exoplay!!.findViewById<View>(R.id.movie_control_layout).visibility = View.GONE
        exoplay!!.findViewById<View>(R.id.live_control_layout).visibility = View.VISIBLE
        exoplay!!.findViewById<View>(R.id.layoutLine1).visibility = View.GONE
        exoplay!!.findViewById<View>(R.id.layoutLine2).visibility = View.GONE

        Glide.with(this).load(live!!.logo).into((exoplay!!.findViewById<View>(R.id.iv_logo) as ImageView))
        (exoplay!!.findViewById<View>(R.id.tv_channel) as TextView).text = live.channel_number
        (exoplay!!.findViewById<View>(R.id.tv_name) as TextView).text = live.name
        btFavorite = exoplay!!.findViewById<ImageButton>(R.id.bt_favorite)
        isFavorite = FavoriteManager.getInstance().checkLiveIsFavorite(liveID = live.id)
        changeColorFavoriteButton()
        btFavorite?.setOnClickListener {
            if (isFavorite)
                unlikeLive()
            else likeLive()

        }

        btFavorite?.setOnLongClickListener {
            exoplay!!.hide()
            showFragment()
            true
        }
        TimeZone.setDefault(TimeZoneManager.getInstance().getData())
//        val calendar = Calendar.getInstance()
//        calendar[Calendar.HOUR] = 0
//        calendar[Calendar.MINUTE] = 0
//        calendar[Calendar.SECOND] = 0
//        calendar[Calendar.HOUR_OF_DAY] = 0
//        val today = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))

        val today = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
        viewModel!!.getTimetable(live.channel_code ?: "", today).observe(this) { (status, data) ->
            when (status) {
                Status.SUCCESS -> {
                    val items = data!!.results!!.one.rows
                    loadProgram(items)
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
                else -> {
                }
            }
        }
        val videoInfo = VideoInfo()
        videoInfo.title = "${live.name} ${live.description}"
        videoInfo.playURL = live.address
        //        videoInfo.setCoverURL("https://4.img-dpreview.com/files/p/E~TS590x0~articles/3925134721/0266554465.jpeg");
//        videoInfo.setDuration((long) (Double.parseDouble("12300")*1000));
        exoplay!!.requestZoomRatio()
        exoplay!!.play(videoInfo)
        Utils.print("Playing live: " + live.address)
    }

    private fun loadProgram(items: List<BLiveTime>) {
        someHandler.removeCallbacksAndMessages(null)
        if (items != null && items.isNotEmpty()) {
            val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))

            val now = c.timeInMillis
            var foundId = -1
            var i = 0
            while (i < items.size) {
                if (now >= items[i].start_time.parseTimeLocal() && now <= items[i].end_time.parseTimeLocal()) {
                    foundId = i
                    break
                }
                ++i
            }

            val firstRow = if (foundId != -1) items[foundId] else null
            val secondRow = if (foundId + 1 < items.size && now < items[foundId + 1].start_time.parseTimeLocal()) items[foundId + 1] else null


            if (firstRow != null) {
                exoplay!!.findViewById<View>(R.id.layoutLine1).visibility = View.VISIBLE
                (exoplay!!.findViewById<View>(R.id.tv_name) as TextView).text = "${mLive?.name} ${firstRow.title ?: ""}"
                (exoplay!!.findViewById<View>(R.id.tv_name_program_current) as TextView).text = firstRow.start_time.parseTime() + " " + firstRow.bestTitle()
                (exoplay!!.findViewById<View>(R.id.progress) as ProgressBar).progress = (((now - firstRow.start_time.parseTimeLocal()).toDouble() / (firstRow.end_time.parseTimeLocal() - firstRow.start_time.parseTimeLocal())) * 100).toInt()

                someHandler.postDelayed(object : Runnable {
                    override fun run() {
                        if (!active) return

                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                        val timeNow = calendar.timeInMillis
                        if (timeNow >= firstRow.end_time.parseTimeLocal()) {
                            loadProgram(items)
                        } else {
                            someHandler.postDelayed(this, 1000)
                            (exoplay?.findViewById<View>(R.id.progress) as ProgressBar).progress =
                                    (((timeNow - firstRow.start_time.parseTimeLocal()).toDouble() / (firstRow.end_time.parseTimeLocal() - firstRow.start_time.parseTimeLocal())) * 100)
                                            .toInt()
                        }
                    }
                }, 10)
            } else {
                exoplay!!.findViewById<View>(R.id.layoutLine1).visibility = View.GONE

            }

            if (secondRow != null) {
                exoplay!!.findViewById<View>(R.id.layoutLine2).visibility = View.VISIBLE
                (exoplay!!.findViewById<View>(R.id.tv_name_program_next) as TextView).text = secondRow.start_time.parseTime() + " " + secondRow.bestTitle()
            } else {
                exoplay!!.findViewById<View>(R.id.layoutLine2).visibility = View.GONE
            }

        }
    }

    override fun onFailure(call: Call<*>?, throwable: Throwable?, id: Int) {
//        nextSong();
    }

    override fun onVideoList(videoInfos: List<VideoInfo?>?) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        menuFragment?.myOnKeyDown(keyCode)
        if ((mMediaType == BeeMediaType.LIVE || mMediaType == BeeMediaType.RECORD) && !isShowMenu && !isShowExist && isLoadMenuSuccess) {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && mMediaType == BeeMediaType.LIVE) {
                exoplay!!.show(true)
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (mMediaType == BeeMediaType.LIVE) {
                    showFragment()
                    exoplay!!.show(true)
                } else {
                    exoplay!!.updatePlayerButton()
                    if (isRecordPause) {
                        isRecordPause = false
                    } else {
                        isRecordPause = true
                        showFragment()
                    }

                }
                event.startTracking()
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && mMediaType == BeeMediaType.LIVE) {
                // Todo up channel
                if (isNextLive) {
//                    val data = Intent()
//                    data.putExtra(NEXT_ACTION, FINISH_WITH_CHANNEL_UP)
//                    data.putExtra(Const.MEDIA_DATA_ARG, mLive)
//                    setResult(Activity.RESULT_OK, data)
//                    finish()
                    changeChannel(FINISH_WITH_CHANNEL_UP)
                }
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && mMediaType == BeeMediaType.LIVE) {
                // Todo down channel
                if (isPreviousLive) {

                    changeChannel(FINISH_WITH_CHANNEL_DOWN)
                }

                return true
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && mMediaType == BeeMediaType.RECORD) {
                exoplay?.show(true)

                return true
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && mMediaType == BeeMediaType.RECORD) {
                exoplay!!.show(true)
                return exoplay!!.onKeyDown(keyCode, event)
            }
        }

        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && mMediaType == BeeMediaType.MOVIE && !isShowMenu && resumeLayout?.visibility == View.GONE && !isShowExist) {
            if (mCategory != Constants.TYPE_CATEGORY.MOVIE.type && !mCategory.isNullOrEmpty()) {
//                  exoplay?.updatePlayerButton()
                showFragment()
            } else {
                exoplay?.show(true)
            }
//            popupVideoControl?.showAtLocation(findViewById(R.id.container), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
//            popupVideoControl?.contentView?.viewTreeObserver?.addOnGlobalFocusChangeListener { oldFocus: View?, newFocus: View? ->
//                HandlerUtils.removeOnMainCallback(hidePop)
//                HandlerUtils.runOnMainThread(hidePop, 5000)
//            }
//            HandlerUtils.runOnMainThread(hidePop, 5000)
            return true
        }


        if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) && !isShowMenu && mMediaType == BeeMediaType.MOVIE) {
            if (isLoaded) {
                exoplay?.updatePlayerButton()
//                popupVideoControl?.showAtLocation(findViewById(R.id.container), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
//                popupVideoControl?.contentView?.viewTreeObserver?.addOnGlobalFocusChangeListener { oldFocus: View?, newFocus: View? ->
//                    HandlerUtils.removeOnMainCallback(hidePop)
//                    HandlerUtils.runOnMainThread(hidePop, 5000)
//                }
//                HandlerUtils.runOnMainThread(hidePop, 5000)
            }
            return true
        }
        if (mMediaType == BeeMediaType.LIVE || mMediaType == BeeMediaType.RECORD) {
            if (keyCode == KeyEvent.KEYCODE_0) {
                removeChannel(0.toString())
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_1) {
                removeChannel(1.toString())
                return true
            }


            if (keyCode == KeyEvent.KEYCODE_2) {

                removeChannel(2.toString())
                return true
            }



            if (keyCode == KeyEvent.KEYCODE_3) {

                removeChannel(3.toString())
                return true
            }


            if (keyCode == KeyEvent.KEYCODE_4) {

                removeChannel(4.toString())
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_5) {

                removeChannel(5.toString())
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_6) {
                removeChannel(6.toString())
                return true
            }


            if (keyCode == KeyEvent.KEYCODE_7) {
                removeChannel(7.toString())
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_8) {

                removeChannel(8.toString())
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_9) {
                removeChannel(9.toString())
                return true
            }
        }


        return exoplay!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    private fun removeChannel(number: String) {
        if (tvChannel?.text?.toString()?.length == 3) {
            tvChannel?.text = tvChannel!!.text.toString().substring(1)
        }

        if (tvChannel?.text.toString().isEmpty()) {
            tvChannel?.text = "00"
        }
        tvChannel?.append(number)
        mHandlerRemoveChannel.removeCallbacksAndMessages(null)
        mHandlerGetChannel.removeCallbacksAndMessages(null)

        if (tvChannel?.text?.toString()?.length == 3) {
            mHandlerGetChannel.postDelayed({
                if (!active) return@postDelayed

                var bLive: BLive? = null
                mLiveList.forEach {
                    val channelNumber = if (it.channel_number!!.length == 2) "0${it.channel_number}" else if (it.channel_number.length == 1) "00${it.channel_number}" else it.channel_number
                    val channelNumberSearch = tvChannel!!.text.toString()
                    if (channelNumber == channelNumberSearch) {
                        bLive = it
                        return@forEach
                    }
                }

                if (bLive != null) {
                    setLive(bLive!!, false, false, true)
                    if (bLive!!.category_lives.isNotEmpty()) {
                        val categoryLive = bLive!!.category_lives.first().category
                        if (categoryLive != null) {
                            val category = CategoryManager.getInstance().getCategoryByID(categoryLive.id)
                            if (category != null) {
                                LiveManager.getInstance().setData(bLive!!, category!!)
                                getLiveByCategoryId(category.id)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Sorry, the channel doesn't exist", Toast.LENGTH_SHORT).show()
                }
                tvChannel?.text = ""
            }, 5000)
        } else {
//            mHandlerRemoveChannel.postDelayed({
//                tvChannel?.text = ""
//            }, 5000)
        }
    }

//    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
//        if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) && !isShowMenu) {
//            exoplay!!.hide()
//            showFragment()
//            return true
//        }
//        return super.onKeyLongPress(keyCode, event)
//    }

    override fun setOnItemClick(v: View?) {
        HandlerUtils.removeOnMainCallback(hidePop)
        when (v!!.id) {
            R.id.pause -> {
//                ToastUtils.toast(applicationContext, "重唱")
//                exoplay!!.getmExoPlayer()!!.seekTo(0)
//                exoplay!!.show(true)
                exoplay?.updatePlayerButton()
            }

            R.id.previous_song -> {
                // Todo handle previous song
                if (mCategory != Constants.TYPE_CATEGORY.MOVIE.type && !mCategory.isNullOrEmpty()) {
                    movieDetailFragment?.changeVideo(FINISH_WITH_PREVIOUS_CHAPTER, mVideoId!!)
                } else {
                    val data = Intent()
                    data.putExtra(NEXT_ACTION, FINISH_WITH_PREVIOUS_CHAPTER)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
            R.id.next_song -> {
                // Todo handle next song
                if (mCategory != Constants.TYPE_CATEGORY.MOVIE.type && !mCategory.isNullOrEmpty()) {
                    movieDetailFragment?.changeVideo(FINISH_WITH_NEXT_CHAPTER, mVideoId!!)
                } else {
                    val data = Intent()
                    data.putExtra(NEXT_ACTION, FINISH_WITH_NEXT_CHAPTER)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
//            R.id.sound_effect -> {
//            }
//            R.id.yuan_ban -> {
//                if (volume) {
//                    exoplay!!.setVolume(0f, 1f)
//                    ToastUtils.toast(applicationContext, "原唱")
//                } else {
//                    exoplay!!.setVolume(1f, 0f)
//                    ToastUtils.toast(applicationContext, "伴唱")
//                }
//                volume = !volume
//            }
//            R.id.song_list -> {
//                ToastUtils.toast(applicationContext, "歌单")
//                popupVideoControl!!.dismiss()
//            }
            else -> {
            }
        }
        HandlerUtils.runOnMainThread(hidePop, 5000)
    }

    override fun onResume() {
        super.onResume()
        exoplay!!.resume()
    }

    override fun onDestroy() {
        startVideoHandler.removeCallbacksAndMessages(null)

        mHandlerRemoveChannel.removeCallbacksAndMessages(null)
        mHandlerGetChannel.removeCallbacksAndMessages(null)
        super.onDestroy()
        exoplay!!.destroy()
        if (popupVideoControl != null) {
            popupVideoControl!!.dismiss()
            popupVideoControl = null
        }
        HandlerUtils.removeOnMainCallback(hidePop)
    }

    override fun onPause() {
        super.onPause()
        exoplay!!.pause()
    }

    var hidePop = Runnable { popupVideoControl?.dismiss() }

    override fun onBackPressed() {
        if (resumeLayout?.isShown() == true) {
            finish()
            return
        }

        if (isShowMenu) {
            hideFragment()
            if (mMediaType == BeeMediaType.MOVIE) {
                if (mCategory == Constants.TYPE_CATEGORY.MOVIE.type || mCategory.isNullOrEmpty())
                    exoplay?.updatePlayerButton()
            }

//            if (mMediaType == BeeMediaType.RECORD) {
//                exoplay?.updatePlayerButton()
//            }
        } else {
            if (exitLayout?.visibility == View.GONE) {
                isShowExist = true
                exoplay?.setFocusButton(false)
                btFavorite?.isFocusable = false
                exitLayout?.show()
                btExit?.requestFocus()
                if ((mMovie != null || mMediaType == BeeMediaType.RECORD) && exoplay?.isPause() == true)
                    exoplay?.updatePlayerButton()

            } else {
                isShowExist = false
                exitLayout?.hide()
                exoplay?.setFocusButton(true)
                btFavorite?.isFocusable = true
                if ((mMovie != null || mMediaType == BeeMediaType.RECORD))
                    exoplay?.updatePlayerButton()
            }
        }
    }

    private fun addFragment(fragment: BaseFragment, TAG: String) {
        isShowMenu = true
        exoplay?.setFocusButton(false)
        btFavorite?.isFocusable = false
        supportFragmentManager.beginTransaction()
                .addToBackStack(TAG)
                .add(R.id.container, fragment, fragment.getMyTag())
                .commit()
    }

    fun hideFragment() {
        menuFragment?.startAnimationHide()
        supportFragmentManager.beginTransaction()
                .hide(menuFragment ?: movieDetailFragment!!)
                .commit()

        exoplay?.setFocusButton(true)
        btFavorite?.isFocusable = true

        popupVideoControl?.setRequestFocus(true)
        isShowMenu = false
    }

    private fun showFragment() {
        isShowMenu = true
        exoplay?.setFocusButton(false)
        if (menuFragment != null) {
            btFavorite?.isFocusable = false
            menuFragment?.requestViewFocus()
        } else {
            popupVideoControl?.setRequestFocus(false)
            movieDetailFragment?.requestViewFocus(mVideoId!!)
        }

        supportFragmentManager.beginTransaction()
                .show(menuFragment ?: movieDetailFragment!!)
                .commit()
    }

    private fun changeChannel(action: Int) {
        menuFragment?.changeChannel(action)
    }

    private fun saveWatched() {
        if (mMovie != null)
            viewModel?.saveWatched(mMovie!!.id)?.observe(this) { (status, data) ->
                when (status) {
                    Status.SUCCESS -> {
                        val mWatchHistory = data!!.results.one
                        mWatchHistory.movie = mMovie!!
                        WatchHistoryManager.getInstance().addWatchHistory(mWatchHistory)
                    }
                    Status.LOADING -> {
                    }
                    Status.ERROR -> {
                    }
                }
            }
    }

    private fun watchMovie() {
        if (mMovie != null)
            viewModel?.watchMovie(mMovie!!.id)?.observe(this) { (status, data) ->
                when (status) {
                    Status.SUCCESS -> {
                    }
                    Status.LOADING -> {
                    }
                    Status.ERROR -> {
                    }
                }
            }
    }


    private fun likeLive() {
        if (mLive != null)

            viewModel?.likeLive(mLive!!.id)?.observe(this) { (status, data) ->
                when (status) {
                    Status.SUCCESS -> {
                        isFavorite = true
                        changeColorFavoriteButton()
                        val mFavorite =
                                data?.results?.one
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

    private fun unlikeLive() {
        if (mLive != null)
            viewModel?.unlikeLive(mLive!!.id)?.observe(this) { (status, data) ->
                when (status) {
                    Status.SUCCESS -> {
                        isFavorite = false
                        changeColorFavoriteButton()
                        FavoriteManager.getInstance().removeFavoriteLive(mLive!!.id)
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

    fun getAllLive() {
        viewModel?.getAllLive()?.observe(this) { (status, data) ->
            when (status) {
                Status.SUCCESS -> {
                    val liveList = data!!.results?.objects?.rows ?: mutableListOf()
                    mLiveList.clear()
                    mLiveList.addAll(liveList)
//                    Log.d("yenyen", Gson().toJson(liveList))
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
            }
        }
    }

    private fun getLiveByCategoryId(categoryId: String) {
        viewModel?.getLivesByCategoryId(categoryId)?.observe(this) { (status, data) ->
            when (status) {
                Status.SUCCESS -> {

                    var livePositionCurrent = -1
                    data?.forEachIndexed { i: Int, bLive: BLive ->
                        if (bLive.id == mLive?.id) {
                            livePositionCurrent = i
                            return@forEachIndexed
                        }
                    }

                    isNextLive = livePositionCurrent < data?.size ?: -1 - 1
                    isPreviousLive = livePositionCurrent > 0
                    if (mLive != null && data != null)
                        menuFragment?.setCurrentLive(mLive!!, data)

                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
            }
        }

    }

    private fun changeColorFavoriteButton() {
        if (isFavorite) {
            btFavorite?.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
        } else btFavorite?.clearColorFilter()
    }

    class SpeedTestTask() : AsyncTask<String, Void, String>() {

        var onResultListener: ((BigDecimal?) -> Unit)? = null
        var onCompleteListener: (() -> Unit)? = null

        override fun doInBackground(vararg p0: String?): String? {
            val speedTestSocket = SpeedTestSocket()
            speedTestSocket.startDownloadRepeat(p0[0],
                    20000, 2000, object : IRepeatListener {
                override fun onCompletion(report: SpeedTestReport?) {
//                    Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report?.getTransferRateOctet());
//                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report?.getTransferRateBit());
                    onCompleteListener?.invoke()
                }

                override fun onReport(report: SpeedTestReport?) {
//                    Log.v("speedtest", "[REPORT] rate in octet/s : " + report?.getTransferRateOctet());
//                    Log.v("speedtest", "[REPORT] rate in bit/s   : " + report?.getTransferRateBit());
                    onResultListener?.invoke(report?.transferRateBit)
                }
            })
            return null
        }
    }
}
