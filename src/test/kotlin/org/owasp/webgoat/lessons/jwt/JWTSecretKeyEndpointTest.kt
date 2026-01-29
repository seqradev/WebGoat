/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm.HS512
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.jwt.JWTSecretKeyEndpoint.Companion.JWT_SECRET
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Duration
import java.time.Instant
import java.util.Date

@WithWebGoatUser
class JWTSecretKeyEndpointTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    private fun createClaims(username: String): Claims {
        val claims = Jwts.claims()
        claims["admin"] = "true"
        claims["user"] = "Tom"
        claims.expiration = Date.from(Instant.now().plus(Duration.ofDays(1)))
        claims.issuedAt = Date.from(Instant.now().plus(Duration.ofDays(1)))
        claims.issuer = "iss"
        claims.audience = "aud"
        claims.subject = "sub"
        claims["username"] = username
        claims["Email"] = "webgoat@webgoat.io"
        claims["Role"] = arrayOf("user")
        return claims
    }

    @Test
    fun solveAssignment() {
        val claims = createClaims("WebGoat")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(HS512, JWT_SECRET)
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun solveAssignmentWithLowercase() {
        val claims = createClaims("webgoat")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(HS512, JWT_SECRET)
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun oneOfClaimIsMissingShouldNotSolveAssignment() {
        val claims = createClaims("WebGoat")
        claims.remove("aud")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(HS512, JWT_SECRET)
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-secret-claims-missing"))),
            )
    }

    @Test
    fun incorrectUser() {
        val claims = createClaims("Tom")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(HS512, JWT_SECRET)
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("jwt-secret-incorrect-user", "default", "Tom")),
                ),
            )
    }

    @Test
    fun incorrectToken() {
        val claims = createClaims("Tom")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(HS512, "wrong_password")
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-invalid-token"))))
    }

    @Test
    fun unsignedToken() {
        val claims = createClaims("WebGoat")
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .compact()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/secret").param("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-invalid-token"))))
    }
}
