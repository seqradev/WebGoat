/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.webwolfintroduction

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@RestController
class MailAssignment(
    private val restTemplate: RestTemplate,
    @Value("\${webwolf.mail.url}") private val webWolfURL: String,
) : AssignmentEndpoint {
    @PostMapping("/WebWolf/mail/send")
    @ResponseBody
    fun sendEmail(
        @RequestParam email: String,
        @CurrentUsername webGoatUsername: String?,
    ): AttackResult {
        val username = email.substring(0, email.indexOf("@"))
        if (username.equals(webGoatUsername, ignoreCase = true)) {
            val mailEvent =
                Email
                    .builder()
                    .recipient(username)
                    .title("Test messages from WebWolf")
                    .contents(
                        "This is a test message from WebWolf, your unique code is: " +
                            username.reversed(),
                    ).sender("webgoat@owasp.org")
                    .build()
            try {
                restTemplate.postForEntity(webWolfURL, mailEvent, Any::class.java)
            } catch (e: RestClientException) {
                return informationMessage(this)
                    .feedback("webwolf.email_failed")
                    .output(e.message)
                    .build()
            }
            return informationMessage(this).feedback("webwolf.email_send").feedbackArgs(email).build()
        } else {
            return informationMessage(this)
                .feedback("webwolf.email_mismatch")
                .feedbackArgs(username)
                .build()
        }
    }

    @PostMapping("/WebWolf/mail")
    @ResponseBody
    fun completed(
        @RequestParam uniqueCode: String,
        @CurrentUsername username: String?,
    ): AttackResult =
        if (uniqueCode == username?.reversed()) {
            success(this).build()
        } else {
            failed(this).feedbackArgs("webwolf.code_incorrect").feedbackArgs(uniqueCode).build()
        }
}
