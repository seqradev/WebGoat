/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Header
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.commons.lang3.RandomStringUtils
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Date
import java.util.concurrent.TimeUnit

@RestController
@AssignmentHints("jwt-refresh-hint1", "jwt-refresh-hint2", "jwt-refresh-hint3", "jwt-refresh-hint4")
class JWTRefreshEndpoint : AssignmentEndpoint {
    @PostMapping(
        value = ["/JWT/refresh/login"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun follow(
        @RequestBody(required = false) json: Map<String, Any>?,
    ): ResponseEntity<Map<String, Any>> {
        if (json == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val user = json["user"] as? String
        val password = json["password"] as? String

        return if ("Jerry".equals(user, ignoreCase = true) && PASSWORD == password) {
            ok(createNewTokens(requireNotNull(user)))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    private fun createNewTokens(user: String): Map<String, Any> {
        val claims = mapOf("admin" to "false", "user" to user)
        val token =
            Jwts
                .builder()
                .setIssuedAt(Date(System.currentTimeMillis() + TimeUnit.DAYS.toDays(10)))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, JWT_PASSWORD)
                .compact()
        val refreshToken = RandomStringUtils.randomAlphabetic(20)
        validRefreshTokens.add(refreshToken)
        return mapOf("access_token" to token, "refresh_token" to refreshToken)
    }

    @PostMapping("/JWT/refresh/checkout")
    @ResponseBody
    fun checkout(
        @RequestHeader(value = "Authorization", required = false) token: String?,
    ): ResponseEntity<AttackResult> {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        return try {
            val jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(token.replace("Bearer ", ""))
            val claims = jwt.body as Claims
            val user = claims["user"] as? String
            if (user == "Tom") {
                if (jwt.header["alg"] == "none") {
                    ok(success(this).feedback("jwt-refresh-alg-none").build())
                } else {
                    ok(success(this).build())
                }
            } else {
                ok(failed(this).feedback("jwt-refresh-not-tom").feedbackArgs(user).build())
            }
        } catch (e: ExpiredJwtException) {
            ok(failed(this).output(e.message).build())
        } catch (e: JwtException) {
            ok(failed(this).feedback("jwt-invalid-token").build())
        }
    }

    @PostMapping("/JWT/refresh/newToken")
    @ResponseBody
    fun newToken(
        @RequestHeader(value = "Authorization", required = false) token: String?,
        @RequestBody(required = false) json: Map<String, Any>?,
    ): ResponseEntity<Map<String, Any>> {
        if (token == null || json == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val user: String?
        val refreshToken: String?
        try {
            @Suppress("UNCHECKED_CAST")
            val jwt =
                Jwts.parser().setSigningKey(JWT_PASSWORD).parse(token.replace("Bearer ", ""))
                    as io.jsonwebtoken.Jwt<Header<*>, Claims>
            user = jwt.body["user"] as? String
            refreshToken = json["refresh_token"] as? String
        } catch (e: ExpiredJwtException) {
            return handleExpiredToken(e, json)
        }

        return when {
            user == null || refreshToken == null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            validRefreshTokens.contains(refreshToken) -> {
                validRefreshTokens.remove(refreshToken)
                ok(createNewTokens(user))
            }
            else -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    private fun handleExpiredToken(
        e: ExpiredJwtException,
        json: Map<String, Any>,
    ): ResponseEntity<Map<String, Any>> {
        val user = e.claims["user"] as? String
        val refreshToken = json["refresh_token"] as? String
        return when {
            user == null || refreshToken == null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            validRefreshTokens.contains(refreshToken) -> {
                validRefreshTokens.remove(refreshToken)
                ok(createNewTokens(user))
            }
            else -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    companion object {
        const val PASSWORD = "bm5nhSkxCXZkKRy4"
        private const val JWT_PASSWORD = "bm5n3SkxCX4kKRy4"
        private val validRefreshTokens = mutableListOf<String>()
    }
}
