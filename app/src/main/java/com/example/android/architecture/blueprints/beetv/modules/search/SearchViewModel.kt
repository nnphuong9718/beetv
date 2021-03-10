package com.example.android.architecture.blueprints.beetv.modules.search

import androidx.lifecycle.*
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.manager.FavoriteManager
import kotlinx.coroutines.Dispatchers

class SearchViewModel(
        private val accountRepository: AccountRepository,
        private val mediaRepository: MediaRepository
) : ViewModel() {

    val movieList: MutableLiveData<List<BMovie>> = MutableLiveData<List<BMovie>>()

    val snackbarInt: MutableLiveData<Event<Int>> = MutableLiveData<Event<Int>>()

    val isLoading = MutableLiveData<Boolean>()
    private val _openMovieDetailEvent = MutableLiveData<Event<String>>()

    val openMovieDetailEvent: LiveData<Event<String>> = _openMovieDetailEvent
    fun openMovieDetail(movieID: String) {
        _openMovieDetailEvent.value = Event(movieID)
    }

    fun search(keyword: String, page: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.searchMovie(keyword, page)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


}