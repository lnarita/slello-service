package org.slello.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@ConfigurationProperties(prefix = "cors")
class CorsConfigurationProperties @Autowired constructor(@Value("\${allowedOrigins}") val allowedOrigins: List<String>,
                                                         @Value("\${allowedHeaders}") val allowedHeaders: List<String>,
                                                         @Value("\${allowedMethods}") val allowedMethods: List<String>)