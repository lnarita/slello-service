package org.slello.security.service

import org.slello.security.repository.UserDetailsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class AuthUserDetailsService @Autowired constructor(val userRepository: UserDetailsRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) {
            throw IllegalArgumentException("Username can't be null")
        }
        val user = userRepository.findByUsername(username)
        return user.block()
    }
}