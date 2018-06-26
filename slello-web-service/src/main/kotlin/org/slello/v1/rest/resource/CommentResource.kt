package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import org.bson.types.ObjectId
import org.slello.model.Comment
import org.slello.model.User
import org.slello.repository.CommentRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CreateCommentRequest
import org.slello.v1.rest.model.response.CommentResponse
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime


@RestController
@RequestMapping("/v1/comments", "/latest/comments")
class CommentResource @Autowired constructor(val commentRepository: CommentRepository) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<CommentResponse>>> = commentRepository.findAll()
            .map { comment ->
                toCommentResponse(comment)
            }.collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    private fun toCommentResponse(comment: Comment): CommentResponse {
        return with(comment) {
            val childrenResponse = children.map { toCommentResponse(it) }
            CommentResponse(id = id.toHexString(), topicId = topicId.toHexString(), body = body, postedAt = postedAt, user = user.id, votes = votes, children = childrenResponse)
        }
    }

    @GetMapping("/{id}")
    fun fetchComment(@PathVariable("id") id: String): Mono<Response<CommentResponse>> = commentRepository.findById(ObjectId(id))
            .map { comment ->
                toCommentResponse(comment)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteComment(@PathVariable("id") id: String): Mono<Response<String>>? = commentRepository.deleteById(ObjectId(id))
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = "OK")
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping
    fun createComment(principal: Authentication, @RequestBody createCommentRequest: CreateCommentRequest): Mono<Response<CommentResponse>>? = Mono.fromFuture(
            future {
                val userDetails = principal.principal as ApplicationUserDetails
                val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
                with(createCommentRequest) {
                    Comment(id = ObjectId(), topicId = ObjectId(topicId), body = body, postedAt = LocalDateTime.now(), user = user, votes = 0, children = listOf())
                }
            }).flatMap {
        commentRepository.save(it)
    }.flatMap { comment ->
        commentRepository.findById(ObjectId(createCommentRequest.parentId)).map {
            it.copy(children = it.children + listOf(comment)) to comment
        }
    }.flatMap {pair ->
        commentRepository.save(pair.first).map { it to pair.second }
    }.map {
        toCommentResponse(it.second)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

}