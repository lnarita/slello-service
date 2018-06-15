package org.slello.security.controller

import org.slello.security.JWT
import org.slello.security.service.AuthUserDetailsService
import org.slello.v1.rest.model.request.LoginRequest
import org.slello.v1.rest.model.response.LoginResponse
import org.slello.v1.rest.model.response.ResponseError
import org.slello.v1.rest.model.response.TokenResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthenticationController @Autowired constructor(val authenticationManager: AuthenticationManager, val jwtTokenUtil: JWT, val userDetailsService: AuthUserDetailsService) {

    @Throws(AuthenticationException::class)
    fun createAuthenticationToken(authenticationRequest: LoginRequest): LoginResponse {

        authenticate(authenticationRequest.username, authenticationRequest.password)

        // reload user details to generate token
        // currently, this step is not really needed as the only thing I really use to generate the token is the username
        // I, however, may or may not improve it someday
        val userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val token = jwtTokenUtil.generateToken(userDetails)

        // return the token
        return LoginResponse(HttpStatus.OK, errors = null, data = TokenResponse(token))
    }

    fun refreshAndGetAuthenticationToken(token: String): LoginResponse {
        return if (jwtTokenUtil.canTokenBeRefreshed(token)) {
            val refreshedToken = jwtTokenUtil.refreshToken(token)
            LoginResponse(HttpStatus.OK, errors = null, data = TokenResponse(refreshedToken))
        } else {
            LoginResponse(HttpStatus.UNAUTHORIZED, errors = listOf(ResponseError.BadCredentials()), data = null)
        }
    }

    /**
     * Authenticates the user.
     * May throw an [AuthenticationException] if something goes wrong
     */
    private fun authenticate(username: String, password: String) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
    }
}