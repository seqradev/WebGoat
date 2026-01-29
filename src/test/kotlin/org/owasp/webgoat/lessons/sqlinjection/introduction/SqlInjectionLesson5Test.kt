/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.sql.SQLException

class SqlInjectionLesson5Test : LessonTest() {
    @Autowired
    private lateinit var dataSource: LessonDataSource

    @AfterEach
    @Throws(SQLException::class)
    fun removeGrant() {
        dataSource
            .connection
            .prepareStatement("revoke select on grant_rights from unauthorized_user cascade")
            .execute()
    }

    @Test
    fun grantSolution() {
        mockMvc
            .perform(
                post("/SqlInjection/attack5")
                    .param("query", "grant select on grant_rights to unauthorized_user"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun differentTableShouldNotSolveIt() {
        mockMvc
            .perform(
                post("/SqlInjection/attack5")
                    .param("query", "grant select on users to unauthorized_user"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    @Test
    fun noGrantShouldNotSolveIt() {
        mockMvc
            .perform(
                post("/SqlInjection/attack5")
                    .param("query", "select * from grant_rights"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
