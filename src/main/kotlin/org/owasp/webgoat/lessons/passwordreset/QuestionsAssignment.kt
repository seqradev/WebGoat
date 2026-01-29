/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuestionsAssignment : AssignmentEndpoint {
    @PostMapping(
        path = ["/PasswordReset/questions"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    @ResponseBody
    fun passwordReset(
        @RequestParam json: Map<String, Any>,
    ): AttackResult {
        val securityQuestion = json.getOrDefault("securityQuestion", "") as String
        val username = json.getOrDefault("username", "") as String

        if ("webgoat".equals(username, ignoreCase = true)) {
            return failed(this).feedback("password-questions-wrong-user").build()
        }

        val validAnswer = COLORS[username.lowercase()]
        return when {
            validAnswer == null ->
                failed(this)
                    .feedback("password-questions-unknown-user")
                    .feedbackArgs(username)
                    .build()
            validAnswer == securityQuestion -> success(this).build()
            else -> failed(this).build()
        }
    }

    companion object {
        private val COLORS =
            mapOf(
                "admin" to "green",
                "jerry" to "orange",
                "tom" to "purple",
                "larry" to "yellow",
                "webgoat" to "red",
            )
    }
}
