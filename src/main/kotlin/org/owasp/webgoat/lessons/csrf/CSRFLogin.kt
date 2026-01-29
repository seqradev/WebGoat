/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("csrf-login-hint1", "csrf-login-hint2", "csrf-login-hint3")
class CSRFLogin : AssignmentEndpoint {
    @PostMapping(
        path = ["/csrf/login"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun completed(
        @CurrentUsername username: String?,
    ): AttackResult =
        if (username?.startsWith("csrf") == true) {
            success(this).feedback("csrf-login-success").build()
        } else {
            failed(this).feedback("csrf-login-failed").feedbackArgs(username ?: "").build()
        }
}
