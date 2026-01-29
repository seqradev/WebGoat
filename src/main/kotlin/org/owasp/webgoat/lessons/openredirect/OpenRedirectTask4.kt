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
 * Task 4: Double-encoding / partial normalization bypass.
 *
 * The (flawed) logic only decodes once and validates that the resulting string appears to target
 * the internal host webgoat.local. A second decode would reveal an attacker controlled host via a
 * userinfo (@) trick or other transformation.
 *
 * Goal: Provide a value which after ONE decode still looks internal (prefix starts with
 * https://webgoat.local) but after TWO decodes the real parsed host is EXTERNAL and not in the
 * internal host list. Example payload: https://webgoat.local%2540evil.com which decodes once to
 * https://webgoat.local%40evil.com (still internal) and twice to https://webgoat.local@evil.com
 */
@RestController
@AssignmentHints("openredirect.hint7", "openredirect.hint8")
class OpenRedirectTask4 : AssignmentEndpoint {
    @PostMapping("/OpenRedirect/task4")
    @ResponseBody
    fun doubleDecode(
        @RequestParam("target") target: String?,
    ): AttackResult {
        if (target.isNullOrBlank()) {
            return failed(this).feedback("openredirect.failure4").output("Empty value").build()
        }

        // First decode (application does this)
        val firstDecoded = URLDecoder.decode(target, StandardCharsets.UTF_8)
        val lower = firstDecoded.lowercase()
        val appearsInternal =
            lower.startsWith("https://webgoat.local") || lower.startsWith("http://webgoat.local")

        val firstUri: URI
        try {
            firstUri = URI(firstDecoded)
        } catch (e: URISyntaxException) {
            return failed(this).feedback("openredirect.failure4").output("Invalid URL after first decode").build()
        }

        val firstHost = firstUri.host

        // Second decode (what some downstream component might accidentally do)
        val secondDecoded = URLDecoder.decode(firstDecoded, StandardCharsets.UTF_8)
        val secondUri: URI
        try {
            secondUri = URI(secondDecoded)
        } catch (e: URISyntaxException) {
            return failed(this).feedback("openredirect.failure4").output("Invalid URL after second decode").build()
        }
        val secondHost = secondUri.host

        val debug =
            buildString {
                appendLine("Raw: ${esc(target)}")
                appendLine("1st decode: ${esc(firstDecoded)}")
                appendLine("1st host: ${esc(firstHost ?: "null")}")
                appendLine("AppearsInternalAfter1: $appearsInternal")
                appendLine("2nd decode: ${esc(secondDecoded)}")
                appendLine("2nd host: ${esc(secondHost ?: "null")}")
            }

        val secondHostExternal = secondHost != null && !INTERNAL_HOSTS.contains(secondHost.lowercase())
        val hostChanged = secondHost != null && (firstHost == null || !firstHost.equals(secondHost, ignoreCase = true))

        return if (appearsInternal && secondHostExternal && hostChanged) {
            success(this)
                .feedback("openredirect.success4")
                .output(debug + "Double decode reveals external host")
                .build()
        } else {
            failed(this)
                .feedback("openredirect.failure4")
                .output(debug + "Bypass not achieved. Use %25 encoding to hide '@' or other host change.")
                .build()
        }
    }

    private fun esc(s: String): String = s.replace("<", "&lt;").replace(">", "&gt;")

    companion object {
        private val INTERNAL_HOSTS = setOf("webgoat.local", "localhost", "127.0.0.1")
    }
}
