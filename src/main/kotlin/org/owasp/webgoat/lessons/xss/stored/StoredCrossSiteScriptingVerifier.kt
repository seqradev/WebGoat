/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss.stored

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
class StoredCrossSiteScriptingVerifier(
    private val lessonSession: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/CrossSiteScriptingStored/stored-xss-follow-up")
    @ResponseBody
    fun completed(
        @RequestParam successMessage: String,
    ): AttackResult =
        if (successMessage == lessonSession.getValue("randValue")) {
            success(this).feedback("xss-stored-callback-success").build()
        } else {
            failed(this).feedback("xss-stored-callback-failure").build()
        }
}
