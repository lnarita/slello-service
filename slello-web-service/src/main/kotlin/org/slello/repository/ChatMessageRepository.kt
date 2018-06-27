package org.slello.repository

import kotlinx.coroutines.experimental.reactor.mono
import org.bson.Document
import org.bson.types.ObjectId
import org.slello.model.Message
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


interface ChatMessageRepositoryCustom {
    fun findByUser(user: String): Flux<Message>
}

@Repository
interface ChatMessageRepository : ReactiveCrudRepository<Message, ObjectId>, ChatMessageRepositoryCustom

class ChatMessageRepositoryImpl(val reactiveMongoTemplate: MongoTemplate) : ChatMessageRepositoryCustom {

    override fun findByUser(user: String): Flux<Message> = mono {

        val redactOperation = AggregationOperation { aggregationOperationContext ->
            val map = mapOf("if" to Document("\$or", listOf(
                    Document("\$eq", listOf("\$user", user)),
                    Document("\$eq", listOf("\$channel_detail.members._id", user)),
                    Document("\$eq", listOf("\$destination", user)))), "then" to "$\$KEEP", "else" to "$\$PRUNE")
            Document("\$redact", Document("\$cond", map))
        }

        val project = Aggregation.project("destination", "user", "type", "message", "timestamp")

        val channelLookup = LookupOperation.newLookup().from("channel").localField("destination").foreignField("_id").`as`("channel_detail")
        val aggregation = Aggregation.newAggregation(channelLookup, redactOperation, project)
        reactiveMongoTemplate.aggregate(aggregation, Message::class.java, Message::class.java)
    }.flatMapIterable { result -> result.mappedResults }
}