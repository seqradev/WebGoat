/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson6bTest : LessonTest() {
    @Test
    fun submitCorrectPassword() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6b")
                    .param("userid_6b", "passW0rD"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun submitWrongPassword() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6b")
                    .param("userid_6b", "John"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
