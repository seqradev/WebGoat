/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cia

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
class CIAQuiz : AssignmentEndpoint {
    private val solutions = arrayOf("Solution 3", "Solution 1", "Solution 4", "Solution 2")
    var guesses = BooleanArray(solutions.size)

    @PostMapping("/cia/quiz")
    @ResponseBody
    fun completed(
        @RequestParam question_0_solution: Array<String>,
        @RequestParam question_1_solution: Array<String>,
        @RequestParam question_2_solution: Array<String>,
        @RequestParam question_3_solution: Array<String>,
    ): AttackResult {
        val givenAnswers =
            listOf(
                question_0_solution[0],
                question_1_solution[0],
                question_2_solution[0],
                question_3_solution[0],
            )

        givenAnswers.zip(solutions.toList()).forEachIndexed { index, (given, solution) ->
            guesses[index] = given.contains(solution)
        }

        return if (guesses.all { it }) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    @GetMapping("/cia/quiz")
    @ResponseBody
    fun getResults(): BooleanArray = guesses
}
