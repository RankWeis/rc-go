package com.rankweis.rc.rcgo.repository

import com.rankweis.rc.rcgo.model.Poll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class LocalChoiceStore(val store: MutableMap<String, Poll> = mutableMapOf()) : ChoiceStore {

    override suspend fun getById(id: String): Poll? {
        return this.store[id]
    }

    override suspend fun create(request: Poll): Poll {
        this.store[request.id] = request
        return request
    }

    override suspend fun update(poll: Poll): Poll {
        this.store.remove(poll.id)
        this.store[poll.id] = poll
        return poll
    }
}