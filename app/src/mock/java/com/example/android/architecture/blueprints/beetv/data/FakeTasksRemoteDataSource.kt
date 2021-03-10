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
package com.example.android.architecture.blueprints.beetv.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.android.architecture.blueprints.beetv.data.models.Task
import com.example.android.architecture.blueprints.beetv.data.source.TasksDataSource
import java.util.LinkedHashMap

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
object FakeTasksRemoteDataSource : TasksDataSource {

    private var TASKS_SERVICE_DATA: LinkedHashMap<String, Task> = LinkedHashMap()

    private val observableTasks = MutableLiveData<com.example.android.architecture.blueprints.beetv.data.models.Result<List<Task>>>()

    override suspend fun refreshTasks() {
        observableTasks.postValue(getTasks())
    }

    override suspend fun refreshTask(taskId: String) {
        refreshTasks()
    }

    override fun observeTasks(): LiveData<com.example.android.architecture.blueprints.beetv.data.models.Result<List<Task>>> {
        return observableTasks
    }

    override fun observeTask(taskId: String): LiveData<com.example.android.architecture.blueprints.beetv.data.models.Result<Task>> {
        return observableTasks.map { tasks ->
            when (tasks) {
                is com.example.android.architecture.blueprints.beetv.data.models.Result.Loading -> com.example.android.architecture.blueprints.beetv.data.models.Result.Loading
                is com.example.android.architecture.blueprints.beetv.data.models.Result.Error -> com.example.android.architecture.blueprints.beetv.data.models.Result.Error(tasks.exception)
                is com.example.android.architecture.blueprints.beetv.data.models.Result.Success -> {
                    val task = tasks.data.firstOrNull() { it.id == taskId }
                        ?: return@map com.example.android.architecture.blueprints.beetv.data.models.Result.Error(Exception("Not found"))
                    com.example.android.architecture.blueprints.beetv.data.models.Result.Success(task)
                }
            }
        }
    }

    override suspend fun getTask(taskId: String): com.example.android.architecture.blueprints.beetv.data.models.Result<Task> {
        TASKS_SERVICE_DATA[taskId]?.let {
            return com.example.android.architecture.blueprints.beetv.data.models.Result.Success(it)
        }
        return com.example.android.architecture.blueprints.beetv.data.models.Result.Error(Exception("Could not find task"))
    }

    override suspend fun getTasks(): com.example.android.architecture.blueprints.beetv.data.models.Result<List<Task>> {
        return com.example.android.architecture.blueprints.beetv.data.models.Result.Success(TASKS_SERVICE_DATA.values.toList())
    }

    override suspend fun saveTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override suspend fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, true, task.id)
        TASKS_SERVICE_DATA[task.id] = completedTask
    }

    override suspend fun completeTask(taskId: String) {
        // Not required for the remote data source.
    }

    override suspend fun activateTask(task: Task) {
        val activeTask = Task(task.title, task.description, false, task.id)
        TASKS_SERVICE_DATA[task.id] = activeTask
    }

    override suspend fun activateTask(taskId: String) {
        // Not required for the remote data source.
    }

    override suspend fun clearCompletedTasks() {
        TASKS_SERVICE_DATA = TASKS_SERVICE_DATA.filterValues {
            !it.isCompleted
        } as LinkedHashMap<String, Task>
    }

    override suspend fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
        refreshTasks()
    }

    override suspend fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
        refreshTasks()
    }
}
