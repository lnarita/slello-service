package org.slello.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.ZonedDateTime

enum class MessageType {
    CHAT_MESSAGE, USER_JOINED, USER_LEFT
}

data class Message(@Id val id: ObjectId, val destination: String, val user: String, val type: MessageType, val message: String?, val timestamp: Long = ZonedDateTime.now().toInstant().toEpochMilli())

data class Channel(@Id val name: String, @DBRef val members: List<User>)