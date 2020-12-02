package com.rankweis.rc.rcgo.handlers

import com.rankweis.rc.rcgo.model.*
import com.rankweis.rc.rcgo.repository.LocalChoiceStore
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PollHandlerTest {

    private var store = LocalChoiceStore()
    private var handler = PollHandler(store)


    @BeforeEach
    fun setup() {
        store = LocalChoiceStore()
        handler = PollHandler(store)
    }

    @Test
    fun `creates stores`() {
        runBlocking {
            val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE")), true, false))
            assertThat(create.isRight())
                    .isTrue()
            val poll = create.orNull()
            assertThat(poll?.anyoneCanAdd)
                    .isTrue()
            assertThat(poll?.public)
                    .isFalse()
            assertThat(poll?.choices?.first()?.name)
                    .isEqualTo("UNIQUE_TITLE")
            assertThat(poll?.choices?.size)
                    .isEqualTo(1)
        }
    }

    @Test
    fun `uniquely stores choices`() {
        runBlocking {
            val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE"),
                    Choice("uNiQuE_tItLe")), anyoneCanAdd = true, public = false))
            assertThat(create.isRight())
                    .isTrue()
            val poll = create.orNull()
            assertThat(poll?.anyoneCanAdd)
                    .isTrue
            assertThat(poll?.public)
                    .isFalse
            assertThat(poll?.choices?.first()?.name)
                    .isEqualTo("UNIQUE_TITLE")
            assertThat(poll?.choices?.size)
                    .isEqualTo(1)
        }
    }

    @Test
    fun `gets Stored Poll`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE"),
                Choice("uNiQuE_tItLe")), anyoneCanAdd = true, public = true))
        assertThat(create.isRight())
                .isTrue()

        val poll = handler.getPoll(create.orNull()?.id ?: "", PollRetrieveRequest()).orNull()
        assertThat(poll?.anyoneCanAdd)
                .isTrue
        assertThat(poll?.public)
                .isTrue
        assertThat(poll?.choices?.first()?.name)
                .isEqualTo("UNIQUE_TITLE")
        assertThat(poll?.choices?.size)
                .isEqualTo(1)
    }

    @Test
    fun `password blocks retrieval`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE"),
                Choice("uNiQuE_tItLe")), anyoneCanAdd = true, public = false)).orNull()
        val e = handler.getPoll(create?.id ?: "", PollRetrieveRequest());
        assertThat(e.isLeft())
                .isTrue
        val poll = handler.getPoll(create?.id ?: "", PollRetrieveRequest(create?.resultsCode)).orNull()
        assertThat(poll?.anyoneCanAdd)
                .isTrue
        assertThat(poll?.public)
                .isFalse
        assertThat(poll?.choices?.first()?.name)
                .isEqualTo("UNIQUE_TITLE")
        assertThat(poll?.choices?.size)
                .isEqualTo(1)
    }

    @Test
    fun `add adds`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE"),
                Choice("uNiQuE_tItLe")), anyoneCanAdd = true, public = false)).orNull()
