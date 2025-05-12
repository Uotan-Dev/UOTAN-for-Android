package com.gustate.uotan.message.data.repository

import com.gustate.uotan.message.data.model.AllMessage
import com.gustate.uotan.message.data.parse.MessageParse

class MessageRepository {
    private val messageParse = MessageParse()
    suspend fun getAllMessage(): MutableList<AllMessage> {
        return messageParse.parseAllMessage()
    }
}