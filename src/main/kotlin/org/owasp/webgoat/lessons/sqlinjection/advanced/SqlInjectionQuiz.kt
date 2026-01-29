/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * add a question: 1. Append new question to JSON string 2. add right solution to solutions array 3.
 * add Request param with name of question to method head For a more detailed description how to
 * implement the quiz go to the quiz.js file in webgoat-container -> js
 */
@RestController
class SqlInjectionQuiz : AssignmentEndpoint {
    private val solutions = arrayOf("Solution 4", "Solution 3", "Solution 2", "Solution 3", "Solution 4")
    private val guesses = BooleanArray(solutions.size)

    @PostMapping("/SqlInjectionAdvanced/quiz")
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

        givenAnswers.forEachIndexed { index, answer ->
            guesses[index] = answer.contains(solutions[index])
        }

        return if (guesses.all { it }) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    @GetMapping("/SqlInjectionAdvanced/quiz")
    @ResponseBody
    fun getResults(): BooleanArray = guesses
}
