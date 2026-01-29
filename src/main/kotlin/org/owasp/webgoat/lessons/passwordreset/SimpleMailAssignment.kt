/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@RestController
class SimpleMailAssignment(
    private val restTemplate: RestTemplate,
    @Value("\${webwolf.mail.url}") private val webWolfURL: String,
) : AssignmentEndpoint {
    @PostMapping(
        path = ["/PasswordReset/simple-mail"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    @ResponseBody
    fun login(
        @RequestParam email: String?,
        @RequestParam password: String?,
        @CurrentUsername webGoatUsername: String?,
    ): AttackResult {
        val emailAddress = email ?: "unknown@webgoat.org"
        val username = extractUsername(emailAddress)

        return if (username == webGoatUsername && username.reversed() == password) {
            success(this).build()
        } else {
            failed(this).feedbackArgs("password-reset-simple.password_incorrect").build()
        }
    }

    @PostMapping(
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        value = ["/PasswordReset/simple-mail/reset"],
    )
    @ResponseBody
    fun resetPassword(
        @RequestParam emailReset: String?,
        @CurrentUsername username: String?,
    ): AttackResult {
        val email = emailReset ?: "unknown@webgoat.org"
        return sendEmail(extractUsername(email), email, username)
    }

    private fun extractUsername(email: String): String = email.substringBefore("@")

    private fun sendEmail(
        username: String,
        email: String,
        webGoatUsername: String?,
    ): AttackResult {
        if (username == webGoatUsername) {
            val mailEvent =
                PasswordResetEmail
                    .builder()
                    .recipient(username)
                    .title("Simple e-mail assignment")
                    .time(LocalDateTime.now())
                    .contents("Thanks for resetting your password, your new password is: ${username.reversed()}")
                    .sender("webgoat@owasp.org")
                    .build()
            try {
                restTemplate.postForEntity(webWolfURL, mailEvent, Any::class.java)
            } catch (e: RestClientException) {
                return informationMessage(this)
                    .feedback("password-reset-simple.email_failed")
                    .output(e.message)
                    .build()
            }
            return informationMessage(this)
                .feedback("password-reset-simple.email_send")
                .feedbackArgs(email)
                .build()
        } else {
            return informationMessage(this)
                .feedback("password-reset-simple.email_mismatch")
                .feedbackArgs(username)
                .build()
        }
    }
}
