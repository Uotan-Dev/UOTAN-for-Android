package com.gustate.uotan

import android.content.Context
import com.gustate.uotan.utils.room.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return UserDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: UserDatabase) = database.userDao()
}