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
package com.example.android.architecture.blueprints.beetv.util

/**
 * Extension functions for Fragment.
 */

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    return buildViewModelFactory(app = requireContext().applicationContext as BeeTVApplication, owner = this)
}


fun ComponentActivity.getViewModelFactory(): ViewModelFactory {
    return buildViewModelFactory(app = applicationContext as BeeTVApplication, owner = this)
}


fun buildViewModelFactory(app: BeeTVApplication, owner: SavedStateRegistryOwner): ViewModelFactory {
    val repository = app.taskRepository
    val movieRepository =  app.movieRepository
    val accountRepository = app.accountRepository
    return ViewModelFactory(repository, movieRepository, accountRepository,owner)
}