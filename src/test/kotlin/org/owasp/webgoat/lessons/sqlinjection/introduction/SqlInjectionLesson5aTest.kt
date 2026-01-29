/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson5aTest : LessonTest() {
    @Test
    fun knownAccountShouldDisplayData() {
        mockMvc
            .perform(
                post("/SqlInjection/assignment5a")
                    .param("account", "Smith")
                    .param("operator", "")
                    .param("injection", ""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("assignment.not.solved"))))
            .andExpect(jsonPath("$.output", containsString("<p>USERID, FIRST_NAME")))
    }

    @Disabled
    @Test
    fun unknownAccount() {
        mockMvc
            .perform(
                post("/SqlInjection/assignment5a")
                    .param("account", "Smith")
                    .param("operator", "")
                    .param("injection", ""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(jsonPath("$.feedback", `is`(messages.getMessage("NoResultsMatched"))))
            .andExpect(jsonPath("$.output").doesNotExist())
    }

    @Test
    fun sqlInjection() {
        mockMvc
            .perform(
                post("/SqlInjection/assignment5a")
                    .param("account", "'")
                    .param("operator", "OR")
                    .param("injection", "'1' = '1"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", containsString("You have succeed")))
            .andExpect(jsonPath("$.output").exists())
    }

    @Test
    fun sqlInjectionWrongShouldDisplayError() {
        mockMvc
            .perform(
                post("/SqlInjection/assignment5a")
                    .param("account", "Smith'")
                    .param("operator", "OR")
                    .param("injection", "'1' = '1'"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
            .andExpect(
                jsonPath("$.feedback", containsString(messages.getMessage("assignment.not.solved"))),
            ).andExpect(
                jsonPath(
                    "$.output",
                    `is`(
                        "malformed string: '1''<br> Your query was: SELECT * FROM user_data WHERE" +
                            " first_name = 'John' and last_name = 'Smith' OR '1' = '1''",
                    ),
                ),
            )
    }
}
