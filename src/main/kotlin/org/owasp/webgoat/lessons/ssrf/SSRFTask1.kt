/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.ssrf

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
@AssignmentHints("ssrf.hint1", "ssrf.hint2")
class SSRFTask1 : AssignmentEndpoint {
    @PostMapping("/SSRF/task1")
    @ResponseBody
    fun completed(
        @RequestParam url: String,
    ): AttackResult = stealTheCheese(url)

    protected fun stealTheCheese(url: String): AttackResult {
        try {
            val html = StringBuilder()

            return if (url.matches(Regex("images/tom\\.png"))) {
                html.append(
                    "<img class=\"image\" alt=\"Tom\" src=\"images/tom.png\" width=\"25%\" height=\"25%\">",
                )
                failed(this).feedback("ssrf.tom").output(html.toString()).build()
            } else if (url.matches(Regex("images/jerry\\.png"))) {
                html.append(
                    "<img class=\"image\" alt=\"Jerry\" src=\"images/jerry.png\" width=\"25%\" height=\"25%\">",
                )
                success(this).feedback("ssrf.success").output(html.toString()).build()
            } else {
                html.append("<img class=\"image\" alt=\"Silly Cat\" src=\"images/cat.jpg\">")
                failed(this).feedback("ssrf.failure").output(html.toString()).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return failed(this).output(e.message).build()
        }
    }
}
