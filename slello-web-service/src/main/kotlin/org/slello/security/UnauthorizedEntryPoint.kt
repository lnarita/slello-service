package org.slello.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseError
import org.slello.v1.rest.model.response.ResponseMetaData
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class UnauthorizedEntryPoint : AuthenticationEntryPoint {

    @Throws(IOException::class)
    override fun commence(request: HttpServletRequest,
                          response: HttpServletResponse,
                          authException: AuthenticationException) {
        // Invoked when an user tries to access a secured resource without supplying any credentials
        val status = HttpStatus.UNAUTHORIZED.value()
        response.status = status
        val mapper = ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
        mapper.writeValue(response.outputStream, Response<Unit>(ResponseMetaData(status), listOf(ResponseError.Unauthorized())))
    }
}