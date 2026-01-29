/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/** Quiz assignment for the Open Redirect lesson. */
@RestController
class OpenRedirectQuiz : AssignmentEndpoint {
    // Correct solution indices mapped to their label prefix used by quiz.js ("Solution <index>")
    private val solutions = arrayOf("Solution 0", "Solution 2", "Solution 0", "Solution 0")
    private val guesses = BooleanArray(solutions.size)

    @PostMapping("/OpenRedirect/quiz")
    @ResponseBody
    fun submit(
        @RequestParam(name = "question_0_solution") q0: Array<String>,
        @RequestParam(name = "question_1_solution") q1: Array<String>,
        @RequestParam(name = "question_2_solution") q2: Array<String>,
        @RequestParam(name = "question_3_solution") q3: Array<String>,
    ): AttackResult {
        val given = arrayOf(q0[0], q1[0], q2[0], q3[0])
        given.zip(solutions).forEachIndexed { index, (answer, solution) ->
            guesses[index] = answer.contains(solution)
        }
        return if (guesses.all { it }) {
            success(this).feedback("openredirect.quiz.success").build()
        } else {
            failed(this).feedback("openredirect.quiz.failure").build()
        }
    }

    @GetMapping("/OpenRedirect/quiz")
    @ResponseBody
    fun results(): BooleanArray = guesses
}
