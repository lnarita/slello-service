package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import org.slello.model.User
import org.slello.repository.AccountRepository
import org.slello.v1.rest.model.request.CreateUserRequest
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.slello.v1.rest.model.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/accounts", "/latest/accounts")
class AccountResource @Autowired constructor(val accountRepository: AccountRepository, val passwordEncoder: PasswordEncoder) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<UserResponse>>> =
            accountRepository.findAll()
                    .map { user ->
                        toUserResponse(user)
                    }.collectList()
                    .map {
                        val metadata = ResponseMetaData(HttpStatus.OK.value())
                        Response(metadata, data = it)
                    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    @GetMapping("/{username}")
    fun fetchUser(@PathVariable("username") username: String): Mono<Response<UserResponse>> = accountRepository.findById(username)
            .map { user ->
                toUserResponse(user)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{username}")
    fun deleteUser(@PathVariable("username") username: String): Mono<Response<String>> = accountRepository.deleteById(username)
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = "OK")
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): Mono<Response<UserResponse>> = Mono.fromFuture(
            future {
                passwordEncoder.encode(createUserRequest.password)
            }.thenApply {
                with(createUserRequest) {
                    User(id = username, email = email, secret = it, authority = role, enabled = true)
                }
            })
            .flatMap {
                accountRepository.save(it)
            }.map { user ->
                toUserResponse(user)
            }.map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    private fun toUserResponse(user: User) = UserResponse(username = user.id, email = user.email, role = user.authority.name)

}