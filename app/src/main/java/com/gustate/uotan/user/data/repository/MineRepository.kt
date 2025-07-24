package com.gustate.uotan.user.data.repository

import com.gustate.uotan.user.data.api.MineApiService
import com.gustate.uotan.user.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MineRepository {
    private val mineApiService = MineApiService()
    suspend fun getMeUserInfo(
        onSuccess: (User) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        mineApiService.getMeUserInfo(
            { onSuccess(it) },
            { onException(it) }
        )
    }
}