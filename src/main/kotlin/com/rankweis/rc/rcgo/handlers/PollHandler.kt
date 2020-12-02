package com.rankweis.rc.rcgo.handlers

import arrow.core.Either
import arrow.core.extensions.list.foldable.nonEmpty
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import arrow.core.valid
import com.rankweis.rc.rcgo.exceptions.PollException
import com.rankweis.rc.rcgo.generators.IdGenerator
import com.rankweis.rc.rcgo.model.*
import com.rankweis.rc.rcgo.repository.ChoiceStore
import com.rankweis.rc.rcgo.tally.RankedChoiceCounter
import com.rankweis.rc.rcgo.validators.validateBallot
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PollHandler(val choiceStore: ChoiceStore, val idGenerator: IdGenerator) {
    val rankedChoiceCounter = RankedChoiceCounter()

    companion object {
        const val ID_LENGTH = 8;
        const val PASSWORD_LENGTH = 6;
    }

    suspend fun create(createRequest: PollCreateRequest): Either<PollException, Poll> {
        val rankedChoice = Poll(idGenerator.generateWordId(),
                createRequest.initialChoices,
                emptyList(),
                getRandomString(PASSWORD_LENGTH),
                getRandomString(PASSWORD_LENGTH),
                createRequest.anyoneCanAdd,
                createRequest.public
        )
        choiceStore.create(rankedChoice)
        return Either.right(rankedChoice)
    }

    suspend fun getPoll(pollId: String, retrieveRequest: PollRetrieveRequest?): Either<PollException, Poll> {
        return getPoll(pollId)
                .flatMap { rankedChoice ->
                    return if (rankedChoice.public ||
                            (retrieveRequest?.resultsCode != null && retrieveRequest.resultsCode == rankedChoice.resultsCode)) {
                        Either.right(rankedChoice)
                    } else {
                        Either.left(PollException(HttpStatus.UNAUTHORIZED, "Please provide the password"))
                    }
                }
    }

    suspend fun add(pollId: String, addRequest: ChoiceAddRequest): Either<PollException, Poll> {
        return getPoll(pollId)
                .flatMap { poll ->
                    return if (poll.anyoneCanAdd ||
                            (addRequest.addCode != null && addRequest.addCode == poll.addCode)) {

                        val updatedPoll = poll.copy(choices = poll.choices + addRequest.choices)
                        Either.right(choiceStore.update(updatedPoll))
                    } else {
                        Either.left(PollException(HttpStatus.UNAUTHORIZED, "Password required but not provided"))
                    }
                }
    }

    suspend fun vote(pollId: String, ballot: Ballot): Either<PollException, Poll> {
        return getPoll(pollId)
                .flatMap { poll ->
                    validateBallot(ballot, poll)
                            .map { poll.copy(ballots = poll.ballots + it) }
                            .map { choiceStore.update(it) }
                            .mapLeft { PollException(HttpStatus.BAD_REQUEST, it.toString()) }
                }
    }

    suspend fun tally(pollId: String): Either<PollException, RankedChoiceResult> {
        return getPoll(pollId)
                .map { rankedChoiceCounter.tally(it) }
    }

    private suspend fun getPoll(pollId: String): Either<PollException, Poll> {
        return choiceStore.getById(pollId).rightIfNotNull { PollException(HttpStatus.NOT_FOUND) }
    }


    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
    }

}