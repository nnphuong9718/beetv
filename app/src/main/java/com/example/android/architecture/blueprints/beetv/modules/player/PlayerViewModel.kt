package com.example.android.architecture.blueprints.beetv.modules.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.data.source.TasksRepository
import com.example.android.architecture.blueprints.beetv.util.Constants
import kotlinx.coroutines.Dispatchers

class PlayerViewModel(
        private val accountRepository: AccountRepository,
        private val mediaRepository: MediaRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
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

    fun getLiveById(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = mediaRepository.getLiveById(id)
            var live = response.results.one
            emit(Resource.success(data = live))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun saveWatched(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.saveWatched(id)
            emit(Resource.success(data = response))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun watchMovie(id: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            var response = accountRepository.watchMovie(id)
            emit(Resource.success(data = response))
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

    fun getTimetable(live: String, date: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getLiveTimeTable(live, date)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getAllLive()= liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getAllLive()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
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

}