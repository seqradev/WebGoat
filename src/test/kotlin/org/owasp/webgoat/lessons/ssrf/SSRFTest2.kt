/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.ssrf

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class SSRFTest2 : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun modifyUrlIfconfigPro() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/SSRF/task2").param("url", "http://ifconfig.pro"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun modifyUrlCat() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/SSRF/task2").param("url", "images/cat.jpg"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
