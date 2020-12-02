package com.rankweis.rc.rcgo.handlers

import arrow.core.Either
import arrow.core.getOrHandle
import com.rankweis.rc.rcgo.exceptions.PollException
import com.rankweis.rc.rcgo.model.Ballot
import com.rankweis.rc.rcgo.model.ChoiceAddRequest
import com.rankweis.rc.rcgo.model.PollCreateRequest
import com.rankweis.rc.rcgo.model.PollRetrieveRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class PollController(val handler: PollHandler) {

    suspend fun create(req: ServerRequest): ServerResponse {
        val rankedChoiceRequest = req.awaitBody(PollCreateRequest::class)
        return serverResponseFromEither(handler.create(rankedChoiceRequest))
    }

    suspend fun getPoll(req: ServerRequest): ServerResponse {
        val pollId = req.pathVariable("id")
        val retrieveRequest = req.awaitBodyOrNull(PollRetrieveRequest::class)
        return serverResponseFromEither(handler.getPoll(pollId, retrieveRequest))
    }

    suspend fun add(req: ServerRequest): ServerResponse {
        val pollId = req.pathVariable("id")
        val addRequest = req.awaitBody(ChoiceAddRequest::class)
        return serverResponseFromEither(handler.add(pollId, addRequest))
    }

    suspend fun vote(req: ServerRequest): ServerResponse {
        val pollId = req.pathVariable("id")
        val addRequest = req.awaitBody(Ballot::class)
        return serverResponseFromEither(handler.vote(pollId, addRequest))
    }

    suspend fun tally(req: ServerRequest): ServerResponse {
        val pollId = req.pathVariable("id")
        return serverResponseFromEither(handler.tally(pollId))
    }

    private suspend fun serverResponseFromEither(e : Either<PollException, Any>): ServerResponse {
        return e
                .map { ServerResponse.ok().bodyValueAndAwait(it) }
                .getOrHandle { ServerResponse.status(it.httpStatus).bodyValueAndAwait((it.message ?: "")) }
    }
}
