package com.example.android.architecture.blueprints.beetv.modules.splash

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.SingleLiveEvent
import com.example.android.architecture.blueprints.beetv.data.api.MyServiceInterceptor
import com.example.android.architecture.blueprints.beetv.data.models.BAccount
import com.example.android.architecture.blueprints.beetv.data.models.BUpdateProfileRequest
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.util.Utils
import kotlinx.coroutines.Dispatchers
import java.util.*

class SplashViewModel(
        private val mediaRepository: MediaRepository,
        private val accountRepository: AccountRepository
) : ViewModel() {

    fun getCategories() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mediaRepository.getCategories()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
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

    fun getAdsList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getAds()))
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

    fun getFavoriteList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getFavorites()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getWatchHistories() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = accountRepository.getWatchHistories()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getAccountInfo() = liveData(Dispatchers.IO) {
        val token = AccountManager.getInstance().getToken()
        if (token.isNullOrEmpty()) emit(Resource.error(data = null, message = "Token invalid"))
        else {
            MyServiceInterceptor.getInstance().setSessionToken(token)
            emit(Resource.loading(data = null))
            try {
                emit(Resource.success(data = accountRepository.getProfile()))
            } catch (exception: Exception) {
                MyServiceInterceptor.getInstance().setSessionToken("")
                emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
            }
        }
    }

    fun updateMacAddress(latitude: Double, longitude: Double, appVersion: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {

            emit(Resource.success(data = accountRepository.updateProfile(AccountManager.getInstance().getAccount()!!.id,
                    BUpdateProfileRequest(Utils.getMacAddress2(), Calendar.getInstance().timeInMillis, latitude, longitude, Utils.getIPAddress(BeeTVApplication.context), appVersion))))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }



}