/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge8

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.lessons.challenges.Flags
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import kotlin.math.ceil

@RestController
class Assignment8(
    private val flags: Flags,
) : AssignmentEndpoint {
    @GetMapping(value = ["/challenge/8/vote/{stars}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun vote(
        @PathVariable("stars") nrOfStars: Int,
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        // Simple implementation of VERB Based Authentication
        if (request.method == "GET") {
            val json = mapOf("error" to true, "message" to "Sorry but you need to login first in order to vote")
            return ResponseEntity.status(200).body(json)
        }
        val allVotesForStar = votes.getOrDefault(nrOfStars, 0)
        votes[nrOfStars] = allVotesForStar + 1
        return ResponseEntity
            .ok()
            .header("X-FlagController", "Thanks for voting, your flag is: " + flags.getFlag(8))
            .build<Any>()
    }

    @GetMapping("/challenge/8/votes/")
    fun getVotes(): ResponseEntity<*> =
        ResponseEntity.ok(
            votes.entries.associate { (key, value) -> key.toString() to value },
        )

    @GetMapping("/challenge/8/votes/average")
    fun average(): ResponseEntity<Map<String, Int>> {
        val totalNumberOfVotes = votes.values.sum()
        val categories = votes.entries.sumOf { (key, value) -> key * value }
        val json = mapOf("average" to ceil(categories.toDouble() / totalNumberOfVotes).toInt())
        return ResponseEntity.ok(json)
    }

    @GetMapping("/challenge/8/notUsed")
    fun notUsed(): AttackResult = throw IllegalStateException("Should never be called, challenge specific method")

    companion object {
        private val votes =
            mutableMapOf(
                1 to 400,
                2 to 120,
                3 to 140,
                4 to 150,
                5 to 300,
            )
    }
}
