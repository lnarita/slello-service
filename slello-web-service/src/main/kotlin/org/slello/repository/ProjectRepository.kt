package org.slello.repository

import org.bson.types.ObjectId
import org.slello.model.Project
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : ReactiveCrudRepository<Project, ObjectId>