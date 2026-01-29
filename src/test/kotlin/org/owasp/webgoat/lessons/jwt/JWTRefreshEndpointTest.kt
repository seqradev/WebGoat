/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.jwt.JWTRefreshEndpoint.Companion.PASSWORD
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WithWebGoatUser
class JWTRefreshEndpointTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solveAssignment() {
        val objectMapper = ObjectMapper()

        // First login to obtain tokens for Jerry
        val loginJson = mapOf("user" to "Jerry", "password" to PASSWORD)
        var result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/JWT/refresh/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginJson)),
                ).andExpect(status().isOk())
                .andReturn()
        var tokens: Map<String, String> =
            objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, String>
        val refreshToken = tokens["refresh_token"]

        // Now create a new refresh token for Tom based on Toms old access token and send the refresh
        // token of Jerry
        var accessTokenTom =
            "eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE1MjYxMzE0MTEsImV4cCI6MTUyNjIxNzgxMSwiYWRtaW4iOiJmYWxzZSIsInVzZXIiOiJUb20ifQ.DCoaq9zQkyDH25EcVWKcdbyVfUL4c9D4jRvsqOqvi9iAd4QuqmKcchfbU8FNzeBNF9tLeFXHZLU4yRkq-bjm7Q"
        val refreshJson = mapOf("refresh_token" to refreshToken)
        result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/JWT/refresh/newToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer $accessTokenTom")
                        .content(objectMapper.writeValueAsString(refreshJson)),
                ).andExpect(status().isOk())
                .andReturn()
        tokens = objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, String>
        accessTokenTom = requireNotNull(tokens["access_token"])

        // Now checkout with the new token from Tom
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/checkout")
                    .header("Authorization", "Bearer $accessTokenTom"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun solutionWithAlgNone() {
        val tokenWithNoneAlgorithm =
            Jwts
                .builder()
                .setHeaderParam("alg", "none")
                .addClaims(mapOf("admin" to "true", "user" to "Tom"))
                .compact()

        // Now checkout with the new token from Tom
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/checkout")
                    .header("Authorization", "Bearer $tokenWithNoneAlgorithm"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-refresh-alg-none"))))
    }

    @Test
    fun checkoutWithTomsTokenFromAccessLogShouldFail() {
        val accessTokenTom =
            "eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE1MjYxMzE0MTEsImV4cCI6MTUyNjIxNzgxMSwiYWRtaW4iOiJmYWxzZSIsInVzZXIiOiJUb20ifQ.DCoaq9zQkyDH25EcVWKcdbyVfUL4c9D4jRvsqOqvi9iAd4QuqmKcchfbU8FNzeBNF9tLeFXHZLU4yRkq-bjm7Q"
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/checkout")
                    .header("Authorization", "Bearer $accessTokenTom"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.output", CoreMatchers.containsString("JWT expired at")))
    }

    @Test
    fun checkoutWitRandomTokenShouldFail() {
        val accessTokenTom =
            "eyJhbGciOiJIUzUxMiJ9.eyJpLXQiOjE1MjYxMzE0MTEsImV4cCI6MTUyNjIxNzgxMSwiYWRtaW4iOiJmYWxzZSIsInVzZXIiOiJUb20ifQ.DCoaq9zQkyDH25EcVWKcdbyVfUL4c9D4jRvsqOqvi9iAd4QuqmKcchfbU8FNzeBNF9tLeFXHZLU4yRkq-bjm7Q"
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/checkout")
                    .header("Authorization", "Bearer $accessTokenTom"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-invalid-token"))))
    }

    @Test
    fun flowForJerryAlwaysWorks() {
        val objectMapper = ObjectMapper()

        val loginJson = mapOf("user" to "Jerry", "password" to PASSWORD)
        val result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/JWT/refresh/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginJson)),
                ).andExpect(status().isOk())
                .andReturn()
        val tokens: Map<String, String> =
            objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, String>
        val accessToken = tokens["access_token"]

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/checkout")
                    .header("Authorization", "Bearer $accessToken"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback", `is`("User is not Tom but Jerry, please try again")))
    }

    @Test
    fun loginShouldNotWorkForJerryWithWrongPassword() {
        val objectMapper = ObjectMapper()

        val loginJson = mapOf("user" to "Jerry", "password" to PASSWORD + "wrong")
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginJson)),
            ).andExpect(status().isUnauthorized())
    }

    @Test
    fun loginShouldNotWorkForTom() {
        val objectMapper = ObjectMapper()

        val loginJson = mapOf("user" to "Tom", "password" to PASSWORD)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginJson)),
            ).andExpect(status().isUnauthorized())
    }

    @Test
    fun newTokenShouldWorkForJerry() {
        val objectMapper = ObjectMapper()
        val loginJson = mapOf("user" to "Jerry", "password" to PASSWORD)
        val result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/JWT/refresh/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginJson)),
                ).andExpect(status().isOk())
                .andReturn()
        val tokens: Map<String, String> =
            objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, String>
        val accessToken = tokens["access_token"]
        val refreshToken = tokens["refresh_token"]

        val refreshJson = mapOf("refresh_token" to refreshToken)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/newToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer $accessToken")
                    .content(objectMapper.writeValueAsString(refreshJson)),
            ).andExpect(status().isOk())
    }

    @Test
    fun unknownRefreshTokenShouldGiveUnauthorized() {
        val objectMapper = ObjectMapper()
        val loginJson = mapOf("user" to "Jerry", "password" to PASSWORD)
        val result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/JWT/refresh/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginJson)),
                ).andExpect(status().isOk())
                .andReturn()
        val tokens: Map<String, String> =
            objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, String>
        val accessToken = tokens["access_token"]

        val refreshJson = mapOf("refresh_token" to "wrong_refresh_token")
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/refresh/newToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer $accessToken")
                    .content(objectMapper.writeValueAsString(refreshJson)),
            ).andExpect(status().isUnauthorized())
    }

    @Test
    fun noTokenWhileCheckoutShouldReturn401() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/refresh/checkout"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun noTokenWhileRequestingNewTokenShouldReturn401() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/refresh/newToken"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun noTokenWhileLoginShouldReturn401() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/refresh/login"))
            .andExpect(status().isUnauthorized())
    }
}
