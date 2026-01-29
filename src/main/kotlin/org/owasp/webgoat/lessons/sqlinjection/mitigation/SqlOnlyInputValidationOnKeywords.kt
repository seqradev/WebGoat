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
    value = [
        "SqlOnlyInputValidationOnKeywords-1",
        "SqlOnlyInputValidationOnKeywords-2",
        "SqlOnlyInputValidationOnKeywords-3",
    ],
)
class SqlOnlyInputValidationOnKeywords(
    private val lesson6a: SqlInjectionLesson6a,
) : AssignmentEndpoint {
    @PostMapping("/SqlOnlyInputValidationOnKeywords/attack")
    @ResponseBody
    fun attack(
        @RequestParam("userid_sql_only_input_validation_on_keywords") userId: String,
    ): AttackResult {
        var filteredUserId = userId.uppercase().replace("FROM", "").replace("SELECT", "")
        if (filteredUserId.contains(" ")) {
            return failed(this).feedback("SqlOnlyInputValidationOnKeywords-failed").build()
        }
        val attackResult = lesson6a.injectableQuery(filteredUserId)
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
