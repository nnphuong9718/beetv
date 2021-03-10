package com.example.android.architecture.blueprints.beetv.modules.menu

import android.os.Handler
import android.view.View
import android.widget.ScrollView
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.CategoryManager
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.formatID
import com.example.android.architecture.blueprints.beetv.util.toExpiredDate
import com.example.android.architecture.blueprints.beetv.util.toNumberString
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class MenuViewModel(
        private val mediaRepository: MediaRepository,
        private val accountRepository: AccountRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _typeCategory = MutableLiveData<String>()

    val categories: LiveData<MutableList<BCategory>> = _typeCategory.map {
        CategoryManager.getInstance().getData(it)
    }

    private val _numberDays: MutableLiveData<Long> = MutableLiveData<Long>()

    var mCalendarRecyclerView: RecyclerView? = null
    var mMenuRecyclerView: RecyclerView? = null
    var mLiveRecyclerView: ScrollView? = null
    var mTimeLiveRecyclerView: RecyclerView? = null
    var isPlayRecord: Boolean = false
    var timeRecordSelect: Long = 0
    var timeStartRecordSelect: Long = 0
    var isLiveLastSelected = false

    val snackbarInt: MutableLiveData<Event<Int>> = MutableLiveData<Event<Int>>()
    private val _id = MutableLiveData<String>()
    val id: LiveData<String> = _id
    val dateExpired: LiveData<String> = _numberDays.map {
        it.toExpiredDate()
    }
    val categoryId = MutableLiveData<String>()
    val total: LiveData<Int> = categories.map {
        it.size
    }

    var isSortUpdateSelected = false
    var isSortPopularSelected = false
    val totalList = MutableLiveData<String>(0.toNumberString())
//            : LiveData<String> =  categories.map {
//        it.size.toNumberString()
//    }

    val positionCurrent = MutableLiveData<String>(0.toNumberString())
    val movieList = MutableLiveData<List<BMovie>>(mutableListOf())

    val isLoading = MutableLiveData<Boolean>()
    val time = MutableLiveData<String>()
    val date = MutableLiveData<String>()
    private val _openMovieDetailEvent = MutableLiveData<Event<String>>()
    val openMovieDetailEvent: LiveData<Event<String>> = _openMovieDetailEvent

    private val _hideMenuEvent = MutableLiveData<Event<Boolean>>()
    val hideMenuEvent: LiveData<Event<Boolean>> = _hideMenuEvent
    private val _openCategoryDetailEvent = MutableLiveData<Event<BLive>>()
    val openCategoryDetailEvent: LiveData<Event<BLive>> = _openCategoryDetailEvent
    private val _loadLiveEvent = MutableLiveData<Event<BCategory>>()
    val loadLiveEvent: LiveData<Event<BCategory>> = _loadLiveEvent
    private val _loadMovieByCategoryEvent = MutableLiveData<Event<Pair<Int, BCategory>>>()
    val loadMovieByCategoryEvent: LiveData<Event<Pair<Int, BCategory>>> = _loadMovieByCategoryEvent

    private val _loadTimeLiveEvent = MutableLiveData<Event<Triple<String, BLive, Pair<Int, View>>>>()
    val loadTimeLiveEvent: LiveData<Event<Triple<String, BLive, Pair<Int, View>>>> = _loadTimeLiveEvent


    private val _loadTimeLiveWithDateEvent = MutableLiveData<Event<Triple<String, BLive, Long>>>()
    val loadTimeLiveWithDateEvent: LiveData<Event<Triple<String, BLive, Long>>> = _loadTimeLiveWithDateEvent

    private val _refreshLiveTimeEvent = MutableLiveData<Event<View>>()
    val refreshLiveTimeEvent: LiveData<Event<View>> = _refreshLiveTimeEvent

    private val _openLiveTimeEvent = MutableLiveData<Event<Pair<BLive, BLiveTime>>>()
    val openLiveTimeEvent: LiveData<Event<Pair<BLive, BLiveTime>>> = _openLiveTimeEvent


    private val _openCalendarEvent = MutableLiveData<Event<Triple<Long, BLive, BLiveTime>>>()
    val openCalendarEvent: LiveData<Event<Triple<Long, BLive, BLiveTime>>> = _openCalendarEvent

    fun openMovieDetail(movieID: String) {
        _openMovieDetailEvent.value = Event(movieID)
    }

    fun hideMenu(isHide : Boolean){
        _hideMenuEvent.value = Event(isHide)
    }

    fun openCategory(bLive: BLive) {
        _openCategoryDetailEvent.value = Event(bLive)
    }

    fun loadLive(bCategory: BCategory) {
        _loadLiveEvent.value = Event(bCategory)
    }

    fun loadMovieByCategory(position: Int, bCategory: BCategory) {
        _loadMovieByCategoryEvent.value = Event(position to bCategory)
    }


    fun loadTimeLive(bCategoryID: String, bLive: BLive, viewSelected: View, position: Int) {
        _loadTimeLiveEvent.value = Event(Triple(bCategoryID, bLive, position to viewSelected))
    }

    fun refreshLiveTime(viewSelected: View) {
        _refreshLiveTimeEvent.value = Event(viewSelected)
    }

    fun loadTimeLiveWithDate(bCategoryID: String, bLive: BLive, date: Long) {
        _loadTimeLiveWithDateEvent.value = Event(Triple(bCategoryID, bLive, date))
    }

    fun openLiveTime(live: BLive, liveTime: BLiveTime) {
        _openLiveTimeEvent.value = Event(live to liveTime)
    }


    fun openLiveTimeWithRecord(live: BLive, liveTime: BLiveTime, date: Long) {
        _openCalendarEvent.value = Event(Triple(date, live, liveTime))
    }

    fun start(type: String?) {
        if (type == _typeCategory.value) {
            return
        }
        // Trigger the load
        _typeCategory.value = type!!
    }


    fun getMovieListByCategoryId(categoryId: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getMoviesByCategoryId(categoryId, page, Constants.LIMIT)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getMoviesByCategoryIdAndSortUpdate(categoryId: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getMoviesByCategoryIdAndSortUpdate(categoryId, page, Constants.LIMIT)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getMoviesBySubCategoryIdAndSortUpdate(categoryId: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getMoviesBySubCategoryIdAndSortUpdate(categoryId, page, Constants.LIMIT)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getMoviesByCategoryIdAndSortView(categoryId: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getMoviesByCategoryIdAndSortView(categoryId, page, Constants.LIMIT)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun setId() {
        val sdf3 = SimpleDateFormat("yyyy.MM.dd")
        sdf3.timeZone = TimeZoneManager.getInstance().getData()

        _id.value = if (AccountManager.getInstance().getAccount() != null)
            AccountManager.getInstance().getAccount()?.logical_id?.formatID()
        else SimpleDateFormat("EEEE").format(Date()) + "\n" + SimpleDateFormat("yyyy.MM.dd").format(Date())
    }

    fun setNumberDays(
    ) {
        _numberDays.value = 0
    }

    fun getLivesByCategoryId(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getLivesByCategoryId(id, 1, 999)
            var liveList = response.results?.objects?.rows?.map { it -> it.live }
            emit(Resource.success(data = liveList))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getAllMyFavorites(page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getAllMyFavorites(AccountManager.getInstance().getAccount()!!.id, page, Constants.LIMIT)
//            var movieList = response.results?.objects?.rows?.map { it -> it.movie }
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getAllMyLiveFavorites() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getAllMyLiveFavorites(AccountManager.getInstance().getAccount()!!.id)
//            var movieList = response.results?.objects?.rows?.map { it -> it.movie }
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getFavoritesWithPaging(categoryIdList: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getFavoritesWithPaging(categoryIdList, page, Constants.LIMIT)
//            var movieList = response.results?.objects?.rows?.map { it -> it.movie }
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getWatchHistoryWithPaging(categoryIdList: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getWatchHistoryWithPaging(categoryIdList, page, Constants.LIMIT)
//            var movieList = response.results?.objects?.rows?.map { it -> it.movie }
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getAllMyWatchHistory(page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getAllMyWatchHistory(AccountManager.getInstance().getAccount()!!.id, page, Constants.LIMIT)
//            var movieList = response.results?.objects?.rows?.map { it -> it.movie }
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getTimetable(live: String, date: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getLiveTimeTable(live, date)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun likeLive(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.likeLive(id)
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun unlikeLive(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.unlikeLive(id)
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}