package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginTop
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.MenuAdapter2
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.modules.ads.loadImage
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.modules.search.SearchViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.metro.CornerVew
import com.google.android.flexbox.*
import kotlin.math.ceil

class SearchMovieView : ScrollView {

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

    var mLastFocus: Int = -1
    var onFocusListener: ((View) -> Unit)? = null

    private var flexboxLayout: FlexboxLayout = FlexboxLayout(context)
    private var mList: List<BMovie>? = null
    var itemCount: Int = 0
    var columnCount: Float = 4f
    lateinit var viewModel: SearchViewModel
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    var viewFocus: View? = null

    fun setMovieList(movies: List<BMovie>, width: Int = itemWidth, height: Int = itemHeight) {
        itemWidth = width
        itemHeight = height
        removeAllViews()
        flexboxLayout.flexDirection = FlexDirection.ROW
        flexboxLayout.flexWrap = FlexWrap.WRAP
        flexboxLayout.alignItems = AlignItems.STRETCH
        flexboxLayout.alignContent = AlignContent.STRETCH
        flexboxLayout.justifyContent = JustifyContent.FLEX_START
        flexboxLayout.removeAllViews()
        val lp = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.MATCH_PARENT)
        lp.order = -1
        lp.flexGrow = 2f
        flexboxLayout.layoutParams = lp
        mList = movies
        itemCount = movies.size
        movies.forEachIndexed { position, item ->
            val inflatedView = inflate(context, R.layout.item_menu_movie, null)
            inflatedView.tag = item.id
            val main = inflatedView.findViewById<ZoomSelectionThumbLayout>(R.id.main)
            val tvTop = inflatedView.findViewById<CornerVew>(R.id.tv_top)
            val ivCover = inflatedView.findViewById<ImageView>(R.id.iv_cover)
            val tvName = inflatedView.findViewById<TextView>(R.id.tv_name)
            val tvScore = inflatedView.findViewById<TextView>(R.id.tv_score)
            val scale = getContext().getResources().getDisplayMetrics().density;
            val lp = LinearLayout.LayoutParams(width, height)
            lp.setMargins((10 *scale +.5f).toInt(), (10 *scale +.5f).toInt(),(2 *scale +.5f).toInt(),(18 *scale +.5f).toInt())
            main.layoutParams = lp
            loadImage(ivCover, item.cover_image)
            tvName.text = item.name
            tvScore.text = String.format("%.1f", if (item.score == null) 0.toFloat() else item.score?.toFloat())

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.billing
                    ?: "", item.position_updated_at)
            if (label.isNotBlank()) {
                tvTop.setText(label)
                tvTop.setBkgColor(DisplayAdaptive.getColorByLabel(label))
                (tvTop as View).show()

            } else {
                (tvTop as View).hide()
            }
            main.tag = position
            main.id = 1999999 + position
            main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
                if (k) {
                    viewFocus = v
                    mLastFocus = v.tag as Int
                    onFocusListener?.invoke(v)
                }
            }

            main.setOnClickListener {
                viewModel.openMovieDetail(item.id)
            }

            val row = ceil((itemCount.toDouble() / columnCount))
            if (ceil(((position + 1).toDouble() / columnCount)) == row || itemCount <= columnCount) {
                main.nextFocusDownId = main.id
            }

            // Lock end list only
            if (position == mList!!.size - 1) {
                main.nextFocusRightId = main.id
            } else {
                main.nextFocusRightId = -1

            }

            if (mLastFocus == position) {
                main.requestFocus()
            }
            flexboxLayout.addView(inflatedView)
        }

        addView(flexboxLayout)
    }

    fun updateMovieList(movies: List<BMovie>) {
        for (i in itemCount - 1 downTo itemCount - 5) {
            val view = flexboxLayout.getChildAt(i)
            view.nextFocusDownId =  1999999 + i + 4
        }
        mList?.plus(movies)

        movies.forEachIndexed { index, item ->
            val position = itemCount + index
            val inflatedView = inflate(context, R.layout.item_menu_movie, null)
            inflatedView.tag = item.id
            val main = inflatedView.findViewById<ZoomSelectionThumbLayout>(R.id.main)
            val tvTop = inflatedView.findViewById<CornerVew>(R.id.tv_top)
            val ivCover = inflatedView.findViewById<ImageView>(R.id.iv_cover)
            val tvName = inflatedView.findViewById<TextView>(R.id.tv_name)
            val tvScore = inflatedView.findViewById<TextView>(R.id.tv_score)
            val scale = getContext().getResources().getDisplayMetrics().density;
            val lp = LinearLayout.LayoutParams(itemWidth, itemHeight)
            lp.setMargins((10 *scale +.5f).toInt(), (10 *scale +.5f).toInt(),(2 *scale +.5f).toInt(),(18 *scale +.5f).toInt())
            main.layoutParams = lp
            loadImage(ivCover, item.cover_image)
            tvName.text = item.name
            tvScore.text = String.format("%.1f", if (item.score == null) 0.toFloat() else item.score?.toFloat())

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.billing
                    ?: "", item.position_updated_at)
            if (label.isNotBlank()) {
                tvTop.setText(label)
                tvTop.setBkgColor(DisplayAdaptive.getColorByLabel(label))
                (tvTop as View).show()

            } else {
                (tvTop as View).hide()
            }
            main.tag = position
            main.id = 1999999 + position
            main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
                if (k) {
                    viewFocus = v
                    mLastFocus = v.tag as Int
                    onFocusListener?.invoke(v)
                }
            }

            main.setOnClickListener {
                viewModel.openMovieDetail(item.id)
            }

            val row = ceil((mList!!.size.toDouble() / columnCount))
            if (ceil(((position + 1).toDouble() / columnCount)) == row || mList!!.size <= columnCount) {
                main.nextFocusDownId = main.id
            }

            // Lock end list only
            if (position == mList!!.size - 1) {
                main.nextFocusRightId = main.id
            } else {
                main.nextFocusRightId = -1

            }

            if (mLastFocus == position) {
                main.requestFocus()
            }
            flexboxLayout.addView(inflatedView)
        }

        itemCount = mList!!.size
    }

    fun removeItemAt(position: Int) {
        flexboxLayout.removeViewAt(position)
    }

    fun scrollToBottom() {
        val lastChild = this.getChildAt(childCount - 1)
        val bottom = lastChild.bottom + paddingBottom
        val delta = bottom - (scrollY+ height)
        smoothScrollBy(0, delta)
    }
}