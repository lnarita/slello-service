package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import org.bson.types.ObjectId
import org.slello.model.Comment
import org.slello.model.Topic
import org.slello.model.User
import org.slello.repository.CommentRepository
import org.slello.repository.CommunityRepository
import org.slello.repository.TopicRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CommentTopicRequest
import org.slello.v1.rest.model.request.CreateTopicRequest
import org.slello.v1.rest.model.response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime


@RestController
@RequestMapping("/v1/topics", "/latest/topics")
class TopicResource @Autowired constructor(val topicRepository: TopicRepository, val communityRepository: CommunityRepository, val commentRepository: CommentRepository) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<TopicResponse>>> = topicRepository.findAll()
            .map { topic ->
                toTopicResponse(topic, false)
            }.collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    private fun toTopicResponse(topic: Topic, buildCommunity: Boolean): TopicResponse {
        return with(topic) {
            val communityResponse = if (buildCommunity) {
                val community = communityRepository.findById(communityId)
                community.map {
                    with(it) {
                        val users = members.map { UserResponse(username = it.id, email = it.email, role = it.authority.name) }
                        CommunityResponse(id = id.toHexString(), name = name, path = uri, visibility = visibility, memberCount = members.size, members = users, readOnly = readOnly, thumbnail = thumbnail)
                    }
                }.block()
            } else {
                null
            }
            val user = UserResponse(username = op.id, email = op.email, role = op.authority.name)
            TopicResponse(id = id.toHexString(), community = communityResponse, comments = comments.map { toCommentResponse(it) }, headline = headline, postedAt = postedAt, description = description, op = user, votes = votes, readOnly = readOnly)
        }
    }

    private fun toCommentResponse(comment: Comment): CommentResponse {
        return with(comment) {
            val childrenResponse = children.map { toCommentResponse(it) }
            CommentResponse(id = id.toHexString(), topicId = topicId.toHexString(), body = body, postedAt = postedAt, user = user.id, votes = votes, children = childrenResponse)
        }
    }

    @GetMapping("/{id}")
    fun fetchTopic(@PathVariable("id") id: String): Mono<Response<TopicResponse>> = topicRepository.findById(ObjectId(id))
            .map { topic ->
                toTopicResponse(topic, false)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteTopic(@PathVariable("id") id: String): Mono<Response<String>>? = topicRepository.deleteById(ObjectId(id))
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = "OK")
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping
    fun createTopic(principal: Authentication, @RequestBody createTopicRequest: CreateTopicRequest): Mono<Response<TopicResponse>>? = Mono.fromFuture(
            future {
                val userDetails = principal.principal as ApplicationUserDetails
                val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
                with(createTopicRequest) {
                    Topic(id = ObjectId(), communityId = ObjectId(communityId), headline = headline, postedAt = LocalDateTime.now(), description = description, op = user)
                }
            }).flatMap {
        topicRepository.save(it)
    }.map {
        toTopicResponse(it, false)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))


    @PostMapping("/{id}:comment")
    fun comment(principal: Authentication, @PathVariable("id") id: String, @RequestBody commentTopicRequest: CommentTopicRequest): Mono<Response<TopicResponse>> = Mono.fromFuture(
            future {
                val userDetails = principal.principal as ApplicationUserDetails
                val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
                with(commentTopicRequest) {
                    Comment(id = ObjectId(), topicId = ObjectId(id), children = listOf(), user = user, votes = 0, body = body, postedAt = LocalDateTime.now())
                }
            }).flatMap { comment ->
        commentRepository.save(comment)
    }.flatMap { comment ->
        topicRepository.findById(ObjectId(id)).map { it to comment }
    }.flatMap {
        with(it.first) {
            topicRepository.save(copy(comments = comments + listOf(it.second)))
        }
    }.map {
        toTopicResponse(it, false)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

}