/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.owasp.webgoat.container.lessons.Assignment
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.lessons.httpbasics.HttpBasics
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@ExtendWith(MockitoExtension::class)
class HintServiceTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val lesson = HttpBasics()
        lesson.addAssignment(
            Assignment("test", "/HttpBasics/attack1", listOf("hint 1", "hint 2")),
        )
        val course = Course(listOf(lesson))
        this.mockMvc = standaloneSetup(HintService(course)).build()
    }

    @Test
    fun hintsPerAssignment() {
        mockMvc
            .perform(MockMvcRequestBuilders.get(HintService.URL_HINTS_MVC))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hint", `is`("hint 1")))
            .andExpect(jsonPath("$[0].assignmentPath", `is`("/HttpBasics/attack1")))
    }
}
