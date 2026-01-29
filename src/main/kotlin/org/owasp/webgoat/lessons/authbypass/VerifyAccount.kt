/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.authbypass

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "auth-bypass.hints.verify.1",
    "auth-bypass.hints.verify.2",
    "auth-bypass.hints.verify.3",
    "auth-bypass.hints.verify.4",
)
class VerifyAccount(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @PostMapping(
        path = ["/auth-bypass/verify-account"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun completed(
        @RequestParam userId: String,
        @RequestParam verifyMethod: String,
        req: HttpServletRequest,
    ): AttackResult {
        val verificationHelper = AccountVerificationHelper()
        val submittedAnswers = parseSecQuestions(req)

        if (verificationHelper.didUserLikelylCheat(submittedAnswers)) {
            return failed(this)
                .feedback("verify-account.cheated")
                .output("Yes, you guessed correctly, but see the feedback message")
                .build()
        }

        // else
        return if (verificationHelper.verifyAccount(userId.toInt(), submittedAnswers)) {
            userSessionData.setValue("account-verified-id", userId)
            success(this).feedback("verify-account.success").build()
        } else {
            failed(this).feedback("verify-account.failed").build()
        }
    }

    private fun parseSecQuestions(req: HttpServletRequest): Map<String, String> =
        req.parameterNames
            .toList()
            .filter { it.contains("secQuestion") }
            .associateWith { req.getParameter(it) }
}
