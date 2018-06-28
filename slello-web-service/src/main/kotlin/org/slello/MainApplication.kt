package org.slello

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker


@SpringBootApplication
@EnableCircuitBreaker
class MainApplication

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args) {
        setBannerMode(Banner.Mode.LOG)
    }
}
