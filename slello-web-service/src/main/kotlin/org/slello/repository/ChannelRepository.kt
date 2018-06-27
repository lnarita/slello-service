package org.slello.repository

import org.slello.model.Channel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepository : ReactiveCrudRepository<Channel, String>