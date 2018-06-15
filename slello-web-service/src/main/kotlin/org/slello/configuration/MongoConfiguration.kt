package org.slello.configuration

import com.mongodb.async.client.MongoClients
import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableReactiveMongoRepositories
class MongoConfiguration : AbstractReactiveMongoConfiguration() {

    @Bean
    override fun reactiveMongoClient(): MongoClient? = MongoClients.create() as MongoClient?


    override fun getDatabaseName(): String {
        return "reactive"
    }
}