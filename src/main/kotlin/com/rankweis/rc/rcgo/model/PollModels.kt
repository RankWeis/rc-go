package com.rankweis.rc.rcgo.model

import java.util.*

data class Choice(val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Choice

        if (name.toLowerCase() != other.name.toLowerCase()) return false

        return true
    }

    override fun hashCode(): Int {
        return name.toLowerCase().hashCode()
    }
}

data class Vote(val choice: Choice, val rank: Int)

data class Ballot(val user: String, val votes: Set<Vote>)

enum class ElectionStatus {
    ACTIVE, ENDED, CANCELED
}

data class Election(val date: Date, val ballots: Set<Ballot>, val choices: Set<Choice>, val electionStatus: ElectionStatus)

data class RankedChoiceResult(val winners: Set<Choice>, val rounds: List<Map<Choice, Int>>)

data class PollCreateRequest(
        // Initial choices and ranks (probably should default to zero)
        val initialChoices: Set<Choice> = emptySet(),
        val anyoneCanAdd: Boolean,
        val public: Boolean = true
)

data class PollRetrieveRequest(
        // Initial choices and ranks (probably should default to zero)
        val resultsCode: String? = null,
)

data class ChoiceAddRequest(
        val choices: Set<Choice>,
        val addCode: String? = null
)

data class Poll(
        val id: String,
        val choices: Set<Choice>,
        val ballots: List<Ballot>,
        val resultsCode: String,
        val addCode: String,
        val anyoneCanAdd: Boolean,
        val public: Boolean,
        val pastElections: List<Election> = emptyList()
)
