/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.UUID

@RestController
class ResetLinkAssignmentForgotPassword(
    private val restTemplate: RestTemplate,
    @Value("\${webwolf.host}") private val webWolfHost: String,
    @Value("\${webwolf.port}") private val webWolfPort: String,
    @Value("\${webwolf.url}") private val webWolfURL: String,
    @Value("\${webwolf.mail.url}") private val webWolfMailURL: String,
) : AssignmentEndpoint {
    @PostMapping("/PasswordReset/ForgotPassword/create-password-reset-link")
    @ResponseBody
    fun sendPasswordResetLink(
        @RequestParam email: String,
        request: HttpServletRequest,
        @CurrentUsername username: String?,
    ): AttackResult {
        val resetLink = UUID.randomUUID().toString()
        ResetLinkAssignment.resetLinks.add(resetLink)
        val host = request.getHeader(HttpHeaders.HOST)

        if (ResetLinkAssignment.TOM_EMAIL == email &&
            host.contains(webWolfPort) &&
            host.contains(webWolfHost)
        ) {
            // User indeed changed the host header.
            ResetLinkAssignment.userToTomResetLink[username] = resetLink
            fakeClickingLinkEmail(webWolfURL, resetLink)
        } else {
            try {
                sendMailToUser(email, host, resetLink)
            } catch (e: Exception) {
                return failed(this).output("E-mail can't be send. please try again.").build()
            }
        }

        return success(this).feedback("email.send").feedbackArgs(email).build()
    }

    private fun sendMailToUser(
        email: String,
        host: String,
        resetLink: String,
    ) {
        val username = email.substringBefore("@")
        val mail =
            PasswordResetEmail
                .builder()
                .title("Your password reset link")
                .contents(ResetLinkAssignment.TEMPLATE.format(host, resetLink))
                .sender("password-reset@webgoat-cloud.net")
                .recipient(username)
                .build()
        restTemplate.postForEntity(webWolfMailURL, mail, Any::class.java)
    }

    private fun fakeClickingLinkEmail(
        webWolfURL: String,
        resetLink: String,
    ) {
        try {
            val httpHeaders = HttpHeaders()
            val httpEntity = HttpEntity<Any>(httpHeaders)
            RestTemplate()
                .exchange(
                    "$webWolfURL/PasswordReset/reset/reset-password/$resetLink",
                    HttpMethod.GET,
                    httpEntity,
                    Void::class.java,
                )
        } catch (e: Exception) {
            // don't care
        }
    }
}
