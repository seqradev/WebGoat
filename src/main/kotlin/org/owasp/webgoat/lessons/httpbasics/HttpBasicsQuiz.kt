/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.httpbasics

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "http-basics.hints.http_basic_quiz.1",
    "http-basics.hints.http_basic_quiz.2",
    "http-basics.hints.http_basic_quiz.3",
)
class HttpBasicsQuiz : AssignmentEndpoint {
    @PostMapping("/HttpBasics/attack2")
    @ResponseBody
    fun completed(
        @RequestParam answer: String,
        @RequestParam magic_answer: String,
        @RequestParam magic_num: String,
    ): AttackResult =
        when {
            !"POST".equals(answer, ignoreCase = true) ->
                failed(this).feedback("http-basics.incorrect").build()
            magic_answer != magic_num ->
                failed(this).feedback("http-basics.magic").build()
            else -> success(this).build()
        }
}
