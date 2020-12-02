package com.rankweis.rc.rcgo.tally

import com.rankweis.rc.rcgo.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RankedChoiceCounterTest {
    private val wok = Choice("The Way of Kings")
    private val wor = Choice("Words of Radiance")
    private val ob = Choice("Oathbringer")
    private val row = Choice("Rhythm of War")

    private val choices = setOf(wok, wor, ob, row)
    private val poll = Poll(
            "1234",
            choices,
            emptyList(),
            "",
            "",
            true,
            public = true
    )

    private val kianBallot = Ballot("Kian", setOf(Vote(wok, 2), Vote(wor, 1), Vote(ob, 4), Vote(row, 3)))
    private val angelaBallot = Ballot("Angela", setOf(Vote(wok, 3), Vote(wor, 1), Vote(ob, 4), Vote(row, 2)))
    private val alexBallot = Ballot("Alex", setOf(Vote(wok, 1), Vote(wor, 2), Vote(ob, 3)))

    @Test
    fun `vote wins with most votes`() {
        val tally = RankedChoiceCounter().tally(poll.copy(ballots = listOf(kianBallot, alexBallot, angelaBallot)))
        assertThat(tally.winners)
                .hasSize(1)
                .containsExactly(wor)
    }

    @Test
    fun `vote can tie`() {
        val tally = RankedChoiceCounter().tally(poll.copy(ballots = listOf(kianBallot, alexBallot)))
        assertThat(tally.winners)
                .hasSize(2)
                .containsExactlyInAnyOrder(wor, wok)

    }

    @Test
    fun `vote runs with single participant`() {
        val tally = RankedChoiceCounter().tally(poll.copy(ballots = listOf(kianBallot)))
        assertThat(tally.winners)
                .hasSize(1)
                .containsExactlyInAnyOrder(wor)

    }

}


