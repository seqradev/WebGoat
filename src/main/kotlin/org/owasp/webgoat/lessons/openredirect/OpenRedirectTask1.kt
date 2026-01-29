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
 * Task 1: Basic open redirect. Application trusts user supplied URL and would redirect directly.
 */
@RestController
@AssignmentHints("openredirect.hint1", "openredirect.hint2")
class OpenRedirectTask1 : AssignmentEndpoint {
    @PostMapping("/OpenRedirect/task1")
    @ResponseBody
    fun simulate(
        @RequestParam("url") url: String?,
    ): AttackResult {
        if (url.isNullOrBlank()) {
            return failed(this).feedback("openredirect.failure1").output("Empty value").build()
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return failed(this).feedback("openredirect.failure1").output("Needs absolute URL with http/https").build()
        }
        return try {
            val u = URI(url)
            val host = u.host
            if (host == null) {
                return failed(this).feedback("openredirect.failure1").output("Host could not be determined").build()
            }
            if (INTERNAL_HOSTS.contains(host.lowercase())) {
                return failed(this).feedback("openredirect.failure1").output("Internal host: $host").build()
            }
            success(this).feedback("openredirect.success1").output("Would redirect to: ${escape(url)}").build()
        } catch (e: URISyntaxException) {
            failed(this).feedback("openredirect.failure1").output("Invalid URL").build()
        }
    }

    private fun escape(s: String): String = s.replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        private val INTERNAL_HOSTS = setOf("webgoat.local", "localhost", "127.0.0.1")
    }
}
