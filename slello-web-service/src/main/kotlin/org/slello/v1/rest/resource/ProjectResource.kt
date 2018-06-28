package org.slello.v1.rest.resource

import kotlinx.coroutines.experimental.reactor.mono
import org.bson.types.ObjectId
import org.slello.model.Project
import org.slello.model.User
import org.slello.repository.ProjectRepository
import org.slello.security.model.ApplicationUserDetails
import org.slello.v1.rest.model.request.CreateProjectRequest
import org.slello.v1.rest.model.response.Response
import org.slello.v1.rest.model.response.ResponseMetaData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@CrossOrigin(allowedHeaders = ["*"])
@RequestMapping("/v1/projects", "/latest/projects")
class ProjectResource @Autowired constructor(val projectRepository: ProjectRepository) {

    @GetMapping
    fun fetchAll(): Mono<Response<MutableList<Project>>> = projectRepository.findAll()
            .collectList()
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NO_CONTENT.value()), errors = null, data = null))

    @GetMapping("/{id}")
    fun fetchProject(@PathVariable("id") id: String): Mono<Response<Project>>? = projectRepository.findById(ObjectId(id))
            .map {
                val metadata = ResponseMetaData(HttpStatus.OK.value())
                Response(metadata, data = it)
            }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable("id") id: String): Mono<Response<String>> = projectRepository.deleteById(ObjectId(id))
            .then(Mono.just(Response(ResponseMetaData(HttpStatus.OK.value()), data = "OK")))

    @PostMapping
    fun createProject(principal: Authentication, @RequestBody createProjectRequest: CreateProjectRequest): Mono<Response<Project>> = mono {
        val userDetails = principal.principal as ApplicationUserDetails
        val user = User(id = userDetails.username, email = userDetails.email, secret = userDetails.password, authority = userDetails.authority, enabled = userDetails.enabled)
        with(createProjectRequest) {
            Project(id = ObjectId(),
                    name = name,
                    status = status,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    tasks = emptyList())
        }
    }.flatMap {
        projectRepository.save(it)
    }.map {
        val metadata = ResponseMetaData(HttpStatus.OK.value())
        Response(metadata, data = it)
    }.defaultIfEmpty(Response(ResponseMetaData(HttpStatus.NOT_FOUND.value()), errors = null, data = null))

}