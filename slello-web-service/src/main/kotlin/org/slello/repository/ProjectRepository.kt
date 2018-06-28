package org.slello.repository

import org.bson.types.ObjectId
import org.slello.model.Project
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProjectRepository : ReactiveCrudRepository<Project, ObjectId> {
    fun findByUri(uri: String): Mono<Project>
}