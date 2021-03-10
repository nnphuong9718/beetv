package com.example.android.architecture.blueprints.beetv.modules.movie_detail

import com.example.android.architecture.blueprints.beetv.R
import androidx.lifecycle.*
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.BVideo
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.data.source.TasksRepository
import com.example.android.architecture.blueprints.beetv.manager.CategoryManager
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import com.example.android.architecture.blueprints.beetv.util.toNumberString
import kotlinx.coroutines.Dispatchers

class MovieDetailViewModel(
        private val accountRepository: AccountRepository,
        private val mediaRepository: MediaRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _totalList: MutableLiveData<String> =  MutableLiveData<String>()
    val totalList: LiveData<String> = _totalList
    val mediaID : MutableLiveData<String> =  MutableLiveData<String>()
    val snackbarInt: MutableLiveData<Event<Int>> = MutableLiveData<Event<Int>>()

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: MutableLiveData<Boolean> = _isFavorite

    val positionChapterCurrent :MutableLiveData<String> = MutableLiveData<String>(0.toNumberString())

    private val _openVideoEvent = MutableLiveData<Event<String>>()
    val openVideoEvent: LiveData<Event<String>> = _openVideoEvent
    fun getMovieDetail(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getMovieDetail(id)
            var movie = response.results.one
            movie.score = String.format("%.1f", (movie.score?:"0").toFloat())
            emit(Resource.success(data = movie))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun likeMovie(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.likeMovie(id)
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun unlikeMovie(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.unlikeMovie(id)
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun openVideo(videoID: String) {
        _openVideoEvent.value = Event(videoID)
    }
    fun setTotalList(total : String){
        _totalList.value = total
    }

    fun setFavorite(isFavorite : Boolean){
        _isFavorite.value = isFavorite
    }
}