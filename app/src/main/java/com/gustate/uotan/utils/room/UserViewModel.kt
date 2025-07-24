package com.gustate.uotan.utils.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        return@withContext repository.getUser()
    }

    fun insert(user: User) = viewModelScope.launch {
        repository.insert(user)
    }

    fun update(user: User) = viewModelScope.launch {
        repository.update(user)
    }

    fun delete(user: User) = viewModelScope.launch {
        repository.delete(user)
    }
}