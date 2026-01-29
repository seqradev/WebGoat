/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MissingFunctionACUsersTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun getUsers() {
        mockMvc
            .perform(
                get("/access-control/users")
                    .header("Content-type", "application/json"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].username", `is`("Tom")))
            .andExpect(jsonPath("$[0].userHash", `is`("Mydnhcy00j2b0m6SjmPz6PUxF9WIeO7tzm665GiZWCo=")))
            .andExpect(jsonPath("$[0].admin", `is`(false)))
    }

    @Test
    fun addUser() {
        val user =
            """
            {"username":"newUser","password":"newUser12","admin": "true"}
            """.trimIndent()
        mockMvc
            .perform(
                post("/access-control/users")
                    .header("Content-type", "application/json")
                    .content(user),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                get("/access-control/users")
                    .header("Content-type", "application/json"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.size()", Matchers.`is`(4)))
    }
}
