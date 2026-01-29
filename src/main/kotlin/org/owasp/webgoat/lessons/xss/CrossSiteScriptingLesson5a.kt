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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    value = [
        "xss-reflected-5a-hint-1",
        "xss-reflected-5a-hint-2",
        "xss-reflected-5a-hint-3",
        "xss-reflected-5a-hint-4",
    ],
)
class CrossSiteScriptingLesson5a(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @GetMapping("/CrossSiteScripting/attack5a")
    @ResponseBody
    fun completed(
        @RequestParam QTY1: Int,
        @RequestParam QTY2: Int,
        @RequestParam QTY3: Int,
        @RequestParam QTY4: Int,
        @RequestParam field1: String,
        @RequestParam field2: String,
    ): AttackResult {
        if (XSS_PATTERN.matches(field2)) {
            return failed(this).feedback("xss-reflected-5a-failed-wrong-field").build()
        }

        val totalSale = QTY1 * 69.99 + QTY2 * 27.99 + QTY3 * 1599.99 + QTY4 * 299.99

        userSessionData.setValue("xss-reflected1-complete", "false")
        val cart =
            StringBuilder().apply {
                append("Thank you for shopping at WebGoat. <br />Your support is appreciated<hr />")
                append("<p>We have charged credit card:$field1<br />")
                append("                             ------------------- <br />")
                append("                               $$totalSale")
            }

        // init state
        if (userSessionData.getValue("xss-reflected1-complete") == null) {
            userSessionData.setValue("xss-reflected1-complete", "false")
        }

        return if (XSS_PATTERN.matches(field1)) {
            userSessionData.setValue("xss-reflected-5a-complete", "true")
            if (field1.lowercase().contains("console.log")) {
                success(this)
                    .feedback("xss-reflected-5a-success-console")
                    .output(cart.toString())
                    .build()
            } else {
                success(this)
                    .feedback("xss-reflected-5a-success-alert")
                    .output(cart.toString())
                    .build()
            }
        } else {
            userSessionData.setValue("xss-reflected1-complete", "false")
            failed(this).feedback("xss-reflected-5a-failure").output(cart.toString()).build()
        }
    }

    companion object {
        @JvmField
        val XSS_PATTERN: Regex =
            """.*<script>(console\.log|alert)\(.*\);?</script>.*"""
                .toRegex(RegexOption.IGNORE_CASE)
    }
}
