package org.slello.security.repository

import org.slello.log.Loggers
import org.slello.repository.AccountRepository
import org.slello.security.model.ApplicationUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class UserDetailsRepository @Autowired constructor(val userRepository: AccountRepository) {
    companion object {
        val logger = Loggers.REPOSITORY.logger
    }

    fun findByUsername(username: String): Mono<ApplicationUserDetails> =
            userRepository.findById(username).map {
                ApplicationUserDetails(it.id, it.email, it.secret, it.authority, it.enabled)
            }
}