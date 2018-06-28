package org.slello.v1.rest.resource

import arrow.core.Option
import kotlinx.coroutines.experimental.future.future
import kotlinx.coroutines.experimental.reactive.awaitFirstOrNull
import kotlinx.coroutines.experimental.runBlocking
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
    fun fetchAll(@RequestParam("fillCommunity", required = false, defaultValue = "false") fillCommunity: Boolean): Mono<Response<MutableList<TopicResponse>>> = topicRepository.findAll()
            .flatMap { topic ->
                toTopicResponse(topic, fillCommunity)
            }.collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    private fun toTopicResponse(topic: Topic, buildCommunity: Boolean): Mono<TopicResponse> {
        return with(topic) {
            val communityResponse = if (buildCommunity) {
                val community = communityRepository.findById(communityId)
                community.map {
                    with(it) {
                        val users = members.map { UserResponse(username = it.id, email = it.email, role = it.authority.name) }
                        Option.just(CommunityResponse(id = id.toHexString(), name = name, path = uri, visibility = visibility, memberCount = members.size, members = users, readOnly = readOnly, thumbnail = thumbnail, description = description))
                    }
                }
            } else {
                Mono.just(Option.empty())
            }
            val user = UserResponse(username = op.id, email = op.email, role = op.authority.name)
            communityResponse.map {
                TopicResponse(id = id.toHexString(), community = it.orNull(), comments = comments.map { toCommentResponse(it) }, headline = headline, postedAt = postedAt, description = description, op = user, votes = votes, readOnly = readOnly)
            }
        }
    }

    private fun toCommentResponse(comment: Comment): CommentResponse {
        return with(comment) {
            val childrenResponse = children.map { toCommentResponse(it) }
            CommentResponse(id = id.toHexString(), topicId = topicId.toHexString(), body = body, postedAt = postedAt, user = user.id, votes = votes, children = childrenResponse)
        }
    }

    @GetMapping("/{id}")
    fun fetchTopic(@PathVariable("id") id: String, @RequestParam("fillCommunity", required = false, defaultValue = "false") fillCommunity: Boolean): Mono<Response<TopicResponse>> = topicRepository.findById(ObjectId(id))
            .flatMap { topic ->
                toTopicResponse(topic, fillCommunity)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @GetMapping(params = ["communityId"])
    fun fetchTopicByCommunity(@RequestParam("communityId", required = true) communityId: String, @RequestParam("fillCommunity", required = false, defaultValue = "false") fillCommunity: Boolean): Mono<Response<MutableList<TopicResponse>>> = topicRepository.findByCommunityId(ObjectId(communityId))
            .flatMap { topic ->
                toTopicResponse(topic, fillCommunity)
            }.collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteTopic(@PathVariable("id") id: String): Mono<Response<String>>? = topicRepository.deleteById(ObjectId(id))
            .then(Mono.just(Response(ResponseMetaData(HttpStatus.OK.value()), data = "OK")))

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
    }.flatMap {
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
    }.flatMap {
        toTopicResponse(it, false)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

}