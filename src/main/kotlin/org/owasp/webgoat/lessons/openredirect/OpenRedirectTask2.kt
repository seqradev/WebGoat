/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URISyntaxException

/**
 * Task 2: Naive filter bypass. Developer added a substring check 'webgoat'.
 * Success if the supplied URL contains 'webgoat' substring but host resolves to external domain.
 */
@RestController
@AssignmentHints("openredirect.hint3", "openredirect.hint4")
class OpenRedirectTask2 : AssignmentEndpoint {
    @PostMapping("/OpenRedirect/task2")
    @ResponseBody
    fun simulate(
        @RequestParam("url") url: String?,
    ): AttackResult {
        if (url.isNullOrBlank()) {
            return failed(this).feedback("openredirect.failure2").output("Empty value").build()
        }
        if (!url.contains("webgoat")) {
            return failed(this).feedback("openredirect.failure2").output("Must contain 'webgoat'").build()
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return failed(this).feedback("openredirect.failure2").output("Needs absolute URL with http/https").build()
        }
        return try {
            val u = URI(url)
            val host = u.host
            if (host == null) {
                return failed(this).feedback("openredirect.failure2").output("Could not parse host").build()
            }
            if (!ALLOWED.contains(host.lowercase())) {
                success(this)
                    .feedback("openredirect.success2")
                    .output("Bypassed naive filter - host: ${escape(host)}")
                    .build()
            } else {
                failed(this)
                    .feedback("openredirect.failure2")
                    .output("Host still allowed: ${escape(host)}")
                    .build()
            }
        } catch (e: URISyntaxException) {
            failed(this).feedback("openredirect.failure2").output("Invalid URL").build()
        }
    }

    private fun escape(s: String): String = s.replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        private val ALLOWED = setOf("webgoat.local", "webgoat.org", "localhost")
    }
}
