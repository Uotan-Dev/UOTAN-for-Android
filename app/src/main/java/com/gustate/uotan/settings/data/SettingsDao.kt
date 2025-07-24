package com.gustate.uotan.settings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingModel?>
    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSettingByKey(key: String): SettingModel?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(settingModel: SettingModel)
    @Query("UPDATE settings SET value = :value WHERE `key` = :key")
    suspend fun updateSettingValue(key: String, value: String)
}