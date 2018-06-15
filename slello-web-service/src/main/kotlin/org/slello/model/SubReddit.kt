package org.slello.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Document
data class Community(@Id val id: ObjectId,
                     val name: String,
                     val description: String,
                     @DBRef(lazy = true)
                     val members: List<User>,
                     val uri: String,
                     val visibility: Visibility = Visibility.OPEN,
                     val readOnly: Boolean = false)

@Document
data class Topic(@Id val id: ObjectId,
                 val communityId: ObjectId,
                 val headline: String,
                 val postedAt: LocalDateTime,
                 val description: String? = null,
                 val op: User,
                 @DBRef(lazy = true)
                 val comments: List<Comment> = listOf(),
                 val votes: Int = 0,
                 val readOnly: Boolean = false)

@Document
data class Comment(@Id val id: ObjectId,
                   val topicId: ObjectId,
                   val body: String,
                   val postedAt: LocalDateTime,
                   val user: User,
                   val votes: Int = 0,
                   @DBRef(lazy = true)
                   val children: List<Comment> = listOf())

