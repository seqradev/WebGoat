/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.htmltampering

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("hint1", "hint2", "hint3")
class HtmlTamperingTask : AssignmentEndpoint {
    @PostMapping("/HtmlTampering/task")
    @ResponseBody
    fun completed(
        @RequestParam QTY: String,
        @RequestParam Total: String,
    ): AttackResult =
        if (QTY.toFloat() * 2999.99f > Total.toFloat() + 1) {
            success(this).feedback("html-tampering.tamper.success").build()
        } else {
            failed(this).feedback("html-tampering.tamper.failure").build()
        }
}
