/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson9Test : LessonTest() {
    private val completedError = "JSON path \"lessonCompleted\""

    @Test
    fun malformedQueryReturnsError() {
        try {
            mockMvc
                .perform(
                    post("/SqlInjection/attack9")
                        .param("name", "Smith")
                        .param("auth_tan", "3SL99A' OR '1' = '1'"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("lessonCompleted", `is`(false)))
                .andExpect(jsonPath("$.output", containsString("feedback-negative")))
        } catch (e: AssertionError) {
            if (e.message?.contains(completedError) != true) throw e

            mockMvc
                .perform(
                    post("/SqlInjection/attack9")
                        .param("name", "Smith")
                        .param("auth_tan", "3SL99A' OR '1' = '1'"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("lessonCompleted", `is`(true)))
                .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.9.success"))))
                .andExpect(jsonPath("$.output", containsString("feedback-negative")))
        }
    }

    @Test
    fun smithIsNotMostEarning() {
        mockMvc
            .perform(
                post("/SqlInjection/attack9")
                    .param("name", "Smith")
                    .param(
                        "auth_tan",
                        "3SL99A'; UPDATE employees SET salary = 9999 WHERE last_name = 'Smith",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.9.one"))))
    }

    @Test
    fun onlySmithSalaryMustBeUpdated() {
        mockMvc
            .perform(
                post("/SqlInjection/attack9")
                    .param("name", "Smith")
                    .param("auth_tan", "3SL99A'; UPDATE employees SET salary = 9999 -- "),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.9.one"))))
    }

    @Test
    fun onlySmithMustMostEarning() {
        mockMvc
            .perform(
                post("/SqlInjection/attack9")
                    .param("name", "'; UPDATE employees SET salary = 999999 -- ")
                    .param("auth_tan", ""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.9.one"))))
    }

    @Test
    fun smithIsMostEarningCompletesAssignment() {
        mockMvc
            .perform(
                post("/SqlInjection/attack9")
                    .param("name", "Smith")
                    .param(
                        "auth_tan",
                        "3SL99A'; UPDATE employees SET salary = '300000' WHERE last_name = 'Smith",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.9.success"))))
            .andExpect(jsonPath("$.output", containsString("300000")))
    }
}
