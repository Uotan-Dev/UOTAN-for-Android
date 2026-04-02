package com.uotan.forum.user.data.repository

import com.uotan.forum.user.data.api.MineApiService
import com.uotan.forum.user.data.model.MeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MineRepository {
    private val mineApiService = MineApiService()
    suspend fun getMeUserInfo(
        onSuccess: (MeModel) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        mineApiService.getMeUserInfo(
            { onSuccess(it) },
            { onException(it) }
        )
    }
}