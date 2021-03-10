package com.example.android.architecture.blueprints.beetv.modules.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.BuildConfig
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.EventObserver
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.common.basegui.BaseFragment
import com.example.android.architecture.blueprints.beetv.data.adapter.FavoriteMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.SearchMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.WatchedMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.BWatchHistory
import com.example.android.architecture.blueprints.beetv.data.models.Status
import com.example.android.architecture.blueprints.beetv.databinding.FragmentSearchBinding
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import com.example.android.architecture.blueprints.beetv.manager.LanguageManager
import com.example.android.architecture.blueprints.beetv.manager.WatchHistoryManager
import com.example.android.architecture.blueprints.beetv.modules.home.HomeActivity
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailFragment
import com.example.android.architecture.blueprints.beetv.util.*
import com.example.android.architecture.blueprints.beetv.widgets.AutoLayoutManager
import com.example.android.architecture.blueprints.beetv.widgets.KeyboardItemView
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl
import com.github.kimkevin.hangulparser.HangulParser
import com.github.kimkevin.hangulparser.HangulParserException
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.ceil


class SearchFragment : BaseFragment(), View.OnClickListener {

    companion object {
        val TAG = SearchFragment::class.java.name
    }

    private val viewModel by viewModels<SearchViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentSearchBinding
//    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    private var mType: String? = null
    var page: Int = 1
    var isLoadMore = false
//    private lateinit var metroViewBorderImpl: MetroViewBorderImpl

    private var mAdapter: SearchMovieAdapter? = null
    private var mFavoriteAdapter: FavoriteMovieAdapter? = null
    private var mWatchedAdapter: WatchedMovieAdapter? = null
    private var mIsKorea = false
    private var mKoreaCharacterList = arrayListOf<String>()
    private var mPositionSpace = -1
    private var mPositionSpaceCurrent = -1

