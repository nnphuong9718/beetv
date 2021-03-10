package com.example.android.architecture.blueprints.beetv.modules.ads

import androidx.lifecycle.*
import com.example.android.architecture.blueprints.beetv.Event
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.Resource
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.manager.ADSManager
import com.example.android.architecture.blueprints.beetv.manager.CategoryManager
import kotlinx.coroutines.Dispatchers

class AdsViewModel(
) : ViewModel() {

    private val _adsList = MutableLiveData<MutableList<BAds>>(ADSManager.getInstance().getData())

     val adsList: LiveData<MutableList<BAds>> = _adsList

}