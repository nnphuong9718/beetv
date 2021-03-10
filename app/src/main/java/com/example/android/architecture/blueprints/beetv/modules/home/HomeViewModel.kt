package com.example.android.architecture.blueprints.beetv.modules.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.data.models.BAccount
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.manager.MovieManager
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import com.example.android.architecture.blueprints.beetv.util.FormatUtils
import com.example.android.architecture.blueprints.beetv.util.formatID
import com.example.android.architecture.blueprints.beetv.util.toExpiredDate
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeViewModel(
        private val mediaRepository: MediaRepository,
        private val accountRepository: AccountRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val time = MutableLiveData<String>()
    private val user = MutableLiveData<BAccount?>(
            AccountManager.getInstance().getAccount()
    )
    private val _id =MutableLiveData<String>()
    val id: LiveData<String> = _id
    val dateExpired = MutableLiveData<String>()

    private val _openMenuEvent = MutableLiveData<Event<String>>()
    val openMenuEvent: LiveData<Event<String>> = _openMenuEvent

    private val _openMovieDetailEvent = MutableLiveData<Event<String>>()
    val openMovieDetailEvent: LiveData<Event<String>> = _openMovieDetailEvent
    private val _topMovieList = MutableLiveData<MutableList<BMovie>>(MovieManager.getInstance().getData())
    val snackbarInt: MutableLiveData<Event<Int>> = MutableLiveData<Event<Int>>()
    val topMovieList: LiveData<MutableList<BMovie>> = _topMovieList


    fun openMenu(category: String) {
        _openMenuEvent.value = Event(category)
    }

    fun openMovieDetail(id: String) {
        _openMovieDetailEvent.value = Event(id)
    }


    fun getMovies() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getMovies()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getLiveList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getLives()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }




    fun login(id: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.login(id, password)

            // Todo please keep token
            var token = response.results?.one?.token
            var account = response.results?.one?.result
            emit(Resource.success(data = account))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getBillings() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            // Todo should get deviceId from profile management
            var deviceId =AccountManager.getInstance().getAccount()!!.id

            emit(Resource.success(data = accountRepository.getBillings(deviceId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getMovieDetail(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getMovieDetail(id)
            var movie = response.results.one
            emit(Resource.success(data = movie))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun setId(context: Context) {
        _id.value = if (AccountManager.getInstance().getAccount() != null)
            AccountManager.getInstance().getAccount()?.logical_id?.formatID() else
        {
            val sdf3 = SimpleDateFormat("yyyy.MM.dd")
            sdf3.timeZone = TimeZoneManager.getInstance().getData()
            FormatUtils.getDate(context) + "\n" + sdf3.format(Date())
        }

    }
    fun getTopMovie() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getTopMovies()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun getNoticeList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getNotices()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun getPersonalNotices() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getPersonalNotices(AccountManager.getInstance().getAccount()!!.id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun removeAllWatchHistory() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.removeAllWatchHistory()
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun removeAllFavorite() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.removeAllFavorite()
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getVersion() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getVersion()))

        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }

    }
}