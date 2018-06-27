package org.slello.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class Project(@Id val id: ObjectId,
                   val name: String,
                   val status: String,
                   val description: String,
                   val startDate: LocalDate,
                   val endDate: LocalDate,
                   val tasks: List<Task>)

@Document
data class Task(
        @Id val id: ObjectId,
        val sprint: Int,
        val status: Status
)

enum class Status {
    IN_PROGRESS,
    FINISHED,
    NONE
}