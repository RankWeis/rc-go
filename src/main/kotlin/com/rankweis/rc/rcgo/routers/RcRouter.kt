package com.rankweis.rc.rcgo.routers

import com.rankweis.rc.rcgo.handlers.PollController
import com.rankweis.rc.rcgo.handlers.PollHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter


@Configuration
class RcRouter(private val controller: PollController) {
    companion object {
        const val BASE_PATH = "/v1/poll"
        const val ID = "/id/{id}"
    }

    @Bean
    fun rcRoute() =
            coRouter {
                BASE_PATH.nest {
                    POST("/create") { controller.create(it) }
                    ID.nest {
                        POST("") { controller.getPoll(it)}
                        POST("/") { controller.getPoll(it) }
                        POST("/add") { controller.add(it) }
                        POST("/vote") { controller.vote(it) }
                        POST("/tally") { controller.tally(it) }
                    }
                }
            }
}