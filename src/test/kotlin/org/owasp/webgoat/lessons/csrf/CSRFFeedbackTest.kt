/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import jakarta.servlet.http.Cookie
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CSRFFeedbackTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun postingJsonMessageThroughWebGoatShouldWork() {
        mockMvc
            .perform(
                post("/csrf/feedback/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"name": "Test", "email": "test1233@dfssdf.de", "subject": "service", "message":"dsaffd"}""",
                    ),
            ).andExpect(status().isOk())
    }

    @Test
    fun csrfAttack() {
        mockMvc
            .perform(
                post("/csrf/feedback/message")
                    .contentType(MediaType.TEXT_PLAIN)
                    .cookie(Cookie("JSESSIONID", "test"))
                    .header("host", "localhost:8080")
                    .header("referer", "webgoat.org")
                    .content(
                        """{"name": "Test", "email": "test1233@dfssdf.de", "subject": "service", "message":"dsaffd"}""",
                    ),
            ).andExpect(jsonPath("lessonCompleted", `is`(true)))
            .andExpect(jsonPath("feedback", containsString("the flag is: ")))
    }
}
