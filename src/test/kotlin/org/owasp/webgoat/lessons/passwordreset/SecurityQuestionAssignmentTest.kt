/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class SecurityQuestionAssignmentTest : LessonTest() {
    override lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun oneQuestionShouldNotSolveTheAssignment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "What is your favorite animal?"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("password-questions-one-successful")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
            .andExpect(jsonPath("$.output", CoreMatchers.notNullValue()))
    }

    @Test
    fun twoQuestionsShouldSolveTheAssignment() {
        val mocksession = MockHttpSession()
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "What is your favorite animal?")
                    .session(mocksession),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "In what year was your mother born?")
                    .session(mocksession),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.solved"))),
            ).andExpect(jsonPath("$.output", CoreMatchers.notNullValue()))
            .andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    fun answeringSameQuestionTwiceShouldNotSolveAssignment() {
        val mocksession = MockHttpSession()
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "What is your favorite animal?")
                    .session(mocksession),
            ).andExpect(status().isOk)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "What is your favorite animal?")
                    .session(mocksession),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("password-questions-one-successful")),
                ),
            ).andExpect(jsonPath("$.output", CoreMatchers.notNullValue()))
            .andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    fun solvingForOneUserDoesNotSolveForOtherUser() {
        val mocksession = MockHttpSession()
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/PasswordReset/SecurityQuestions")
                .param("question", "What is your favorite animal?")
                .session(mocksession),
        )
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "In what year was your mother born?")
                    .session(mocksession),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))

        val mocksession2 = MockHttpSession()
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/SecurityQuestions")
                    .param("question", "What is your favorite animal?")
                    .session(mocksession2),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }
}
