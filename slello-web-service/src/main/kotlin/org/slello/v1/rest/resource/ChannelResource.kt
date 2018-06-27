package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import kotlinx.coroutines.experimental.reactor.mono
import org.bson.types.ObjectId
import org.slello.model.Channel
import org.slello.model.Community
import org.slello.model.User
import org.slello.repository.AccountRepository
import org.slello.repository.ChannelRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CreateChannelRequest
import org.slello.v1.rest.model.response.CommunityResponse
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.slello.v1.rest.model.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/channels", "/latest/channels")
class ChannelResource @Autowired constructor(val channelRepository: ChannelRepository, val userRepository: AccountRepository) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<Channel>>> = channelRepository.findAll()
            .collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    @GetMapping("/{id}")
    fun fetchCommunity(@PathVariable("id") id: String): Mono<Response<Channel>>? = channelRepository.findById(id)
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteCommunity(@PathVariable("id") id: String): Mono<Response<String>> = channelRepository.deleteById(id)
            .then(Mono.just(Response(ResponseMetaData(HttpStatus.OK.value()), data = "OK")))

    @PostMapping
    fun createCommunity(principal: Authentication, @RequestBody createChannelRequest: CreateChannelRequest): Mono<Response<Channel>> = mono {
        val userDetails = principal.principal as ApplicationUserDetails
        val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
        with(createChannelRequest) {
            Channel(name = name, members = listOf(user))
        }
    }.flatMap {
        channelRepository.save(it)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping("/{id}:join")
    fun join(authentication: Authentication, @PathVariable("id") id: String): Mono<Response<Channel>> = mono {
        authentication.principal as ApplicationUserDetails
    }.map {
        it.username
    }.flatMap {
        userRepository.findById(it)
    }.flatMap { user ->
        channelRepository.findById(id).map { it to user }
    }.map {
        with(it.first) {
            copy(members = members + listOf(it.second))
        }
    }.flatMap {
        channelRepository.save(it)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping("/{id}:leave")
    fun leave(authentication: Authentication, @PathVariable("id") id: String): Mono<Response<Channel>>? = mono {
        authentication.principal as ApplicationUserDetails
    }.map {
        it.username
    }.flatMap {
        userRepository.findById(it)
    }.flatMap { user ->
        channelRepository.findById(id).map { it to user }
    }.map {
        with(it.first) {
            copy(members = members - listOf(it.second))
        }
    }.flatMap {
        channelRepository.save(it)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))
}