    private var isRequesting: Boolean = false
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        viewDataBinding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            click = ClickProxy(viewModel, this, requireContext(), this@SearchFragment)
            viewmodel = viewModel
        }
        mType = arguments?.getString("type")
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.FLAVOR.equals("beetv")) {
//            metroViewBorderImpl = MetroViewBorderImpl(context)
//            metroViewBorderImpl.attachTo(viewDataBinding.main)
//
//            metroViewBorderImpl.viewBorder.addOnFocusChanged { oldFocus, newFocus ->
//                metroViewBorderImpl.view.tag = newFocus
//                changeBackgroundButton(oldFocus, newFocus)
//            }
            viewDataBinding.btA.requestFocus()
//            changeBackgroundButton(null, viewDataBinding.btA)
//
//            viewDataBinding.btEmpty.setOnFocusChangeListener { view, b ->
//                changeBackgroundFocusButton(b, view)
//            }
//            viewDataBinding.btDelete.setOnFocusChangeListener { view, b ->
//                changeBackgroundFocusButton(b, view)
//            }
//            viewDataBinding.btKorean.setOnFocusChangeListener { view, b ->
//                changeBackgroundFocusButton(b, view)
//            }
//            viewDataBinding.btEnglish.setOnFocusChangeListener { view, b ->
//                changeBackgroundFocusButton(b, view)
//            }
            initButton()
        } else {
            viewDataBinding.etSearch.setOnEditorActionListener { view, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (event == null || !event.isShiftPressed()) {
                        search()
                        true
                    }
                }
                false
            }
        }

        setupMovie()
        setupSnackbar()
        setupNavigation()

        if (LanguageManager.getInstance().getData(requireContext()).equals("ko")) {
            changeKorea()
        } else {
            changeEnglish()
        }
        page = 1
        search()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupMovie() {
        viewDataBinding.rvResult2.viewModel = viewModel
        val widthItem: Int
        if (BuildConfig.FLAVOR.equals("mobile")) {
            widthItem = (DisplayAdaptive.getDisplayMetrics(activity!!).widthPixels
                    - context!!.resources.getDimensionPixelOffset(R.dimen.size_20) * 2
                    - context!!.resources.getDimensionPixelOffset(R.dimen.size_12) * 5) / 5 // between padding item
        } else {
            widthItem = (DisplayAdaptive.getDisplayMetrics(activity!!).widthPixels
                    - context!!.resources.getDimensionPixelOffset(R.dimen.size_276)
                    - context!!.resources.getDimensionPixelOffset(R.dimen.size_40) * 3
                    - context!!.resources.getDimensionPixelOffset(R.dimen.size_18) * 3) / 4// between padding item
        }


        val heightItem = (widthItem * DisplayAdaptive.THUMB_RATIO).toInt()
        if (mType == Constants.TYPE_CATEGORY.SEARCH.type) {
            viewDataBinding.rvResult2.onFocusListener = {
                val position: Int = it.tag as Int

                viewDataBinding.tvCurrentPage.text = ceil((position + 1).toDouble() / 16).toInt().toNumberString()
                val row = ceil((viewDataBinding.rvResult2.itemCount.toDouble() / 4)).toInt()
                val curRow = ceil(((position + 1).toDouble() / 4)).toInt()
                if (curRow ==  row && !isLoadMore) {
                    viewDataBinding.rvResult2.scrollToBottom()
                } else if (curRow  > 2) {
                    viewDataBinding.rvResult2.viewFocus?.let { it1 -> viewDataBinding.rvResult2.smoothScrollTo(0, it1.top - 250) }
                }
                if (viewDataBinding.rvResult2.mLastFocus >= viewDataBinding.rvResult2.itemCount - 4 && isLoadMore && !isRequesting) {
                    isRequesting = true
                    search()
                }

            }
            viewDataBinding.rvResult2.setMovieList(mMovieAllList, widthItem, heightItem)
        }


    }

    private fun changeBackgroundFocusButton(gainFocus: Boolean, view: View?) {

        if (gainFocus) {
            if (view is MetroItemFrameLayout) {
                Log.d("changeBackgroundOld", view.toString())
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
                if (view.getChildAt(0) is TextView) {
                    (view.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                if (view.getChildAt(0) is LinearLayout) {
                    val childView = view.getChildAt(0) as LinearLayout
                    if (childView.getChildAt(0) is ImageView) {
                        (childView.getChildAt(0) as ImageView).setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    }
                    if (childView.getChildAt(1) is TextView) {
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
        } else {
            if (view is MetroItemFrameLayout) {
                Log.d("changeBackgroundOld", view.toString())
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chapter_bkg_color))
                if (view.getChildAt(0) is TextView) {
                    (view.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                if (view.getChildAt(0) is LinearLayout) {
                    val childView = view.getChildAt(0) as LinearLayout
                    if (childView.getChildAt(0) is ImageView) {
                        (childView.getChildAt(0) as ImageView).setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    }
                    if (childView.getChildAt(1) is TextView) {
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
        }


    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {

        if (oldView != null) {
            if (oldView is KeyboardItemView) {
                oldView.setColor(R.color.chapter_bkg_color)
            }
            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chapter_bkg_color))
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                if (oldView.getChildAt(0) is LinearLayout) {
                    val childView = oldView.getChildAt(0) as LinearLayout
                    if (childView.getChildAt(0) is ImageView) {
                        (childView.getChildAt(0) as ImageView).setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    }
                    if (childView.getChildAt(1) is TextView) {
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
        }

        if (newView != null) {
            if (newView is KeyboardItemView) {
                newView.setColor(R.color.sunsetOrange)
            }
            if (newView is MetroItemFrameLayout) {
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sunsetOrange))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

                if (newView.getChildAt(0) is LinearLayout) {
                    val childView = newView.getChildAt(0) as LinearLayout
                    if (childView.getChildAt(0) is ImageView) {
                        (childView.getChildAt(0) as ImageView).setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    }
                    if (childView.getChildAt(1) is TextView) {
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }

            }

        }

    }

    private fun setupNavigation() {
        viewModel.openMovieDetailEvent.observe(viewLifecycleOwner, EventObserver {
            openMovieDetail(it)
        })

    }


    private fun openMovieDetail(movieID: String) {
        val fragment = MovieDetailFragment()
        val args = Bundle().apply {
            putString("movie_id", movieID)
        }
        fragment.arguments = args
        (requireActivity() as HomeActivity).addFragment(fragment, MovieDetailFragment.TAG)
    }


    private fun initButton() {
        if (BuildConfig.FLAVOR.equals("beetv")) {
            viewDataBinding.btA.setOnClickListener(this)
            viewDataBinding.btB.setOnClickListener(this)
            viewDataBinding.btC.setOnClickListener(this)
            viewDataBinding.btD.setOnClickListener(this)
            viewDataBinding.btE.setOnClickListener(this)
            viewDataBinding.btF.setOnClickListener(this)
            viewDataBinding.btG.setOnClickListener(this)
            viewDataBinding.btH.setOnClickListener(this)
            viewDataBinding.btI.setOnClickListener(this)
            viewDataBinding.btJ.setOnClickListener(this)
            viewDataBinding.btK.setOnClickListener(this)
            viewDataBinding.btL.setOnClickListener(this)
            viewDataBinding.btM.setOnClickListener(this)
            viewDataBinding.btN.setOnClickListener(this)
            viewDataBinding.btO.setOnClickListener(this)
            viewDataBinding.btP.setOnClickListener(this)
            viewDataBinding.btQ.setOnClickListener(this)
            viewDataBinding.btR.setOnClickListener(this)
            viewDataBinding.btS.setOnClickListener(this)
            viewDataBinding.btT.setOnClickListener(this)
            viewDataBinding.btU.setOnClickListener(this)
            viewDataBinding.btW.setOnClickListener(this)
            viewDataBinding.btX.setOnClickListener(this)
            viewDataBinding.btY.setOnClickListener(this)
            viewDataBinding.btZ.setOnClickListener(this)
            viewDataBinding.btV.setOnClickListener(this)
            viewDataBinding.btOne.setOnClickListener(this)
            viewDataBinding.btTwo.setOnClickListener(this)
            viewDataBinding.btThree.setOnClickListener(this)
            viewDataBinding.btFour.setOnClickListener(this)
            viewDataBinding.btFive.setOnClickListener(this)
            viewDataBinding.btSix.setOnClickListener(this)
            viewDataBinding.btSeven.setOnClickListener(this)
            viewDataBinding.btEight.setOnClickListener(this)
            viewDataBinding.btNine.setOnClickListener(this)
            viewDataBinding.btZero.setOnClickListener(this)
            viewDataBinding.btButton3.setOnClickListener(this)
            viewDataBinding.btButton4.setOnClickListener(this)
            viewDataBinding.btButton5.setOnClickListener(this)
            viewDataBinding.btButton6.setOnClickListener(this)
            viewDataBinding.btButton7.setOnClickListener(this)
            viewDataBinding.btButton8.setOnClickListener(this)
            viewDataBinding.btButton9.setOnClickListener(this)
            viewDataBinding.btButton10.setOnClickListener(this)
            viewDataBinding.btButton11.setOnClickListener(this)
            viewDataBinding.btButton12.setOnClickListener(this)
            viewDataBinding.btButton13.setOnClickListener(this)
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarInt, Snackbar.LENGTH_SHORT)
    }


    private val mMovieAllList = mutableListOf<BMovie>()

    fun search() {
        val keyword = viewDataBinding.etSearch.text.toString()
        if (mType == Constants.TYPE_CATEGORY.SEARCH.type) {
            viewModel.search(viewDataBinding.etSearch.text.toString(), page).observe(viewLifecycleOwner, Observer { it ->
                run {
                    when (it.status) {
                        Status.SUCCESS -> {
                            isRequesting = false
                            viewModel.isLoading.value = false
                            viewDataBinding.progressBar.hide()
                            val rawItems = it.data!!.results?.objects?.rows ?: mutableListOf()

                            // Todo sort items list
                            val kw = viewDataBinding.etSearch.text.toString().toLowerCase()!!
                            val items = rawItems?.sortedWith(movieComparator(kw))


                            if (it.data.pagination.total == 0) {
                                viewDataBinding.tvCurrentPage.text = 0.toNumberString()
                            } else
                                viewDataBinding.tvCurrentPage.text = it.data.pagination.current_page.toNumberString()
                            viewDataBinding.tvTotalPage.text = it.data.pagination.total.toNumberString()

                            if (it.data.pagination.current_page == 1) {
                                mMovieAllList.clear()
                                mMovieAllList.addAll(items)
                                viewDataBinding.rvResult2.setMovieList(mMovieAllList)
                            } else {
                                mMovieAllList.addAll(items)
                                viewDataBinding.rvResult2.updateMovieList(items)

                            }

                            isLoadMore = it.data.pagination.current_page < it.data.pagination.total
                            Log.d("yenyen", "search : $isLoadMore")

                            if (it.data.pagination.current_page == 1 && it.data.results!!.objects.count == 0) {
                                viewDataBinding.tvResult.show()
                                viewDataBinding.rvResult2.hide()
                                viewDataBinding.tvResult.text = getString(R.string.text_search_not_found)
                            } else {
                                viewDataBinding.rvResult2.show()
                                viewDataBinding.tvResult.hide()
                            }
                            if (page == 1) {
                                Handler().postDelayed({ viewDataBinding.rvResult2.scrollTo(0, 0) }, 200)
                            }
                            page = it.data.pagination.next_page

                        }
                        Status.LOADING -> {
                            viewModel.isLoading.value = true
                            if (page == 1) {
                                viewDataBinding.rvResult2.invisible()
                                viewDataBinding.progressBar.show()
                            }
                        }
                        Status.ERROR -> {
                            isRequesting = false
                            viewModel.isLoading.value = false
                            viewDataBinding.progressBar.hide()
                        }
                    }
                }
            })
        } else if (mType == Constants.TYPE_CATEGORY.FAVORITE.type) {
            val rawList = FavoriteManager.getInstance().searchMovieByName(keyword)
            val list = rawList.sortedWith(favoriteComparator(keyword))
            mFavoriteAdapter?.submitList(list)
            val listMain = FavoriteManager.getInstance().getDataByType(Constants.TYPE_FILE.MOVIE.toString())
            if (keyword.isEmpty()) {
                mFavoriteAdapter?.submitList(listMain)
            }

            if ((list.isEmpty() && keyword.isNotEmpty()) || listMain.isEmpty()) {
                viewDataBinding.tvResult.show()
                viewDataBinding.rvResult2.hide()
                if (listMain.isNotEmpty())
                    viewDataBinding.tvResult.text = getString(R.string.text_search_not_found)
            } else {
                viewDataBinding.rvResult2.show()

                viewDataBinding.tvResult.hide()
            }

        } else {
            val rawList = WatchHistoryManager.getInstance().searchMovieByName(keyword)
            val list = rawList.sortedWith(historyComparator(keyword))
            val listMain = WatchHistoryManager.getInstance().getData()
            mWatchedAdapter?.submitList(list)
            if (keyword.isEmpty()) {
                mWatchedAdapter?.submitList(listMain)
            }
            if ((list.isEmpty() && keyword.isNotEmpty()) || listMain.isEmpty()) {
                viewDataBinding.tvResult.show()
                viewDataBinding.rvResult2.hide()
                if (listMain.isNotEmpty())
                    viewDataBinding.tvResult.text = getString(R.string.text_search_not_found)
            } else {
                viewDataBinding.rvResult2.show()

                viewDataBinding.tvResult.hide()
            }
        }
    }

    fun changeEnglish() {
        mIsKorea = false
        if (BuildConfig.FLAVOR.equals("beetv")) {
            viewDataBinding.btA.setCharacter(requireContext().getString(R.string.a_en))
            viewDataBinding.btB.setCharacter(requireContext().getString(R.string.b_en))
            viewDataBinding.btC.setCharacter(requireContext().getString(R.string.c_en))
            viewDataBinding.btD.setCharacter(requireContext().getString(R.string.d_en))
            viewDataBinding.btE.setCharacter(requireContext().getString(R.string.e_en))
            viewDataBinding.btF.setCharacter(requireContext().getString(R.string.f_en))
            viewDataBinding.btG.setCharacter(requireContext().getString(R.string.g_en))
            viewDataBinding.btH.setCharacter(requireContext().getString(R.string.h_en))
            viewDataBinding.btI.setCharacter(requireContext().getString(R.string.i_en))
            viewDataBinding.btJ.setCharacter(requireContext().getString(R.string.j_en))
            viewDataBinding.btK.setCharacter(requireContext().getString(R.string.k_en))
            viewDataBinding.btL.setCharacter(requireContext().getString(R.string.l_en))
            viewDataBinding.btM.setCharacter(requireContext().getString(R.string.m_en))
            viewDataBinding.btN.setCharacter(requireContext().getString(R.string.n_en))
            viewDataBinding.btO.setCharacter(requireContext().getString(R.string.o_en))
            viewDataBinding.btP.setCharacter(requireContext().getString(R.string.p_en))
            viewDataBinding.btQ.setCharacter(requireContext().getString(R.string.q_en))
            viewDataBinding.btR.setCharacter(requireContext().getString(R.string.r_en))
            viewDataBinding.btS.setCharacter(requireContext().getString(R.string.s_en))
            viewDataBinding.btT.setCharacter(requireContext().getString(R.string.t_en))
            viewDataBinding.btU.setCharacter(requireContext().getString(R.string.u_en))
            viewDataBinding.btV.setCharacter(requireContext().getString(R.string.v_en))
            viewDataBinding.btW.setCharacter(requireContext().getString(R.string.w_en))
            viewDataBinding.btX.setCharacter(requireContext().getString(R.string.x_en))
            viewDataBinding.btY.setCharacter(requireContext().getString(R.string.y_en))
            viewDataBinding.btZ.setCharacter(requireContext().getString(R.string.z_en))
            viewDataBinding.btOne.setCharacter(requireContext().getString(R.string.one))
            viewDataBinding.btTwo.setCharacter(requireContext().getString(R.string.two))
            viewDataBinding.btThree.setCharacter(requireContext().getString(R.string.three))
            viewDataBinding.btFour.show()
            viewDataBinding.btFive.show()
            viewDataBinding.btSix.show()
            viewDataBinding.btSeven.show()
            viewDataBinding.btEight.show()
            viewDataBinding.btNine.show()
            viewDataBinding.btZero.show()
            viewDataBinding.btButton3.hide()
            viewDataBinding.btButton4.hide()
            viewDataBinding.btButton5.hide()
            viewDataBinding.btButton6.hide()
            viewDataBinding.btButton7.hide()
            viewDataBinding.btButton8.hide()
            viewDataBinding.btButton9.hide()
            viewDataBinding.btButton10.hide()
            viewDataBinding.btButton11.hide()
            viewDataBinding.btButton12.hide()
            viewDataBinding.btButton13.hide()
            val layoutParams = viewDataBinding.btSpace.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.BELOW, R.id.bt_five)
        }
    }

    fun changeKorea() {
        mIsKorea = true
        if (BuildConfig.FLAVOR.equals("beetv")) {
            viewDataBinding.btA.setCharacter(requireContext().getString(R.string.a_ko))
            viewDataBinding.btB.setCharacter(requireContext().getString(R.string.b_ko))
            viewDataBinding.btC.setCharacter(requireContext().getString(R.string.c_ko))
            viewDataBinding.btD.setCharacter(requireContext().getString(R.string.d_ko))
            viewDataBinding.btE.setCharacter(requireContext().getString(R.string.e_ko))
            viewDataBinding.btF.setCharacter(requireContext().getString(R.string.f_ko))
            viewDataBinding.btG.setCharacter(requireContext().getString(R.string.g_ko))
            viewDataBinding.btH.setCharacter(requireContext().getString(R.string.h_ko))
            viewDataBinding.btI.setCharacter(requireContext().getString(R.string.i_ko))
            viewDataBinding.btJ.setCharacter(requireContext().getString(R.string.j_ko))
            viewDataBinding.btK.setCharacter(requireContext().getString(R.string.k_ko))
            viewDataBinding.btL.setCharacter(requireContext().getString(R.string.l_ko))
            viewDataBinding.btM.setCharacter(requireContext().getString(R.string.m_ko))
            viewDataBinding.btN.setCharacter(requireContext().getString(R.string.n_ko))
            viewDataBinding.btO.setCharacter(requireContext().getString(R.string.o_ko))
            viewDataBinding.btP.setCharacter(requireContext().getString(R.string.p_ko))
            viewDataBinding.btQ.setCharacter(requireContext().getString(R.string.q_ko))
            viewDataBinding.btR.setCharacter(requireContext().getString(R.string.r_ko))
            viewDataBinding.btS.setCharacter(requireContext().getString(R.string.s_ko))
            viewDataBinding.btT.setCharacter(requireContext().getString(R.string.t_ko))
            viewDataBinding.btU.setCharacter(requireContext().getString(R.string.u_ko))
            viewDataBinding.btV.setCharacter(requireContext().getString(R.string.v_ko))
            viewDataBinding.btW.setCharacter(requireContext().getString(R.string.w_ko))
            viewDataBinding.btX.setCharacter(requireContext().getString(R.string.x_ko))
            viewDataBinding.btY.setCharacter(requireContext().getString(R.string.y_ko))
            viewDataBinding.btZ.setCharacter(requireContext().getString(R.string.z_ko))
            viewDataBinding.btOne.setCharacter(requireContext().getString(R.string.one_ko))
            viewDataBinding.btTwo.setCharacter(requireContext().getString(R.string.two_ko))
            viewDataBinding.btThree.setCharacter(requireContext().getString(R.string.three_ko))
            viewDataBinding.btFour.hide()
            viewDataBinding.btFive.hide()
            viewDataBinding.btSix.hide()
            viewDataBinding.btSeven.hide()
            viewDataBinding.btEight.hide()
            viewDataBinding.btNine.hide()
            viewDataBinding.btZero.hide()
            viewDataBinding.btButton3.hide()
            viewDataBinding.btButton4.hide()
            viewDataBinding.btButton5.hide()
            viewDataBinding.btButton6.hide()
            viewDataBinding.btButton7.hide()
            viewDataBinding.btButton8.hide()
            viewDataBinding.btButton9.hide()
            viewDataBinding.btButton10.hide()
            viewDataBinding.btButton11.hide()
            viewDataBinding.btButton12.hide()
            viewDataBinding.btButton13.hide()

            val layoutParams = viewDataBinding.btSpace.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.BELOW, R.id.bt_button9)
        }
    }

    public class ClickProxy(val viewModel: SearchViewModel, val viewBinding: FragmentSearchBinding, val context: Context, val fragment: SearchFragment) {


        fun emptySearch() {
            viewBinding.etSearch.text.clear()
            fragment.mPositionSpace = -1
            fragment.mPositionSpaceCurrent = -1
            fragment.mKoreaCharacterList.clear()
            fragment.page = 1
            fragment.search()
        }

        fun deleteSearch() {

//            if (fragment.mKoreaCharacterList.isNotEmpty()) {
//                fragment.mKoreaCharacterList.removeAt(fragment.mKoreaCharacterList.size - 1)
//
//                fragment.mPositionSpace = fragment.mKoreaCharacterList.lastIndexOf(" ")
//                var countSpace = 0
//
//                fragment.mKoreaCharacterList.forEach {
//                    if (it == " ") {
//                        countSpace++
//                    }
//                }
//
//                if (viewBinding.etSearch.text.toString().last().equals(' ')) {
//                    viewBinding.etSearch.setText(viewBinding.etSearch.text.toString().substring(0, viewBinding.etSearch.text.toString().length - 1))
//                    fragment.mPositionSpaceCurrent = viewBinding.etSearch.text.toString().lastIndexOf(" ")
//                } else {
//                    fragment.mPositionSpaceCurrent = viewBinding.etSearch.text.toString().lastIndexOf(" ")
//                    val koreaCopyList = arrayListOf<String>()
//                    val koreaCopyTotalList = arrayListOf<String>()
//                    if (fragment.mKoreaCharacterList.isEmpty()) {
//                        viewBinding.etSearch.setText("")
//                    } else if (fragment.mKoreaCharacterList.size == 1) {
//                        viewBinding.etSearch.setText(fragment.mKoreaCharacterList.first())
//                    } else {
//                        if (fragment.mPositionSpace > -1) {
//                            koreaCopyList.addAll(fragment.mKoreaCharacterList.subList(fragment.mPositionSpace + 1, fragment.mKoreaCharacterList.size))
//                            koreaCopyTotalList.addAll(fragment.mKoreaCharacterList.subList(fragment.mPositionSpace + 1, fragment.mKoreaCharacterList.size))
//
//                        } else {
//                            koreaCopyList.addAll(fragment.mKoreaCharacterList)
//                            koreaCopyTotalList.addAll(fragment.mKoreaCharacterList)
//                        }
//
//                        fragment.getKeySearchKorea(koreaCopyTotalList, koreaCopyList)
//                    }
//                }
//
//
//            }
            val length: Int = viewBinding.etSearch.text.length
            if (length > 0) {
                viewBinding.etSearch.text.delete(length - 1, length);
            }
            search()
        }

        fun changeEnglish() {
            fragment.changeEnglish()
        }

        fun changeKorea() {
            fragment.changeKorea()
        }

        fun search() {
            fragment.page = 1
            fragment.search()
        }

        fun space() {
            if (viewBinding.etSearch.text.toString().isNotEmpty()) {

                fragment.mKoreaCharacterList.add(" ")
                fragment.mPositionSpace = fragment.mKoreaCharacterList.size - 1
                viewBinding.etSearch.setText(viewBinding.etSearch.text.toString() + " ")
                fragment.mPositionSpaceCurrent = viewBinding.etSearch.text.toString().length - 1
            }
        }

        fun goBack() {
            fragment.activity?.supportFragmentManager?.popBackStack()
        }
    }


    private fun getKeySearchKorea(totalList: ArrayList<String>, characterList: ArrayList<String>) {
        try {
            val hangul: String = HangulParser.assemble(characterList)
            if (mPositionSpaceCurrent > -1) {
                viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().substring(0, mPositionSpaceCurrent + 1))
                viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(hangul))
            } else {
                viewDataBinding.etSearch.setText(hangul)

            }
            if (characterList.size < totalList.size) {
                for (i in characterList.size until totalList.size) {
                    viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(totalList[i]))
                }


            }

        } catch (e: HangulParserException) {
            e.printStackTrace()
            if (characterList.size > 2) {
                characterList.removeAt(characterList.size - 1)
                getKeySearchKorea(totalList, characterList)
            } else {
                if (mPositionSpaceCurrent > -1) {
                    viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().substring(0, mPositionSpaceCurrent + 1))
                    totalList.forEach {
                        viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(it))
                    }
                } else {
                    viewDataBinding.etSearch.text.clear()
                    mKoreaCharacterList.forEach {
                        viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(it))
                    }
                }

            }

        }
    }

    override fun onClick(v: View?) {

        if (v is KeyboardItemView) {
//            if (mIsKorea) {
//            mKoreaCharacterList.add(v.getCharacter())
//            val koreaCopyList = arrayListOf<String>()
//            val koreaCopyTotalList = arrayListOf<String>()
//            if (mPositionSpace > -1) {
//                koreaCopyList.addAll(mKoreaCharacterList.subList(mPositionSpace + 1, mKoreaCharacterList.size))
//                koreaCopyTotalList.addAll(mKoreaCharacterList.subList(mPositionSpace + 1, mKoreaCharacterList.size))
//            } else {
//                koreaCopyList.addAll(mKoreaCharacterList)
//                koreaCopyTotalList.addAll(mKoreaCharacterList)
//            }
//
//            if (koreaCopyTotalList.size == 1) {
//                viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(v.getCharacter()))
//            } else
//                getKeySearchKorea(koreaCopyTotalList, koreaCopyList)
            viewDataBinding.etSearch.setText(viewDataBinding.etSearch.text.toString().plus(v.getCharacter()))
            page = 1
            search()
        }
    }

    override fun onBackPress() {}

    override fun onDetach() {

        super.onDetach()
    }


    private fun movieComparator(kw: String): Comparator<BMovie> {
        return Comparator<BMovie> { a, b ->
            when {
                (a == null && b == null) -> 0
                (b.name.toLowerCase().startsWith(kw) && (a.name.toLowerCase().startsWith(kw))) -> b.name.compareTo(a.name)
                (b.name.toLowerCase().startsWith(kw)) -> 1
                (a.name.toLowerCase().startsWith(kw)) -> -1
                else -> b.name.compareTo(a.name)
            }
        }
    }

    private fun favoriteComparator(kw: String): Comparator<BFavorite> {
        return Comparator<BFavorite> { a, b ->
            when {
                (a == null && b == null) -> 0
                (b.movie?.name?.toLowerCase()?.startsWith(kw) == true && (a.movie?.name?.toLowerCase()?.startsWith(kw)) == true) -> (b.movie?.name
                        ?: "").compareTo(a.movie?.name ?: "")
                (b.movie?.name?.toLowerCase()?.startsWith(kw) == true) -> 1
                (a.movie?.name?.toLowerCase()?.startsWith(kw) == true) -> -1
                else -> (b.movie?.name ?: "").compareTo(a.movie?.name ?: "")
            }
        }
    }

    private fun historyComparator(kw: String): Comparator<BWatchHistory> {
        return Comparator<BWatchHistory> { a, b ->
            when {
                (a == null && b == null) -> 0
                (b.movie?.name?.toLowerCase()?.startsWith(kw) && (a.movie?.name?.toLowerCase()?.startsWith(kw))) -> (b.movie?.name
                        ?: "").compareTo(a.movie?.name ?: "")
                (b.movie?.name?.toLowerCase()?.startsWith(kw)) -> 1
                (a.movie?.name?.toLowerCase()?.startsWith(kw)) -> -1
                else -> (b.movie?.name ?: "").compareTo(a.movie?.name ?: "")
            }
        }
    }
}