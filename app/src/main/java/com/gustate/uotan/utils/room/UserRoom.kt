package com.gustate.uotan.utils.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user")
data class User(

    @PrimaryKey
    val id: Int = 0,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "signature")
    val signature: String,

    @ColumnInfo(name = "auth")
    val auth: String? = null,

    @ColumnInfo(name = "post_count")
    val postCount: String? = null,

    @ColumnInfo(name = "res_count")
    val resCount: String? = null,

    @ColumnInfo(name = "user_id")
    val userId: String? = null,

    @ColumnInfo(name = "points")
    val points: String? = null,

    @ColumnInfo(name = "u_coin")
    val uCoin: String? = null,

    @ColumnInfo(name = "ip_address")
    val ipAddress: String? = null
)

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUser(id: Int = 0): User?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}