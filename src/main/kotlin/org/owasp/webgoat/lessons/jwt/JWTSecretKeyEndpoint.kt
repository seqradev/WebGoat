/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.TextCodec
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.Calendar
import java.util.Date

@RestController
@AssignmentHints("jwt-secret-hint1", "jwt-secret-hint2", "jwt-secret-hint3")
class JWTSecretKeyEndpoint : AssignmentEndpoint {
    @RequestMapping(path = ["/JWT/secret/gettoken"], produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getSecretToken(): String =
        Jwts
            .builder()
            .setIssuer("WebGoat Token Builder")
            .setAudience("webgoat.org")
            .setIssuedAt(Calendar.getInstance().time)
            .setExpiration(Date.from(Instant.now().plusSeconds(60)))
            .setSubject("tom@webgoat.org")
            .claim("username", "Tom")
            .claim("Email", "tom@webgoat.org")
            .claim("Role", arrayOf("Manager", "Project Administrator"))
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
            .compact()

    @PostMapping("/JWT/secret")
    @ResponseBody
    fun login(
        @RequestParam token: String,
    ): AttackResult =
        try {
            val jwt = Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token)
            val claims = jwt.body as Claims
            if (!claims.keys.containsAll(EXPECTED_CLAIMS)) {
                failed(this).feedback("jwt-secret-claims-missing").build()
            } else {
                val user = claims["username"] as String
                if (WEBGOAT_USER.equals(user, ignoreCase = true)) {
                    success(this).build()
                } else {
                    failed(this).feedback("jwt-secret-incorrect-user").feedbackArgs(user).build()
                }
            }
        } catch (e: Exception) {
            failed(this).feedback("jwt-invalid-token").output(e.message).build()
        }

    companion object {
        @JvmField
        val SECRETS =
            arrayOf("victory", "business", "available", "shipping", "washington")

        @JvmField
        val JWT_SECRET: String = TextCodec.BASE64.encode(SECRETS.random())

        private const val WEBGOAT_USER = "WebGoat"
        private val EXPECTED_CLAIMS =
            listOf("iss", "iat", "exp", "aud", "sub", "username", "Email", "Role")
    }
}
