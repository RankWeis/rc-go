package com.rankweis.rc.rcgo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(scanBasePackages = ["com.rankweis.rc.rcgo"])
class RcGoApplication

fun main(args: Array<String>) {
    runApplication<RcGoApplication>(*args)
}
