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
 * Mitigation assignment: demonstrate that an attempted external redirect would be blocked and
 * rewritten to a safe internal destination.
 */
@RestController
@AssignmentHints("openredirect.mitigation.hint1", "openredirect.mitigation.hint2")
class OpenRedirectMitigationCheck : AssignmentEndpoint {
    @PostMapping("/OpenRedirect/mitigation")
    @ResponseBody
    fun check(
        @RequestParam("url") url: String?,
    ): AttackResult {
        if (url.isNullOrBlank()) {
            return failed(this).feedback("openredirect.mitigation.failure").output("Empty value").build()
        }
        val absolute = url.startsWith("http://") || url.startsWith("https://")
        if (!absolute) {
            return failed(this)
                .feedback("openredirect.mitigation.failure")
                .output("Provide an absolute external URL (http/https)")
                .build()
        }
        return try {
            val u = URI(url)
            val host = u.host
            if (host == null) {
                return failed(this).feedback("openredirect.mitigation.failure").output("Host parse failed").build()
            }
            if (INTERNAL.contains(host.lowercase())) {
                return failed(this)
                    .feedback("openredirect.mitigation.failure")
                    .output("This host is internal, show a blocked external attempt instead")
                    .build()
            }
            // Simulated mitigation: external target rejected, internal safe path chosen
            val safe = "/home"
            val output =
                "Attempted external host: ${esc(host)} blocked. " +
                    "Application would redirect to safe internal path: ${esc(safe)}"
            success(this).feedback("openredirect.mitigation.success").output(output).build()
        } catch (e: URISyntaxException) {
            failed(this).feedback("openredirect.mitigation.failure").output("Invalid URL").build()
        }
    }

    private fun esc(s: String): String = s.replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        private val INTERNAL = setOf("webgoat.local", "localhost", "127.0.0.1")
    }
}
