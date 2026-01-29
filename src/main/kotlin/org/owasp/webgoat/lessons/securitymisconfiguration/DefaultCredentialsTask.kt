/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/** Task demonstrating exploitation of default credentials. */
@RestController
@AssignmentHints(
    "securitymisconfiguration.task1.hint1",
    "securitymisconfiguration.task1.hint2",
)
class DefaultCredentialsTask : AssignmentEndpoint {
    @PostMapping(
        value = ["/SecurityMisconfiguration/task1"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    @ResponseBody
    fun login(
        @RequestParam(value = "username", required = false) username: String?,
        @RequestParam(value = "password", required = false) password: String?,
    ): AttackResult =
        when {
            username.isNullOrBlank() || password.isNullOrBlank() ->
                failed(this)
                    .feedback("securitymisconfiguration.task1.failure.blank")
                    .build()
            DEFAULT_USERNAME == username?.trim() && DEFAULT_PASSWORD == password ->
                success(this)
                    .feedback("securitymisconfiguration.task1.success")
                    .output("User profile: staging admin (no MFA)")
                    .build()
            else ->
                failed(this)
                    .feedback("securitymisconfiguration.task1.failure.invalid")
                    .build()
        }

    companion object {
        private const val DEFAULT_USERNAME = "admin"
        private const val DEFAULT_PASSWORD = "admin"
    }
}
