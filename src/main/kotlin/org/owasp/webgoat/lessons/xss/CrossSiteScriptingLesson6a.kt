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
        "xss-reflected-6a-hint-1",
        "xss-reflected-6a-hint-2",
        "xss-reflected-6a-hint-3",
        "xss-reflected-6a-hint-4",
    ],
)
class CrossSiteScriptingLesson6a(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/attack6a")
    @ResponseBody
    fun completed(
        @RequestParam DOMTestRoute: String,
    ): AttackResult =
        if (DOMTestRoute.matches(Regex("start\\.mvc#test(\\/|)"))) {
            success(this).feedback("xss-reflected-6a-success").build()
        } else {
            failed(this).feedback("xss-reflected-6a-failure").build()
        }
}
