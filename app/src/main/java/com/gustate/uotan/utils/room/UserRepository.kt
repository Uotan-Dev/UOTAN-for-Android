package com.gustate.uotan.utils.room

class UserRepository(private val userDao: UserDao) {

    suspend fun getUser(): User? {
        return userDao.getUser()
    }

    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }

    suspend fun delete(user: User) {
        userDao.delete(user)
    }

}