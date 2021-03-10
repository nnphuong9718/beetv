package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.CalendarAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.LiveAdapter2
import com.example.android.architecture.blueprints.beetv.data.adapter.LiveTimeAdapter
import com.example.android.architecture.blueprints.beetv.data.models.BLive
import com.example.android.architecture.blueprints.beetv.data.models.BLiveTime
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.google.gson.Gson
import java.util.*


class DynamicListView : LinearLayout {

    private var recyclerHashSet: LinkedHashMap<Constants.TYPE_MENU, RelativeLayout> = linkedMapOf()
    private var lineHashSet: LinkedHashMap<Constants.TYPE_MENU, View> = linkedMapOf()
    private lateinit var mViewModel: MenuViewModel
    private var positionAddProgressTimeLive = 2
    private var mLastFocusItem: Long = SystemClock.elapsedRealtime()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

    }


    fun showLiveLoadingView() {
        removeAllViews()
        val relativeLayout = RelativeLayout(context)
        val layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.MATCH_PARENT)
        relativeLayout.layoutParams = layoutParams
        relativeLayout.isFocusable = false

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyle)
        progressBar?.isFocusable = false
        progressBar?.isIndeterminate = true
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(progressBar, params)
        addView(relativeLayout)
    }

    fun initLiveList(bCategoryID: String, list: List<BLive>, viewModel: MenuViewModel,
                     viewMenuFocus: View, positonRequest: Int?,
                     function: (Int, BLive, View) -> Unit,
                     functionFavorite: (Int, BLive) -> Unit): ScrollView {


        mViewModel = viewModel
        var relativeLayout = recyclerHashSet.get(Constants.TYPE_MENU.CHANNEL)
        if (childCount > 0) {
            if (getChildAt(0) != relativeLayout) {
                removeViewAt(0)
            }
        }

        var recyclerView: LiveAdapter2? = relativeLayout?.getChildAt(0) as? LiveAdapter2
        var tvNoData: TextView? = relativeLayout?.getChildAt(1) as? TextView

        var line: View? = lineHashSet.get(Constants.TYPE_MENU.CHANNEL)
//        val adapter = LiveAdapter(viewModel, context, bCategoryID)

        if (recyclerView == null) {
            line = View(context)
            line.isFocusable = false
            line.layoutParams = LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT)
            line.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            relativeLayout = RelativeLayout(context)
            val layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.MATCH_PARENT)
            relativeLayout.layoutParams = layoutParams
            relativeLayout.isFocusable = false
            recyclerView = LiveAdapter2(context, list, bCategoryID, viewModel)
            recyclerView.id = 11233
            recyclerView.layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.MATCH_PARENT)
//            val reLayoutParams = RelativeLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.TOP
//            recyclerView.layoutParams = reLayoutParams
            recyclerView.isFocusable = false
            (recyclerView as LiveAdapter2).initChannel()
            relativeLayout.addView(recyclerView)
            tvNoData = TextView(context)
            tvNoData.text = context.getString(R.string.text_no_data_found)
            val tvLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            tvLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            tvNoData.layoutParams = tvLayoutParams
            tvNoData.setTextAppearance(context, android.R.style.TextAppearance_Medium)
            tvNoData.setTextColor(ContextCompat.getColor(context, R.color.white))
            relativeLayout.addView(tvNoData)

            relativeLayout.setBackgroundResource(R.drawable.bg_line)
            recyclerHashSet.put(Constants.TYPE_MENU.CHANNEL, relativeLayout)
            lineHashSet.put(Constants.TYPE_MENU.CHANNEL, line)
        }

        recyclerView.positonRequest = if (positonRequest != null) positonRequest + 400000 else null
        recyclerView.viewMenuFocus = viewMenuFocus

        recyclerView.onFocusListener = { i: Int, bLive: BLive ->
            function?.invoke(i, bLive, recyclerView.mViewList[i])
        }
        recyclerView.onFavoriteClickListener = { i: Int, bLive: BLive ->
            functionFavorite?.invoke(i, bLive)
        }

        if (recyclerHashSet.get(Constants.TYPE_MENU.PROGRAM) != null) {
            removeView(recyclerHashSet.get(Constants.TYPE_MENU.PROGRAM))
            removeView(lineHashSet.get(Constants.TYPE_MENU.PROGRAM))
        }


        if (list.isEmpty()) tvNoData?.show() else tvNoData?.hide()

