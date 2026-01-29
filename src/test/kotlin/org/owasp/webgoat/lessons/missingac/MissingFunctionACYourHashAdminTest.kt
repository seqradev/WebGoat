/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_ADMIN
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MissingFunctionACYourHashAdminTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solve() {
        val userHash = DisplayUser(User("Jerry", "doesnotreallymatter", true), PASSWORD_SALT_ADMIN).userHash
        mockMvc
            .perform(
                post("/access-control/user-hash-fix")
                    .param("userHash", userHash),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    containsString("Congrats! You really succeeded when you added the user."),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun wrongUserHash() {
        mockMvc
            .perform(
                post("/access-control/user-hash-fix")
                    .param("userHash", "wrong"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
