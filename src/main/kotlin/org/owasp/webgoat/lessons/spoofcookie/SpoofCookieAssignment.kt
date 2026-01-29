/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.spoofcookie

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.spoofcookie.encoders.EncDec
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@AssignmentHints("spoofcookie.hint1", "spoofcookie.hint2", "spoofcookie.hint3")
@RestController
class SpoofCookieAssignment : AssignmentEndpoint {
    @PostMapping(path = ["/SpoofCookie/login"])
    @ResponseBody
    @ExceptionHandler(UnsatisfiedServletRequestParameterException::class)
    fun login(
        @RequestParam username: String,
        @RequestParam password: String,
        @CookieValue(value = COOKIE_NAME, required = false) cookieValue: String?,
        response: HttpServletResponse,
    ): AttackResult =
        if (cookieValue.isNullOrEmpty()) {
            credentialsLoginFlow(username, password, response)
        } else {
            cookieLoginFlow(cookieValue)
        }

    @GetMapping(path = ["/SpoofCookie/cleanup"])
    fun cleanup(response: HttpServletResponse) {
        Cookie(COOKIE_NAME, "").apply { maxAge = 0 }.also { response.addCookie(it) }
    }

    private fun credentialsLoginFlow(
        username: String,
        password: String,
        response: HttpServletResponse,
    ): AttackResult {
        val lowerCasedUsername = username.lowercase()
        if (ATTACK_USERNAME == lowerCasedUsername && users[lowerCasedUsername] == password) {
            return informationMessage(this).feedback("spoofcookie.cheating").build()
        }

        val authPassword = users.getOrDefault(lowerCasedUsername, "")
        if (authPassword.isNotBlank() && authPassword == password) {
            val newCookieValue = EncDec.encode(lowerCasedUsername)
            val newCookie = Cookie(COOKIE_NAME, newCookieValue)
            newCookie.path = "/WebGoat"
            newCookie.secure = true
            response.addCookie(newCookie)
            return informationMessage(this)
                .feedback("spoofcookie.login")
                .output("Cookie details for user $lowerCasedUsername:<br />$COOKIE_NAME=${newCookie.value}")
                .build()
        }

        return informationMessage(this).feedback("spoofcookie.wrong-login").build()
    }

    private fun cookieLoginFlow(cookieValue: String): AttackResult {
        val cookieUsername: String =
            try {
                EncDec.decode(cookieValue)?.lowercase()
                    ?: return failed(this).output("Failed to decode cookie").build()
            } catch (e: Exception) {
                // for providing some instructive guidance, we won't return 4xx error here
                return failed(this).output(e.message).build()
            }
        if (users.containsKey(cookieUsername)) {
            if (cookieUsername == ATTACK_USERNAME) {
                return success(this).build()
            }
            return failed(this)
                .feedback("spoofcookie.cookie-login")
                .output("Cookie details for user $cookieUsername:<br />$COOKIE_NAME=$cookieValue")
                .build()
        }

        return failed(this).feedback("spoofcookie.wrong-cookie").build()
    }

    companion object {
        private const val COOKIE_NAME = "spoof_auth"
        private const val ATTACK_USERNAME = "tom"

        private val users =
            mapOf(
                "webgoat" to "webgoat",
                "admin" to "admin",
                ATTACK_USERNAME to "apasswordfortom",
            )
    }
}
