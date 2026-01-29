/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("csrf-get.hint1", "csrf-get.hint2", "csrf-get.hint3", "csrf-get.hint4")
class CSRFConfirmFlag1(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @PostMapping(
        path = ["/csrf/confirm-flag-1"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun completed(confirmFlagVal: String): AttackResult {
        val userSessionDataStr = userSessionData.getValue("csrf-get-success")
        return if (userSessionDataStr != null && confirmFlagVal == userSessionDataStr.toString()) {
            success(this)
                .feedback("csrf-get-null-referer.success")
                .output("Correct, the flag was ${userSessionData.getValue("csrf-get-success")}")
                .build()
        } else {
            failed(this).build()
        }
    }
}
