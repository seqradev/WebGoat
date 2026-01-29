/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CrossSiteScriptingLesson1 : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/attack1")
    @ResponseBody
    fun completed(
        @RequestParam(value = "checkboxAttack1", required = false) checkboxValue: String?,
    ): AttackResult =
        if (checkboxValue != null) {
            success(this).build()
        } else {
            failed(this).feedback("xss.lesson1.failure").build()
        }
}
