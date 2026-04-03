package com.uotan.forum.utils.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object BusinessStateHelper {
    fun isOperationAccepted(responseBody: String): Boolean {
        val tree = Json.parseToJsonElement(string = responseBody).jsonObject
        val status = tree["status"]?.jsonPrimitive?.content
        return when (status) {
            "ok" -> true
            "success" -> true
            "error" -> false
            // 。。。你看我像预言家吗？ 这玩意什么神人写的
            else -> false
        }
    }
}


