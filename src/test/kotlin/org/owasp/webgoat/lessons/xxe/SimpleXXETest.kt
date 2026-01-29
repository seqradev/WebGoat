/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WithWebGoatUser
class SimpleXXETest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun workingAttack() {
        // Call with XXE injection
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/simple")
                    .content(
                        """<?xml version="1.0" standalone="yes" ?><!DOCTYPE user [<!ENTITY root SYSTEM "file:///"> ]><comment><text>&root;</text></comment>""",
                    ),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.solved"))),
            )
    }

    @Test
    fun postingJsonCommentShouldNotSolveAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/simple")
                    .content("<comment><text>test</ext></comment>"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            )
    }

    @Test
    fun postingXmlCommentWithoutXXEShouldNotSolveAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/simple")
                    .content(
                        """<?xml version="1.0" standalone="yes" ?><comment><text>&root;</text></comment>""",
                    ),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            )
    }

    @Test
    fun postingPlainTextShouldThrowException() {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/xxe/simple").content("test"))
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.output", CoreMatchers.startsWith("jakarta.xml.bind.UnmarshalException")),
            ).andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            )
    }
}
