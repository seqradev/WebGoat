/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cia

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CIAQuizTest : LessonTest() {
    @Test
    fun allAnswersCorrectIsSuccess() {
        val solution0 = arrayOf("Solution 3")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 2")

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/cia/quiz")
                    .param("question_0_solution", *solution0)
                    .param("question_1_solution", *solution1)
                    .param("question_2_solution", *solution2)
                    .param("question_3_solution", *solution3),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(true)))
    }

    @Test
    fun oneAnswerWrongIsFailure() {
        val solution0 = arrayOf("Solution 1")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 2")

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/cia/quiz")
                    .param("question_0_solution", *solution0)
                    .param("question_1_solution", *solution1)
                    .param("question_2_solution", *solution2)
                    .param("question_3_solution", *solution3),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
    }

    @Test
    fun twoAnswersWrongIsFailure() {
        val solution0 = arrayOf("Solution 1")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 3")

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/cia/quiz")
                    .param("question_0_solution", *solution0)
                    .param("question_1_solution", *solution1)
                    .param("question_2_solution", *solution2)
                    .param("question_3_solution", *solution3),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
    }

    @Test
    fun threeAnswersWrongIsFailure() {
        val solution0 = arrayOf("Solution 1")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 1")
        val solution3 = arrayOf("Solution 3")

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/cia/quiz")
                    .param("question_0_solution", *solution0)
                    .param("question_1_solution", *solution1)
                    .param("question_2_solution", *solution2)
                    .param("question_3_solution", *solution3),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
    }

    @Test
    fun allAnswersWrongIsFailure() {
        val solution0 = arrayOf("Solution 2")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 3")
        val solution3 = arrayOf("Solution 1")

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/cia/quiz")
                    .param("question_0_solution", *solution0)
                    .param("question_1_solution", *solution1)
                    .param("question_2_solution", *solution2)
                    .param("question_3_solution", *solution3),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("lessonCompleted", `is`(false)))
    }

    @Test
    fun allAnswersCorrectGetResultsReturnsTrueTrueTrueTrue() {
        val solution0 = arrayOf("Solution 3")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 2")

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/cia/quiz")
                .param("question_0_solution", *solution0)
                .param("question_1_solution", *solution1)
                .param("question_2_solution", *solution2)
                .param("question_3_solution", *solution3),
        )

        val result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/cia/quiz"))
                .andExpect(status().isOk)
                .andReturn()

        val responseString = result.response.contentAsString
        assertThat(responseString).isEqualTo("[ true, true, true, true ]")
    }

    @Test
    fun firstAnswerFalseGetResultsReturnsFalseTrueTrueTrue() {
        val solution0 = arrayOf("Solution 2")
        val solution1 = arrayOf("Solution 1")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 2")

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/cia/quiz")
                .param("question_0_solution", *solution0)
                .param("question_1_solution", *solution1)
                .param("question_2_solution", *solution2)
                .param("question_3_solution", *solution3),
        )

        val result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/cia/quiz"))
                .andExpect(status().isOk)
                .andReturn()

        val responseString = result.response.contentAsString
        assertThat(responseString).isEqualTo("[ false, true, true, true ]")
    }

    @Test
    fun secondAnswerFalseGetResultsReturnsTrueFalseTrueTrue() {
        val solution0 = arrayOf("Solution 3")
        val solution1 = arrayOf("Solution 2")
        val solution2 = arrayOf("Solution 4")
        val solution3 = arrayOf("Solution 2")

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/cia/quiz")
                .param("question_0_solution", *solution0)
                .param("question_1_solution", *solution1)
                .param("question_2_solution", *solution2)
                .param("question_3_solution", *solution3),
        )

        val result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/cia/quiz"))
                .andExpect(status().isOk)
                .andReturn()

        val responseString = result.response.contentAsString
        assertThat(responseString).isEqualTo("[ true, false, true, true ]")
    }

    @Test
    fun allAnswersFalseGetResultsReturnsFalseFalseFalseFalse() {
        val solution0 = arrayOf("Solution 1")
        val solution1 = arrayOf("Solution 2")
        val solution2 = arrayOf("Solution 1")
        val solution3 = arrayOf("Solution 1")

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/cia/quiz")
                .param("question_0_solution", *solution0)
                .param("question_1_solution", *solution1)
                .param("question_2_solution", *solution2)
                .param("question_3_solution", *solution3),
        )

        val result =
            mockMvc
                .perform(MockMvcRequestBuilders.get("/cia/quiz"))
                .andExpect(status().isOk)
                .andReturn()

        val responseString = result.response.contentAsString
        assertThat(responseString).isEqualTo("[ false, false, false, false ]")
    }
}
