/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.jwt.JWTVotesEndpoint.Companion.JWT_PASSWORD
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WithWebGoatUser
class JWTVotesEndpointTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solveAssignment() {
        // Create new token and set alg to none and do not sign it
        val claims = Jwts.claims()
        claims["admin"] = "true"
        claims["user"] = "Tom"
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .setHeaderParam("alg", "none")
                .compact()

        // Call the reset endpoint
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/votings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(Cookie("access_token", token)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun solveAssignmentWithBoolean() {
        // Create new token and set alg to none and do not sign it
        val claims = Jwts.claims()
        claims["admin"] = true
        claims["user"] = "Tom"
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .setHeaderParam("alg", "none")
                .compact()

        // Call the reset endpoint
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/votings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(Cookie("access_token", token)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun resetWithoutTokenShouldNotWork() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/JWT/votings").contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("jwt-invalid-token"))))
    }

    @Test
    fun guestShouldNotGetAToken() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/JWT/votings/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("user", "Guest"),
            ).andExpect(status().isUnauthorized())
            .andExpect(cookie().value("access_token", ""))
    }

    @Test
    fun tomShouldGetAToken() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/JWT/votings/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("user", "Tom"),
            ).andExpect(status().isOk())
            .andExpect(cookie().value("access_token", containsString("eyJhbGciOiJIUzUxMiJ9.")))
    }

    @Test
    fun guestShouldNotSeeNumberOfVotes() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/JWT/votings").cookie(Cookie("access_token", "")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numberOfVotes").doesNotExist())
            .andExpect(jsonPath("$[0].votingAllowed").doesNotExist())
            .andExpect(jsonPath("$[0].average").doesNotExist())
    }

    @Test
    fun tomShouldSeeNumberOfVotes() {
        val result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/JWT/votings/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("user", "Tom"),
                ).andExpect(status().isOk())
                .andReturn()

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/JWT/votings").cookie(result.response.cookies[0]),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numberOfVotes").exists())
            .andExpect(jsonPath("$[0].votingAllowed").exists())
            .andExpect(jsonPath("$[0].average").exists())
    }

    @Test
    fun invalidTokenShouldSeeGuestView() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/JWT/votings")
                    .cookie(Cookie("access_token", "abcd.efgh.ijkl")),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numberOfVotes").doesNotExist())
            .andExpect(jsonPath("$[0].votingAllowed").doesNotExist())
            .andExpect(jsonPath("$[0].average").doesNotExist())
    }

    @Test
    fun tomShouldBeAbleToVote() {
        var result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/JWT/votings/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("user", "Tom"),
                ).andExpect(status().isOk())
                .andReturn()
        val cookie = result.response.getCookie("access_token")

        result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/JWT/votings").cookie(cookie))
                .andExpect(status().isOk())
                .andReturn()
        var nodes =
            ObjectMapper().readValue(result.response.contentAsString, Array<Any>::class.java)
        val currentNumberOfVotes =
            (findNodeByTitle(nodes, "Admin lost password")?.get("numberOfVotes") as Int)

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/votings/Admin lost password").cookie(cookie))
            .andExpect(status().isAccepted())
        result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/JWT/votings").cookie(cookie))
                .andExpect(status().isOk())
                .andReturn()
        nodes = ObjectMapper().readValue(result.response.contentAsString, Array<Any>::class.java)
        val numberOfVotes = findNodeByTitle(nodes, "Admin lost password")?.get("numberOfVotes") as Int
        assertThat(numberOfVotes).isEqualTo(currentNumberOfVotes + 1)
    }

    private fun findNodeByTitle(
        nodes: Array<Any>,
        title: String,
    ): Map<String, Any>? {
        for (n in nodes) {
            @Suppress("UNCHECKED_CAST")
            val node = n as Map<String, Any>
            if (node["title"] == title) {
                return node
            }
        }
        return null
    }

    @Test
    fun guestShouldNotBeAbleToVote() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/votings/Admin lost password")
                    .cookie(Cookie("access_token", "")),
            ).andExpect(status().isUnauthorized())
    }

    @Test
    fun unknownUserWithValidTokenShouldNotBeAbleToVote() {
        val claims = Jwts.claims()
        claims["admin"] = "true"
        claims["user"] = "Intruder"
        val token =
            Jwts
                .builder()
                .signWith(SignatureAlgorithm.HS512, JWT_PASSWORD)
                .setClaims(claims)
                .compact()

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/votings/Admin lost password")
                    .cookie(Cookie("access_token", token)),
            ).andExpect(status().isUnauthorized())
    }

    @Test
    fun unknownUserShouldSeeGuestView() {
        val claims = Jwts.claims()
        claims["admin"] = "true"
        claims["user"] = "Intruder"
        val token =
            Jwts
                .builder()
                .signWith(SignatureAlgorithm.HS512, JWT_PASSWORD)
                .setClaims(claims)
                .compact()

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/JWT/votings").cookie(Cookie("access_token", token)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numberOfVotes").doesNotExist())
            .andExpect(jsonPath("$[0].votingAllowed").doesNotExist())
            .andExpect(jsonPath("$[0].average").doesNotExist())
    }
}
