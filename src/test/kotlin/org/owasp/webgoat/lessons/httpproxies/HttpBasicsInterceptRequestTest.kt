/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.httpproxies

import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(MockitoExtension::class)
class HttpBasicsInterceptRequestTest : LessonTest() {
    @Test
    fun success() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/HttpProxies/intercept-request")
                    .header("x-request-intercepted", "true")
                    .param("changeMe", "Requests are tampered easily"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("http-proxies.intercept.success")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    fun failure() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/HttpProxies/intercept-request")
                    .header("x-request-intercepted", "false")
                    .param("changeMe", "Requests are tampered easily"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("http-proxies.intercept.failure")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    fun missingParam() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/HttpProxies/intercept-request")
                    .header("x-request-intercepted", "false"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("http-proxies.intercept.failure")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    fun missingHeader() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/HttpProxies/intercept-request")
                    .param("changeMe", "Requests are tampered easily"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("http-proxies.intercept.failure")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    fun whenPostAssignmentShouldNotPass() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/HttpProxies/intercept-request")
                    .header("x-request-intercepted", "true")
                    .param("changeMe", "Requests are tampered easily"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.`is`(messages.getMessage("http-proxies.intercept.failure")),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }
}
