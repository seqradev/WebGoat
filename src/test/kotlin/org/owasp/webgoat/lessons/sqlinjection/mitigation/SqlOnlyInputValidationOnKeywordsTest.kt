/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlOnlyInputValidationOnKeywordsTest : LessonTest() {
    @Test
    fun solve() {
        mockMvc
            .perform(
                post("/SqlOnlyInputValidationOnKeywords/attack")
                    .param(
                        "userid_sql_only_input_validation_on_keywords",
                        "Smith';SESELECTLECT/**/*/**/FRFROMOM/**/user_system_data;--",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
            .andExpect(jsonPath("$.feedback", containsString("passW0rD")))
    }

    @Test
    fun containsForbiddenSqlKeyword() {
        mockMvc
            .perform(
                post("/SqlOnlyInputValidationOnKeywords/attack")
                    .param(
                        "userid_sql_only_input_validation_on_keywords",
                        "Smith';SELECT/**/*/**/from/**/user_system_data;--",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
            .andExpect(
                jsonPath(
                    "$.output",
                    containsString(
                        "unexpected token: *<br> Your query was: SELECT * FROM user_data WHERE" +
                            " last_name =" +
                            " 'SMITH';\\/**\\/*\\/**\\/\\/**\\/USER_SYSTEM_DATA;--'",
                    ),
                ),
            )
    }
}
