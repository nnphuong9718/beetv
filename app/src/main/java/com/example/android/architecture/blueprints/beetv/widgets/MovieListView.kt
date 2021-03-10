package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.LinearLayout
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.MenuAdapter2
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.modules.ads.loadImage
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.metro.CornerVew
import com.google.android.flexbox.*
import kotlin.math.ceil


class MovieListView : ScrollView {

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

    var onFocusListener: ((View) -> Unit)? = null
    var viewMenuFocus: View? = null
    var lastFocus: Int = -1

    private var flexboxLayout: FlexboxLayout = FlexboxLayout(context)
    private var mList: List<BMovie>? = null
    var itemCount: Int = 0
    var columnCount: Float = 5f
    lateinit var viewModel: MenuViewModel
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    private var mCategory: String = Constants.TYPE_CATEGORY.MOVIE.type
    var viewFocus: View? = null

    fun setMovieList(movies: List<BMovie>, category: String?, width: Int = itemWidth, height: Int = itemHeight) {
        if (category != null) {
            mCategory = category
        }
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
            val ratingBar = inflatedView.findViewById<RatingBar>(R.id.rating)
            val epsView = inflatedView.findViewById<LinearLayout>(R.id.epScrollview)
            val epsPart = inflatedView.findViewById<TextView>(R.id.movie_ep)
            val scale = getContext().getResources().getDisplayMetrics().density;
            val lp = LinearLayout.LayoutParams(width, height)
            lp.setMargins((8 * scale + .5f).toInt(), (12 * scale + .5f).toInt(), (5 * scale + .5f).toInt(), (12 * scale + .5f).toInt())
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
            main.id = 7777 + position

            val row = ceil((itemCount.toDouble() / columnCount)).toInt()
            val curRow = ceil(((position + 1).toDouble() / columnCount)).toInt()
            if (curRow == row || itemCount <= 5) {
                main.nextFocusDownId = main.id
            }

            if (curRow == 1) {
                main.nextFocusUpId = main.id
            }

            // Lock end list only
            if (position == mList!!.size - 1) {
                main.nextFocusRightId = main.id
            } else {
                main.nextFocusRightId = -1

            }

            if (position == 0 || position % 5 == 0) {
                main.nextFocusLeftId = (viewModel.mMenuRecyclerView?.adapter as MenuAdapter2).idSelected
                        ?: -1

            } else {
                main.nextFocusLeftId = -1
            }

            main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
                if (k) {
                    val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(-10, 0, 0, -12)
                    tvName.setLayoutParams(params)
                    tvScore.gravity = Gravity.BOTTOM
                    if (mCategory == Constants.TYPE_CATEGORY.MOVIE.type) {
                        ratingBar.rating = if (item.score != null) item!!.score!!.toFloat() / 2f else 0 / 2f
                        ratingBar.show()
                    } else {
                        if (!item.videos.isNullOrEmpty()) {
                            item.videos.sortedBy { it.position }
                            epsPart.text = item.videos[item.videos.size - 1].title
//                        } else if (item.last_video_title != null ) {
//                            epsPart.text = item.last_video_title
                        } else {
                            epsPart.text = ""
                        }
                        epsView.show()
                        Handler().postDelayed({
                            epsPart.isSelected = true
                        }, 3000)

//                        if (epsPart.text.length > 8) {
//                            startAnimation(resources.getDimensionPixelOffset(R.dimen.size_160), epsPart)
//                        }
                    }
                    viewFocus = v
                    lastFocus = v.tag as Int
                    onFocusListener?.invoke(v)
                } else {
                    tvScore.gravity = Gravity.CENTER
                    val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 0, 0, 0)
                    tvName.setLayoutParams(params)
                    ratingBar.hide()
                    epsView.hide()
                }
            }

            main.setOnClickListener {
                viewModel.openMovieDetail(item.id)
            }



            if (lastFocus == position) {
                main.requestFocus()
            }
            flexboxLayout.addView(inflatedView)
        }

        addView(flexboxLayout)
    }

    fun updateMovieList(movies: List<BMovie>) {
        mList?.plus(movies)

        for (i in (itemCount - 6) until  (itemCount)) {
            val view = flexboxLayout.getChildAt(i)
            view.nextFocusDownId = -1
        }

        movies.forEachIndexed { index, item ->
            val position = itemCount + index
            val inflatedView = inflate(context, R.layout.item_menu_movie, null)
            inflatedView.tag = item.id
            val main = inflatedView.findViewById<ZoomSelectionThumbLayout>(R.id.main)
            val tvTop = inflatedView.findViewById<CornerVew>(R.id.tv_top)
            val ivCover = inflatedView.findViewById<ImageView>(R.id.iv_cover)
            val tvName = inflatedView.findViewById<TextView>(R.id.tv_name)
            val tvScore = inflatedView.findViewById<TextView>(R.id.tv_score)
            val ratingBar = inflatedView.findViewById<RatingBar>(R.id.rating)
            val epsView = inflatedView.findViewById<LinearLayout>(R.id.epScrollview)
            val epsPart = inflatedView.findViewById<TextView>(R.id.movie_ep)
            val scale = getContext().getResources().getDisplayMetrics().density;
            val lp = LinearLayout.LayoutParams(itemWidth, itemHeight)
            lp.setMargins((8 * scale + .5f).toInt(), (12 * scale + .5f).toInt(), (5 * scale + .5f).toInt(), (12 * scale + .5f).toInt())
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
            main.id = 7777 + position
            val row = ceil((mList!!.size.toDouble() / columnCount)).toInt()
            val curRow = ceil(((position + 1).toDouble() / columnCount)).toInt()
            if (curRow == row || itemCount <= 5) {
                main.nextFocusDownId = main.id
            }

            // Lock end list only
            if (position == mList!!.size - 1) {
                main.nextFocusRightId = main.id
            } else {
                main.nextFocusRightId = -1

            }

            if (position == 0 || position % 5 == 0) {
                main.nextFocusLeftId = (viewModel.mMenuRecyclerView?.adapter as MenuAdapter2).idSelected
                        ?: -1

            } else {
                main.nextFocusLeftId = -1
            }

            main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
                if (k) {
                    val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(-10, 0, 0, -12)
                    tvName.setLayoutParams(params)
                    tvScore.gravity = Gravity.BOTTOM
                    if (mCategory == Constants.TYPE_CATEGORY.MOVIE.type) {
                        ratingBar.rating = if (item.score != null) item!!.score!!.toFloat() / 2f else 0 / 2f
                        ratingBar.show()
                    } else {
                        if (!item.videos.isNullOrEmpty()) {
                            item.videos.sortedBy { it.position }
                            epsPart.text = item.videos[item.videos.size - 1].title
                        } else {
                            epsPart.text = ""
                        }
                        epsView.show()
                        epsPart.isSelected = true
//                        if (epsPart.text.length > 8) {
//                            startAnimation(resources.getDimensionPixelOffset(R.dimen.size_160), epsPart)
//                        }
                    }
                    viewFocus = v
                    lastFocus = v.tag as Int
                    onFocusListener?.invoke(v)
                } else {
                    tvScore.gravity = Gravity.CENTER
                    val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 0, 0, 0)
                    tvName.setLayoutParams(params)
                    ratingBar.hide()
                    epsView.hide()
                }
            }

            main.setOnClickListener {
                viewModel.openMovieDetail(item.id)
            }

            if (lastFocus == position) {
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