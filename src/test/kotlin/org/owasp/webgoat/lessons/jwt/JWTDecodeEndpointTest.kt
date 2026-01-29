/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class JWTDecodeEndpointTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solveAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/decode")
                    .param("jwt-encode-user", "user")
                    .content(""),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun wrongUserShouldNotSolveAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/JWT/decode")
                    .param("jwt-encode-user", "wrong")
                    .content(""),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
