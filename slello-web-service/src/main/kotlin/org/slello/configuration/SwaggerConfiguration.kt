package org.slello.configuration

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@EnableAutoConfiguration
class SwaggerConfiguration {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .enableUrlTemplating(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.slello"))
                .paths(PathSelectors.any())
                .build()
                .genericModelSubstitutes(Mono::class.java, Flux::class.java)
    }
}