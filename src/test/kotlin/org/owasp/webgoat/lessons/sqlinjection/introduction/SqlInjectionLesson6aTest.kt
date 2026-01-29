/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson6aTest : LessonTest() {
    @Test
    fun wrongSolution() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param("userid_6a", "John"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    @Test
    fun wrongNumberOfColumns() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param(
                        "userid_6a",
                        "Smith' union select userid,user_name, password,cookie from user_system_data --",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
            .andExpect(
                jsonPath(
                    "$.output",
                    containsString(
                        "column number mismatch detected in rows of UNION, INTERSECT, EXCEPT, or VALUES operation",
                    ),
                ),
            )
    }

    @Test
    fun wrongDataTypeOfColumns() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param(
                        "userid_6a",
                        "Smith' union select 1,password, 1,'2','3', '4',1 from user_system_data --",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.output", containsString("incompatible data types in combination")))
    }

    @Test
    fun correctSolution() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param("userid_6a", "Smith'; SELECT * from user_system_data; --"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", containsString("passW0rD")))
    }

    @Test
    fun noResultsReturned() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param("userid_6a", "Smith' and 1 = 2 --"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("sql-injection.6a.no.results"))))
    }

    @Test
    fun noUnionUsed() {
        mockMvc
            .perform(
                post("/SqlInjectionAdvanced/attack6a")
                    .param("userid_6a", "S'; Select * from user_system_data; --"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", containsString("UNION")))
    }
}
