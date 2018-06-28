package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.future.future
import org.slello.model.Authority
import org.slello.model.User
import org.slello.repository.AccountRepository
import org.slello.v1.rest.model.request.CreateUserRequest
import org.slello.v1.rest.model.request.RegisterRequest
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.slello.v1.rest.model.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@CrossOrigin(allowedHeaders = ["*"])
class RegisterResource @Autowired constructor(val accountRepository: AccountRepository, val passwordEncoder: PasswordEncoder) {

    @PostMapping("/register")
    fun createUser(@RequestBody registerRequest: RegisterRequest): Mono<Response<UserResponse>> = Mono.fromFuture(
            future {
                passwordEncoder.encode(registerRequest.password)
            }.thenApply {
                with(registerRequest) {
                    User(id = username, email = email, secret = it, authority = Authority.EXT, enabled = true)
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