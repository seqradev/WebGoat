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
@AssignmentHints("http-basics.hints.http_basics_lesson.1")
class HttpBasicsLesson : AssignmentEndpoint {
    @PostMapping("/HttpBasics/attack1")
    @ResponseBody
    fun completed(
        @RequestParam person: String,
    ): AttackResult =
        if (person.isNotBlank()) {
            success(this)
                .feedback("http-basics.reversed")
                .feedbackArgs(person.reversed())
                .build()
        } else {
            failed(this).feedback("http-basics.empty").build()
        }
}
