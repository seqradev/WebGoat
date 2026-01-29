/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.chromedevtools

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class NetworkDummy(
    private val lessonSession: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/ChromeDevTools/dummy")
    @ResponseBody
    fun completed(
        @RequestParam successMessage: String?,
    ): AttackResult {
        val answer = lessonSession.getValue("randValue") as? String

        return if (successMessage != null && successMessage == answer) {
            success(this).feedback("xss-dom-message-success").build()
        } else {
            failed(this).feedback("xss-dom-message-failure").build()
        }
    }
}
