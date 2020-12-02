package com.rankweis.rc.rcgo.generators

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.util.*

@Component
class IdGenerator {

    companion object {
        private val ANIMALS = ClassPathResource("dict/animals.txt")
                .file
                .readLines()
                .map { it.toLowerCase() }
                .drop(1)
        private val NOUNS = ClassPathResource("dict/nouns.txt")
                .file
                .readLines()
                .map { it.toLowerCase() }
                .drop(1)
        val ADJECTIVES = ClassPathResource("dict/adjectives.txt")
                .file
                .readLines()
                .map { it.toLowerCase() }
                .drop(1)
        val ADJECTIVES_OR_NOUNS = listOf(ANIMALS, NOUNS)
    }

    val random  = Random()

    fun generateWordId(): String {
        val nounSet = ADJECTIVES_OR_NOUNS[random.nextInt(ADJECTIVES_OR_NOUNS.size)]
        val randomNoun = nounSet[random.nextInt(nounSet.size)]
        val randomAdjective = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
        val randomDigit = 10 + random.nextInt(90)
        return "$randomAdjective-$randomNoun-$randomDigit"
    }
}