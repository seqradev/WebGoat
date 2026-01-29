/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

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
    value = [
        "xss-dom-message-hint-1",
        "xss-dom-message-hint-2",
        "xss-dom-message-hint-3",
        "xss-dom-message-hint-4",
        "xss-dom-message-hint-5",
        "xss-dom-message-hint-6",
    ],
)
class DOMCrossSiteScriptingVerifier(
    private val lessonSession: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/dom-follow-up")
    @ResponseBody
    fun completed(
        @RequestParam successMessage: String,
    ): AttackResult {
        val answer = lessonSession.getValue("randValue") as? String

        return if (successMessage == answer) {
            success(this).feedback("xss-dom-message-success").build()
        } else {
            failed(this).feedback("xss-dom-message-failure").build()
        }
    }
}
