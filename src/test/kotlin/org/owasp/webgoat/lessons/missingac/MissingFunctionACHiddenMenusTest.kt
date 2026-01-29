/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class MissingFunctionACHiddenMenusTest : LessonTest() {
    @Test
    fun hiddenMenusSuccess() {
        mockMvc
            .perform(
                post("/access-control/hidden-menu")
                    .param("hiddenMenu1", "Users")
                    .param("hiddenMenu2", "Config"),
            ).andExpect(
                jsonPath(
                    "$.feedback",
                    `is`(messages.getMessage("access-control.hidden-menus.success")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun hiddenMenusClose() {
        mockMvc
            .perform(
                post("/access-control/hidden-menu")
                    .param("hiddenMenu1", "Config")
                    .param("hiddenMenu2", "Users"),
            ).andExpect(
                jsonPath(
                    "$.feedback",
                    `is`(messages.getMessage("access-control.hidden-menus.close")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    @Test
    fun hiddenMenusFailure() {
        mockMvc
            .perform(
                post("/access-control/hidden-menu")
                    .param("hiddenMenu1", "Foo")
                    .param("hiddenMenu2", "Bar"),
            ).andExpect(
                jsonPath(
                    "$.feedback",
                    `is`(messages.getMessage("access-control.hidden-menus.failure")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
