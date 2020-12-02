package com.rankweis.rc.rcgo.repository

import com.rankweis.rc.rcgo.model.Poll

interface ChoiceStore {
    suspend fun getById(id : String) : Poll?

    suspend fun create(poll : Poll) : Poll

    suspend fun update(poll : Poll) : Poll
}