package org.slello.v1.rest.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.slello.model.Comment
import org.slello.model.Visibility
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


data class ResponseMetaData(val status: Int) {
    val time = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}

sealed class ResponseError(val code: String, val description: String) {
    class Unknown : ResponseError("0x100", "Something really bad happened")
    class Unauthorized : ResponseError("0x200", "Access denied, please login in")
    class InvalidAuthenticationHeader(header: String) : ResponseError("0x201", "Basic Authentication header `${header}` is unparseable")
    class InvalidUser : ResponseError("0x202", "Username does not exist")
    class BadCredentials : ResponseError("0x203", "Bad credentials")
    class BadRequest(reason: String) : ResponseError("0x300", reason)
    class DataAccessFailure(val reason: Throwable) : ResponseError("0x400", "We're facing some problems, please try again later")
    class InternalServerError(val reason: Throwable) : ResponseError("0x666", "Wild ERROR appeared!")
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
open class Response<T>(val meta: ResponseMetaData, val errors: List<ResponseError>? = null, val data: T? = null)

data class UserResponse(val username: String, val email: String, val role: String)
data class CommunityResponse(val id: String, val name: String, val path: String, val memberCount: Int, val members: List<UserResponse>, val visibility: Visibility, val readOnly: Boolean)
data class CommentResponse(val id: String, val topicId: String, val body: String, val postedAt: LocalDateTime, val user: String, val votes: Int = 0, val children: List<CommentResponse> = listOf())
data class TopicResponse(val id: String, val community: CommunityResponse?, val headline: String, val postedAt: LocalDateTime, val description: String? = null, val op: UserResponse, val comments: List<CommentResponse> = listOf(), val votes: Int = 0, val readOnly: Boolean = false)

data class TokenResponse(val token: String)
class LoginResponse(status: HttpStatus, errors: List<ResponseError>?, data: TokenResponse?) : Response<TokenResponse>(ResponseMetaData(status.value()), errors, data)