package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import org.bson.types.ObjectId
import org.slello.model.Community
import org.slello.model.User
import org.slello.repository.AccountRepository
import org.slello.repository.CommunityRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CreateCommunityRequest
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
@RequestMapping("/v1/communities", "/latest/communities")
class CommunityResource @Autowired constructor(val communityRepository: CommunityRepository, val userRepository: AccountRepository) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<CommunityResponse>>> = communityRepository.findAll()
            .map { community ->
                toCommunityResponse(community, community.id)
            }.collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    @GetMapping("/{id}")
    fun fetchCommunity(@PathVariable("id") id: String): Mono<Response<CommunityResponse>> = communityRepository.findById(ObjectId(id))
            .map {
                toCommunityResponse(it, it.id)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @GetMapping(params = ["path"])
    fun fetchCommunityByPath(@RequestParam("path", required = true) path: String): Mono<Response<CommunityResponse>> = communityRepository.findByUri(path)
            .map {
                toCommunityResponse(it, it.id)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteCommunity(@PathVariable("id") id: String): Mono<Response<String>> = communityRepository.deleteById(ObjectId(id))
            .then(Mono.just(Response(ResponseMetaData(HttpStatus.OK.value()), data = "OK")))

    @PostMapping
    fun createCommunity(principal: Authentication, @RequestBody createCommunityRequest: CreateCommunityRequest): Mono<Response<CommunityResponse>> = Mono.fromFuture(
            future {
                val userDetails = principal.principal as ApplicationUserDetails
                val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
                with(createCommunityRequest) {
                    Community(id = ObjectId(), name = name, description = description, uri = path, visibility = visibility, members = listOf(user), readOnly = false, thumbnail = thumbnail)
                }
            }).flatMap {
        communityRepository.save(it)
    }.map {
        toCommunityResponse(it, it.id)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping("/{id}:join")
    fun join(authentication: Authentication, @PathVariable("id") id: String): Mono<Response<CommunityResponse>> = Mono.fromFuture(
            future {
                authentication.principal as ApplicationUserDetails
            }.thenApply {
                it.username
            }).flatMap {
        userRepository.findById(it)
    }.flatMap { user ->
        communityRepository.findById(ObjectId(id)).map { it to user }
    }.map {
        with(it.first) {
            copy(members = members + listOf(it.second))
        }
    }.flatMap {
        communityRepository.save(it)
    }.map {
        toCommunityResponse(it, it.id)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    private fun toCommunityResponse(it: Community, id: ObjectId): CommunityResponse {
        return with(it) {
            val users = members.map { UserResponse(username = it.id, email = it.email, role = it.authority.name) }
            CommunityResponse(id = id.toHexString(), name = name, path = uri, visibility = visibility, memberCount = members.size, members = users, readOnly = readOnly, thumbnail = thumbnail)
        }
    }

    @PostMapping("/{id}:leave")
    fun leave(authentication: Authentication, @PathVariable("id") id: String): Mono<Response<CommunityResponse>>? = Mono.fromFuture(
            future {
                authentication.principal as ApplicationUserDetails
            }.thenApply {
                it.username
            }).flatMap {
        userRepository.findById(it)
    }.flatMap { user ->
        communityRepository.findById(ObjectId(id)).map { it to user }
    }.map {
        with(it.first) {
            copy(members = members - listOf(it.second))
        }
    }.flatMap {
        communityRepository.save(it)
    }.map {
        toCommunityResponse(it, it.id)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))
}