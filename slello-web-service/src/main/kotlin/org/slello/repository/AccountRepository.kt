package org.slello.repository

import org.slello.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : ReactiveCrudRepository<User, String>