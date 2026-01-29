/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@WithWebGoatUser
class ContentTypeAssignmentTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun sendingXmlButContentTypeIsJson() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/content-type")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """<?xml version="1.0" standalone="yes" ?><!DOCTYPE user [<!ENTITY root SYSTEM "file:///"> ]><comment><text>&root;</text></comment>""",
                    ),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("xxe.content.type.feedback.json")),
                ),
            )
    }

    @Test
    fun workingAttack() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/content-type")
                    .contentType(MediaType.APPLICATION_XML)
                    .content(
                        """<?xml version="1.0" standalone="yes" ?><!DOCTYPE user [<!ENTITY root SYSTEM "file:///"> ]><comment><text>&root;</text></comment>""",
                    ),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.solved"))),
            )
    }

    @Test
    fun postingJsonShouldAddComment() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/content-type")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{ "text" : "Hello World"}"""),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("xxe.content.type.feedback.json")),
                ),
            )

        mockMvc
            .perform(get("/xxe/comments").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[*].text").value(Matchers.hasItem("Hello World")))
    }

    private fun countComments(): Int {
        val response =
            mockMvc
                .perform(get("/xxe/comments").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andReturn()
        return ObjectMapper().reader().readTree(response.response.contentAsString).size()
    }

    @Test
    fun postingInvalidJsonShouldNotAddComment() {
        val numberOfComments = countComments()
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/content-type")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ 'text' : 'Wrong'"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("xxe.content.type.feedback.json")),
                ),
            )

        mockMvc
            .perform(get("/xxe/comments").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[*]").value(Matchers.hasSize<Any>(numberOfComments)))
    }
}
