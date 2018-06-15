package org.slello.v1.rest.model.request

import org.slello.model.Authority
import org.slello.model.Visibility

data class CreateUserRequest(val username: String, val email: String, val password: String, val role: Authority)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class UpdateUserRequest(val email: String, val password: String, val role: Authority)
data class PatchUserRequest(val email: String?, val password: String?, val role: Authority?)

data class CreateCommunityRequest(val name: String, val path: String, val description: String, val visibility: Visibility)

data class CreateTopicRequest(val communityId: String, val headline: String, val description: String?)

data class CommentTopicRequest(val parentId: Int?, val body: String)

data class CreateCommentRequest(val topicId: String, val parentId: String?, val body: String)

data class LoginRequest(val authenticationHeader: String?, val username: String = "", val password: String = "")