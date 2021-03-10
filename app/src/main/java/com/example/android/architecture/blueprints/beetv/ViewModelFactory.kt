/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.beetv

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.data.source.TasksRepository
import com.example.android.architecture.blueprints.beetv.modules.ads.AdsViewModel
import com.example.android.architecture.blueprints.beetv.modules.favorite.FavoriteViewModel
import com.example.android.architecture.blueprints.beetv.modules.home.HomeViewModel
import com.example.android.architecture.blueprints.beetv.modules.login.LoginViewModel
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailViewModel
import com.example.android.architecture.blueprints.beetv.modules.register.RegisterViewModel
import com.example.android.architecture.blueprints.beetv.modules.search.SearchViewModel
import com.example.android.architecture.blueprints.beetv.modules.splash.SplashViewModel
import com.example.android.architecture.blueprints.beetv.modules.player.PlayerViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
        private val tasksRepository: TasksRepository,
        private val movieRepository: MediaRepository,
        private val accountRepository: AccountRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(movieRepository, accountRepository, handle)
            isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(movieRepository,accountRepository, handle)
            isAssignableFrom(MovieDetailViewModel::class.java) -> MovieDetailViewModel(accountRepository,movieRepository, handle)
            isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(accountRepository,movieRepository)
            isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(movieRepository, accountRepository)
            isAssignableFrom(FavoriteViewModel::class.java) -> FavoriteViewModel()
            isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(accountRepository)
            isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel()
            isAssignableFrom(PlayerViewModel::class.java) -> PlayerViewModel(accountRepository,movieRepository, handle)
            isAssignableFrom(AdsViewModel::class.java) -> AdsViewModel()

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
