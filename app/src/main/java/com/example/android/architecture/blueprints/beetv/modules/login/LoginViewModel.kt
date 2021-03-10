package com.example.android.architecture.blueprints.beetv.modules.login

import android.content.Context.WIFI_SERVICE
import android.location.Location
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.SingleLiveEvent
import com.example.android.architecture.blueprints.beetv.data.api.MyServiceInterceptor
import com.example.android.architecture.blueprints.beetv.data.models.BUpdateProfileRequest
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import com.example.android.architecture.blueprints.beetv.util.Utils.getIPAddress
import com.example.android.architecture.blueprints.beetv.util.Utils.getMacAddress2
import kotlinx.coroutines.Dispatchers
import java.util.*


class LoginViewModel (
        private val accountRepository: AccountRepository
): ViewModel() {

    val snackbarText: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()
    val snackbarInt: MutableLiveData<Event<Int>> = MutableLiveData<Event<Int>>()
    val id = MutableLiveData<String>()

    val password = MutableLiveData<String>()
    fun login(id: String, password : String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val response = accountRepository.login(id, password)

            // Todo please keep token

            if (response.code == "200" && response.results != null){
                val token = response.results.one.token
                val account = response.results.one.result
                MyServiceInterceptor.getInstance().setSessionToken(token)
                AccountManager.getInstance().saveToken(token)
                AccountManager.getInstance().setAccount(account)
                emit(Resource.success(data = account))
            }else{
                if (response.code == "1001")
                emit(Resource.error(data = null, message = BeeTVApplication.context.applicationContext.getString(R.string.error_password_incorrect)))
                if (response.code == "1002")
                    emit(Resource.error(data = null, message = BeeTVApplication.context.applicationContext.getString(R.string.error_id_incorrect)))
            }

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

    fun updateMacAddress(latitude:Double, longitude : Double, appVersion: String)= liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {

            emit(Resource.success(data = accountRepository.updateProfile(AccountManager.getInstance().getAccount()!!.id,
            BUpdateProfileRequest(getMacAddress2(),Calendar.getInstance().timeInMillis,latitude,longitude, getIPAddress(BeeTVApplication.context), appVersion))))
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

}