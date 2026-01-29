/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

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
class CrossSiteScriptingQuiz : AssignmentEndpoint {
    private val solutions = arrayOf("Solution 4", "Solution 3", "Solution 1", "Solution 2", "Solution 4")
    private val guesses = BooleanArray(solutions.size)

    @PostMapping("/CrossSiteScripting/quiz")
    @ResponseBody
    fun completed(
        @RequestParam question_0_solution: Array<String>,
        @RequestParam question_1_solution: Array<String>,
        @RequestParam question_2_solution: Array<String>,
        @RequestParam question_3_solution: Array<String>,
        @RequestParam question_4_solution: Array<String>,
    ): AttackResult {
        val givenAnswers =
            arrayOf(
                question_0_solution[0],
                question_1_solution[0],
                question_2_solution[0],
                question_3_solution[0],
                question_4_solution[0],
            )

        givenAnswers.zip(solutions).forEachIndexed { i, (given, solution) ->
            guesses[i] = given.contains(solution)
        }
        val correctAnswers = guesses.count { it }

        return if (correctAnswers == solutions.size) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    @GetMapping("/CrossSiteScripting/quiz")
    @ResponseBody
    fun getResults(): BooleanArray = guesses
}
