package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.reactor.mono
import org.bson.types.ObjectId
import org.slello.model.Message
import org.slello.repository.ChatMessageRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CreateMessageRequest
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux


@RestController
@RequestMapping("/v1/messages", "/latest/messages")
class ChatMessageResource @Autowired constructor(val messageRepository: ChatMessageRepository) {

    @GetMapping
    fun fetchAll(principal: Authentication): Flux<Message> = mono {
        val userDetails = principal.principal as ApplicationUserDetails
        userDetails.username
    }.toFlux().flatMap {
        messageRepository.findByUser(it)
    }

    @PostMapping
    fun createMessage(principal: Authentication, @RequestBody createCommentRequest: CreateMessageRequest): Mono<Response<Message>> = mono {
        val userDetails = principal.principal as ApplicationUserDetails
        with(createCommentRequest) {
            Message(id = ObjectId(), destination = destination, user = userDetails.username, type = type, message = message, timestamp = timestamp)
        }
    }.flatMap {
        messageRepository.save(it)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))
}