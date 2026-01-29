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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Task 3 (Challenge): Demonstrates flawed normalization logic using startsWith on the raw host
 * portion which can be bypassed using userinfo (@) or crafted subdomains.
 */
@RestController
@AssignmentHints("openredirect.hint5", "openredirect.hint6")
class OpenRedirectTask3 : AssignmentEndpoint {
    @PostMapping("/OpenRedirect/task3")
    @ResponseBody
    fun challenge(
        @RequestParam("target") target: String?,
        @RequestParam(value = "token", required = false) token: String?,
    ): AttackResult {
        if (target.isNullOrBlank()) {
            return failed(this).feedback("openredirect.failure3").output("Empty value").build()
        }
        val decoded = URLDecoder.decode(target, StandardCharsets.UTF_8)
        val lower = decoded.lowercase()
        // Vulnerable heuristic: treat anything starting with protocol + webgoat.local as internal
        val appearsInternal =
            lower.startsWith("http://webgoat.local") || lower.startsWith("https://webgoat.local")

        val uri: URI
        try {
            uri = URI(decoded)
        } catch (e: URISyntaxException) {
            return failed(this).feedback("openredirect.failure3").output("Invalid URL").build()
        }

        val realHost = uri.host
        val debug =
            buildString {
                appendLine("Raw: ${escape(target)}")
                appendLine("Decoded: ${escape(decoded)}")
                appendLine("AppearsInternal: $appearsInternal")
                appendLine("RealHost: ${escape(realHost.toString())}")
                if (token != null) {
                    appendLine("Token: ${escape(token)}")
                }
            }

        return if (appearsInternal && realHost != null && !INTERNAL_HOSTS.contains(realHost.lowercase())) {
            success(this)
                .feedback("openredirect.success3")
                .output(debug + "Bypassed flawed normalization, real host external")
                .build()
        } else {
            failed(this)
                .feedback("openredirect.failure3")
                .output(debug + "Did not bypass. Provide host confusion payload (try userinfo @).")
                .build()
        }
    }

    private fun escape(s: String): String = s.replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        private val INTERNAL_HOSTS = setOf("webgoat.local", "localhost", "127.0.0.1")
    }
}
