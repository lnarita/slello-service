package org.slello.security.resource

import arrow.core.Try
import arrow.core.getOrElse
import arrow.data.fix
import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.experimental.future.future
import org.slello.security.JWT
import org.slello.security.controller.AuthenticationController
import org.slello.security.exception.UserNotFoundException
import org.slello.v1.rest.model.request.LoginRequest
import org.slello.v1.rest.model.response.LoginResponse
import org.slello.v1.rest.model.response.ResponseError
import org.slello.v1.rest.validator.Rules
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.servlet.http.HttpServletRequest


@RestController
class AuthenticationResource @Autowired constructor(val authenticationController: AuthenticationController) {
    companion object {
        const val BASIC_AUTHORIZATION_PREFIX = "Basic "
    }

    @Timed
    @PostMapping("/auth")
    fun login(httpServletRequest: HttpServletRequest): Mono<LoginResponse> = Mono.fromFuture(
            future {
                LoginRequest(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION))
            }.thenApply {
                Rules.accumulateErrors { it.hasCredentials(BASIC_AUTHORIZATION_PREFIX) }.fix()
            }.thenApply {
                Try { it.map { authenticationController.createAuthenticationToken(it) } }
            }.thenApply { requestResponse ->
                requestResponse.map {
                    it.fold({ LoginResponse(HttpStatus.UNAUTHORIZED, errors = it.all, data = null) }, { it })
                }
            }.thenApply {
                it.getOrElse {
                    when (it) {
                        is InternalAuthenticationServiceException -> {
                            when (it.cause) {
                                is UserNotFoundException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.InvalidUser()), data = null)
                                else -> LoginResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors = listOf(ResponseError.InternalServerError(it)), data = null)
                            }
                        }
                        is UserNotFoundException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.InvalidUser()), data = null)
                        is BadCredentialsException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.BadCredentials()), data = null)
                        else -> LoginResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors = listOf(ResponseError.InternalServerError(it)), data = null)
                    }
                }
            }
    )

    @Timed
    @PostMapping("/auth:refresh")
    fun refresh(httpServletRequest: HttpServletRequest): Mono<LoginResponse> = Mono.fromFuture(
            future {
                val authToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
                authToken.removePrefix(JWT.BEARER_AUTHORIZATION_HEADER_PREFIX)
            }.thenApply {
                Try {
                    authenticationController.refreshAndGetAuthenticationToken(it)
                }
            }.thenApply {
                it.getOrElse {
                    when (it) {
                        is InternalAuthenticationServiceException -> {
                            when (it.cause) {
                                is UserNotFoundException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.InvalidUser()), data = null)
                                else -> LoginResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors = listOf(ResponseError.InternalServerError(it)), data = null)
                            }
                        }
                        is UserNotFoundException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.InvalidUser()), data = null)
                        is BadCredentialsException -> LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.BadCredentials()), data = null)
                        else -> LoginResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors = listOf(ResponseError.InternalServerError(it)), data = null)
                    }
                }
            }
    )
}