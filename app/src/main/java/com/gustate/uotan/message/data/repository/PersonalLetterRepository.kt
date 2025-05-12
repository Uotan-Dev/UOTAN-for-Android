package com.gustate.uotan.message.data.repository

import com.gustate.uotan.message.data.model.PersonalLetter
import com.gustate.uotan.message.data.parse.PersonalLetterParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonalLetterRepository {
    private val personalLetterParse = PersonalLetterParse()
    suspend fun getPersonalLetterList(userId: String, page: Int):
            MutableList<PersonalLetter> = withContext(Dispatchers.IO) {
        return@withContext personalLetterParse.parsePersonalLetterList(userId, page)
    }
}