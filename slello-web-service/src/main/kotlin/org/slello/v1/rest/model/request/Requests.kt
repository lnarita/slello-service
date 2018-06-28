package org.slello.v1.rest.model.request

import org.slello.model.Authority
import org.slello.model.MessageType
import org.slello.model.Visibility
import java.time.LocalDate
import java.time.ZonedDateTime

data class CreateUserRequest(val username: String, val email: String, val password: String, val role: Authority)
data class RegisterRequest(val username: String, val email: String, val password: String)

data class CreateCommunityRequest(val name: String, val path: String, val thumbnail: String, val description: String, val visibility: Visibility)

data class CreateTopicRequest(val communityId: String, val headline: String, val description: String?)

data class CommentTopicRequest(val parentId: Int?, val body: String)

data class CreateCommentRequest(val topicId: String, val parentId: String?, val body: String)

data class CreateMessageRequest(val destination: String, val type: MessageType, val message: String?) {
    val timestamp: Long = ZonedDateTime.now().toInstant().toEpochMilli()
}

data class CreateChannelRequest(val name: String)

data class CreateProjectRequest(val name: String,
                                val status: String,
                                val description: String,
                                val startDate: LocalDate,
                                val endDate: LocalDate)

data class LoginRequest(val authenticationHeader: String?, val username: String = "", val password: String = "")