//        recyclerView.adapter = adapter

        recyclerView.submitList(list)

        recyclerView.nextFocusDownId = recyclerView.id
        recyclerView.nextFocusUpId = recyclerView.id
        if (childCount > 0) {
            removeView(line)
            removeView(relativeLayout)
        }

        addView(line)
        addView(relativeLayout)

        return recyclerView
    }


    var isShowLoading = false
    fun showLiveTimeLoadingView() {
        positionAddProgressTimeLive = 2
        if (childCount > 3) {
            if (getChildAt(3) is RelativeLayout) {
                removeViewAt(3)
            }
            positionAddProgressTimeLive = 3
        } else
            if (childCount > 2) {
                if (getChildAt(2) is RelativeLayout) {
                    removeViewAt(2)
                }
            }

        val relativeLayout = RelativeLayout(context)
        val layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.MATCH_PARENT)
        relativeLayout.layoutParams = layoutParams
        relativeLayout.isFocusable = false

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyle)
        progressBar?.isFocusable = false
        progressBar?.isIndeterminate = true
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(progressBar, params)
        if (childCount > 1) {
            isShowLoading = true
            addView(relativeLayout, positionAddProgressTimeLive)
        }
    }

    fun hideLoadingTimeLive() {

        if (getChildAt(positionAddProgressTimeLive) != null) {
            isShowLoading = false
            removeViewAt(positionAddProgressTimeLive)
        }

    }


    fun initLiveTimeList(list: List<BLiveTime>, viewModel: MenuViewModel, bLive: BLive, viewFocus: View, isHideCalendar: Boolean, function: (View, Int, BLiveTime) -> Unit): RecyclerView {
        var relativeLayout = recyclerHashSet[Constants.TYPE_MENU.PROGRAM]

        if (getChildAt(positionAddProgressTimeLive) != null && getChildAt(positionAddProgressTimeLive) != relativeLayout) {
            removeViewAt(positionAddProgressTimeLive)
            isShowLoading = false
        }

        var recyclerView: RecyclerView? = relativeLayout?.getChildAt(0) as? RecyclerView
        var tvNoData: TextView? = relativeLayout?.getChildAt(1) as? TextView

        var line: View? = lineHashSet[Constants.TYPE_MENU.PROGRAM]
        val adapter = LiveTimeAdapter(viewModel, context, bLive)
        adapter.viewLiveFocus = viewFocus
        if (recyclerView == null) {

            line = View(context)
            line.isFocusable = false
            line.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            line.setBackgroundColor(Color.parseColor("#4DD1D1D1"))
            relativeLayout = RelativeLayout(context)
            val layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), LinearLayout.LayoutParams.MATCH_PARENT)
            relativeLayout.layoutParams = layoutParams
            relativeLayout.isFocusable = false

            recyclerView = RecyclerView(context)
            recyclerView.id = 11222
            recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val reLayoutParams = RelativeLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_180), RelativeLayout.LayoutParams.WRAP_CONTENT)
            reLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            recyclerView.layoutParams = reLayoutParams
            recyclerView.isFocusable = false

            relativeLayout.addView(recyclerView)
            tvNoData = TextView(context)
            tvNoData.text = context.getString(R.string.text_no_data_found)
            val tvLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            tvLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            tvNoData.layoutParams = tvLayoutParams
            tvNoData.setTextAppearance(context, android.R.style.TextAppearance_Medium)
            tvNoData.setTextColor(ContextCompat.getColor(context, R.color.white))
            relativeLayout.addView(tvNoData)
            relativeLayout.setBackgroundResource(R.drawable.bg_line)
            recyclerHashSet.put(Constants.TYPE_MENU.PROGRAM, relativeLayout)
            lineHashSet.put(Constants.TYPE_MENU.PROGRAM, line)
        }


        if (recyclerHashSet.get(Constants.TYPE_MENU.CALENDAR) != null && isHideCalendar) {
            removeView(recyclerHashSet.get(Constants.TYPE_MENU.CALENDAR))
            removeView(lineHashSet.get(Constants.TYPE_MENU.CALENDAR))
        }

        if (list.isEmpty()) tvNoData?.show() else tvNoData?.hide()
        adapter.onFocusListener = function
        recyclerView.adapter = adapter
        recyclerView.nextFocusDownId = recyclerView.id
        recyclerView.nextFocusUpId = recyclerView.id
        adapter.submitList(list)
        if (isHideCalendar) {
            if (childCount > 2)
                removeView(line)
            if (childCount > 2)
                removeView(relativeLayout)
            addView(line)
            addView(relativeLayout)

        } else {
            addView(relativeLayout, positionAddProgressTimeLive)
        }

        return recyclerView
    }


    fun initCalendarList(categoryId: String, viewModel: MenuViewModel, bLive: BLive, viewTimeLiveSelected: View?): RecyclerView {
        var relativeLayout = recyclerHashSet[Constants.TYPE_MENU.CALENDAR]
        var recyclerView: RecyclerView? = relativeLayout?.getChildAt(0) as? RecyclerView
        var tvNoData: TextView? = relativeLayout?.getChildAt(1) as? TextView

        var line: View? = lineHashSet[Constants.TYPE_MENU.CALENDAR]
        val adapter = CalendarAdapter(viewModel, context, categoryId, bLive)


        adapter.viewTimeLiveFocus = viewTimeLiveSelected
        val list = mutableListOf<Long>()
        for (i in 0 until 7) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
            calendar.add(Calendar.DATE, -i)
            list.add(calendar.timeInMillis)
        }
        list.reverse()
        if (recyclerView == null) {

            line = View(context)
            line.isFocusable = false
            line.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            line.setBackgroundColor(Color.parseColor("#4DD1D1D1"))
            relativeLayout = RelativeLayout(context)
            val layoutParams = LinearLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_130), LinearLayout.LayoutParams.MATCH_PARENT)
            relativeLayout.layoutParams = layoutParams
            relativeLayout.isFocusable = false

            recyclerView = RecyclerView(context)
            recyclerView.id = 11333
            recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val reLayoutParams = RelativeLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_130), RelativeLayout.LayoutParams.WRAP_CONTENT)
            reLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            recyclerView.layoutParams = reLayoutParams
            recyclerView.isFocusable = false

            relativeLayout.addView(recyclerView)
            tvNoData = TextView(context)
            tvNoData.text = context.getString(R.string.text_no_data_found)
            val tvLayoutParams = RelativeLayout.LayoutParams(context.resources.getDimensionPixelOffset(R.dimen.size_130), RelativeLayout.LayoutParams.WRAP_CONTENT)
            tvLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            tvNoData.layoutParams = tvLayoutParams
            tvNoData.setTextAppearance(context, android.R.style.TextAppearance_Medium)
            tvNoData.setTextColor(ContextCompat.getColor(context, R.color.white))
            relativeLayout.addView(tvNoData)

            relativeLayout.setBackgroundResource(R.drawable.bg_line)
            recyclerHashSet.put(Constants.TYPE_MENU.CALENDAR, relativeLayout)
            lineHashSet.put(Constants.TYPE_MENU.CALENDAR, line)
        }


        tvNoData?.hide()
        recyclerView.adapter = adapter
        recyclerView.nextFocusDownId = recyclerView.id
        recyclerView.nextFocusUpId = recyclerView.id
        adapter.submitList(list)
        Log.d("yenyen", Gson().toJson(list))
        removeView(line)
        removeView(relativeLayout)
        addView(line)
        addView(relativeLayout)
        return recyclerView
    }


    fun hideProgram() {
        if (recyclerHashSet.get(Constants.TYPE_MENU.PROGRAM) != null) {
            removeView(recyclerHashSet.get(Constants.TYPE_MENU.PROGRAM))

        }
        if (lineHashSet.get(Constants.TYPE_MENU.PROGRAM) != null) {
            removeView(lineHashSet.get(Constants.TYPE_MENU.PROGRAM))

        }
        hideCalendar()

    }

    fun hideCalendar() {
        if (recyclerHashSet.get(Constants.TYPE_MENU.CALENDAR) != null) {
            removeView(recyclerHashSet.get(Constants.TYPE_MENU.CALENDAR))

        }
        if (lineHashSet.get(Constants.TYPE_MENU.CALENDAR) != null) {
            removeView(lineHashSet.get(Constants.TYPE_MENU.CALENDAR))

        }

    }

}