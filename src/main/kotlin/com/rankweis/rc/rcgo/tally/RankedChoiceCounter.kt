package com.rankweis.rc.rcgo.tally

import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.extensions.set.foldable.nonEmpty
import com.rankweis.rc.rcgo.model.Ballot
import com.rankweis.rc.rcgo.model.Choice
import com.rankweis.rc.rcgo.model.Poll
import com.rankweis.rc.rcgo.model.RankedChoiceResult
import java.lang.RuntimeException

class RankedChoiceCounter {


    fun tally(poll: Poll): RankedChoiceResult {
        val ballots = poll.ballots
        val choices = poll.choices

        var rounds = emptyList<Map<Choice, Int>>()
        var cur = mapOf(*choices.map { Pair(it, 0) }.toTypedArray())
        var winner: Set<Choice> = emptySet()
        var eliminated = emptySet<Choice>()

        while (winner.isEmpty()) {
            // zero out votes
            cur = cur.mapValues { 0 }

            cur = runTally(cur, ballots, eliminated).filter { it.value != 0 }

            rounds = rounds + cur
            winner = winner(cur)
            if (winner.isEmpty()) {
                val lowest = cur.minByOrNull { it.value }
                if (lowest != null) {
                    val newlyEliminated = cur.filter { it.value == lowest.value }.keys
                    if (newlyEliminated.size == cur.size) {
                        // all-way tie
                        winner = cur.keys
                    }
                    eliminated = eliminated + newlyEliminated
                    cur = cur - eliminated
                } else {
                    throw RuntimeException("No lowest found, improper state")
                }
            }
        }
        return RankedChoiceResult(if (winner.nonEmpty()) winner else cur.keys, rounds)
    }

    private fun winner(votes: Map<Choice, Int>): Set<Choice> {
        val numberOfVotes = votes.values
                .fold(0) { acc, x -> acc + x }
        val winThreshhold = numberOfVotes / 2
        return if (numberOfVotes == 0) emptySet() else votes.filter { (_, v) -> v > winThreshhold }.keys
    }

    private fun getNonEliminatedVote(ballot: Ballot, eliminated: Set<Choice>): Choice? {
        return ballot.votes
                .filter { !eliminated.contains(it.choice) }
                .minByOrNull { it.rank }
                ?.choice
    }

    private fun runTally(currentChoices: Map<Choice, Int>, ballots: List<Ballot>, eliminated: Set<Choice>): Map<Choice, Int> {
        return ballots
                .mapNotNull { getNonEliminatedVote(it, eliminated) }
                .foldLeft(currentChoices) { acc, x -> acc + (x to (acc[x]?.plus(1) ?: 1)) }
    }
}