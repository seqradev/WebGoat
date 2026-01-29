/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge7

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.challenges.Email
import org.owasp.webgoat.lessons.challenges.Flags
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime

@RestController
class Assignment7(
    private val flags: Flags,
    private val restTemplate: RestTemplate,
    @Value("\${webwolf.mail.url}") private val webWolfMailURL: String,
) : AssignmentEndpoint {
    @GetMapping("/challenge/7/reset-password/{link}")
    fun resetPassword(
        @PathVariable("link") link: String,
    ): ResponseEntity<String> =
        if (link == ADMIN_PASSWORD_LINK) {
            ResponseEntity
                .accepted()
                .body(
                    """
                    <h1>Success!!</h1>
                    <img src='/WebGoat/images/hi-five-cat.jpg'>
                    <br/><br/>Here is your flag: ${flags.getFlag(7)}
                    """.trimIndent(),
                )
        } else {
            ResponseEntity
                .status(HttpStatus.I_AM_A_TEAPOT)
                .body("That is not the reset link for admin")
        }

    @PostMapping("/challenge/7")
    @ResponseBody
    fun sendPasswordResetLink(
        @RequestParam email: String,
        request: HttpServletRequest,
    ): AttackResult {
        if (email.isNotBlank()) {
            val username = email.substringBefore("@")
            if (username.isNotBlank()) {
                val uri = URI(request.requestURL.toString())
                val resetLink = PasswordResetLink().createPasswordReset(username, "webgoat")
                val mail =
                    Email
                        .builder()
                        .title("Your password reset link for challenge 7")
                        .contents(TEMPLATE.format("${uri.scheme}://${uri.host}", resetLink))
                        .sender("password-reset@webgoat-cloud.net")
                        .recipient(username)
                        .time(LocalDateTime.now())
                        .build()
                restTemplate.postForEntity(webWolfMailURL, mail, Any::class.java)
            }
        }
        return success(this).feedback("email.send").feedbackArgs(email).build()
    }

    @GetMapping(value = ["/challenge/7/.git"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    fun git(): ClassPathResource = ClassPathResource("lessons/challenges/challenge7/git.zip")

    companion object {
        const val ADMIN_PASSWORD_LINK: String = "375afe1104f4a487a73823c50a9292a2"

        private const val TEMPLATE =
            "Hi, you requested a password reset link, please use this <a target='_blank'" +
                " href='%s:8080/WebGoat/challenge/7/reset-password/%s'>link</a> to reset your" +
                " password.\n" +
                " \n\n" +
                "If you did not request this password change you can ignore this message.\n" +
                "If you have any comments or questions, please do not hesitate to reach us at" +
                " support@webgoat-cloud.org\n\n" +
                "Kind regards, \n" +
                "Team WebGoat"
    }
}
