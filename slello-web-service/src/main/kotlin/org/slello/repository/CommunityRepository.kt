package org.slello.repository

import org.bson.types.ObjectId
import org.slello.model.Community
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CommunityRepository : ReactiveCrudRepository<Community, ObjectId> {
    fun findByUri(uri: String): Mono<Community>
}