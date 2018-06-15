package org.slello.configuration

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy


@Configuration
@EnableAspectJAutoProxy
class MetricsConfiguration {
    @Bean
    fun meterRegistryCustomizer(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config()
                    .meterFilter(MeterFilter.deny { id ->
                        val uri = id.getTag("path") ?: ""
                        // remove metrics for swagger documentation and actuator requests
                        listOf("swagger", "actuator", "webjars").map { uri.startsWith("/${it}") }.any { it }
                    })
                    // also, no tomcat
                    .meterFilter(MeterFilter.denyNameStartsWith("tomcat"))
        }
    }

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

}