//        val poll = handler.getPoll(create?.id ?: "", PollRetrieveRequest(create?.resultsCode)).orNull()
        val poll = handler.add(create?.id
                ?: "", ChoiceAddRequest(setOf(Choice("unique_title"), Choice("NEW_TITLE")))).orNull()
        assertThat(poll?.anyoneCanAdd)
                .isTrue
        assertThat(poll?.public)
                .isFalse
        assertThat(poll?.choices?.size)
                .isEqualTo(2)
        assertThat(poll?.choices?.first()?.name)
                .isEqualTo("UNIQUE_TITLE")
        assertThat(poll?.choices?.elementAt(1)?.name)
                .isEqualTo("NEW_TITLE")
    }

    @Test
    fun `private add requires password`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("UNIQUE_TITLE"),
                Choice("uNiQuE_tItLe")), anyoneCanAdd = false, public = false)).orNull()
        val e = handler.add(create?.id ?: "", ChoiceAddRequest(setOf(Choice("unique_title"), Choice("NEW_TITLE"))))
        assertThat(e.isLeft())
                .isTrue
        handler.add(create?.id ?: "", ChoiceAddRequest(
                setOf(Choice("unique_title"), Choice("NEW_TITLE")),
                create?.addCode ?: "")).orNull()
        val poll = handler.getPoll(create?.id ?: "", PollRetrieveRequest(create?.resultsCode)).orNull()
        assertThat(poll?.anyoneCanAdd)
                .isFalse
        assertThat(poll?.public)
                .isFalse
        assertThat(poll?.choices?.size)
                .isEqualTo(2)
        assertThat(poll?.choices?.first()?.name)
                .isEqualTo("UNIQUE_TITLE")
        assertThat(poll?.choices?.elementAt(1)?.name)
                .isEqualTo("NEW_TITLE")
    }

    @Test
    fun `voting gets stored`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("The Way of Kings"),
                Choice("Words of Radiance"), Choice("Oathbringer"), Choice("Rhythm of War")
        ), anyoneCanAdd = false, public = false)).orNull()
        val ballot = setOf(Vote(Choice("The Way of Kings"), 3),
                Vote(Choice("Words of Radiance"), 1),
                Vote(Choice("Oathbringer"), 4),
                Vote(Choice("Rhythm of War"), 2))
        val vote = handler.vote(create?.id ?: "", Ballot("Kian", ballot))
        assertThat(vote.isRight())
                .isTrue()
        val poll = handler.getPoll(create?.id ?: "", PollRetrieveRequest(create?.resultsCode)).orNull()
        assertThat(poll?.ballots)
                .hasSize(1)
        assertThat(poll?.ballots?.first()?.votes)
                .containsAll(ballot)
    }

    @Test
    fun `must vote for an existing option`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("The Way of Kings"),
                Choice("Words of Radiance"), Choice("Oathbringer"), Choice("Rhythm of War")
        ), anyoneCanAdd = false, public = false)).orNull()
        val ballot = setOf(Vote(Choice("The Way of Kings"), 3),
                Vote(Choice("Words of Radiance"), 1),
                Vote(Choice("FAKE_CHOICE"), 4),
                Vote(Choice("ANOTHER_BAD_CHOICE"), 4),
                Vote(Choice("Rhythm of War"), 2))
        val vote = handler.vote(create?.id ?: "", Ballot("Kian", ballot))
        assertThat(vote.isLeft())
                .isTrue
        var exception = vote.swap().orNull()
        assertThat(exception?.httpStatus?.value())
                .isEqualTo(400)
        assertThat(exception?.message)
                .contains("NON_EXISTING_CHOICE")
    }

    @Test
    fun `cannot put two votes in the same priority`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("The Way of Kings"),
                Choice("Words of Radiance"), Choice("Oathbringer"), Choice("Rhythm of War")
        ), anyoneCanAdd = false, public = false)).orNull()
        val ballot = setOf(Vote(Choice("The Way of Kings"), 1),
                Vote(Choice("Words of Radiance"), 1),
                Vote(Choice("Rhythm of War"), 2))
        val vote = handler.vote(create?.id ?: "", Ballot("Kian", ballot))
        assertThat(vote.isLeft())
                .isTrue
        val exception = vote.swap().orNull()
        assertThat(exception?.httpStatus?.value())
                .isEqualTo(400)
        assertThat(exception?.message)
                .contains("INVALID_PRIORITY")
    }

    @Test
    fun `must have sequential priorities`() = runBlocking<Unit> {
        val create = handler.create(PollCreateRequest(setOf(Choice("The Way of Kings"),
                Choice("Words of Radiance"), Choice("Oathbringer"), Choice("Rhythm of War")
        ), anyoneCanAdd = false, public = false)).orNull()
        val ballot = setOf(Vote(Choice("The Way of Kings"), 4),
                Vote(Choice("Words of Radiance"), 3),
                Vote(Choice("Rhythm of War"), 2))
        val vote = handler.vote(create?.id ?: "", Ballot("Kian", ballot))
        assertThat(vote.isLeft())
                .isTrue
        val exception = vote.swap().orNull()
        assertThat(exception?.httpStatus?.value())
                .isEqualTo(400)
        assertThat(exception?.message)
                .contains("INVALID_PRIORITY")
    }
}