/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson10Test : LessonTest() {
    private val completedError = "JSON path \"lessonCompleted\""

    @Test
    fun tableExistsIsFailure() {
        try {
            mockMvc
                .perform(post("/SqlInjection/attack10").param("action_string", ""))
                .andExpect(status().isOk)
                .andExpect(jsonPath("lessonCompleted", `is`(false)))
                .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.10.entries"))))
        } catch (e: AssertionError) {
            if (e.message?.contains(completedError) != true) throw e

            mockMvc
                .perform(post("/SqlInjection/attack10").param("action_string", ""))
                .andExpect(status().isOk)
                .andExpect(jsonPath("lessonCompleted", `is`(true)))
                .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.10.success"))))
        }
    }

    @Test
    fun tableMissingIsSuccess() {
        mockMvc
            .perform(
                post("/SqlInjection/attack10")
                    .param("action_string", "%'; DROP TABLE access_log;--"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.10.success"))))
    }
}
