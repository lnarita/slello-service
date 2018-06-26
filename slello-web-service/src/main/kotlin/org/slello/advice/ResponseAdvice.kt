package org.slello.advice

import arrow.core.getOrElse
import arrow.core.toOption
import org.slello.logger.Loggers
import org.slello.v1.rest.model.response.Response
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@ControllerAdvice
/**
 * Converts `Try.Failure`s to a Response object with a proper `errors` list and no data
 * This class is also responsible for setting the HTTP Status equals to the status sent in the Response metadata
 */
class ResponseAdvice : ResponseBodyAdvice<Any?> {

    companion object {
        val exceptionsLogger = Loggers.EXCEPTIONS.logger
    }

    val supportedTypes = listOf(Response::class.java)

    override fun supports(returnType: MethodParameter?, converterType: Class<out HttpMessageConverter<*>>?): Boolean =
            returnType.toOption().map { methodParameter -> supportedTypes.map { methodParameter.nestedParameterType == it }.any { it } }.getOrElse { false }

    override fun beforeBodyWrite(body: Any?, returnType: MethodParameter?, selectedContentType: MediaType?, selectedConverterType: Class<out HttpMessageConverter<*>>?, request: ServerHttpRequest?, response: ServerHttpResponse?): Any? {
        when (body) {
            is Response<*> -> response?.setStatusCode(HttpStatus.valueOf(body.meta.status))
        }

        return body
    }
}