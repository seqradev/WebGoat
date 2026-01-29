/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.lessons.sqlinjection.advanced.SqlInjectionLesson6a
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    value = ["SqlOnlyInputValidation-1", "SqlOnlyInputValidation-2", "SqlOnlyInputValidation-3"],
)
class SqlOnlyInputValidation(
    private val lesson6a: SqlInjectionLesson6a,
) : AssignmentEndpoint {
    @PostMapping("/SqlOnlyInputValidation/attack")
    @ResponseBody
    fun attack(
        @RequestParam("userid_sql_only_input_validation") userId: String,
    ): AttackResult {
        if (userId.contains(" ")) {
            return failed(this).feedback("SqlOnlyInputValidation-failed").build()
        }
        val attackResult = lesson6a.injectableQuery(userId)
        return AttackResult(
            attackResult.lessonCompleted,
            attackResult.feedback,
            attackResult.feedbackArgs,
            attackResult.output,
            attackResult.outputArgs,
            javaClass.simpleName,
            true,
        )
    }
}
