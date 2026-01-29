/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ClientSideFilteringFreeAssignmentTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun success() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/clientSideFiltering/attack1")
                    .param("answer", "450000"),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    fun wrongSalary() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/clientSideFiltering/attack1")
                    .param("answer", "10000"),
            ).andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`("This is not the salary from Neville Bartholomew..."),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    fun getSalaries() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/clientSideFiltering/salaries"))
            .andExpect(jsonPath("$[0]", Matchers.hasKey("UserID")))
            .andExpect(jsonPath("$.length()", CoreMatchers.`is`(12)))
    }
}
