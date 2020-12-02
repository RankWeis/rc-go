package com.rankweis.rc.rcgo.exceptions

import org.springframework.http.HttpStatus

class PollException : Exception {
    val httpStatus: HttpStatus

    constructor(status: HttpStatus, message: String) : super(message) {
        this.httpStatus = status
    }

    constructor(status: HttpStatus) {
        this.httpStatus = status
    }
}
