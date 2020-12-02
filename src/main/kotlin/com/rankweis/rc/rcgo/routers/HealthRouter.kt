package com.rankweis.rc.rcgo.routers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono


@Configuration
class HealthRouter {

    @Bean
    fun healthRoute(): RouterFunction<ServerResponse> {
        return router { GET("/health/status") { ServerResponse.ok().body(Mono.just("Ok!"), String::class.java) } }
    }
}