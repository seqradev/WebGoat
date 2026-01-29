/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.hijacksession

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.hijacksession.cas.Authentication
import org.owasp.webgoat.lessons.hijacksession.cas.HijackSessionAuthenticationProvider
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "hijacksession.hints.1",
    "hijacksession.hints.2",
    "hijacksession.hints.3",
    "hijacksession.hints.4",
    "hijacksession.hints.5",
)
class HijackSessionAssignment(
    private val provider: HijackSessionAuthenticationProvider,
) : AssignmentEndpoint {
    @PostMapping(path = ["/HijackSession/login"])
    @ResponseBody
    fun login(
        @RequestParam username: String,
        @RequestParam password: String,
        @CookieValue(value = COOKIE_NAME, required = false) cookieValue: String?,
        response: HttpServletResponse,
    ): AttackResult {
        val authentication: Authentication
        if (cookieValue.isNullOrEmpty()) {
            authentication =
                provider.authenticate(
                    Authentication
                        .builder()
                        .name(username)
                        .credentials(password)
                        .build(),
                )
            setCookie(response, authentication.id)
        } else {
            authentication = provider.authenticate(Authentication.builder().id(cookieValue).build())
        }

        return if (authentication.isAuthenticated) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    private fun setCookie(
        response: HttpServletResponse,
        cookieValue: String?,
    ) {
        response.addCookie(
            Cookie(COOKIE_NAME, cookieValue).apply {
                path = "/WebGoat"
                secure = true
            },
        )
    }

    companion object {
        private const val COOKIE_NAME = "hijack_cookie"
    }
}
