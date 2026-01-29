/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
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

class SqlInjectionLesson8Test : LessonTest() {
    @Test
    fun oneAccount() {
        mockMvc
            .perform(
                post("/SqlInjection/attack8")
                    .param("name", "Smith")
                    .param("auth_tan", "3SL99A"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.8.one"))))
            .andExpect(jsonPath("$.output", containsString("<table><tr><th>")))
    }

    @Test
    fun multipleAccounts() {
        mockMvc
            .perform(
                post("/SqlInjection/attack8")
                    .param("name", "Smith")
                    .param("auth_tan", "3SL99A' OR '1' = '1"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.8.success"))))
            .andExpect(
                jsonPath(
                    "$.output",
                    containsString(
                        "<tr><td>96134<\\/td><td>Bob<\\/td><td>Franco<\\/td><td>Marketing<\\/td><td>83700<\\/td><td>LO9S2V<\\/td><\\/tr>",
                    ),
                ),
            )
    }

    @Test
    fun wrongNameReturnsNoAccounts() {
        mockMvc
            .perform(
                post("/SqlInjection/attack8")
                    .param("name", "Smithh")
                    .param("auth_tan", "3SL99A"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.8.no.results"))))
            .andExpect(jsonPath("$.output").doesNotExist())
    }

    @Test
    fun wrongTANReturnsNoAccounts() {
        mockMvc
            .perform(
                post("/SqlInjection/attack8")
                    .param("name", "Smithh")
                    .param("auth_tan", ""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.8.no.results"))))
            .andExpect(jsonPath("$.output").doesNotExist())
    }

    @Test
    fun malformedQueryReturnsError() {
        mockMvc
            .perform(
                post("/SqlInjection/attack8")
                    .param("name", "Smith")
                    .param("auth_tan", "3SL99A' OR '1' = '1'"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.output", containsString("feedback-negative")))
    }
}
