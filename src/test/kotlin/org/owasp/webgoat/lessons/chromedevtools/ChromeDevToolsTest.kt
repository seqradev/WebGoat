/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.chromedevtools

import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ChromeDevToolsTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun networkAssignmentTest_Success() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/ChromeDevTools/network")
                    .param("network_num", "123456")
                    .param("number", "123456"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", Matchers.`is`(true)))
    }

    @Test
    fun networkAssignmentTest_Fail() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/ChromeDevTools/network")
                    .param("network_num", "123456")
                    .param("number", "654321"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", Matchers.`is`(false)))
    }
}
