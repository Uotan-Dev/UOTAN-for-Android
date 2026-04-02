package com.uotan.forum.message.data.repository

import com.uotan.forum.message.data.model.AllMessage
import com.uotan.forum.message.data.parse.MessageParse

class MessageRepository {
    private val messageParse = MessageParse()
    suspend fun getAllMessage(): MutableList<AllMessage> {
        return messageParse.parseAllMessage()
    }
}