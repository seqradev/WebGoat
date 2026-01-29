/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MissingFunctionYourHashTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun hashDoesNotMatch() {
        mockMvc
            .perform(
                post("/access-control/user-hash")
                    .param("userHash", "42"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    @Test
    fun hashMatches() {
        mockMvc
            .perform(
                post("/access-control/user-hash")
                    .param("userHash", "SVtOlaa+ER+w2eoIIVE5/77umvhcsh5V8UyDLUa1Itg="),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }
}
