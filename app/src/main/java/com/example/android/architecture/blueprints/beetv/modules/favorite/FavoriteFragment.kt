package com.example.android.architecture.blueprints.beetv.modules.favorite

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.MenuAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.models.Category
import com.example.android.architecture.blueprints.beetv.data.models.Movie
import com.example.android.architecture.blueprints.beetv.databinding.FragmentFavoriteBinding
import com.example.android.architecture.blueprints.beetv.util.getViewModelFactory
import com.example.android.architecture.blueprints.beetv.widgets.AutoLayoutManager
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderHandler
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl
import java.text.SimpleDateFormat
import java.util.*

class FavoriteFragment : Fragment() {
    private val viewModel by viewModels<FavoriteViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentFavoriteBinding
    private val args: FavoriteFragmentArgs by navArgs()
    private lateinit var mAdapter: MenuAdapter

    private var lastView3: View? = null
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    private lateinit var metroViewBorderImpl2: MetroViewBorderImpl
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentFavoriteBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        metroViewBorderImpl = MetroViewBorderImpl(context)
        metroViewBorderImpl2 = MetroViewBorderImpl(context)
        metroViewBorderImpl2.setBackgroundResource(R.drawable.border_color_red)
        metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
            if (lastView3 != null) {
                changeBackgroundButton(lastView3, null)
            }
            lastView3 = newFocus

            changeBackgroundButton(oldFocus, newFocus)
            metroViewBorderImpl.view.tag = newFocus
            val tag = newFocus?.tag as? Pair<*, *>
            if (tag != null) {

                getListMovie((tag.second as Category).movies)


            }
        }

        initMenu()
        showTime()
    }
    private fun showTime() {
        val someHandler = Handler(Looper.getMainLooper())
        someHandler.postDelayed(object : Runnable {
            override fun run() {
                viewDataBinding.tvClock.text = SimpleDateFormat("HH:mm").format(Date())
                someHandler.postDelayed(this, 1000)
            }
        }, 10)
        viewDataBinding.tvDate.text = SimpleDateFormat("EEEE").format(Date()) + "\n" + SimpleDateFormat("yyyy.MM.dd").format(Date())
    }
    private fun initMenu() {
        metroViewBorderImpl.attachTo(viewDataBinding.list)
        metroViewBorderImpl.attachTo(viewDataBinding.rvMenuItem)
        mAdapter = MenuAdapter(Category.category(requireContext()), requireContext())
        viewDataBinding.rvMenuItem.layoutManager = GridLayoutManager(context, 1)
        viewDataBinding.rvMenuItem.adapter = mAdapter
        getListMovie(Category.category(requireContext()).get(0).movies)

    }

    private fun getListMovie(movie: MutableList<Movie>) {
        val gridlayoutManager: GridLayoutManager = AutoLayoutManager(context, 5)
        gridlayoutManager.orientation = GridLayoutManager.VERTICAL
        viewDataBinding.rvDetailList.layoutManager = gridlayoutManager
        metroViewBorderImpl2.attachTo(viewDataBinding.rvDetailList)
        val widthItem = requireContext().resources.getDimensionPixelOffset(R.dimen.size_130)
        val heightItem = widthItem * 377 / 260
        val movieAdapter = TopMovieAdapter(movie, requireContext(), widthItem, heightItem)

        movieAdapter.mOnItemClickListener = {
//            viewModel.openMovieDetail(it.id)
        }
        viewDataBinding.rvDetailList.adapter = movieAdapter

    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {

        if (oldView != null)

            if (oldView is LinearLayout) {
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.alto))
                }
            }
        if (newView != null)
            if (newView is LinearLayout) {

                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
                }
            }


    }

}