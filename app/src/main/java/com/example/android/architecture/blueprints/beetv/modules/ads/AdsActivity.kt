package com.example.android.architecture.blueprints.beetv.modules.ads

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.BeeMediaType
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.player.BeeVideoPlayerActivity
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.getViewModelFactory
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl



class AdsActivity : AppCompatActivity(), ViewPager.OnPageChangeListener{


    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    private val viewModel by viewModels<AdsViewModel> { getViewModelFactory() }


    private lateinit var viewPager : ViewPager


    private var mAdapter: CustomPagerAdapter? = null
    private var pager_indicator: LinearLayout? = null
    private var dotsCount = 0
    private var currentIndex = 0
    private lateinit var dots: Array<ImageView?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ads)

        Log.d("bambi", "bambi coi vo man hinh nay chua");
        metroViewBorderImpl = MetroViewBorderImpl(this)
        viewPager = findViewById(R.id.viewpager)

        pager_indicator = findViewById<View>(R.id.viewPagerCountDots) as LinearLayout

        setupListAdapter()
    }

    private fun setupListAdapter() {
        viewModel.adsList.observe(this){
            //Add data here
            mAdapter = CustomPagerAdapter(this, it)
            viewPager.setAdapter(mAdapter)
            viewPager.setCurrentItem(0)
            viewPager.setOnPageChangeListener(this)

            setPageViewIndicator()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            viewPager.setCurrentItem(Math.max(currentIndex - 1, 0), true)
            return true
        }

        if (currentIndex == dotsCount - 1) { //At the last page
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                startActivity(Intent(this@AdsActivity, HomeActivity::class.java))
                finish()
                return true
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                viewPager.setCurrentItem(Math.min(currentIndex + 1, dotsCount), true)
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun setPageViewIndicator() {
        dotsCount = mAdapter?.getCount()!!
        dots = arrayOfNulls(dotsCount)
        for (i in 0 until dotsCount) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(resources.getDrawable(R.drawable.nonselecteditem_dot))
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(4, 0, 4, 0)
            dots[i]?.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    viewPager.setCurrentItem(i)
                    return true
                }
            })
            pager_indicator!!.addView(dots[i], params)
        }
        dots[0]?.setImageDrawable(resources.getDrawable(R.drawable.selecteditem_dot))
    }
    override fun onPageSelected(position: Int) {
        for (i in 0 until dotsCount) {
            dots[i]?.setImageDrawable(resources.getDrawable(R.drawable.nonselecteditem_dot))
        }

        currentIndex = position

        dots[position]?.setImageDrawable(resources.getDrawable(R.drawable.selecteditem_dot))
    }



    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

}