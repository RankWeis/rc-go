package com.rankweis.rc.rcgo.validators

import arrow.core.Either
import arrow.core.Validated
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.extensions.list.foldable.nonEmpty
import com.rankweis.rc.rcgo.exceptions.PollException
import com.rankweis.rc.rcgo.model.Ballot
import com.rankweis.rc.rcgo.model.Poll
import org.springframework.http.HttpStatus


enum class BallotError {
    MULTIPLE_VOTES_WITH_SAME_PRIORITY,
    NON_EXISTING_CHOICE,
    INVALID_PRIORITY
}

fun validateBallot(b: Ballot, poll: Poll) : Either<List<BallotError>, Ballot> {
    val errors = mutableListOf<BallotError>()
    val priorities = b.votes.map { it.rank }.sorted()
    val nonExistingChoices = b.votes.map { it.choice }
            .minus(poll.choices)

//    // This is part of invalid priority as well
//    if(priorities.distinct() != priorities) {
//        errors += BallotError.MULTIPLE_VOTES_WITH_SAME_PRIORITY
//    }
    if(nonExistingChoices.nonEmpty()) {
        errors += BallotError.NON_EXISTING_CHOICE
    }
    if(priorities.isEmpty() || ((1..priorities.size).toList() != priorities)) {
        errors += BallotError.INVALID_PRIORITY
    }
    return if(errors.nonEmpty()) Either.left(errors) else Either.right(b);


}