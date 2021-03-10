package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BLive
import com.example.android.architecture.blueprints.beetv.modules.ads.loadImage
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.widgets.AnimationAutoTextScroller


class LiveAdapter2(context: Context?, listChannel: List<BLive>, val bCategoryID: String, private val viewModel: MenuViewModel) : ScrollView(context) {
    var positonRequest: Int? = null
    var viewFocus: View? = null
    var viewMenuFocus: View? = null
    var viewSelected: View? = null

    var mViewList = mutableListOf<View>()
    var onFavoriteClickListener: ((Int, BLive) -> Unit)? = null
    var onFocusListener: ((Int, BLive) -> Unit)? = null
    var itemCount: Int = listChannel.size
    var isFirst: Boolean = true
    var startAnimationInMilliseconds: Long = System.currentTimeMillis()

    private var mList = listChannel

    fun initChannel() {

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.isFocusable = false
        mList.forEachIndexed { position, item ->
            val inflatedView = inflate(context, R.layout.item_channel, null)
            inflatedView.tag = position to item
            val tvContent = inflatedView.findViewById<TextView>(R.id.tv_content)
            val main = inflatedView.findViewById<LinearLayout>(R.id.main)
            val tvId = inflatedView.findViewById<TextView>(R.id.tv_id)
            val tvName = inflatedView.findViewById<TextView>(R.id.tv_name)
            val ivLogo = inflatedView.findViewById<ImageView>(R.id.iv_logo)
            val icFav = inflatedView.findViewById<ImageView>(R.id.favorite_ic)

            tvId.text = item.channel_number
            tvId.visibility = if (item.channel_number == null) View.GONE else View.VISIBLE
            loadImage(ivLogo, item.logo)
            tvName.text = item.name
            tvContent.text = item.description
            tvContent.visibility = if (item.description == null) View.GONE else View.VISIBLE
            icFav.visibility = if (item.is_favorite == true) View.VISIBLE else View.GONE

            main.id = 400000 + position
            main.nextFocusLeftId = (viewModel.mMenuRecyclerView?.adapter as? MenuAdapter2)?.idSelected
                    ?: -1

            main.nextFocusRightId = -1

            if (position == 0) {
                main.nextFocusUpId = 400000 + position
            } else {
                main.nextFocusUpId = 400000 + position - 1
            }
            if (position == mList.size - 1) {
                main.nextFocusDownId = 400000 + position
            } else {
                main.nextFocusDownId = 400000 + position + 1
            }



            if (item.description?.startsWith("방송중") == true) {
                tvContent.text = item.description.substring(4).trim()
            }
//            else  tvContent.text = "item.description aaa"
            else tvContent.text = item.description
            main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
                if (k) {

                    viewFocus = v


                    val viewPosition = (v.tag as Pair<Int, BLive>).first
                    val viewChildMain = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    if (400000 + viewPosition == positonRequest) {

                        if (viewChildMain.getChildAt(0) is LinearLayout) {
                            val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                            (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                            val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                            (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                            ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        }
                        viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)
                    } else
                        viewFocus?.setBackgroundResource(R.drawable.border_color_red)


                    if (viewChildMain.getChildAt(0) is LinearLayout) {
                        val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout

                        ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).isSelected = true
//                        isFirst = true;
//                        startAnimation(resources.getDimensionPixelOffset(R.dimen.size_180), ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView))
                    }

                    viewModel.hideMenu(true)
                    onFocusListener?.invoke(position, item)

                } else {
                    val viewPosition = (v.tag as Pair<Int, BLive>).first
                    if (400000 + viewPosition == positonRequest) {
                        val viewChildMain = ((v as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        if (viewChildMain.getChildAt(0) is LinearLayout) {
                            val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                            (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                            val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                            (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                            ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
//                            ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).clearAnimation()
                            ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).isSelected = false
                        }

                        v?.setBackgroundColor(Color.parseColor("#33FFFFFF"))
                    } else {
                        if (v is LinearLayout) {
                            val viewChildMain = ((v as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                            if (viewChildMain.getChildAt(0) is LinearLayout) {
                                val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                                (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                                val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                                (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                                ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
//                                ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).clearAnimation()
                                ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).isSelected = false
                            }

                        }

                        v?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    }
                    viewModel.hideMenu(false)

                    viewFocus = null
                }
            }

            if (positonRequest == position + 400000) {
                viewSelected = main
                tvName.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                tvId.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                tvContent.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))

                main.setBackgroundColor(Color.parseColor("#33FFFFFF"))
                (viewModel.mMenuRecyclerView!!.adapter as MenuAdapter2).viewSelected?.nextFocusRightId = viewSelected?.id
                        ?: -1
                if (viewModel.isPlayRecord && viewModel.isLiveLastSelected) {
                    if (positonRequest != null)
                        viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, positonRequest!! - 400000)
                    else viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, 0)
                }

            } else {

                tvName.setTextColor(ContextCompat.getColor(context, R.color.alto))
                tvId.setTextColor(ContextCompat.getColor(context, R.color.alto))
                tvContent.setTextColor(ContextCompat.getColor(context, R.color.alto))

                main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

            }

            main.setOnLongClickListener(View.OnLongClickListener {
                onFavoriteClickListener?.invoke(position, item)
                icFav.visibility = if (item.is_favorite == true) View.GONE else View.VISIBLE
                true
            })


            main.setOnClickListener {
                positonRequest = 400000 + (it.tag as Pair<Int, BLive>).first
                if (viewSelected is LinearLayout) {
                    val viewChildMain = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    if (viewChildMain.getChildAt(0) is LinearLayout) {
                        val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                        (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                        val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                        (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                        ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                    }
                    viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

                }
                viewSelected = it
                val viewChildMain = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                if (viewChildMain.getChildAt(0) is LinearLayout) {
                    val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                    (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                    (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                }
                viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)

//            viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, position)
                viewModel.openCategory(item)
            }

            mViewList.add(main)
            linearLayout.addView(inflatedView)


        }

        this.addView(linearLayout)

        if (positonRequest != null && positonRequest ?: 0 > 399999 && !viewModel.isPlayRecord)
            mViewList[positonRequest!! - 400000].requestFocus()
    }

    fun startAnimation(witdh: Int, v: TextView) {
        startAnimationInMilliseconds = System.currentTimeMillis()
        var textWidth = v.paint.measureText(v.text.toString()).toInt()

        val textscroller = AnimationAutoTextScroller(v, witdh.toFloat(), textWidth * 20,
                object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        if (System.currentTimeMillis() - startAnimationInMilliseconds < 20) {
                            //Workaround to fix an issue
                            return
                        }
                       if (viewFocus is LinearLayout) {
                            val viewChildMain = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                            if (viewChildMain.getChildAt(0) is LinearLayout) {
                                val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                                ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView).clearAnimation()
                                isFirst = false

                                startAnimation(witdh, ((viewChildTwo.getChildAt(1) as LinearLayout).getChildAt(0) as TextView))
                            }
                        }
                    }

                    override fun onAnimationStart(p0: Animation?) {

                    }
                }, v.paint.measureText(v.text.toString()).toInt(), isFirst)
        textscroller.setScrollingText(v.text.toString())
        textscroller.start()
    }

    fun updateFavorite(position: Int, isFavorite: Boolean) {
        mList[position].is_favorite = isFavorite
    }

    fun submitList(list: List<BLive>) {
        mList = list
        this.removeAllViews()
        initChannel()
    }
}