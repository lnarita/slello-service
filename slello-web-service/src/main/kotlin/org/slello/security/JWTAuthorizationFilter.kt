package org.slello.security

import io.jsonwebtoken.ExpiredJwtException
import org.slello.log.Loggers
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JWTAuthorizationFilter(private val userDetailsService: UserDetailsService, private val jwtTokenUtil: JWT) : OncePerRequestFilter() {

    companion object {
        private val systemLogger = Loggers.SYSTEM.logger
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val requestHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (requestHeader != null && requestHeader.startsWith(JWT.BEARER_AUTHORIZATION_HEADER_PREFIX)) {
            val authToken = requestHeader.removePrefix(JWT.BEARER_AUTHORIZATION_HEADER_PREFIX)
            try {
                val username = jwtTokenUtil.getUsernameFromToken(authToken)
                systemLogger.debug("checking authentication for user '{}'", username)
                if (SecurityContextHolder.getContext().authentication == null) {
                    systemLogger.debug("authorizing user '{}'", username)

                    val userDetails = this.userDetailsService.loadUserByUsername(username)

                    // verifying token integrity
                    if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        systemLogger.info("authorized user '{}', setting security context {}", username, authentication)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (e: IllegalArgumentException) {
                systemLogger.error("an error occurred during token integrity verification", e)
            } catch (e: ExpiredJwtException) {
                systemLogger.warn("the token is expired", e)
            }
        } else {
            systemLogger.info("no bearer token, ignoring header")
        }

        chain.doFilter(request, response)
    }
}