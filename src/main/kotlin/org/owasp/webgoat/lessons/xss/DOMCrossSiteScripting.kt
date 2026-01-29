/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

@RestController
class DOMCrossSiteScripting(
    private val lessonSession: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/phone-home-xss")
    @ResponseBody
    fun completed(
        @RequestParam param1: Int,
        @RequestParam param2: Int,
        request: HttpServletRequest,
    ): AttackResult {
        lessonSession.setValue("randValue", SecureRandom().nextInt().toString())

        return if (param1 == 42 && param2 == 24 && request.getHeader("webgoat-requested-by") == "dom-xss-vuln") {
            success(this)
                .output("phoneHome Response is ${lessonSession.getValue("randValue")}")
                .build()
        } else {
            failed(this).build()
        }
    }
}
