/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class JWTQuiz : AssignmentEndpoint {
    private val solutions = arrayOf("Solution 1", "Solution 2")
    private val guesses = BooleanArray(solutions.size)

    @PostMapping("/JWT/quiz")
    @ResponseBody
    fun completed(
        @RequestParam question_0_solution: Array<String>,
        @RequestParam question_1_solution: Array<String>,
    ): AttackResult {
        val givenAnswers = arrayOf(question_0_solution[0], question_1_solution[0])

        givenAnswers.forEachIndexed { i, answer ->
            guesses[i] = answer.contains(solutions[i])
        }

        return if (guesses.all { it }) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    @GetMapping("/JWT/quiz")
    @ResponseBody
    fun getResults(): BooleanArray = guesses
}
