/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

@RestController
class EncodingAssignment : AssignmentEndpoint {
    @GetMapping(path = ["/crypto/encoding/basic"], produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getBasicAuth(request: HttpServletRequest): String {
        var basicAuth = request.session.getAttribute("basicAuth") as? String
        val username = request.userPrincipal.name
        if (basicAuth == null) {
            val password = HashingAssignment.SECRETS.random()
            basicAuth = getBasicAuth(username, password)
            request.session.setAttribute("basicAuth", basicAuth)
        }
        return "Authorization: Basic $basicAuth"
    }

    @PostMapping("/crypto/encoding/basic-auth")
    @ResponseBody
    fun completed(
        request: HttpServletRequest,
        @RequestParam answer_user: String?,
        @RequestParam answer_pwd: String?,
    ): AttackResult {
        val basicAuth = request.session.getAttribute("basicAuth") as? String
        return if (basicAuth != null &&
            answer_user != null &&
            answer_pwd != null &&
            basicAuth == getBasicAuth(answer_user, answer_pwd)
        ) {
            success(this).feedback("crypto-encoding.success").build()
        } else {
            failed(this).feedback("crypto-encoding.empty").build()
        }
    }

    companion object {
        @JvmStatic
        fun getBasicAuth(
            username: String,
            password: String,
        ): String = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    }
}
